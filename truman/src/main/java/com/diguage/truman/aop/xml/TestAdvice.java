package com.diguage.truman.aop.xml;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-08-09 10:40:17
 */
public class TestAdvice {

	public void beforeTest() {
		System.out.println("beforeTest");
	}

	public void afterTest() {
		System.out.println("afterTest");
	}

	public Object aroundTest(ProceedingJoinPoint pjp) {
		System.out.println("aroundBefore1");
		Object restul = null;
		Signature signature = pjp.getSignature();
		System.out.println(pjp.getKind());
		Object target = pjp.getTarget();
		System.out.println(target.getClass().getName() + "#" + signature.getName());
		try {
			restul = pjp.proceed();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		System.out.println("aroundAfter1");
		return restul;
	}
}
