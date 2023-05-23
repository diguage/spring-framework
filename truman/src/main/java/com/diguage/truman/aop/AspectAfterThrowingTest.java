package com.diguage.truman.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;

/**
 * 验证 @AfterThrowing 的属性 throwing
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-19 23:28:27
 */
@Slf4j
public class AspectAfterThrowingTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		Movable bean = context.getBean(Movable.class);
		bean.move("Henan");
	}

	@Configuration
	@Import(AspectImportSelector.class)
	@EnableAspectJAutoProxy
	public static class Config {
	}

	public static class AspectImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata metadata) {
			return new String[]{
					Rabbit.class.getName(),
					AfterThrowingAspect.class.getName()
			};
		}
	}

	public interface Movable {
		void move(String target);
	}

	public static class Rabbit implements Movable {
		@Override
		public void move(String target) {
			log.info("Rabbit.move executing...");
			throw new RuntimeException("Rabbit throws an error.");
		}
	}

	@Aspect
	public static class AfterThrowingAspect {
		@AfterThrowing(pointcut = "execution(void *.move(String, ..))",
				throwing = "e")
		public void afterThrowing(JoinPoint joinPoint, RuntimeException e) {
			Object[] args = joinPoint.getArgs();
			log.error("Target threw an error. args={}",
					Arrays.toString(args), e);
		}
	}
}
