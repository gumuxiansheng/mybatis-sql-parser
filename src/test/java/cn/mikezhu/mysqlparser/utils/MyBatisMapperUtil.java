package cn.mikezhu.mysqlparser.utils;

import org.apache.ibatis.session.SqlSession;

public class MyBatisMapperUtil {
    public static <T> T getMapper(Class<T> mapperClass) {
        SqlSession sqlSession = MyBatisUtil.getSqlSessionFactory().openSession();
        try {
            return sqlSession.getMapper(mapperClass);
        } finally {
//            sqlSession.close();
        }
    }
}
