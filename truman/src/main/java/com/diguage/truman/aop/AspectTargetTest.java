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

/**
 * 验证 target 的匹配规则。
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-19 23:28:27
 */
public class AspectTargetTest {
	public static final Logger logger = LoggerFactory.getLogger(AspectTargetTest.class);

	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		Movable bean = context.getBean(Movable.class);
		bean.move();
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
					Rabbit.class.getName(),
					TargetAspect.class.getName()
			};
		}
	}

	public interface Movable {
		void move();
	}

	public static class Rabbit implements Movable {
		@Override
		public void move() {
			System.out.println("Rabbit.move executing...");
		}
	}

	@Aspect
	public static class TargetAspect {
		@Pointcut("target(com.diguage.truman.aop.AspectTargetTest.Movable)")
		public void doTargetInterface() {
		}

		@Around("doTargetInterface()")
		public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
			String name = joinPoint.getSignature().getName();
			try {
				return joinPoint.proceed();
			} finally {
				logger.info("aspect executing. Signature = {}", name);
			}
		}
	}
}
