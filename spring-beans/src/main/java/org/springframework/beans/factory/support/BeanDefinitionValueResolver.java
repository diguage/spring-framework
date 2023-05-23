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

package org.springframework.beans.factory.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Helper class for use in bean factory implementations,
 * resolving values contained in bean definition objects
 * into the actual values applied to the target bean instance.
 *
 * <p>Operates on an {@link AbstractBeanFactory} and a plain
 * {@link org.springframework.beans.factory.config.BeanDefinition} object.
 * Used by {@link AbstractAutowireCapableBeanFactory}.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 1.2
 * @see AbstractAutowireCapableBeanFactory
 */
public class BeanDefinitionValueResolver {

	private final AbstractAutowireCapableBeanFactory beanFactory;

	private final String beanName;

	private final BeanDefinition beanDefinition;

	private final TypeConverter typeConverter;


	/**
	 * Create a BeanDefinitionValueResolver for the given BeanFactory and BeanDefinition,
	 * using the given {@link TypeConverter}.
	 * @param beanFactory the BeanFactory to resolve against
	 * @param beanName the name of the bean that we work on
	 * @param beanDefinition the BeanDefinition of the bean that we work on
	 * @param typeConverter the TypeConverter to use for resolving TypedStringValues
	 */
	public BeanDefinitionValueResolver(AbstractAutowireCapableBeanFactory beanFactory, String beanName,
			BeanDefinition beanDefinition, TypeConverter typeConverter) {

		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.typeConverter = typeConverter;
	}

	/**
	 * Create a BeanDefinitionValueResolver for the given BeanFactory and BeanDefinition
	 * using a default {@link TypeConverter}.
	 * @param beanFactory the BeanFactory to resolve against
	 * @param beanName the name of the bean that we work on
	 * @param beanDefinition the BeanDefinition of the bean that we work on
	 */
	public BeanDefinitionValueResolver(AbstractAutowireCapableBeanFactory beanFactory, String beanName,
			BeanDefinition beanDefinition) {

		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		BeanWrapper beanWrapper = new BeanWrapperImpl();
		beanFactory.initBeanWrapper(beanWrapper);
		this.typeConverter = beanWrapper;
	}


