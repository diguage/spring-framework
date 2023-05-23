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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;

/**
 * A simple but definitive way of working out an advice chain for a Method,
 * given an {@link Advised} object. Always rebuilds each advice chain;
 * caching can be provided by subclasses.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0.3
 */
@SuppressWarnings("serial")
public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

	/**
	 * Singleton instance of this class.
	 * @since 6.0.10
	 */
	public static final DefaultAdvisorChainFactory INSTANCE = new DefaultAdvisorChainFactory();


  // tag::getInterceptorsAndDynamicInterceptionAdvice[]
	/**
	 * 从提供的配置实例 config 中获取 advisor 列表，遍历处理这些 adviso.
	 * 如果是 IntroductionAdvisor，则判断此 Advisor 能否应用到目标 targetClass 上。
	 * 如果是 PointcutAdvisor，则判断此 Advisor 能否应用到目标方法 Method 上，
	 * 将满足条件的 Advisor 通过 AdvisorAdapter 转化成 Interceptor 列表返回。
	 */
	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(
			Advised config, Method method, @Nullable Class<?> targetClass) {

		// This is somewhat tricky... We have to process introductions first,
		// but we need to preserve order in the ultimate list.
		// advice适配器注册中心
		// MethodBeforeAdviceAdapter:将Advisor适配成MethodBeforeAdvice
		// AfterReturningAdviceAdapter:将Advisor适配成AfterReturningAdvice
		// ThrowsAdviceAdapter: 将Advisor适配成ThrowsAdvice
		// 这里实际上注册一系列 AdvisorAdapter，用于将 Advisor 转化成 MethodInterceptor
		AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
		Advisor[] advisors = config.getAdvisors();
		// 返回值集合，里面装的都是Interceptor或者它的子类接口MethodInterceptor
		List<Object> interceptorList = new ArrayList<>(advisors.length);
		// 获取目标类的类型
		Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
		// 是否有引介
		Boolean hasIntroductions = null;

		// 去产生代理对象的过程中，针对该目标方法获取到的所有合适的Advisor集合
		for (Advisor advisor : advisors) {
			if (advisor instanceof PointcutAdvisor pointcutAdvisor) {
				// Add it conditionally.
				// 如果该Advisor可以对目标类进行增强，则进行后续操作
				if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
					// 获取方法适配器，该方法匹配器可以根据指定的切入点表达式进行方法匹配
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					boolean match;
					if (mm instanceof IntroductionAwareMethodMatcher iamm) {
						if (hasIntroductions == null) {
							// 判断是否包含 IntroductionAdvisor
							hasIntroductions = hasMatchingIntroductions(advisors, actualClass);
						}
						match = iamm.matches(method, actualClass, hasIntroductions);
					}
					else {
						match = mm.matches(method, actualClass);
					}
					if (match) {
						// 将advisor转成MethodInterceptor
						// 从 GlobalAdvisorAdapterRegistry 获得 MethodInterceptor
						MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
						// MethodMatcher接口通过重载定义了两个matches()方法
						// 两个参数的matches() 被称为静态匹配，在匹配条件不是太严格时使用，可以满足大部分场景的使用
						// 称之为静态的主要是区分为三个参数的matches()方法需要在运行时动态的对参数的类型进行匹配
						// 两个方法的分界线就是boolean isRuntime()方法
						// 进行匹配时先用两个参数的matches()方法进行匹配，若匹配成功，则检查boolean isRuntime()的返回值若为
						// true, 则调用三个参数的matches()方法进行匹配（若两个参数的都匹配不中，三个参数的必定匹配不中）

						// 需要根据参数动态匹配（比如重载）
						if (mm.isRuntime()) {
							// Creating a new object instance in the getInterceptors() method
							// isn't a problem as we normally cache created chains.
							for (MethodInterceptor interceptor : interceptors) {
								interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
							}
						}
						else {
							interceptorList.addAll(Arrays.asList(interceptors));
						}
					}
				}
			}
			else if (advisor instanceof IntroductionAdvisor ia) {
				if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
					Interceptor[] interceptors = registry.getInterceptors(advisor);
					interceptorList.addAll(Arrays.asList(interceptors));
				}
			}
			else {
				Interceptor[] interceptors = registry.getInterceptors(advisor);
				interceptorList.addAll(Arrays.asList(interceptors));
			}
		}

		return interceptorList;
	}
  // end::getInterceptorsAndDynamicInterceptionAdvice[]

	/**
	 * Determine whether the Advisors contain matching introductions.
	 */
	private static boolean hasMatchingIntroductions(Advisor[] advisors, Class<?> actualClass) {
		for (Advisor advisor : advisors) {
			if (advisor instanceof IntroductionAdvisor ia && ia.getClassFilter().matches(actualClass)) {
				return true;
			}
		}
		return false;
	}

}
