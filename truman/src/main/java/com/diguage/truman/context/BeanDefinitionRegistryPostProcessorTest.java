package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 17:29
 */
public class BeanDefinitionRegistryPostProcessorTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.addBeanFactoryPostProcessor(new LogBeanDefinitionRegistryPostProcessor());
		context.refresh();
		LogBeanFactoryPostProcessor processor = context.getBean(LogBeanFactoryPostProcessor.class);
		System.out.println(processor);
	}

	public static class LogBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
			System.out.println("\nLogBeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry\n");
			RootBeanDefinition beanDefinition = new RootBeanDefinition(LogBeanFactoryPostProcessor.class);
			registry.registerBeanDefinition(beanDefinition.getBeanClassName(), beanDefinition);
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			System.out.println("\nLogBeanDefinitionRegistryPostProcessor.postProcessBeanFactory\n");
		}
	}

	public static class LogBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			System.out.println("\nLogBeanFactoryPostProcessor.postProcessBeanFactory");
			System.out.println(Arrays.toString(beanFactory.getBeanDefinitionNames()).replaceAll(",", ",\n"));
			System.out.println();
		}
	}


}
