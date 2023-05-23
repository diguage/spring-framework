package com.diguage.truman.context;

import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.diguage.truman.util.Constans.BASE_CLASS_PATH;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 19:33
 */
public class ApplicationListenerParentTest {
  @Test
  public void test() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    UserService userService = LoaderListener.subcontext.getBean(UserService.class);
    String user = userService.getUserById(119L);
    System.out.println(user);
  }

  @Configuration
  @Import({UserDao.class, LoaderListener.class})
  public static class Config {
  }

  @Component("userDao")
  public static class UserDao {
    public String getUserById(Long id) {
      return "user-" + id + "@UserDao";
    }
  }

  public static class LoaderListener implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {
    private static ApplicationContext context;
    private static ApplicationContext subcontext;
    private static boolean loaded = false;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
      if (Objects.isNull(LoaderListener.context)) {
        LoaderListener.context = ctx;
      } else {
        boolean equals = Objects.equals(LoaderListener.context, ctx);
        System.out.println("equals=" + equals);
      }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
      System.out.println(event);
      if (!LoaderListener.loaded) {
        LoaderListener.loaded = true;
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[]{BASE_CLASS_PATH + "/context/ApplicationListenerParentTest.xml"}, LoaderListener.context);
        LoaderListener.subcontext = ctx;
      }
    }
  }

  public static class UserService {
    @Setter
    private UserDao userDao;

    public String getUserById(Long id) {
      return userDao.getUserById(id);
    }
  }
}
