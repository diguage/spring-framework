package com.diguage.truman.mybatis;

import com.mysql.cj.jdbc.Driver;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-29 17:11
 */
public class MybatisTest {
  private static final Logger logger = LoggerFactory.getLogger(MybatisTest.class);

  // tag::testCacheQuery[]
  /**
   * @author D瓜哥 · https://www.diguage.com
   * @since 2022-07-03 09:47:37
   */
  @Test
  public void testCacheQuery() {
    DataSource dataSource = getDataSource();
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment =
        new Environment("development", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMapper(EmployeesMapper.class);
    configuration.setCacheEnabled(true);
    configuration.setMapUnderscoreToCamelCase(true);
    SqlSessionFactory sqlSessionFactory =
        new SqlSessionFactoryBuilder().build(configuration);
    SqlSession session = sqlSessionFactory.openSession();
    EmployeesMapper mapper = session.getMapper(EmployeesMapper.class);
    System.out.println(mapper.getById(10001));
    System.out.println(mapper.getById(10001));
  }
  // end::testCacheQuery[]

  // tag::getDataSource[]
  /**
   * @author D瓜哥 · https://www.diguage.com
   * @since 2022-07-03 09:47:37
   */
  public DataSource getDataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setUsername("root");
    dataSource.setPassword("123456");
    dataSource.setDriverClassName(Driver.class.getName());
    dataSource.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5));

    dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/employees?useUnicode=true" +
        "&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true");
    return dataSource;
  }
  // end::getDataSource[]
}
