package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-27 20:05
 */
public class ObjectFactoryTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		// TODO

//		UserService userService = context.getBean(UserService.class);
//		System.out.println(userService.getById(119L));
//
//		System.out.println("-↓----");
//		System.out.println("&userServiceFactoryBean = "
//				+ context.getBean("&userServiceObjectFactory")); // <1>
//		System.out.println(" userServiceFactoryBean = "
//				+ context.getBean("userServiceObjectFactory"));  // <2>
//		System.out.println("-↑----");
	}

	@Configuration
	public static class Config {
		@Bean
		public UserServiceObjectFactory userServiceObjectFactory() {
			return new UserServiceObjectFactory();
		}
	}


	public static class UserService {
		public String getById(Long id) {
			return "Name-" + id;
		}
	}

	public static class UserServiceObjectFactory implements ObjectFactory<UserService> {
		@Override
		public UserService getObject() throws BeansException {
			return new UserService();
		}
	}
}
