package com.diguage.truman.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.util.StopWatch;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-18 14:43:36
 */
@Slf4j
public class AspectAopManualTest {
	@Test
	public void test() {
		AspectJProxyFactory weaver = new AspectJProxyFactory();
		weaver.setProxyTargetClass(true);
		weaver.setTarget(new Foo());
		weaver.addAspect(PerformanceTraceAspect.class);
		Foo proxy = weaver.getProxy();
		proxy.method1();
		proxy.method2();
	}

	@Aspect
	public static class PerformanceTraceAspect {
		@Pointcut("execution(public void *.method1()) " +
				"|| execution(public void *.method2())")
		public void pointcutName() {
		}

		@Around("pointcutName()")
		public Object trace(ProceedingJoinPoint jp) throws Throwable {
			StopWatch watch = new StopWatch();
			try {
				watch.start();
				return jp.proceed();
			} finally {
				watch.stop();
				if (log.isInfoEnabled()) {
					log.info("PT in method[{}] {}",
							jp.getSignature().getName(), watch);
				}
			}
		}
	}

	public class Foo {
		public void method1() {
			log.info("method1 execution.");
		}

		public void method2() {
			log.info("method2 execution.");
		}
	}
}
