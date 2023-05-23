package com.diguage.truman.context;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
public class RetryableTest {
  @Test
  public void test() throws BeansException, RetryException {
    AnnotationConfigApplicationContext context
        = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    UserService service = context.getBean(UserService.class);
    try {
      service.anno();
    } catch (Throwable e) {
    }

    try {
      service.code();
    } catch (Throwable e) {
    }
  }

  @Configuration
  @Import(RetryableSelector.class)
  @EnableResilientMethods
  public static class Config {
  }

  public static class RetryableSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
      return new String[]{
          UserService.class.getName()
      };
    }
  }


  @Component
  public static class UserService {

    private static volatile int counter = 0;

    @Retryable(includes = MessageDeliveryException.class,
        maxRetries = 5,
        delay = 1000,
        jitter = 10,
        multiplier = 2,
        maxDelay = 10000)
    public void anno() {
      log.info("invoke anno..., counter:" + (++counter));
      throw new RuntimeException("counter:" + counter);
    }

    public void code() throws RetryException {
      log.info("invoke code...");
      RetryPolicy retryPolicy = RetryPolicy.builder()
          .includes(MessageDeliveryException.class)
          .maxRetries(5)
          .delay(Duration.ofMillis(1000))
          .jitter(Duration.ofMillis(10))
          .multiplier(2)
          .maxDelay(Duration.ofSeconds(100))
          .build();
      RetryTemplate retryTemplate = new RetryTemplate(retryPolicy);
      retryTemplate.execute(() -> {
        log.info("invoke code to execute..., counter:" + (++counter));
        throw new MessageDeliveryException("counter:" + (counter));
      });
    }
  }

  public static class MessageDeliveryException extends RuntimeException {

    public MessageDeliveryException(String message) {
      super(message);
    }
  }
}
