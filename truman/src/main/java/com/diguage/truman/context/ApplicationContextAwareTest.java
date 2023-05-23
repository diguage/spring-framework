package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 21:35
 */
public class ApplicationContextAwareTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();
		UserService service = context.getBean(UserService.class);
		System.out.println(Arrays.toString(service.getBeanNames()));
	}

	@Configuration
	@Import(UserService.class)
	public static class Config {
	}

	@Component
	public static class UserService {
		@Autowired
		ApplicationContext applicationContext;

		public String[] getBeanNames() {
			if (Objects.nonNull(applicationContext)) {
				return applicationContext.getBeanDefinitionNames();
			}
			return new String[0];
		}
	}
}
