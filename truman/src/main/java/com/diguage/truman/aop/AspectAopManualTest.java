package com.diguage.truman.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.util.StopWatch;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-18 14:43:36
 */
public class AspectAopManualTest {
	public static final Logger logger = LoggerFactory.getLogger(AspectAopManualTest.class);

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
		public Object performanceTrace(ProceedingJoinPoint joinPoint) throws Throwable {
			StopWatch watch = new StopWatch();
			try {
				watch.start();
				return joinPoint.proceed();
			} finally {
				watch.stop();
				if (logger.isInfoEnabled()) {
					logger.info("PT in method[{}] {}",
							joinPoint.getSignature().getName(), watch);
				}
			}
		}
	}

	public class Foo {
		public void method1() {
			System.out.println("method1 execution.");
		}

		public void method2() {
			System.out.println("method2 execution.");
		}
	}
}
