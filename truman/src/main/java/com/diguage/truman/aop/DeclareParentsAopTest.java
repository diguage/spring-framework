package com.diguage.truman.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

import jakarta.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-07-09 12:23
 */
@Slf4j
public class DeclareParentsAopTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();
		UserService bean = context.getBean(UserService.class);
		bean.test();

		String user = bean.getById(119);
		System.out.println(user);
		if (bean instanceof UsageTracked) {
			System.out.println("OKKK");
		}

		UsageTracked tracked = (UsageTracked) context.getBean(UserService.class);
		System.out.println(tracked.incrementUseCount());
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
		public String[] selectImports(AnnotationMetadata metadata) {
			return new String[]{
					UserDao.class.getName(),
					UserService.class.getName(),
					UserServiceAspect.class.getName(),
					DeclareParentsAspect.class.getName(),
					DefaultUsageTracked.class.getName()
			};
		}
	}

	@Aspect
	public static class UserServiceAspect {
		@Pointcut("execution(* com.diguage.truman.aop." +
				"MoreAopTest$UserService.test(..))")
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
			log.info(target.getClass().getName() + "#" + signature.getName());
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
	public static class DeclareParentsAspect {

		// 目前这部分是 OK了。


		@DeclareParents(value = "com.diguage.truman.aop." +
				"DeclareParentsAopTest.UserService+",
				defaultImpl = DefaultUsageTracked.class)
		public static UsageTracked mixin;

		// TODO 还不成功，纳闷，不知道该怎么弄？
//		@Before("com.diguage.truman.aop.MoreAopTest$UserService.test() && this(usageTracked)")
//		public void recordUsage(UsageTracked usageTracked) {
//			usageTracked.incrementUseCount();
//		}
	}

	public static interface UsageTracked {
		int incrementUseCount();
	}


	public static class DefaultUsageTracked implements UsageTracked {
		private AtomicInteger count = new AtomicInteger(0);

		@Override
		public int incrementUseCount() {
			return count.incrementAndGet();
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
			log.info("getDesc");
			this.test();
			log.info("--this----------getDesc");
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
			// 使用 @EnableAspectJAutoProxy(exposeProxy = true) 打开 exposeProxy = true
			// 则必须这样写，才能获取到当前的代理对象，然后调用的方法才是被 AOP 处理后的方法。
			// 使用 this.methodName() 调用，依然调用的是原始的、未经 AOP 处理的方法
			((UserService) AopContext.currentProxy()).test();
			log.info("--AopContext----setDesc");
		}

		public void test() {
			log.info("----------------test");
		}

		public String getById(int id) {
			return userDao.getById(id);
		}
	}
}
