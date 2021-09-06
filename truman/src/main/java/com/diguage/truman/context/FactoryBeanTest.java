package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * FactoryBean 测试
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 16:34
 */
public class FactoryBeanTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		UserService userService = context.getBean(UserService.class);
		System.out.println(userService.getById(119L));

		System.out.println("-↓----");
		System.out.println("&userServiceFactoryBean = "
				+ context.getBean("&userServiceFactoryBean")); // <1>
		System.out.println(" userServiceFactoryBean = "
				+ context.getBean("userServiceFactoryBean"));  // <2>
		System.out.println("-↑----");

		UserServiceFactoryBean factoryBean = context.getBean(UserServiceFactoryBean.class);
		System.out.println(factoryBean);
		System.out.println(Arrays.toString(context.getBeanDefinitionNames())
				.replaceAll(",", ",\n"));
	}

	@Configuration
	public static class Config {
		@Bean
		public UserServiceFactoryBean userServiceFactoryBean() {
			return new UserServiceFactoryBean();
		}
	}


	public static class UserService {
		public String getById(Long id) {
			return "Name-" + id;
		}
	}

	public static class UserServiceFactoryBean implements FactoryBean<UserService> {
		@Override
		public UserService getObject() throws Exception {
			return new UserService();
		}

		@Override
		public Class<?> getObjectType() {
			return UserService.class;
		}

		@Override
		public boolean isSingleton() {
			return false;
		}
	}
}
