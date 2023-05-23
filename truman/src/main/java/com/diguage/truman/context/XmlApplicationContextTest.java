package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2022-10-27 22:46:56
 */
public class XmlApplicationContextTest {

  @Test
  public void test() {
    ClassPathXmlApplicationContext context
        = new ClassPathXmlApplicationContext("classpath:com/diguage/"
        + "truman/context/XmlApplicationContextTest.xml");
    UserService userService = context.getBean(UserService.class);
    System.out.println(userService.getUserById(119L));
  }

  public static class UserService {
    public String getUserById(Long id) {
      return "user-" + id;
    }
  }
}
