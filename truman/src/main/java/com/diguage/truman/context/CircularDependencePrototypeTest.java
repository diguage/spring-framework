package com.diguage.truman.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.annotation.Scope;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-24 13:02
 */
public class CircularDependencePrototypeTest {
	public static final Log log = LogFactory.getLog(CircularDependencePrototypeTest.class);

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
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(Config.class);
		applicationContext.refresh();

		log.debug(applicationContext.getBean(A.class));
		log.debug(applicationContext.getBean(B.class));
		log.debug(applicationContext.getBean(C.class));

		System.out.println("-A--------");
		A a = applicationContext.getBean(A.class);
		log.debug(a);
		log.debug(a.b);
		System.out.println("-B--------");
		B b = applicationContext.getBean(B.class);
		log.debug(b);
		log.debug(b.c);
		System.out.println("-C--------");
		C c = applicationContext.getBean(C.class);
		log.debug(c);
		log.debug(c.a);
		log.debug(applicationContext.getBean(C.class));
		log.debug(applicationContext.getBean(C.class).a);
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
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public static class A {
		@Autowired
		B b;
	}

	@Component
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public static class B {
		@Autowired
		C c;
	}

	@Component
//	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public static class C {
		@Autowired
		A a;
	}
}
