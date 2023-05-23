package com.diguage.truman.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-18 21:18:43
 */
@Slf4j
public class AspectWithinTest {

	@Test
	public void test() {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		DiguageTask task = context.getBean(DiguageTask.class);
		task.run();
	}

	@Configuration
	@Import(WithinImportSelector.class)
	@EnableAspectJAutoProxy // 注意：这行必须加
	public static class Config {
	}

	public static class WithinImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					DiguageTask.class.getName(),
					WithinTypeAspect.class.getName(),
					WithinMethodAspect.class.getName()
			};
		}
	}

	@WithinTypeAnnotation
	public static class DiguageTask {
		@WithinMethodAnnotation
		public void run() {
			log.info("Diguage.run executing...");
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface WithinTypeAnnotation {
	}

	@Aspect
	public static class WithinTypeAspect {
		@Pointcut("@within(com.diguage.truman.aop." +
				"AspectWithinTest.WithinTypeAnnotation)")
		public void withinType() {
		}

		@Around("withinType()")
		public Object around(ProceedingJoinPoint jp) throws Throwable {
			try {
				return jp.proceed();
			} finally {
				log.info("WithinTypeAspect executing...");
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface WithinMethodAnnotation {
	}

	@Aspect
	public static class WithinMethodAspect {
		@Pointcut("@within(com.diguage.truman.aop." +
				"AspectWithinTest.WithinMethodAnnotation)")
		public void withinMethod() {
		}

		/**
		 * 这里的代码没有执行到，说明对于 @Within 注解来说，只支持类注解。
		 */
		@Around("withinMethod()")
		public Object around(ProceedingJoinPoint jp) throws Throwable {
			try {
				return jp.proceed();
			} finally {
				log.info("WithinTypeAspect executing...");
			}
		}
	}
}
