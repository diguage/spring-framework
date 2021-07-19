package com.diguage.truman.aop;

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
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-18 23:40:42
 */
public class AspectAnnotationTest {
	public static final Logger logger = LoggerFactory.getLogger(AspectAnnotationTest.class);

	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		DiguageTask task = context.getBean(DiguageTask.class);
		task.run();
	}

	@Configuration
	@Import(AnnoImportSelector.class)
	@EnableAspectJAutoProxy // 注意：这行必须加
	public static class Config {
	}

	public static class AnnoImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					DiguageTask.class.getName(),
					AnnoTypeAspect.class.getName(),
					AnnoMethodAspect.class.getName()
			};
		}
	}

	@AnnoTypeAnnotation
	public static class DiguageTask {
		@AnnoMethodAnnotation
		public void run() {
			logger.info("Diguage.run executing...");
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface AnnoMethodAnnotation {
	}

	@Aspect
	public static class AnnoMethodAspect {
		@Pointcut("@annotation(com.diguage.truman.aop.AspectAnnotationTest.AnnoMethodAnnotation)")
		public void annoMethod() {
		}

		@Around("annoMethod()")
		public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
			try {
				return joinPoint.proceed();
			} finally {
				logger.info("AnnoMethodAspect.annoMethod executing...");
			}
		}
	}


	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface AnnoTypeAnnotation {
	}

	@Aspect
	public static class AnnoTypeAspect {
		@Pointcut("@annotation(com.diguage.truman.aop.AspectAnnotationTest.AnnoTypeAnnotation)")
		public void annoType() {
		}

		/**
		 * 这里的代码没有执行到，说明对于 @annotation 注解来说，只支持方法注解。
		 */
		@Around("annoType()")
		public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
			try {
				return joinPoint.proceed();
			} finally {
				logger.info("AnnoTypeAspect.annoType executing...");
			}
		}
	}
}
