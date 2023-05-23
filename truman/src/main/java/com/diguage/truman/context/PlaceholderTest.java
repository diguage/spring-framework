package com.diguage.truman.context;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2023-05-02 10:23:49
 */
public class PlaceholderTest {
  @Test
  public void test() {
    ClassPathXmlApplicationContext context
        = new ClassPathXmlApplicationContext("classpath:com/" +
        "diguage/truman/context/PlaceholderTest.xml");
    UserRpc userRpc = context.getBean(UserRpc.class);
    System.out.println(userRpc.appId);
    System.out.println(userRpc.token);
  }

  @Data
  public static class UserRpc {

    @Value("${user.appId}")
    private String appId;

    // 这里不使用注解，而是使用 XML 配置
    // @Value("${user.token}")
    private String token;

  }
}
