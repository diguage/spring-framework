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
 * 验证 this 的匹配规则。
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-19 21:27:47
 */
public class AspectThisTest {
	public static final Logger logger = LoggerFactory.getLogger(AspectThisTest.class);

	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		// TODO 竟然不能写成 Rabbit.class。 为什么？
		Movable bean = context.getBean(Movable.class);
		bean.move();
	}

	@Configuration
	@Import(ThisImportSelector.class)
	@EnableAspectJAutoProxy // 注意：这行必须加
	public static class Config {
	}

	public static class ThisImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata metadata) {
			return new String[]{
					Rabbit.class.getName(),
					ThisAspect.class.getName()
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
	public static class ThisAspect {
		@Pointcut("this(com.diguage.truman.aop.AspectThisTest.Movable)")
		public void doThisInterface() {
		}

		@Around("doThisInterface()")
		public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
			try {
				return joinPoint.proceed();
			} finally {
				logger.info("aspect executing...");
			}
		}
	}
}
