package com.diguage.truman.context;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import static com.diguage.truman.util.Constans.BASE_CLASS_PATH;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2023-05-02 10:23:49
 */
public class PlaceholderAnnoTest {
  @Test
  public void test() {
    AnnotationConfigApplicationContext context
        = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    UserRpc userRpc = context.getBean(UserRpc.class);
    System.out.println(userRpc.appId);
    System.out.println(userRpc.token);
  }

  @Configuration
  @Import(UserRpc.class)
  @PropertySource(BASE_CLASS_PATH + "/context/token.properties")
  public static class Config {
  }

  @Data
  public static class UserRpc {

    @Value("${user.appId:defaultAppId}")
    private String appId;

    @Value("${user.token:defaultAppToken}")
    private String token;

  }
}
