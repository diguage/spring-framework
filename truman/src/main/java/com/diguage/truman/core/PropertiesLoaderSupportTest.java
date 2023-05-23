package com.diguage.truman.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PropertiesLoaderSupportTest {
  @Test
  public void test() {
    ClassPathXmlApplicationContext context
        = new ClassPathXmlApplicationContext("classpath:com/diguage/"
        + "truman/core/PropertiesApplicationContextTest.xml");
    UserService userService = context.getBean(UserService.class);
    System.out.println(userService.getUserById(119L));
  }

  public static class UserService {
    @Value("${user.appId}")
    private String appId;

    public String getUserById(Long id) {
      return "user-/" + appId + "/-" + id;
    }
  }
}
