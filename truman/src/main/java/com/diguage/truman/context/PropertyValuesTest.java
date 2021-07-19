package com.diguage.truman.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-08-05 10:15
 */
public class PropertyValuesTest {
	public static final Log log = LogFactory.getLog(PropertyValuesTest.class);

	@Test
	public void test() {
		AnnotationConfigApplicationContext applicationContext
				= new AnnotationConfigApplicationContext();
		applicationContext.register(Config.class);
		applicationContext.refresh();

		// TODO 还没有成功
		// 查看如果处理这种依赖，如何进行实例化？会不会封装成 BeanDefinition ？

		BeanDefinition definition = applicationContext.getBeanDefinition(A.class.getName());
		MutablePropertyValues propertyValues = definition.getPropertyValues();
		propertyValues.add("name", "https://www.diguage.com");
//		propertyValues.add("b", "com.diguage.truman.context.PropertyValuesTest.B");
//		propertyValues.add("b", B.class);
		propertyValues.add("b", new B());

		A bean = applicationContext.getBean(A.class);
		System.out.println(bean);
		System.out.println(bean.b);
	}

	@Configuration
	@Import(AbcImportSelector.class)
	public static class Config {
	}

	public static class AbcImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					A.class.getName()};
		}
	}


	@Component
	@Lazy
	public static class A {
		String name;
		B b;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public B getB() {
			return b;
		}

		public void setB(B b) {
			this.b = b;
		}
	}

	public static class B {
	}
}
