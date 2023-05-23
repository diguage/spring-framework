package com.diguage.truman.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-24 13:02
 */
public class CircularDependenceConstructorTest {
	public static final Log log = LogFactory.getLog(CircularDependenceConstructorTest.class);

	@Test
	public void test() {
		log.debug("OK");
		/**
		 * 1. scan --- bd --- map
		 * 2. 遍历map
		 * 3. validate
		 * 4. 得到class
		 * 5. 推断构造方法
		 * 6. 反射，实例化这个对象
		 * 7. 合并 beanDefinition
		 * 8. 提前暴露一个bean工厂对象
		 * 9. 填充属性---自动注入
		 * 10. 部分aware接口
		 * 11. 执行---部分aware接口，执行 Spring 生命周期回调方法
		 */
		AnnotationConfigApplicationContext applicationContext
				= new AnnotationConfigApplicationContext();
		applicationContext.register(Config.class);
		applicationContext.refresh();

		log.debug(applicationContext.getBean(A.class));
		log.debug(applicationContext.getBean(B.class));
		log.debug(applicationContext.getBean(C.class));
	}

	@Configuration
	@Import(AbcImportSelector.class)
	public static class Config {
	}

	public static class AbcImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					A.class.getName(),
					B.class.getName(),
					C.class.getName()};
		}
	}


	@Component
	public static class A {
		B b;

		public A(@Autowired B b) {
			this.b = b;
		}
	}

	@Component
	public static class B {
		C c;

		public B(@Autowired C c) {
			this.c = c;
		}
	}

	@Component
	public static class C {
		A a;

		public C(@Autowired A a) {
			this.a = a;
		}
	}
}