	/**
	 * 这个方法解析 PropertyValue 中 value 对应的值。解析属性值，对注入类型进行转换
	 *
	 * Given a PropertyValue, return a value, resolving any references to other
	 * beans in the factory if necessary. The value could be:
	 * <li>A BeanDefinition, which leads to the creation of a corresponding
	 * new bean instance. Singleton flags and names of such "inner beans"
	 * are always ignored: Inner beans are anonymous prototypes.
	 * <li>A RuntimeBeanReference, which must be resolved.
	 * <li>A ManagedList. This is a special collection that may contain
	 * RuntimeBeanReferences or Collections that will need to be resolved.
	 * <li>A ManagedSet. May also contain RuntimeBeanReferences or
	 * Collections that will need to be resolved.
	 * <li>A ManagedMap. In this case the value may be a RuntimeBeanReference
	 * or Collection that will need to be resolved.
	 * <li>An ordinary object or {@code null}, in which case it's left alone.
	 * @param argName the name of the argument that the value is defined for
	 * @param value the value object to resolve
	 * @return the resolved object
	 */
	public @Nullable Object resolveValueIfNecessary(Object argName, @Nullable Object value) {
		// We must check each value to see whether it requires a runtime reference
		// to another bean to be resolved.
		// 必须检查每个值，以查看它是否需要对另一个 Bean 的运行时引用才能解决
		// RuntimeBeanReference：当属性值对象是工厂中另一个 Bean 的引用时，使用不可变的占位符类，在运行时进行解析
		// 如果 value 是 RuntimeBeanReference 的实例
		// 对引用类型的属性进行解析
		if (value instanceof RuntimeBeanReference ref) {
			// 调用引用类型属性的解析方法
			// 解析出对应 ref 所封装 Bean 元信息（即 Bean 名称、Bean 类型）的 Bean 对象
			return resolveReference(argName, ref);
		}
		// 对属性值是引用容器中另一个 Bean 名称的解析
		// RuntimeBeanNameReference：对应于标签 <idref bean='a' />
		// idref 注入的是目标 Bean 的 id，而不是目标 Bean 的实例，同时使用 idref 容器在部署的时候还会验证这个 Bean
		// 是否真实存在，其实 idref 就跟 value 一样，只是将某个字符串注入到属性或者构造函数中，只不过注入的是某个 Bean 定义的 id 属性值
		// 即：<idref bean='a' /> 等同于 <value>a</value>
		// 如果value是RuntimeBeanNameReference实例
		else if (value instanceof RuntimeBeanNameReference ref) {
			// 从 value 中获取引用的 BeanName
			String refName = ref.getBeanName();
			refName = String.valueOf(doEvaluate(refName));
			// 从容器中获取指定名称的 Bean
			// 如果 BeanFactory 不包含具有 refName 的 BeanDefinition 或外部注册的 singleton 实例
			if (!this.beanFactory.containsBean(refName)) {
				throw new BeanDefinitionStoreException(
						"Invalid bean name '" + refName + "' in bean reference for " + argName);
			}
			return refName;
		}
		// 对 Bean 类型属性的解析，主要是 Bean 中的内部类 FIXME 此话何意？
		else if (value instanceof BeanDefinitionHolder bdHolder) {
			// Resolve BeanDefinitionHolder: contains BeanDefinition with name and aliases.
			return resolveInnerBean(bdHolder.getBeanName(), bdHolder.getBeanDefinition(),
					(name, mbd) -> resolveInnerBeanValue(argName, name, mbd));
		}
		else if (value instanceof BeanDefinition bd) {
			return resolveInnerBean(null, bd,
					(name, mbd) -> resolveInnerBeanValue(argName, name, mbd));
		}
		else if (value instanceof DependencyDescriptor dependencyDescriptor) {
			Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
			Object result = this.beanFactory.resolveDependency(
					dependencyDescriptor, this.beanName, autowiredBeanNames, this.typeConverter);
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, this.beanName);
				}
			}
			return result;
		}
		// 对集合数组类型的属性解析
		else if (value instanceof ManagedArray managedArray) {
			// May need to resolve contained runtime references.
			// 获取数组的类型
			Class<?> elementType = managedArray.resolvedElementType;
			if (elementType == null) {
				// 获取数组元素的类型
				String elementTypeName = managedArray.getElementTypeName();
				if (StringUtils.hasText(elementTypeName)) {
					try {
						// 使用反射机制创建指定类型的对象
						elementType = ClassUtils.forName(elementTypeName, this.beanFactory.getBeanClassLoader());
						managedArray.resolvedElementType = elementType;
					}
					catch (Throwable ex) {
						// Improve the message by showing the context.
						throw new BeanCreationException(
								this.beanDefinition.getResourceDescription(), this.beanName,
								"Error resolving array type for " + argName, ex);
					}
				}
				// 没有获取到数组的类型，也没有获取到数组元素的类型，
				// 则直接设置数组的类型为 Object
				else {
					elementType = Object.class;
				}
			}
			// 创建指定类型的数组
			return resolveManagedArray(argName, (List<?>) value, elementType);
		}
		// 解析 List 类型的属性值
		else if (value instanceof ManagedList<?> managedList) {
			// May need to resolve contained runtime references.
			return resolveManagedList(argName, managedList);
		}
		// 解析 Set 类型的属性值
		else if (value instanceof ManagedSet<?> managedSet) {
			// May need to resolve contained runtime references.
			return resolveManagedSet(argName, managedSet);
		}
		// 解析 Map 类型的属性值
		else if (value instanceof ManagedMap<?, ?> managedMap) {
			// May need to resolve contained runtime references.
			return resolveManagedMap(argName, managedMap);
		}
		// 解析 props 类型的属性值，props 其实就是 key 和 value 均为字符串的 Map
		else if (value instanceof ManagedProperties original) {
			// Properties original = managedProperties;
			// 创建一个拷贝，用于作为解析后的返回值
			Properties copy = new Properties();
			original.forEach((propKey, propValue) -> {
				if (propKey instanceof TypedStringValue typedStringValue) {
					propKey = evaluate(typedStringValue);
				}
				if (propValue instanceof TypedStringValue typedStringValue) {
					propValue = evaluate(typedStringValue);
				}
				if (propKey == null || propValue == null) {
					throw new BeanCreationException(
							this.beanDefinition.getResourceDescription(), this.beanName,
							"Error converting Properties key/value pair for " + argName + ": resolved to null");
				}
				copy.put(propKey, propValue);
			});
			return copy;
		}
		// 解析字符串类型的属性值
		else if (value instanceof TypedStringValue typedStringValue) {
			// Convert value to target type here.
			Object valueObject = evaluate(typedStringValue);
			try {
				// 获取属性的目标类型
				Class<?> resolvedTargetType = resolveTargetType(typedStringValue);
				if (resolvedTargetType != null) {
					// 对目标类型的属性进行解析，递归调用
					return this.typeConverter.convertIfNecessary(valueObject, resolvedTargetType);
				}
				// 没有获取到属性的目标对象，则按 Object 类型返回
				else {
					return valueObject;
				}
			}
			catch (Throwable ex) {
				// Improve the message by showing the context.
				throw new BeanCreationException(
						this.beanDefinition.getResourceDescription(), this.beanName,
						"Error converting typed String value for " + argName, ex);
			}
		}
		else if (value instanceof NullBean) {
			return null;
		}
		else {
			return evaluate(value);
		}
	}

	/**
	 * Resolve an inner bean definition and invoke the specified {@code resolver}
	 * on its merged bean definition.
	 * @param innerBeanName the inner bean name (or {@code null} to assign one)
	 * @param innerBd the inner raw bean definition
	 * @param resolver the function to invoke to resolve
	 * @param <T> the type of the resolution
	 * @return a resolved inner bean, as a result of applying the {@code resolver}
	 * @since 6.0
	 */
	public <T> T resolveInnerBean(@Nullable String innerBeanName, BeanDefinition innerBd,
			BiFunction<String, RootBeanDefinition, T> resolver) {

		String nameToUse = (innerBeanName != null ? innerBeanName : "(inner bean)" +
				BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(innerBd));
		return resolver.apply(nameToUse,
				this.beanFactory.getMergedBeanDefinition(nameToUse, innerBd, this.beanDefinition));
	}

	/**
	 * Evaluate the given value as an expression, if necessary.
	 * @param value the candidate value (may be an expression)
	 * @return the resolved value
	 */
	protected @Nullable Object evaluate(TypedStringValue value) {
		Object result = doEvaluate(value.getValue());
		if (!ObjectUtils.nullSafeEquals(result, value.getValue())) {
			value.setDynamic();
		}
		return result;
	}

	/**
	 * Evaluate the given value as an expression, if necessary.
	 * @param value the original value (may be an expression)
	 * @return the resolved value if necessary, or the original value
	 */
	protected @Nullable Object evaluate(@Nullable Object value) {
		if (value instanceof String str) {
			return doEvaluate(str);
		}
		else if (value instanceof String[] values) {
			boolean actuallyResolved = false;
			@Nullable Object[] resolvedValues = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				String originalValue = values[i];
				Object resolvedValue = doEvaluate(originalValue);
				if (resolvedValue != originalValue) {
					actuallyResolved = true;
				}
				resolvedValues[i] = resolvedValue;
			}
			return (actuallyResolved ? resolvedValues : values);
		}
		else {
			return value;
		}
	}

	/**
	 * Evaluate the given String value as an expression, if necessary.
	 * @param value the original value (may be an expression)
	 * @return the resolved value if necessary, or the original String value
	 */
	private @Nullable Object doEvaluate(@Nullable String value) {
		return this.beanFactory.evaluateBeanDefinitionString(value, this.beanDefinition);
	}

	/**
	 * Resolve the target type in the given TypedStringValue.
	 * @param value the TypedStringValue to resolve
	 * @return the resolved target type (or {@code null} if none specified)
	 * @throws ClassNotFoundException if the specified type cannot be resolved
	 * @see TypedStringValue#resolveTargetType
	 */
	protected @Nullable Class<?> resolveTargetType(TypedStringValue value) throws ClassNotFoundException {
		if (value.hasTargetType()) {
			return value.getTargetType();
		}
		return value.resolveTargetType(this.beanFactory.getBeanClassLoader());
	}

	/**
	 * 解析引用类型的属性值。
	 *
	 * Resolve a reference to another bean in the factory.
	 */
	private @Nullable Object resolveReference(Object argName, RuntimeBeanReference ref) {
		try {
			Object bean;
			// 获取引用的 Bean 名称
			Class<?> beanType = ref.getBeanType();
			String resolvedName = String.valueOf(doEvaluate(ref.getBeanName()));
			// 如果引用的对象在父类容器中，则从父类容器中获取指定的引用类型
			if (ref.isToParent()) {
				BeanFactory parent = this.beanFactory.getParentBeanFactory();
				if (parent == null) {
					throw new BeanCreationException(
							this.beanDefinition.getResourceDescription(), this.beanName,
							"Cannot resolve reference to bean " + ref +
									" in parent factory: no parent factory available");
				}
				if (beanType != null) {
					bean = (parent.containsBean(resolvedName) ?
							parent.getBean(resolvedName, beanType) : parent.getBean(beanType));
				}
				else {
					// 如果父工厂不为空，则从父工厂获取引用 BeanName 对应的 Bean 对象
					bean = parent.getBean(resolvedName);
				}
			}
			// 从当前的容器中获取指定的引用 Bean 对象，
			// 如果指定的 Bean 没有被实例化，则会递归触发引用 Bean 的初始化和依赖注入
			else {
				if (beanType != null) {
					if (this.beanFactory.containsBean(resolvedName)) {
						bean = this.beanFactory.getBean(resolvedName, beanType);
					}
					else {
						NamedBeanHolder<?> namedBean = this.beanFactory.resolveNamedBean(beanType);
						bean = namedBean.getBeanInstance();
						resolvedName = namedBean.getBeanName();
					}
				}
				else {
					// 根据引用名称获取 Bean 实例
					bean = this.beanFactory.getBean(resolvedName);
				}
				// 将当前实例化对象的依赖引用对象
				// 注册 beanName 于 DependentBeanName 的依赖关系到 BeanFactory
				this.beanFactory.registerDependentBean(resolvedName, this.beanName);
			}
			if (bean instanceof NullBean) {
				bean = null;
			}
			return bean;
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot resolve reference to bean '" + ref.getBeanName() + "' while setting " + argName, ex);
		}
	}

	/**
	 * Resolve an inner bean definition.
	 * @param argName the name of the argument that the inner bean is defined for
	 * @param innerBeanName the name of the inner bean
	 * @param mbd the merged bean definition for the inner bean
	 * @return the resolved inner bean instance
	 */
	private @Nullable Object resolveInnerBeanValue(Object argName, String innerBeanName, RootBeanDefinition mbd) {
		try {
			// Check given bean name whether it is unique. If not already unique,
			// add counter - increasing the counter until the name is unique.
			String actualInnerBeanName = innerBeanName;
			if (mbd.isSingleton()) {
				actualInnerBeanName = adaptInnerBeanName(innerBeanName);
			}
			this.beanFactory.registerContainedBean(actualInnerBeanName, this.beanName);
			// Guarantee initialization of beans that the inner bean depends on.
			String[] dependsOn = mbd.getDependsOn();
			if (dependsOn != null) {
				for (String dependsOnBean : dependsOn) {
					this.beanFactory.registerDependentBean(dependsOnBean, actualInnerBeanName);
					this.beanFactory.getBean(dependsOnBean);
				}
			}
			// Actually create the inner bean instance now...
			Object innerBean = this.beanFactory.createBean(actualInnerBeanName, mbd, null);
			if (innerBean instanceof FactoryBean<?> factoryBean) {
				boolean synthetic = mbd.isSynthetic();
				innerBean = this.beanFactory.getObjectFromFactoryBean(
						factoryBean, null, actualInnerBeanName, !synthetic);
			}
			if (innerBean instanceof NullBean) {
				innerBean = null;
			}
			return innerBean;
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot create inner bean '" + innerBeanName + "' " +
					(mbd.getBeanClassName() != null ? "of type [" + mbd.getBeanClassName() + "] " : "") +
					"while setting " + argName, ex);
		}
	}

	/**
	 * Checks the given bean name whether it is unique. If not already unique,
	 * a counter is added, increasing the counter until the name is unique.
	 * @param innerBeanName the original name for the inner bean
	 * @return the adapted name for the inner bean
	 */
	private String adaptInnerBeanName(String innerBeanName) {
		String actualInnerBeanName = innerBeanName;
		int counter = 0;
		String prefix = innerBeanName + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;
		while (this.beanFactory.isBeanNameInUse(actualInnerBeanName)) {
			counter++;
			actualInnerBeanName = prefix + counter;
		}
		return actualInnerBeanName;
	}

	/**
	 * 解析 array 类型的属性。
	 *
	 * For each element in the managed array, resolve reference if necessary.
	 */
	private Object resolveManagedArray(Object argName, List<?> ml, Class<?> elementType) {
		/// 创建一个指定类型的数组，用于存放和返回解析后的数组
		Object resolved = Array.newInstance(elementType, ml.size());
		for (int i = 0; i < ml.size(); i++) {
			// 递归解析 array 的每一个元素，并将解析后的值设置到 resolved 数组中，索引为 i
			Array.set(resolved, i, resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
		}
		return resolved;
	}

	/**
	 * For each element in the managed list, resolve reference if necessary.
	 */
	private List<?> resolveManagedList(Object argName, List<?> ml) {
		List<Object> resolved = new ArrayList<>(ml.size());
		for (int i = 0; i < ml.size(); i++) {
			resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
		}
		return resolved;
	}

	/**
	 * For each element in the managed set, resolve reference if necessary.
	 */
	private Set<?> resolveManagedSet(Object argName, Set<?> ms) {
		Set<Object> resolved = CollectionUtils.newLinkedHashSet(ms.size());
		int i = 0;
		for (Object m : ms) {
			resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), m));
			i++;
		}
		return resolved;
	}

	/**
	 * For each element in the managed map, resolve reference if necessary.
	 */
	private Map<?, ?> resolveManagedMap(Object argName, Map<?, ?> mm) {
		Map<Object, Object> resolved = CollectionUtils.newLinkedHashMap(mm.size());
		mm.forEach((key, value) -> {
			Object resolvedKey = resolveValueIfNecessary(argName, key);
			Object resolvedValue = resolveValueIfNecessary(new KeyedArgName(argName, key), value);
			resolved.put(resolvedKey, resolvedValue);
		});
		return resolved;
	}


	/**
	 * Holder class used for delayed toString building.
	 */
	private static class KeyedArgName {

		private final Object argName;

		private final Object key;

		public KeyedArgName(Object argName, Object key) {
			this.argName = argName;
			this.key = key;
		}

		@Override
		public String toString() {
			return this.argName + " with key " + BeanWrapper.PROPERTY_KEY_PREFIX +
					this.key + BeanWrapper.PROPERTY_KEY_SUFFIX;
		}
	}

}
