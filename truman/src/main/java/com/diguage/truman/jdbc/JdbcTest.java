package com.diguage.truman.jdbc;

import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JDBC 测试类
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-07-09 14:57
 */
public class JdbcTest {
  @Test
  public void test() {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(Config.class);
    ctx.refresh();
    EmployeesService service = ctx.getBean(EmployeesService.class);
    Employees employees = new Employees();
    employees.empNo = (int) System.currentTimeMillis();
    Date now = new Date();
    employees.birthDate = now;
    employees.firstName = "Dummy";
    employees.lastName = "Fake";
    employees.gender = "F";
    employees.hireDate = now;
    service.save(employees);
  }


  @Configuration
  @EnableTransactionManagement
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
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JdbcTemplate getJdbcTemplate(DataSource dataSource) {
      return new JdbcTemplate(dataSource);
    }
  }

  public static class EmployeesService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(rollbackFor = Throwable.class)
    public boolean save(Employees emp) {
      String sql = "INSERT INTO employees"
          + "(emp_no, birth_date, first_name, last_name, gender, hire_date)"
          + "VALUES(?, ?, ?, ?, ?, ?)";
      int count = jdbcTemplate.update(sql, emp.empNo, emp.birthDate,
          emp.firstName, emp.lastName, emp.gender, emp.hireDate);
      return count > 0;
    }
  }
}
