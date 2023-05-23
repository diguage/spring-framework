package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

public class ApplicationEventTest {
  @Test
  public void test() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
//    context.addApplicationListener(new LogApplicationListener());
    context.register(Config.class);
    context.refresh();
    UserService service = context.getBean(UserService.class);
    System.out.println(service);
  }

  @Configuration
  @Import({UserService.class, LogApplicationListener.class})
  public static class Config {
  }

  @Component
  public static class UserService {
    public String getById(long id) {
      return "id-" + id;
    }
  }

  public static class LogApplicationListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
      System.out.println(event);
      Object source = event.getSource();
      if (source instanceof ConfigurableApplicationContext ctx) {
        ConfigurableListableBeanFactory factory = ctx.getBeanFactory();
        String[] names = ctx.getBeanDefinitionNames();
        for (String name : names) {
          BeanDefinition definition = factory.getBeanDefinition(name);
          if (UserService.class.getName().equals(definition.getBeanClassName())) {
            UserService bean = (UserService) ctx.getBean(name);
            String username = bean.getById(System.currentTimeMillis());
            System.out.println(username);
          }
        }
      }
    }
  }

}
