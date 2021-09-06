package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 21:35
 */
public class ResourceLoaderTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();
		UserService service = context.getBean(UserService.class);
		service.get();
	}

	@Configuration
	@Import(UserService.class)
	public static class Config {
	}

	@Component
	public static class UserService {
		@Autowired
		ResourceLoader resourceLoader;

		@Resource
		private ApplicationContext applicationContext;

		@PostConstruct
		public void init() {
			System.out.println(resourceLoader);
			System.out.println(applicationContext);
		}

		public void get() {
			// 由此证明，这两个对象可以直接注入进来的。
			// 而且，注入后的对象是同一个对象，就是上面实例化
			// 的 AnnotationConfigApplicationContext 对象
			System.out.println(resourceLoader);
			System.out.println(applicationContext);
		}
	}
}
