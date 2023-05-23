package com.diguage.truman.context;

import com.diguage.labs.Printers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.annotation.Scope;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 19:49
 */
public class LifecycleTest {

	static int sequence = 0;

	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.addBeanFactoryPostProcessor(new LogBeanDefinitionRegistryPostProcessor());
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.addBeanPostProcessor(new LogBeanPostProcessor());
//		beanFactory.addBeanPostProcessor(new LogInstantiationAwareBeanPostProcessor());
		beanFactory.addBeanPostProcessor(new LogDestructionAwareBeanPostProcessor());

		context.register(Config.class);
		context.refresh();

		ProtoService protoService = context.getBean(ProtoService.class);
		System.out.println(protoService);
		beanFactory.destroyBean(protoService);

		UserService userService = context.getBean(UserService.class);
		System.out.println(userService.getById(119L));

		BeanDefinition definition = context.getBeanDefinition(UserService.class.getName());
		System.out.println(definition.getClass().getName());
		System.out.println(definition);

//		context.close();
//		context.start();
	}

	@Configuration
	@Import(LogImportSelector.class)
	@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
	public static class Config {
	}

	public static class LogImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
//					BeanPostProcessorAspect.class.getName(),
					UserService.class.getName(),
					UserDao.class.getName(),
					ProtoService.class.getName()
			};
		}
	}

	public static class LogImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
											BeanDefinitionRegistry registry) {
			RootBeanDefinition definition = new RootBeanDefinition(UserService.class);
			registry.registerBeanDefinition(UserService.class.getName(), definition);
		}
	}

	@Repository
	public static class UserDao {
		String getById(Long id) {
			return "User-" + id;
		}
	}

	@Service
	public static class UserService implements InitializingBean, BeanFactoryAware, ApplicationContextAware {
		@Resource
		UserDao userDao;

		public UserService() {
			System.out.println(".. 构造函数\n");
		}

		@Override
		public void afterPropertiesSet() throws Exception {
      Printers.printf(".. %s#%s()%n%n",
					getClass().getSimpleName(),
					"afterPropertiesSet");
		}

		public void init() {
      Printers.printf(".. %s#%s()%n%n",
					getClass().getSimpleName(),
					"init");
		}

		String getById(Long id) {
			return userDao.getById(id);
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      Printers.printf(".. %s#%s(%s)%n%n",
					getClass().getSimpleName(),
					"setBeanFactory",
					beanFactory.getClass().getSimpleName());
		}

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			Printers.printf(".. %s#%s(%s)%n%n",
					getClass().getSimpleName(),
					"setApplicationContext",
					applicationContext.getClass().getSimpleName());
		}
	}

	@Component
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public static class ProtoService implements DisposableBean {

		@Override
		public void destroy() throws Exception {
			Printers.printf(".. %s#%s()%n%n",
					getClass().getSimpleName(),
					"destroy");
		}
	}


	public static class LogBeanPostProcessor implements BeanPostProcessor {
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessBeforeInitialization",
					bean.getClass().getSimpleName(),
					beanName);
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessAfterInitialization",
					bean.getClass().getSimpleName(),
					beanName);
			return bean;
		}
	}

	public static class LogBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
			Printers.printf(". %s#%s(%s)%n%n",
					getClass().getSimpleName(),
					"postProcessBeanDefinitionRegistry",
					registry.getClass().getSimpleName());

			RootBeanDefinition beanDefinition = new RootBeanDefinition(LogBeanFactoryPostProcessor.class);
			registry.registerBeanDefinition(beanDefinition.getBeanClassName(), beanDefinition);
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			Printers.printf(". %s#%s(%s)%n%n",
					getClass().getSimpleName(),
					"postProcessBeanFactory",
					beanFactory.getClass().getSimpleName());
		}
	}

	public static class LogBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			Printers.printf(". %s#%s(%s)%n%n",
					getClass().getSimpleName(),
					"postProcessBeanFactory",
					beanFactory.getClass().getSimpleName());

			BeanDefinition definition = beanFactory.getBeanDefinition(UserService.class.getName());
			// 设置 init 方法
			definition.setInitMethodName("init");
		}
	}

	public static class LogInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
		@Override
		public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessBeforeInstantiation",
					beanClass.getSimpleName(),
					beanName);

			return null;
		}

		@Override
		public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessAfterInstantiation",
					bean.getClass().getSimpleName(),
					beanName);
			return true;
		}

		@Override
		public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessProperties",
					pvs.getClass().getSimpleName(),
					bean.getClass().getSimpleName(),
					beanName);

			return pvs;
		}

//		@Override
//		public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
//			System.out.println(bean.getClass().getName());
//			return pvs;
//		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessBeforeInitialization",
					bean.getClass().getSimpleName(),
					beanName);

			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessAfterInitialization",
					bean.getClass().getSimpleName(),
					beanName);

			return bean;
		}
	}

	public static class LogDestructionAwareBeanPostProcessor implements DestructionAwareBeanPostProcessor {
		@Override
		public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
			Printers.printf(".. %s#%s(%s, %s)%n%n",
					getClass().getSimpleName(),
					"postProcessBeforeDestruction",
					bean.getClass().getSimpleName(),
					beanName);
		}
	}


	public static String getAndIncrement() {
		return String.format("%n - %2d - ", ++sequence);
	}
}
