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
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Date;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-09-11 10:20
 */
public class TransactionTemplateTest {

  @Test
  public void test() {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.register(Config.class);
    applicationContext.refresh();
    EmployeesService employeesService = applicationContext.getBean(EmployeesService.class);
    Employees employees = new Employees();
//    employees.empNo = 5000000;
    employees.birthDate = new Date();
    employees.firstName = "D";
    employees.gender = "M";
    employees.hireDate = new Date();
    employees.lastName = "瓜哥";
    employeesService.save(employees);
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

    private final TransactionTemplate transactionTemplate;

    public EmployeesService(PlatformTransactionManager transactionManager) {
      transactionTemplate = new TransactionTemplate(transactionManager);
      transactionTemplate.setTimeout(Integer.MAX_VALUE);
    }

    public boolean save(Employees employees) {
      Integer result = transactionTemplate.execute(status -> {
        String sql = "INSERT INTO employees(emp_no, birth_date, first_name," +
            " last_name, gender, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
        int updatedCount = jdbcTemplate.update(sql, employees.empNo,
            employees.birthDate, employees.firstName, employees.lastName,
            employees.gender, employees.hireDate);
        return updatedCount;
      });
      return result == 1;
    }
  }
}
