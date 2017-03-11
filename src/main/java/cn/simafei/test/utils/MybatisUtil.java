package cn.simafei.test.utils;

import java.io.InputStream;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MybatisUtil {
	private static SqlSession session = null;

	public static SqlSession openSession() {
		if (session == null) {
			String resource = "mybatis-config.xml";
			InputStream inputStream = MybatisUtil.class.getClassLoader()
					.getResourceAsStream(resource);
			SqlSessionFactory sqlFactory = new SqlSessionFactoryBuilder()
					.build(inputStream);
			session = sqlFactory.openSession();
		}
		return session;
	}
}
