/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.util.Arrays;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.Assert;

/**
 * Spring中出来注解Bean定义的类有两个：
 * AnnotationConfigApplicationContext 和 AnnotationConfigWebApplicationContex。
 * AnotationConfigWebApplicationContext 是 AnnotationConfigApplicationContext 的web版本
 * 两者的用法以及对注解的处理方式几乎没有什么差别
 * 通过分析这个类我们知道注册一个bean到spring容器有两种办法
 * 一、直接将注解Bean注册到容器中：（参考）public void register(Class<?>... annotatedClasses)
 * 但是直接把一个注解的bean注册到容器当中也分为两种方法
 * 1、在初始化容器时注册并且解析
 * 2、也可以在容器创建之后手动调用注册方法向容器注册，然后通过手动刷新容器，使得容器对注册的注解Bean进行处理。
 * 思考：为什么@profile要使用这类的第2种方法
 *
 * 二、通过扫描指定的包及其子包下的所有类
 * 扫描其实同上，也是两种方法，初始化的时候扫描，和初始化之后再扫描
 *
 *
 * Standalone application context, accepting <em>component classes</em> as input &mdash;
 * in particular {@link Configuration @Configuration}-annotated classes, but also plain
 * {@link org.springframework.stereotype.Component @Component} types and JSR-330 compliant
 * classes using {@code jakarta.inject} annotations.
 *
 * <p>Allows for registering classes one by one using {@link #register(Class...)}
 * as well as for classpath scanning using {@link #scan(String...)}.
 *
 * <p>In case of multiple {@code @Configuration} classes, {@link Bean @Bean} methods
 * defined in later classes will override those defined in earlier classes. This can
 * be leveraged to deliberately override certain bean definitions via an extra
 * {@code @Configuration} class.
 *
 * <p>See {@link Configuration @Configuration}'s javadoc for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see #register
 * @see #scan
 * @see AnnotatedBeanDefinitionReader
 * @see ClassPathBeanDefinitionScanner
 * @see org.springframework.context.support.GenericXmlApplicationContext
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	/**
	 * 这个类顾名思义是一个reader，一个读取器。
	 * 读取什么呢？还是顾名思义 AnnotatedBeanDefinition 意思是读取一个被加了注解的 bean。
	 * 这个变量在构造方法中实例化的
	 */
	private final AnnotatedBeanDefinitionReader reader;

	/**
	 * 同意顾名思义，这是一个扫描器，扫描所有加了注解的 bean。
	 * 同样是在构造方法中被实例化的
	 */
	private final ClassPathBeanDefinitionScanner scanner;


	/**
	 * 初始化一个bean的读取和扫描器
	 * 何谓读取器和扫描器参考上面的属性注释
	 * 默认构造函数，如果直接调用这个默认构造方法，需要在稍后通过调用其register()
	 * 去注册配置类（javaconfig），并调用refresh()方法刷新容器，
	 * 触发容器对注解Bean的载入、解析和注册过程
	 * 这种使用过程我在ioc应用的第二节课讲@profile的时候讲过
	 *
	 * Create a new AnnotationConfigApplicationContext that needs to be populated
	 * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
	 */
	public AnnotationConfigApplicationContext() {
		/**
		 * 父类的构造方法
		 * 创建一个读取注解的Bean定义读取器。
		 * 什么是bean定义？BeanDefinition
		 *
		 * 在该构造函数中，通过调用
		 * AnnotationConfigUtils#registerAnnotationConfigProcessors(BeanDefinitionRegistry, Object)
		 * 方法，来注册内置的 PostProcessor 等：
		 *
		 * 1. ConfigurationClassPostProcessor
		 * 2. AutowiredAnnotationBeanPostProcessor
		 * 3. CommonAnnotationBeanPostProcessor
		 * 4. PersistenceAnnotationBeanPostProcessor？ -- 这个得看是否需要
		 * 5. EventListenerMethodProcessor
		 * 6. DefaultEventListenerFactory
		 */
		this.reader = new AnnotatedBeanDefinitionReader(this);

		// 可以用来扫描包或者类，继而转换成 BeanDefinition。
		// 但是实际上我们扫描包工作不是 scanner 这个对象来完成的。
		// 是 Spring 自己 new 的一个 ClassPathBeanDefinitionScanner。
		// 这里的 scanner 仅仅是为了程序员能够在外部调用 AnnotationConfigApplicationContext 对象的 scan 方法。
		// 初始化 classPath 类型的bean定义扫描器
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * Create a new AnnotationConfigApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 这个构造方法需要传入一个被javaconfig注解了的配置类
	 * 然后会把这个被注解了javaconfig的类通过注解读取器读取后继而解析
	 *
	 * Create a new AnnotationConfigApplicationContext, deriving bean definitions
	 * from the given component classes and automatically refreshing the context.
	 * @param componentClasses one or more component classes &mdash; for example,
	 * {@link Configuration @Configuration} classes
	 */
	public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
		//annotatedClasses  appconfig.class
		// 这里由于他有父类，故而会先调用父类的构造方法，然后才会调用自己的构造方法
		// 在自己构造方法中初始一个读取器和扫描器
		this();
		// 注册配置类
		register(componentClasses);
		// IoC 容器刷新接口
		refresh();
	}

	/**
	 * 该构造函数会自动扫描给定的包及其子包下的所有类，并自动识别所有的 Spring Bean，将其注册到容器中。<p/>
	 *
	 * Create a new AnnotationConfigApplicationContext, scanning for components
	 * in the given packages, registering bean definitions for those components,
	 * and automatically refreshing the context.
	 * @param basePackages the packages to scan for component classes
	 */
	public AnnotationConfigApplicationContext(String... basePackages) {
		this();
		// 扫描配置类
		scan(basePackages);
		// IoC 容器刷新接口
		refresh();
	}


	/**
	 * Propagate the given custom {@code Environment} to the underlying
	 * {@link AnnotatedBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * 为容器的注解 Bean 读取器和注解 Bean 扫描器设置 Bean 名称生成器。
	 *
	 * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 * <p>Default is {@link AnnotationBeanNameGenerator}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 * @see AnnotationBeanNameGenerator
	 * @see FullyQualifiedAnnotationBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}

	/**
	 * 为容器的注解 Bean 读取器和注解 Bean 扫描器设置作用范围元信息解析器。<p/>
	 *
	 * Set the {@link ScopeMetadataResolver} to use for registered component classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}


	//---------------------------------------------------------------------
	// Implementation of AnnotationConfigRegistry
	//---------------------------------------------------------------------

	/**
	 * 注册单个bean给容器
	 * 比如有新加的类可以用这个方法
	 * 但是注册之后需要手动调用refresh方法去触发容器解析注解
	 *
	 * 有两个意思
	 * 他可以注册一个配置类
	 * 他还可以单独注册一个bean
	 *
	 * Register one or more component classes to be processed.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param componentClasses one or more component classes &mdash; for example,
	 * {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	@Override
	public void register(Class<?>... componentClasses) {
		Assert.notEmpty(componentClasses, "At least one component class must be specified");
		StartupStep registerComponentClass = getApplicationStartup().start("spring.context.component-classes.register")
				.tag("classes", () -> Arrays.toString(componentClasses));
		this.reader.register(componentClasses);
		registerComponentClass.end();
	}

	/**
	 * 扫描指定包路径及其子包下的注解类，为了使新添加的类被处理，必须手动调用 refresh() 方法刷新容器。
	 * 
	 * Perform a scan within the specified base packages.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param basePackages the packages to scan for component classes
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	@Override
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		StartupStep scanPackages = getApplicationStartup().start("spring.context.base-packages.scan")
				.tag("packages", () -> Arrays.toString(basePackages));
		this.scanner.scan(basePackages);
		scanPackages.end();
	}


	//---------------------------------------------------------------------
	// Adapt superclass registerBean calls to AnnotatedBeanDefinitionReader
	//---------------------------------------------------------------------

	// 注册一个注解 Bean 定义类
	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
			@Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		this.reader.registerBean(beanClass, beanName, supplier, customizers);
	}

}
