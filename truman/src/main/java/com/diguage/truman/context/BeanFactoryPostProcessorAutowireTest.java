package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

/**
 * 实现 BeanFactoryPostProcessor 接口，可以在spring的bean创建之前修改bean的定义属性。
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 15:07
 */
public class BeanFactoryPostProcessorAutowireTest {
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
	@Import(LogSelector.class)
	public static class Config {
	}

	public static class LogSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					UserService.class.getName(),
					// 注意这里！应该整两个 BeanFactoryPostProcessor 实现类来对比
					LogBeanFactoryPostProcessor.class.getName()
			};
		}
	}

	@Component
	public static class UserService {
		public String getById(Long id) {
			return "Name-" + id;
		}

		public void init() {
			System.out.println("start to invoke init method...");
			System.out.println("init");
			System.out.println();
		}
	}

	@Test
	void name() {
	}

	@Component
	public static class LogBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			String[] names = beanFactory.getBeanDefinitionNames();
			System.out.println();
			System.out.println(Arrays.toString(names).replaceAll(",", ",\n"));
			System.out.println();
			BeanDefinition definition = beanFactory.getBeanDefinition(UserService.class.getName());
			definition.getAttribute("");
			if (Objects.nonNull(definition)) {
				definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
				definition.setDescription("This is a dealed bean.");
				definition.setInitMethodName("init");
			}
		}
	}

	/**
	 * BeanFactoryPostProcessor
	 * BeanDefinitionRegistryPostProcessor
	 * BeanPostProcessor
	 */
}
