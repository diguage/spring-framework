package com.diguage.truman.context;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * TODO dgg 分析这篇文章 https://www.cnblogs.com/thisiswhy/p/16003571.html 的示例程序
 */
@Slf4j
public class AsyncTest {

	@Test
	public void test() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(Config.class);
		ctx.refresh();
		UserService service = ctx.getBean(UserService.class);
		log.info("start to invoke UserService.insert");
		User user = new User();
		user.setId(1L);
		user.setName("D瓜哥");
		user.setBlog("https://www.diguage.com/");
		user.setBirthday(new Date());
		service.insert(user);
		log.info("finish invoking UserService.insert");

		// 防止退出
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(6));
	}

	@EnableAsync
	@Configuration
	@Import(UserService.class)
	public static class Config {
		@Bean(value = "diguage-executor")
		public Executor getExecutor() {
			return new ThreadPoolTaskExecutor();
		}
	}

	// TODO dgg 这个 ${thread-pool.name} 变量无法解析，调试一下怎么回事
	@Service
	public static class UserService {
		@Async("${thread-pool.name}")
		public void insert(User user) {
			log.info("begin to insert user({})", user);
			log.info("current thread: {}", Thread.currentThread().getName());
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
		private String blog;
		private Date birthday;
	}
}
