package com.diguage.truman.aop;

import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author D瓜哥, https://www.diguage.com
 */
public class ProxyTargetClassTest {
  @Test
  public void test() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    UserService bean = context.getBean(UserService.class);
    bean.test();
    bean.getDesc();
    bean.setDesc("This is a test.");

    String user = bean.getById(119);
    System.out.println(user);

    BeanDefinition definition = context.getBeanDefinition(UserService.class.getName());
    System.out.println(definition);
  }

  @Configuration
  @Import(AopImportSelector.class)
  @EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
  public static class Config {
  }

  // 使用 @Import 和 ImportSelector 搭配，就可以省去 XML 配置
  public static class AopImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
      return new String[]{
          UserDao.class.getName(),
          UserService.class.getName(),
          TestAspect.class.getName()
      };
    }
  }

  @Aspect
  public static class TestAspect {
    @Pointcut("execution(* com.diguage.truman.aop.ProxyTargetClassTest.UserService.test(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void beforeTest() {
      System.out.println("beforeTest");
    }

    @After("pointcut()")
    public void afterTest() {
      System.out.println("afterTest");
    }

    @Around("pointcut()")
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

}
