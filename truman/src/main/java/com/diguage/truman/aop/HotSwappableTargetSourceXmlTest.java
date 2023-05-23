package com.diguage.truman.aop;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static com.diguage.truman.util.Constans.BASE_CLASS_PATH;
import static org.mockito.Mockito.when;

/**
 * 根据配置透明切换数据源的插件
 * <p>
 * https://afoo.me/posts/2005-08-10-improve-datasources-swap-solution.html
 *
 * @author D瓜哥, https://www.diguage.com
 * @since 2021-07-16 23:59:56
 */
public class HotSwappableTargetSourceXmlTest {
	private static final String PRIMARY_DATASOURCE = "primaryDatasource";
	private static final String SLAVE_DATASOURCE = "slaveDatasource";
	private static boolean usePrimary = true;

	@Test
	public void test() throws SQLException {
		ApplicationContext context
				= new ClassPathXmlApplicationContext(
        BASE_CLASS_PATH + "/aop/HotSwappableTargetSource.xml");

		DataSource primaryDatasource = (DataSource) context.getBean(PRIMARY_DATASOURCE);
		DataSource slaveDatasource = (DataSource) context.getBean(SLAVE_DATASOURCE);

		when(primaryDatasource.getConnection())
				.thenReturn(Mockito.mock(Connection.class, "primaryConnection"));
		when(slaveDatasource.getConnection())
				.thenReturn(Mockito.mock(Connection.class, "slaveConnection"));

		DataSource dataSource = (DataSource) context.getBean("dataSource");

		System.out.println(dataSource.getConnection());
		usePrimary = !usePrimary;
		System.out.println(dataSource.getConnection());
		System.out.println(dataSource.getConnection());
		usePrimary = !usePrimary;
		System.out.println(dataSource.getConnection());
	}

	@Configuration
	@EnableAspectJAutoProxy
	public static class Config {
		@Bean(PRIMARY_DATASOURCE)
		public DataSource primaryDatasource() {
			return Mockito.mock(DataSource.class, PRIMARY_DATASOURCE);
		}

		@Bean(SLAVE_DATASOURCE)
		public DataSource slaveDatasource() {
			return Mockito.mock(DataSource.class, SLAVE_DATASOURCE);
		}
	}

	@Data
	public static class SwapDataSourceAdvice implements MethodBeforeAdvice {
		@Resource(name = PRIMARY_DATASOURCE)
		private DataSource primaryDatasource;

		@Resource(name = SLAVE_DATASOURCE)
		private DataSource slaveDatasource;

		@Resource
		private HotSwappableTargetSource targetSource;


		@Override
		public void before(Method method, Object[] args, Object target) throws Throwable {
			DataSource used = usePrimary ? primaryDatasource : slaveDatasource;
			targetSource.swap(used);
		}
	}
}
