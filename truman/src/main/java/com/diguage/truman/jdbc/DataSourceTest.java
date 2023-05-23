package com.diguage.truman.jdbc;

import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class DataSourceTest {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceTest.class);

  @Test
  public void testDataSource() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    DataSource dataSource = context.getBean(DataSource.class);
    while (true) {
      try {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT CURRENT_TIMESTAMP()");
        while (resultSet.next()) {
          Timestamp date = resultSet.getTimestamp(1);
          System.out.println(date.toInstant());
        }
        connection.close();
        TimeUnit.SECONDS.sleep(3);
      } catch (Throwable e) {
        logger.error("query error", e);
      }
    }
  }

  @Configuration
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
  }
}
