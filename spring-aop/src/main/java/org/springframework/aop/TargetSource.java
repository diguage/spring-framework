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

package org.springframework.aop;

import org.jspecify.annotations.Nullable;

/**
 * `TargetSource` 最主要的特性是，每次方法调用都会触发 `TargetSource` 的
 * `getTarget()` 方法， `getTarget()` 方法将从相应的 `TargetSource`
 * 实现类中取得具体的目标对象，这样就可以控制每次方法调用作用到的具体实例对象。
 *
 * <p>主要实现有：
 *
 * <ul>
 *     <li>SingletonTargetSource -- 内部只持有一个目标对象，每次调用时，都会返回这同一个目标对象。</li>
 *     <li>PrototypeTargetSource -- 每次调用目标对象上的方法时，都会返回一个新的目标对象实例供调用。注意： `scope` 要设置成 `prototype`；通过 `targetBeanName` 属性指定目标对象的 bean 定义名称，而不是引用。</li>
 *     <li>HotSwappableTargetSource -- 使用 `HotSwappableTargetSource` 封装目标对象，可以在应用程序运行时，根据某种特定条件，动态地替换目标对象类的具体实现。</li>
 *     <li>CommonsPool2TargetSource -- 使用 Apache Commons Pool 2 来提供对象池的支持。</li>
 *     <li>ThreadLocalTargetSource -- 为不同线程调用提供不同的目标对象。保证各自线程上对目标对象的调用，可以被分配到当前线程对应的那个目标对象实例上。注意： scope 要设置成 prototype。</li>
 * </ul>
 *
 * A {@code TargetSource} is used to obtain the current "target" of
 * an AOP invocation, which will be invoked via reflection if no around
 * advice chooses to end the interceptor chain itself.
 *
 * <p>If a {@code TargetSource} is "static", it will always return
 * the same target, allowing optimizations in the AOP framework. Dynamic
 * target sources can support pooling, hot swapping, etc.
 *
 * <p>Application developers don't usually need to work with
 * {@code TargetSources} directly: this is an AOP framework interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * Return the type of targets returned by this {@link TargetSource}.
	 * <p>Can return {@code null}, although certain usages of a {@code TargetSource}
	 * might just work with a predetermined target class.
	 * @return the type of targets returned by this {@link TargetSource}
	 */
	@Override
	@Nullable Class<?> getTargetClass();

	/**
	 * Will all calls to {@link #getTarget()} return the same object?
	 * <p>In that case, there will be no need to invoke {@link #releaseTarget(Object)},
	 * and the AOP framework can cache the return value of {@link #getTarget()}.
	 * <p>The default implementation returns {@code false}.
	 * @return {@code true} if the target is immutable
	 * @see #getTarget
	 */
	default boolean isStatic() {
		return false;
	}

	/**
	 * Return a target instance. Invoked immediately before the
	 * AOP framework calls the "target" of an AOP method invocation.
	 * @return the target object which contains the joinpoint,
	 * or {@code null} if there is no actual target instance
	 * @throws Exception if the target object can't be resolved
	 */
	@Nullable Object getTarget() throws Exception;

	/**
	 * Release the given target object obtained from the
	 * {@link #getTarget()} method, if any.
	 * <p>The default implementation is empty.
	 * @param target object obtained from a call to {@link #getTarget()}
	 * @throws Exception if the object can't be released
	 */
	default void releaseTarget(Object target) throws Exception {
	}

}
