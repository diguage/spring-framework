package com.diguage.truman.mybatis;

import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-29 17:11
 */
public class MybatisTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();
		EmployeesMapper employeesMapper = context.getBean(EmployeesMapper.class);
		Employees employees = employeesMapper.getById(10001);
		System.out.println(employees);
	}

	@org.springframework.context.annotation.Configuration
	@MapperScan(basePackages = "com.diguage.truman.mybatis")
	public static class Config {
		@Bean
		public DataSource dataSource() {
			HikariDataSource dataSource = new HikariDataSource();
			dataSource.setUsername("root");
			dataSource.setPassword("123456");
			dataSource.setDriverClassName(Driver.class.getName());
			dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/employees?useUnicode=true&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true");
			return dataSource;
		}

		@Bean
		public SqlSessionFactoryBean sqlSessionFactory(@Autowired DataSource dataSource) {
			SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
			factoryBean.setDataSource(dataSource);
			Configuration configuration = new Configuration();
			configuration.setMapUnderscoreToCamelCase(true);
			factoryBean.setConfiguration(configuration);
			return factoryBean;
		}
	}
}
