package com.diguage.truman.tx;

import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
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
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Date;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-09-11 10:20
 */
public class TxTest {

  private static final Logger log = LoggerFactory.getLogger(TxTest.class);

  @Test
  public void test() {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.register(Config.class);
    applicationContext.refresh();
    EmployeesService employeesService = applicationContext.getBean(EmployeesService.class);

    Employees newEmp = new Employees();
    newEmp.empNo = Math.toIntExact(System.currentTimeMillis() / 100000);
    newEmp.birthDate = new Date();
    newEmp.firstName = "diguage";
    newEmp.lastName = "test";
    newEmp.hireDate = new Date();
    newEmp.gender = "F";
    employeesService.save(newEmp);

    Employees existEmp = employeesService.getById(newEmp.empNo);
    System.out.println(existEmp);
  }

  @Configuration
  @EnableTransactionManagement
  @Import({EmployeesService.class, TransactionPostTask.class})
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
  public static class EmployeesService implements ApplicationEventPublisherAware {
    @Resource
    private JdbcTemplate jdbcTemplate;

    private ApplicationEventPublisher applicationEventPublisher;

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

    @Transactional(rollbackFor = Throwable.class)
    public boolean save(Employees employee) {

      // :empNo, :birthDate, :firstName, :lastName, :gender, :hireDate
      int updated = jdbcTemplate.update("INSERT INTO employees(emp_no, birth_date, first_name, last_name, gender, hire_date) " +
          "VALUES(?, ?, ?, ?, ?, ?)", employee.empNo, employee.birthDate, employee.firstName, employee.lastName, employee.gender, employee.hireDate);

      // 以下是事务提交后置处理器
      // TransactionSynchronization 示例
      // TODO 如何封装一下？可以通过简单的注解或者配置完成这个事件处理。
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          // TODO 这里如何传递参数？直接引用上面的参数？
          log.info("TransactionSynchronization.afterCommit was called");
        }
      });

      // @TransactionalEventListener 示例
      // TODO 如何封装一下？可以通过简单的注解或者配置完成这个事件处理。
      TransactionPostEvent<String> event = new TransactionPostEvent<>();
      event.data = "This is a object";
      applicationEventPublisher.publishEvent(event);

      return updated > 0;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
      this.applicationEventPublisher = applicationEventPublisher;
    }
  }


  public static class TransactionPostEvent<T> {
    T data;

    @Override
    public String toString() {
      return "TransactionPostEvent{" +
          "data=" + data +
          '}';
    }
  }

  public static class TransactionPostTask {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, classes = TransactionPostEvent.class)
    public void doTask(TransactionPostEvent transactionPostEvent) {
      log.info("TransactionPostTask doTask was called: {}", transactionPostEvent);
    }
  }
}
