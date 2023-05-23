package com.diguage.truman.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 验证 @target 的匹配规则。
 *
 * TODO 如何验证其动态匹配的特性？
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-18 21:18:43
 */
@Slf4j
public class AspectAnnoTargetTest {
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
	@Import(TargetImportSelector.class)
	@EnableAspectJAutoProxy // 注意：这行必须加
	public static class Config {
	}

	public static class TargetImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata metadata) {
			return new String[]{
					DiguageTask.class.getName(),
					TypeAspect.class.getName(),
					MethodAspect.class.getName()
			};
		}
	}

	@TypeAnnotation
	public static class DiguageTask {
		@MethodAnnotation
		public void run() {
			log.info("Diguage.run executing...");
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface TypeAnnotation {
	}

	@Aspect
	public static class TypeAspect {
		@Pointcut("@target(com.diguage.truman.aop." +
				"AspectAnnoTargetTest.TypeAnnotation)")
		public void doType() {
		}

		@Around("doType()")
		public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
			try {
				return joinPoint.proceed();
			} finally {
				log.info("TypeAspect executing...");
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface MethodAnnotation {
	}

	@Aspect
	public static class MethodAspect {
		@Pointcut("@target(com.diguage.truman.aop." +
				"AspectAnnoTargetTest.MethodAnnotation)")
		public void doMethod() {
		}

		/**
		 * 这里的代码没有执行到，说明对于 @target 注解来说，只支持类注解。
		 */
		@Around("doMethod()")
		public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
			try {
				return joinPoint.proceed();
			} finally {
				log.info("MethodAspect executing...");
			}
		}
	}
}
