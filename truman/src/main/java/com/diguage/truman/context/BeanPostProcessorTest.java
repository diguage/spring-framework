package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 15:53
 */
public class BeanPostProcessorTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);

		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.addBeanPostProcessor(new LogBeanPostProcessor());

		context.refresh();
		UserService userService = context.getBean(UserService.class);
		System.out.println(userService.getById(119L));
	}

	@Configuration
	@Import(LogSelector.class)
	public static class Config {
	}

	public static class LogSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					UserService.class.getName()
			};
		}
	}

	@Component
	public static class UserService implements InitializingBean {
		public String getById(Long id) {
			return "Name-" + id;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			System.out.println("\nstart to invoke init method...");
			System.out.println("init");
			System.out.println();
		}
	}

	@Component
	public static class LogBeanPostProcessor implements BeanPostProcessor {
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			System.out.println("start to create [" + beanName + "]: isNull=" + Objects.isNull(bean));
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			System.out.println("finish creating [" + beanName + "]: isNull=" + Objects.isNull(bean));
			return bean;
		}
	}
}
