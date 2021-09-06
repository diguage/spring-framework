package com.diguage.truman.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;


/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-06-21 11:03
 */
public class TargetSourceTest {
	@Test
	public void test() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();
		String[] names = context.getBeanDefinitionNames();
		System.out.println(Arrays.toString(names).replaceAll(",", ",\n"));
//		UserServiceTargetSource targetSource = context.getBean(UserServiceTargetSource.class);
//		UserService bean = (UserService) targetSource.getTarget();
//
//		bean.test();
//
//		bean.getDesc();
//		bean.setDesc("This is a test.");
	}

	@Configuration
	@Import(AopImportSelector.class)
	@EnableAspectJAutoProxy(exposeProxy = true)
	public static class Config {

		public UserServiceTargetSource userServiceTargetSource(UserService userService) {
			return new UserServiceTargetSource(userService);
		}

		public TargetSourceCreator targetSourceCreator() {
			return new TargetSourceCreator() {
				@Override
				public TargetSource getTargetSource(Class<?> beanClass, String beanName) {
					if (beanClass.equals(UserService.class)) {
						return userServiceTargetSource((UserService) BeanUtils.instantiateClass(beanClass));
					}
					return null;
				}
			};
		}

		@Bean
		BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
			BeanNameAutoProxyCreator autoProxyCreator = new BeanNameAutoProxyCreator();
			autoProxyCreator.setBeanNames("*");
			autoProxyCreator.setCustomTargetSourceCreators(targetSourceCreator());
			return autoProxyCreator;
		}

//		@Bean
//		public ProxyFactoryBean getProxyFactoryBean(@Autowired UserServiceTargetSource userServiceTargetSource) {
//			ProxyFactoryBean result = new ProxyFactoryBean();
//			result.setTargetSource(userServiceTargetSource);
//			return result;
//		}
	}

	public static class AopImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					TestAspect.class.getName()
			};
		}
	}

	@Aspect
	public static class TestAspect {
		@Pointcut("execution(* com.diguage.truman.aop.TargetSourceTest$UserService.test(..))")
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

	public static class UserDao {
		public String getById(int id) {
			return "diguage-" + id;
		}
	}

	public static class UserService {
		private String desc = "testBean";


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
	}

	public static class UserServiceTargetSource implements TargetSource {

		private UserService userService;

		public UserServiceTargetSource(@Autowired UserService userService) {
			this.userService = userService;
		}

		@Override
		public Class<?> getTargetClass() {
			return UserService.class;
		}

		@Override
		public boolean isStatic() {
			return false;
		}

		@Override
		public Object getTarget() throws Exception {
			return userService;
		}

		@Override
		public void releaseTarget(Object target) throws Exception {
		}
	}

}
