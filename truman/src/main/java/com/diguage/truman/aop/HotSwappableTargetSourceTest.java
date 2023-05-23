package com.diguage.truman.aop;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.TargetSource;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.when;

/**
 * 根据配置透明切换数据源的插件
 * <p>
 * https://afoo.me/posts/2005-08-10-improve-datasources-swap-solution.html
 *
 * @author D瓜哥, https://www.diguage.com
 * @since 2021-07-16 23:59:56
 */
public class HotSwappableTargetSourceTest {
	private static final String PRIMARY_DATASOURCE = "primaryDatasource";
	private static final String SLAVE_DATASOURCE = "slaveDatasource";
	private static final String ADVISOR_NAME = "swapDataSourceAdvisor";
	private static boolean usePrimary = true;

	@Test
	public void test() throws SQLException {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext();context.register(Config.class);
		context.refresh();

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

		@Bean
		public HotSwappableTargetSource hotSwappableTargetSource(
				@Qualifier(PRIMARY_DATASOURCE) DataSource dataSource) {
			return new HotSwappableTargetSource(dataSource);
		}

		@Bean
		public SwapDataSourceAdvice swapDataSourceAdvice() {
			return new SwapDataSourceAdvice();
		}

//		/**
//		 * 使用 Spring 自制的 Advisor
//		 */
//		@Bean(ADVISOR_NAME)
//		public RegexpMethodPointcutAdvisor methodAdvisor(SwapDataSourceAdvice advice) {
//			RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor();
//			advisor.setAdvice(advice);
//			advisor.setPattern(".*getConnection.*");
//			return advisor;
//		}

		/**
		 * 使用 AspectJ 表达式的 Advisor
		 */
		@Bean(ADVISOR_NAME)
		public AspectJExpressionPointcutAdvisor methodAdvisor(SwapDataSourceAdvice advice) {
			AspectJExpressionPointcutAdvisor advisor
					= new AspectJExpressionPointcutAdvisor();
			advisor.setAdvice(advice);
			advisor.setExpression("target(javax.sql.DataSource) " +
					"&& execution(java.sql.Connection getConnection(..))");
			return advisor;
		}

		@Bean("dataSource")
		public ProxyFactoryBean getProxyFactoryBean(TargetSource targetSource) {
			ProxyFactoryBean result = new ProxyFactoryBean();
			result.setTargetSource(targetSource);
			result.setInterceptorNames(ADVISOR_NAME);
			return result;
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
