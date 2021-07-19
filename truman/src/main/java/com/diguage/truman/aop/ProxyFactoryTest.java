package com.diguage.truman.aop;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;

/**
 * @author D瓜哥, https://www.diguage.com
 */
public class ProxyFactoryTest {
	@Test
	public void testJdkProxy() {
		MockExecutable mockTask = new MockExecutable();
		ProxyFactory factory = new ProxyFactory(mockTask);
		factory.setInterfaces(Executable.class);
		NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
		advisor.setMappedName("execute");
		advisor.setAdvice(new PerformanceMonitorInterceptor());
		factory.addAdvisor(advisor);
		Executable proxyExecutable = (Executable) factory.getProxy();
		System.out.println(proxyExecutable.getClass());
		proxyExecutable.execute();
	}

	public interface Executable {
		void execute();
	}

	public static class MockExecutable implements Executable {
		@Override
		public void execute() {
			System.out.println("MockTask.execute");
		}
	}

	@Test
	public void testCglibProxy() {
		ProxyFactory factory = new ProxyFactory(new Task());
		NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
		advisor.setMappedName("execute");
		advisor.setAdvice(new PerformanceMonitorInterceptor());
		factory.addAdvisor(advisor);
		Task proxyTask = (Task) factory.getProxy();
		System.out.println(proxyTask.getClass());
		proxyTask.execute();
	}

	public static class Task {
		public void execute() {
			System.out.println("Task.execute");
		}
	}
}
