package com.diguage.truman.context;

import com.diguage.truman.mybatis.Employees;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2020-05-29 17:11
 */
public class BeanFactoryPostProcessorOrderTest {
  private static final Logger logger = LoggerFactory.getLogger(BeanFactoryPostProcessorOrderTest.class);

  @Test
  public void test() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(Config.class);
    context.refresh();
    EmployeesService service = context.getBean(EmployeesService.class);
    service.save(new Employees());
  }


  @org.springframework.context.annotation.Configuration
  @Import({EmployeesService.class,
      InterOrder1BeanFactoryPostProcessor.class,
      InterOrder2BeanFactoryPostProcessor.class,
      InterOrder3BeanFactoryPostProcessor.class,
      AnnoOrder1BeanFactoryPostProcessor.class,
      AnnoOrder2BeanFactoryPostProcessor.class})
  public static class Config {
  }

  @Service
  public static class EmployeesService {
    public int save(Employees employees) {
      logger.info("save employees: {}", employees);
      return 1;
    }
  }


  public static class InterOrder1BeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      logger.info("InterOrdered1FactoryPostProcessor.postProcessBeanFactory run");
    }

    @Override
    public int getOrder() {
      return 1;
    }
  }

  public static class InterOrder2BeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      logger.info("InterOrdered2FactoryPostProcessor.postProcessBeanFactory run");
    }

    @Override
    public int getOrder() {
      return 2;
    }
  }

  public static class InterOrder3BeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      logger.info("InterOrdered3FactoryPostProcessor.postProcessBeanFactory run");
    }

    @Override
    public int getOrder() {
      return 3;
    }
  }

  @Order(1)
  public static class AnnoOrder1BeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      logger.info("AnnoOrder1BeanFactoryPostProcessor.postProcessBeanFactory run");
    }
  }

  @Order(2)
  public static class AnnoOrder2BeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      logger.info("AnnoOrder2BeanFactoryPostProcessor.postProcessBeanFactory run");
    }
  }
}
