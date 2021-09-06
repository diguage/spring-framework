package com.diguage.truman.tx;

import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Date;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-09-11 10:20
 */
public class TxTest {

  @Test
  public void test() {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.register(Config.class);
    applicationContext.refresh();
    EmployeesService employeesService = applicationContext.getBean(EmployeesService.class);
    Employees employees = employeesService.getById(10001);
    System.out.println(employees);
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
      dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/employees?useUnicode=true" +
          "&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true");
      return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
      return new JdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }
  }

  public static class Employees {
    Integer empNo;
    Date birthDate;
    String firstName;
    String lastName;
    String gender;
    Date hireDate;

    @Override
    public String toString() {
      return "Employees{" +
          "empNo=" + empNo +
          ", birthDate=" + birthDate +
          ", firstName='" + firstName + '\'' +
          ", lastName='" + lastName + '\'' +
          ", gender='" + gender + '\'' +
          ", hireDate=" + hireDate +
          '}';
    }
  }

  @Service
  public static class EmployeesService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true, rollbackFor = Throwable.class)
    public Employees getById(Integer id) {
      String sql = "SELECT * FROM employees WHERE emp_no = ?";

      return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
        Employees employee = new Employees();
        employee.empNo = rs.getInt("emp_no");
        employee.birthDate = rs.getDate("birth_date");
        employee.firstName = rs.getString("first_name");
        employee.lastName = rs.getString("last_name");
        employee.gender = rs.getString("gender");
        employee.hireDate = rs.getDate("hire_date");
        return employee;
      }, new Object[]{id});
    }
  }
}
