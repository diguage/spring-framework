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

import jakarta.annotation.Resource;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-24 13:02
 */
public class CircularDependenceSingletonTest {
	public static final Log log = LogFactory.getLog(CircularDependenceSingletonTest.class);

	@Test
	public void test() {
		log.info("OK");
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

		log.info(applicationContext.getBean(A.class));
		log.info(applicationContext.getBean(B.class));

		log.info("-A--------");
		A a = applicationContext.getBean(A.class);
		log.info(a);
		log.info(a.b);

		log.info("-B--------");
		B b = applicationContext.getBean(B.class);
		log.info(b);
		log.info(b.a);
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
					B.class.getName()};
		}
	}


	@Component
	public static class A {
		@Autowired
		B b;
	}

	@Component
	public static class B {
		@Resource
		A a;
	}
}
