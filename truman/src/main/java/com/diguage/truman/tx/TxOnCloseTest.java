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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-09-11 10:20
 */
public class TxOnCloseTest {

  /**
   * TODO 这个实验还没搞好！
   */
  @Test
  public void test() {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.register(Config.class);
    applicationContext.refresh();
    EmployeesService employeesService = applicationContext.getBean(EmployeesService.class);
    PlatformTransactionManager transactionManager = applicationContext.getBean(PlatformTransactionManager.class);

    Employees employees = new Employees();
    employees.empNo = (int) System.currentTimeMillis() / 1000;
    employees.birthDate = new Date();
    employees.firstName = "trans";
    employees.lastName = "action";
    employees.gender = "F";
    employees.hireDate = new Date();
    TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
    employeesService.save(employees);
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    executorService.execute(() -> applicationContext.close());
    try {
      TimeUnit.SECONDS.sleep(300);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    transactionManager.commit(transactionStatus);

    System.out.println(employees);
  }

  @Configuration
  @EnableTransactionManagement
  @Import(EmployeesService.class)
  public static class Config {
    @Bean
    public DataSource dataSource() {
      // TODO 设置超时时间，事务超时时间以及数据库里面的超时时间。
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

    public int save(Employees employees) {
      String sql = "INSERT INTO employees(emp_no, birth_date, first_name," +
          " last_name, gender, hire_date) VALUE (?, ?, ?, ?, ?, ?)";
      return jdbcTemplate.update(sql, employees.empNo, employees.birthDate, employees.firstName, employees.lastName, employees.gender, employees.hireDate);
    }
  }
}
