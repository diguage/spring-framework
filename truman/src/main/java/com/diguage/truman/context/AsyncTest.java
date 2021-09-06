package com.diguage.truman.context;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * TODO dgg 分析这篇文章 https://www.cnblogs.com/thisiswhy/p/16003571.html 的示例程序
 */
@Slf4j
public class AsyncTest {

	@Test
	public void test() {
	}

	@EnableAsync
	@Configuration
	public static class Config {
		@Bean(value = "diguage-executor")
		public Executor getExecutor() {
			return new ThreadPoolTaskExecutor();
		}
	}

	@Service
	public static class UserService {
		@Async(value = "diguage")
		public void insert(User user) {
			log.info("begin to insert user({})", user);
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				log.warn("lock was interrupted.", e);
			}
			log.info("finish inserting user.");
		}
	}

	@Data
	public static class User {
		private Long id;
		private String name;
		private Date birthday;
	}
}
