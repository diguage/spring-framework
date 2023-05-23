package com.diguage.truman.jdbc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RoutingDataSource 测试类
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2022-02-04 22:57:14
 */
public class RoutingDataSourceTest {

	private static final Logger log = LoggerFactory.getLogger(RoutingDataSourceTest.class);

	private static volatile boolean isMaster = true;

	private static final String MASTER_PREFIX = "master";
	private static final String SLAVE_PREFIX = "slave";

	private static final String MASTER_DATA_SOURCE_NAME = "masterDataSource";
	private static final String SLAVE_DATA_SOURCE_NAME = "slaveDataSource";

	// TODO 在注解中使用报错。
	// private static final String DATA_SOURCE_NAME = DataSource.class.getSimpleName();
	// private static final String MASTER_DATA_SOURCE_NAME = MASTER_PREFIX + DATA_SOURCE_NAME;
	// private static final String SLAVE_DATA_SOURCE_NAME = SLAVE_PREFIX + DATA_SOURCE_NAME;

	private static final String CONNECTION_NAME = Connection.class.getSimpleName();
	private static final String MASTER_CONNECTION_NAME = MASTER_PREFIX + CONNECTION_NAME;
	private static final String SLAVE_CONNECTION_NAME = SLAVE_PREFIX + CONNECTION_NAME;

	/**
	 * TODO 这个实验还不算成功。还需要再改进。
	 */
	@Test
	public void test() throws SQLException {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);
		ctx.refresh();

		DataSource dataSource = ctx.getBean(DataSource.class);
		Connection connection = dataSource.getConnection();
		assertThat(connection.toString()).isEqualTo(MASTER_CONNECTION_NAME);

		isMaster = false;
		dataSource = ctx.getBean(DataSource.class);
		connection = dataSource.getConnection();
		assertThat(connection.toString()).isEqualTo(SLAVE_CONNECTION_NAME);
	}

	@Configuration
	@EnableTransactionManagement
	public static class Config {
		@Bean(MASTER_DATA_SOURCE_NAME)
		public DataSource masterDataSource() {
			DataSource dataSource = mock(DataSource.class, MASTER_DATA_SOURCE_NAME);
			try {
				when(dataSource.getConnection())
						.thenReturn(mock(Connection.class, MASTER_CONNECTION_NAME));
			} catch (SQLException e) {
				log.info("invoke getConnection error", e);
			}
			return dataSource;
		}

		@Bean(SLAVE_DATA_SOURCE_NAME)
		public DataSource slaveDataSource() {
			DataSource dataSource = mock(DataSource.class, SLAVE_DATA_SOURCE_NAME);
			try {
				when(dataSource.getConnection())
						.thenReturn(mock(Connection.class, SLAVE_CONNECTION_NAME));
			} catch (SQLException e) {
				log.info("invoke getConnection error", e);
			}
			return dataSource;
		}

		@Bean
		@Primary
		public DataSource primaryDataSource(
				@Autowired @Qualifier(MASTER_DATA_SOURCE_NAME) DataSource masterDataSource,
				@Autowired @Qualifier(SLAVE_DATA_SOURCE_NAME) DataSource slaveDataSource) {
			HotSwappableRoutingDataSource dataSource = new HotSwappableRoutingDataSource();
			HashMap<Object, Object> dataSources = new HashMap<>();
			dataSources.put(MASTER_DATA_SOURCE_NAME, masterDataSource);
			dataSources.put(SLAVE_DATA_SOURCE_NAME, slaveDataSource);
			dataSource.setTargetDataSources(dataSources);
			dataSource.setDefaultTargetDataSource(masterDataSource);
			return dataSource;
		}
	}

	public static class HotSwappableRoutingDataSource extends AbstractRoutingDataSource {
		@Override
		protected Object determineCurrentLookupKey() {
			return isMaster ? MASTER_DATA_SOURCE_NAME : SLAVE_DATA_SOURCE_NAME;
		}
	}
}
