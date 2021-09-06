package com.diguage.truman.mybatis;

import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2020-05-29 17:11
 */
public class SpringMybatisTest {
  private static final Logger logger = LoggerFactory.getLogger(SpringMybatisTest.class);

  @Test
  public void testCacheQuery() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    EmployeesMapper employeesMapper = context.getBean(EmployeesMapper.class);
    Employees employees = employeesMapper.getById(10001);
    System.out.println(employees);
    System.out.println(employeesMapper.getById(10001));
  }

  @Test
  public void testInsert() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    EmployeesService service = context.getBean(EmployeesService.class);
    Employees employees = new Employees();
    employees.empNo = 123456789;
    employees.birthDate = new Date();
    employees.firstName = "Dummy";
    employees.lastName = "Fake";
    employees.gender = "F";
    employees.hireDate = new Date();
    int insert = service.save(employees);
  }

  @org.springframework.context.annotation.Configuration
  @EnableTransactionManagement
  @MapperScan(basePackages = "com.diguage.truman.mybatis")
  @Import(EmployeesService.class)
  public static class Config {
    @Bean
    public DataSource dataSource() {
      HikariDataSource dataSource = new HikariDataSource();
      dataSource.setUsername("root");
      dataSource.setPassword("123456");
      dataSource.setDriverClassName(Driver.class.getName());
      dataSource.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5));

      dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/employees?useUnicode=true" +
          "&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true");
      return dataSource;
    }

    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
      TransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
      return transactionManager;
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

  @Service
  public static class EmployeesService {
    @Resource
    private EmployeesMapper employeesMapper;

    @Transactional
    public int save(Employees employees) {
      return employeesMapper.insert(employees);
    }
  }
}
