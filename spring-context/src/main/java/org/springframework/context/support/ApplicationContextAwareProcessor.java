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

package org.springframework.context.support;

import com.diguage.labs.Printers;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.util.StringValueResolver;

/**
 * 这个类说起来相当复杂
 * 要从他的父类BeanPostProcessor说起，可以先查看他的父类，
 * 看完父类之后再来下面的注释
 *
 * {@link BeanPostProcessor} implementation that supplies the
 * {@link org.springframework.context.ApplicationContext ApplicationContext},
 * {@link org.springframework.core.env.Environment Environment},
 * {@link StringValueResolver}, or
 * {@link org.springframework.core.metrics.ApplicationStartup ApplicationStartup}
 * for the {@code ApplicationContext} to beans that implement the {@link EnvironmentAware},
 * {@link EmbeddedValueResolverAware}, {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware}, {@link MessageSourceAware},
 * {@link ApplicationStartupAware}, and/or {@link ApplicationContextAware} interfaces.
 *
 * <p>Implemented interfaces are satisfied in the order in which they are
 * mentioned above.
 *
 * <p>Application contexts will automatically register this with their
 * underlying bean factory. Applications do not use this directly.
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @since 10.10.2003
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.EmbeddedValueResolverAware
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.context.ApplicationEventPublisherAware
 * @see org.springframework.context.MessageSourceAware
 * @see org.springframework.context.ApplicationStartupAware
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
 */
class ApplicationContextAwareProcessor implements BeanPostProcessor {

	private final ConfigurableApplicationContext applicationContext;

	private final StringValueResolver embeddedValueResolver;


	/**
	 * Create a new ApplicationContextAwareProcessor for the given context.
	 */
	public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.embeddedValueResolver = new EmbeddedValueResolver(applicationContext.getBeanFactory());
	}


	/**
	 * 接口 BeanPostProcessor 规定的方法，会在 Bean 创建时实例化后，初始化前，对 Bean 对象应用该 Before 方法
	 */
	@Override
	public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Printers.printf(".. %s#%s(%s, %s)%n%n",
				getClass().getSimpleName(),
				"postProcessBeforeInitialization",
				bean.getClass().getSimpleName(),
				beanName);

		if (bean instanceof Aware) {
			// 检测 Bean 是否实现了某个 Aware接口，有的话进行相关的调用
			invokeAwareInterfaces(bean);
		}
		return bean;
	}

	/**
	 * 对实现 Aware 接口的实现进行方法的调用
	 */
	private void invokeAwareInterfaces(Object bean) {
		// 判断 bean 类型是否为 EnvironmentAware
		if (bean instanceof EnvironmentAware environmentAware) {
			// 回调 EnvironmentAware 的 setEnvironment 方法
			environmentAware.setEnvironment(this.applicationContext.getEnvironment());
		}
		// 判断 bean 类型是否为 EmbeddedValueResolverAware
		if (bean instanceof EmbeddedValueResolverAware embeddedValueResolverAware) {
			// 回调 EmbeddedValueResolverAware 的 setEmbeddedValueResolver 方法
			embeddedValueResolverAware.setEmbeddedValueResolver(this.embeddedValueResolver);
		}
		// 判断 bean 类型是否为 ResourceLoaderAware
		if (bean instanceof ResourceLoaderAware resourceLoaderAware) {
			// 回调 ResourceLoaderAware 的 setResourceLoader 方法
			resourceLoaderAware.setResourceLoader(this.applicationContext);
		}
		// 判断 bean 类型是否为 ApplicationEventPublisherAware 方法
		if (bean instanceof ApplicationEventPublisherAware applicationEventPublisherAware) {
			// 回调 ApplicationEventPublisherAware 的setApplicationEventPublisher
			applicationEventPublisherAware.setApplicationEventPublisher(this.applicationContext);
		}
		// 判断 bean 类型是否为 MessageSourceAware
		if (bean instanceof MessageSourceAware messageSourceAware) {
			// 回调 MessageSourceAware 的 setMessageSource 方法
			messageSourceAware.setMessageSource(this.applicationContext);
		}
		// 判断 bean 类型是否 ApplicationContextAware
		if (bean instanceof ApplicationStartupAware applicationStartupAware) {
			// 回调 ApplicationContextAware 的 setApplicationContext 方法
			applicationStartupAware.setApplicationStartup(this.applicationContext.getApplicationStartup());
		}
		//spring帮你set一个applicationContext对象
		//所以当我们自己的一个对象实现了ApplicationContextAware对象只需要提供setter就能得到applicationContext对象
		//此处应该有鲜花。。。。
		if (bean instanceof ApplicationContextAware applicationContextAware) {
			applicationContextAware.setApplicationContext(this.applicationContext);
		}
	}

}
