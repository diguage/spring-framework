package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

/**
 * 实现 BeanFactoryPostProcessor 接口，可以在spring的bean创建之前修改bean的定义属性。
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 15:07
 */
public class BeanFactoryPostProcessorTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.addBeanFactoryPostProcessor(new LogBeanFactoryPostProcessor());
		context.refresh();
		UserService userService = context.getBean(UserService.class);
		System.out.println(userService.getById(119L));
	}

	@Configuration
	@Import(UserService.class)
	public static class Config {
	}


	@Component
	public static class UserService {

		private String[] filters;

		public String[] getFilters() {
			return filters;
		}

		public void setFilters(String[] filters) {
			this.filters = filters;
		}

		public String getById(Long id) {
			System.out.println(Arrays.toString(filters));
			return "Name-" + id;
		}

		public void init() {
			System.out.println("init");
		}
	}

	public static class LogBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			String[] names = beanFactory.getBeanDefinitionNames();
			System.out.println();
			System.out.println(Arrays.toString(names).replaceAll(",", ",\n"));
			System.out.println();
			BeanDefinition definition = beanFactory.getBeanDefinition(UserService.class.getName());
			if (Objects.nonNull(definition)) {
				// TODO 确认一下 getAttribute 中可以访问什么？
				// 刚刚调试，就放了一个 ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE。
				// 跟代码发现，是在 ConfigurationClassPostProcessor.processConfigBeanDefinitions 中处理的。
				// 后续再跟一下代码。
				// definition.getAttribute("");

				definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
				definition.setDescription("This is a dealed bean.");
				definition.setInitMethodName("init");

				definition.getPropertyValues()
						.addPropertyValue("filters", "com.diguage.filter.LogFilter");
			}
		}
	}

	/**
	 * BeanFactoryPostProcessor
	 * BeanDefinitionRegistryPostProcessor
	 * BeanPostProcessor
	 */
}
