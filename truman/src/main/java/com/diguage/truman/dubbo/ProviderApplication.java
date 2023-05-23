package com.diguage.truman.dubbo;

import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.locks.LockSupport;

public class ProviderApplication {
  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(ProviderConfiguration.class);
    context.refresh();
    context.start();
    LockSupport.park();
  }

  @Configuration
  @EnableDubbo(scanBasePackages = "com.diguage.truman.dubbo")
  @PropertySource("classpath:/dubbo/provider.properties")
  static class ProviderConfiguration {
    @Bean
    public RegistryConfig registryConfig() {
      RegistryConfig result = new RegistryConfig();
      result.setAddress("zookeeper://127.0.0.1:2181");
      return result;
    }
  }
}
