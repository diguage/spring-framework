package com.diguage.truman.beans;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * FactoryBean 测试
 *
 * @author D瓜哥 · https://www.diguage.com/
 * @since 2020-05-26 16:34
 */
public class FactoryBeanTest {
  private static final String FACTORY_BEAN_NAME = "userServiceFactoryBean";

  @Test
  public void test() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();

    Object bean = context.getBean(FACTORY_BEAN_NAME);
    System.out.println(bean.getClass().getName());

    UserService userService = context.getBean(UserService.class);
    System.out.println(userService.getById(119L));

    // 实例不会缓存，每次调用 getBean 都会创建一个实例
    UserService userService2 = context.getBean(UserService.class);
    System.out.println(userService == userService2);

    System.out.println("-↓----");
    // &userServiceFactoryBean = FactoryBeanTest$UserServiceFactoryBean@c260bdc
    System.out.println("&userServiceFactoryBean = "
        + context.getBean("&userServiceFactoryBean")); // <1>

    //  userServiceFactoryBean = FactoryBeanTest$UserService@75e01201
    System.out.println(" userServiceFactoryBean = "
        + context.getBean("userServiceFactoryBean"));  // <2>
    System.out.println("-↑----");

    UserServiceFactoryBean factoryBean = context.getBean(UserServiceFactoryBean.class);
    System.out.println(factoryBean);
    System.out.println(Arrays.toString(context.getBeanDefinitionNames())
        .replaceAll(",", ",\n"));
  }

  @Configuration
  public static class Config {
    @Bean(FACTORY_BEAN_NAME)
    public UserServiceFactoryBean userServiceFactoryBean() {
      return new UserServiceFactoryBean();
    }
  }


  public static class UserService {
    public String getById(Long id) {
      return "Name-" + id;
    }
  }

  public static class UserServiceFactoryBean implements FactoryBean<UserService> {
    @Override
    public UserService getObject() throws Exception {
      return new UserService();
    }

    @Override
    public Class<?> getObjectType() {
      return UserService.class;
    }

    @Override
    public boolean isSingleton() {
      return true;
    }
  }
}
