package com.diguage.truman.beans;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimeCostBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {
  private ConcurrentMap<String, Long> beanToStartTimeMap = new ConcurrentHashMap<>();
  private ConcurrentMap<String, Long> beanToTimeMap = new ConcurrentHashMap<>();

  private ConfigurableBeanFactory beanFactory;

  @Override
  public @Nullable Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
    if (!beanToStartTimeMap.containsKey(beanName) && !beanToTimeMap.containsKey(beanName)) {
      beanToStartTimeMap.put(beanName, System.nanoTime());
    }
    return null;
  }

  @Override
  public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (beanToStartTimeMap.containsKey(beanName)) {
      Long startTime = beanToStartTimeMap.remove(beanName);
      long time = System.nanoTime() - startTime;
      beanToTimeMap.put(beanName, time);
      // TODO 通过依赖关系，分析出每个 Bean 初始化的真实耗时
      // TODO 要确定存在实例化依赖关系才减去耗时，不要错误减去已完成实例化的依赖耗时。
      String[] dependencies = beanFactory.getDependenciesForBean(beanName);
      System.out.printf("TimeCost: " + " ".repeat(2 * beanToStartTimeMap.size()) + "%2d bean: %s, %10dns = %7.2fms \n",
          (beanToStartTimeMap.size() + 1), beanName, time, time / 1000.0D);
    }
    return bean;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = (ConfigurableBeanFactory) beanFactory;
  }
}
