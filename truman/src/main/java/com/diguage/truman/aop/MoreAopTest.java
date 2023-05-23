package com.diguage.truman.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import jakarta.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-06-02 11:12
 */
public class MoreAopTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();
		UserService bean = context.getBean(UserService.class);
		bean.test();
		bean.test();

		String user = bean.getById(119);
		System.out.println(user);

		BeanDefinition definition = context.getBeanDefinition(UserService.class.getName());
		System.out.println(definition);

		OrderServiceImpl orderService = context.getBean(OrderServiceImpl.class);
		System.out.println(orderService.getById(120));

		System.out.println(context.getBean(OrderServiceImpl.class).getById(122));
	}

	@Configuration
	@Import(AopImportSelector.class)
	@EnableAspectJAutoProxy(exposeProxy = true)
	public static class Config {
		@Bean
		public UsageTracked usageTracked() {
			return new DefaultUsageTracked();
		}
	}

	public static class AopImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					UserDao.class.getName(),
					UserService.class.getName(),
					OrderServiceImpl.class.getName(),
					UserServiceAspect.class.getName(),
					UserServiceAspect2.class.getName(),
					OrderServiceAspect.class.getName(),
					UsageTrackingAspect.class.getName()
			};
		}
	}

	@Aspect
	public static class UserServiceAspect {
		@Pointcut("execution(* com.diguage.truman.aop.MoreAopTest$UserService.test(..))")
		public void test() {
		}

		@Before("test()")
		public void beforeTest() {
			System.out.println("beforeTest");
		}

		@After("test()")
		public void afterTest() {
			System.out.println("afterTest");
		}

		@Around("test()")
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

	@Aspect
	public static class UserServiceAspect2 {
		@Pointcut("execution(* com.diguage.truman.aop.MoreAopTest$UserService.test(..))")
		public void test() {
		}

		@Before("test()")
		public void beforeTest() {
			System.out.println("222-beforeTest");
		}

		@After("test()")
		public void afterTest() {
			System.out.println("222-afterTest");
		}

		@Around("test()")
		public Object aroundTest(ProceedingJoinPoint pjp) {
			System.out.println("222-aroundBefore1");
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
			System.out.println("222-aroundAfter1");
			return restul;
		}
	}

	@Aspect
	public static class UsageTrackingAspect {

		@DeclareParents(value = "com.diguage.truman.aop.*+", defaultImpl = DefaultUsageTracked.class)
		public static UsageTracked mixin;

		@Before("execution(* com.diguage.truman.aop.MoreAopTest$OrderServiceImpl.getById(..)) && this(usageTracked)")
		public void recordUsage(UsageTracked usageTracked) {
			usageTracked.incrementUseCount();
		}
	}

	public interface UsageTracked {
		int incrementUseCount();
	}


	public static class DefaultUsageTracked implements UsageTracked {
		private AtomicInteger count = new AtomicInteger(0);

		@Override
		public int incrementUseCount() {
			return count.incrementAndGet();
		}
	}

	@Aspect
	public static class OrderServiceAspect {
		@Pointcut("execution(* com.diguage.truman.aop.MoreAopTest$OrderService.getById(..))")
		public void getById() {
		}

		@Before("getById()")
		public void beforeGetById() {
			System.out.println("beforeTest");
		}

		@Around("getById()")
		public Object aroundGetById(ProceedingJoinPoint pjp) {
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


	public static class UserDao {
		public String getById(int id) {
			return "diguage-" + id;
		}
	}

	public static class UserService {
		private String desc = "testBean";

		@Resource
		private UserDao userDao;

		public String getDesc() {
			System.out.println("getDesc");
			this.test();
			System.out.println("--this----------getDesc");
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
			// 使用 @EnableAspectJAutoProxy(exposeProxy = true) 打开 exposeProxy = true
			// 则必须这样写，才能获取到当前的代理对象，然后调用的方法才是被 AOP 处理后的方法。
			// 使用 this.methodName() 调用，依然调用的是原始的、未经 AOP 处理的方法
			((UserService) AopContext.currentProxy()).test();
			System.out.println("--AopContext----setDesc");
		}

		public void test() {
			System.out.println("----------------test");
		}

		public String getById(int id) {
			return userDao.getById(id);
		}
	}

	public interface OrderService {
		String getById(int id);
	}

	public static class OrderServiceImpl implements OrderService {
		public String getById(int id) {
			return "Order-" + id;
		}
	}
}
