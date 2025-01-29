package cn.mikezhu.mysqlparser.mapper;

import cn.mikezhu.mysqlparser.utils.AstVisitor;
import cn.mikezhu.mysqlparser.utils.ParamConstraint;
import cn.mikezhu.mysqlparser.utils.ParameterGenerator;
import org.apache.ibatis.ognl.Node;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.ognl.SimpleNode;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SomeTest {
    private final Logger logger = LoggerFactory.getLogger(SomeTest.class);

    @Test
    public void testOgnl() throws OgnlException {
        String expressionStr = "!(user.name=='Mike' and user.age==18) or user.name != null and user.auths.size() > 0 or name==''.toString()";
        SimpleNode expression = (SimpleNode) Ognl.parseExpression(expressionStr);
        List<ParamConstraint> paramConstraintList = new AstVisitor().visit(expression).stream().distinct().collect(Collectors.toList());

        List<Map<String, Object>> params = ParameterGenerator.generate(paramConstraintList);
        params.forEach(p -> {
            Object s = OgnlCache.getValue(expressionStr, p);
            logger.info("param: {}, ognl: {}, result: {}", p, expressionStr, s);
        });

        logger.info("ognl: {}", expression);
    }

}
