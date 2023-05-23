package com.diguage.truman.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.diguage.truman.util.Constans.BASE_CLASS_PATH;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(BASE_CLASS_PATH + "/test/XmlApplicationContextTest.xml")
public class ContextConfigurationTest {

  @Autowired
  private UserService userService;

  @Test
  public void test() {
    User user = userService.getById(1L);
    System.out.println(user.toString());
  }

  @Data
  @AllArgsConstructor
  public static class User {
    private long id;
    private String name;
  }

  public static class UserService {
    public User getById(long id) {
      return new User(id, "diguage-" + id);
    }
  }
}
