package cn.mikezhu.mysqlparser.mapper;

import cn.mikezhu.mysqlparser.utils.*;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.ognl.SimpleNode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class AllDaoTest {
    private final Logger logger = LoggerFactory.getLogger(AllDaoTest.class);

    @Test
    public void queryBatBocsBasmEntityList() {
        try (SqlSession session = MyBatisUtil.getSqlSessionFactory().openSession()) {
            final Collection<String> msNames = session.getConfiguration().getMappedStatementNames();
            final List<SqlSource> source = msNames.stream()
                    .filter(msName -> msName.startsWith("cn.mikezhu."))
                    .map(msName -> session.getConfiguration().getMappedStatement(msName).getSqlSource())
                    .collect(Collectors.toList());
            final List<RawSqlSource> rawSqlSourceList = source.stream()
                    .filter(sqlSource -> sqlSource instanceof RawSqlSource)
                    .map(sqlsource -> (RawSqlSource) sqlsource)
                    .collect(Collectors.toList());
            final List<DynamicSqlSource> dynamicSqlSourceList = source.stream()
                    .filter(sqlSource -> sqlSource instanceof DynamicSqlSource)
                    .map(sqlsource -> (DynamicSqlSource) sqlsource)
                    .collect(Collectors.toList());

            for (final RawSqlSource rawSqlSource : rawSqlSourceList) {
                final Map<String, String> param = new HashMap<>();
                try {
                    final SqlSource sqlSource = (SqlSource) ObjectUtil.getPrivateField(rawSqlSource, "sqlSource");
                    if (sqlSource instanceof StaticSqlSource) {
                        final StaticSqlSource staticSqlSource = (StaticSqlSource) sqlSource;
                        final List<ParameterMapping> parameterMappingList = (List<ParameterMapping>) ObjectUtil.getPrivateField(staticSqlSource, "parameterMappings");
                        for (final ParameterMapping parameterMapping : parameterMappingList) {
                            param.put(parameterMapping.getProperty(), "m");
                        }
                        final String sql = showSql(session.getConfiguration(), sqlSource.getBoundSql(param));
                        logger.info(sql);

                    }
                } catch (Exception e) {
                    logger.error("some error happened", e);
                }
            }

            for (final DynamicSqlSource dynamicSqlSource : dynamicSqlSourceList) {
                try {
                    SqlNode rootSqlNode = (SqlNode) ObjectUtil.getPrivateField(dynamicSqlSource, "rootSqlNode"); // 通过反射获取私有字段
                    SqlNodeExplorer sqlNodeExplorer = new SqlNodeExplorer();
                    sqlNodeExplorer.explore(rootSqlNode);
                    List<String> conditions = sqlNodeExplorer.getConditions();
                    Set<String> parameters = sqlNodeExplorer.getParameters();
                    final List<Map<String, Object>> candidateTmpParams = new ArrayList<>();
                    conditions.forEach(condition -> {
                        try {
                            SimpleNode expression = (SimpleNode) Ognl.parseExpression(condition);
                            List<ParamConstraint> paramConstraintList = new AstVisitor().visit(expression).stream().distinct().collect(Collectors.toList());

                            List<Map<String, Object>> params = ParameterGenerator.generate(paramConstraintList);
                            candidateTmpParams.addAll(params);
                            params.forEach(p -> {
                                Object s = OgnlCache.getValue(condition, p);
                                logger.info("param: {}, ognl: {}, result: {}", p, condition, s);
                            });
                        } catch (OgnlException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    final List<Map<String, Object>> candidateParams =  candidateTmpParams.stream().distinct().collect(Collectors.toList());

                    candidateParams.forEach(cp -> {
                        parameters.forEach(p -> {
                            if (!cp.containsKey(p)) {
                                cp.put(p, "m");
                            }
                        });
                        final String sql = showSql(session.getConfiguration(), dynamicSqlSource.getBoundSql(cp));
                        logger.info(sql);
                    });


                    Map map = new HashMap<>();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            Map map = new HashMap<>();
        }
    }

    public static class SqlNodeExplorer {
        private List<String> conditions = new ArrayList<>();
        private Set<String> parameters = new HashSet<>();

        public List<String> getConditions() {
            return conditions;
        }

        public Set<String> getParameters() {
            return parameters;
        }

        public void explore(SqlNode sqlNode) throws Exception {
            if (sqlNode instanceof MixedSqlNode) {
                // 处理混合节点（如 <where> 包含多个子节点）
                List<SqlNode> contents = (List<SqlNode>) ObjectUtil.getPrivateField(sqlNode, "contents");
                for (SqlNode child : contents) {
                    explore(child);
                }
            } else if (sqlNode instanceof IfSqlNode) {
                // 处理 <if> 标签
                IfSqlNode ifNode = (IfSqlNode) sqlNode;

                conditions.add((String) ObjectUtil.getPrivateField(ifNode, "test"));
                SqlNode contents = (SqlNode) ObjectUtil.getPrivateField(ifNode, "contents");
                explore(contents);
            } else if (sqlNode instanceof TextSqlNode) {
                // 处理静态文本 SQL
                String sql = (String) ObjectUtil.getPrivateField(sqlNode, "text");
                parameters.addAll(parseParameters(sql));
            } else if (sqlNode instanceof TrimSqlNode) {
                SqlNode contents = (SqlNode) ObjectUtil.getPrivateField(sqlNode, "contents");
                explore(contents);
            } else if (sqlNode instanceof ForEachSqlNode) {
                SqlNode contents = (SqlNode) ObjectUtil.getPrivateField(sqlNode, "contents");
                explore(contents);
            } else if (sqlNode instanceof ChooseSqlNode) {
                List<SqlNode> contents = (List<SqlNode>) ObjectUtil.getPrivateField(sqlNode, "contents");
                for (SqlNode child : contents) {
                    explore(child);
                }
            } else if (sqlNode instanceof StaticTextSqlNode) {
                // 处理静态 SQL
                String sql = (String) ObjectUtil.getPrivateField(sqlNode, "text");
                parameters.addAll(parseParameters(sql));
            } else if (sqlNode instanceof VarDeclSqlNode) {

            }
        }
    }

    public static Set<String> parseParameters(String text) {
        Set<String> parameters = new HashSet<>();

        String[] tokens = text.split("#\\{");
        for (int i = 1; i < tokens.length; i++) {
            String parameter = tokens[i].split("}")[0];
            parameters.add(parameter);
        }

        return parameters;
    }

    private String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (null != obj) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }

    private String showSql(Configuration configuration, BoundSql boundSql) {
        final Object parameterObject = boundSql.getParameterObject();  // 获取参数
        final List<ParameterMapping> parameterMappings = boundSql
                .getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");  // sql语句中多个空格都用一个空格代替
        if (!CollectionUtils.isEmpty(parameterMappings) && null != parameterObject) {
            final TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry(); // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换<br>// 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));

            } else {
                final MetaObject metaObject = configuration.newMetaObject(parameterObject);// MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
                for (final ParameterMapping parameterMapping : parameterMappings) {
                    final String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        final Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        final Object obj = boundSql.getAdditionalParameter(propertyName);  // 该分支是动态sql
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        sql = sql.replaceFirst("\\?", "缺失");
                    }//打印出缺失，提醒该参数缺失并防止错位
                }
            }
        }
        return sql;
    }

}
