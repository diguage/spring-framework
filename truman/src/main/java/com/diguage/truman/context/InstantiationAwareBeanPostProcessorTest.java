package com.diguage.truman.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.beans.PropertyDescriptor;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 20:30
 */
public class InstantiationAwareBeanPostProcessorTest {
	// TODO

//	public static class LogInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
//		@Override
//		public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
//			return null;
//		}
//
//		@Override
//		public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
//			return false;
//		}
//
//		@Override
//		public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
//			return null;
//		}
//
//		@Override
//		public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
//			return null;
//		}
//
//		@Override
//		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//			return null;
//		}
//
//		@Override
//		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//			return null;
//		}
//	}

}
