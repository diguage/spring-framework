/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;

/**
 * Utility class for working with Strings that have placeholder values in them.
 * A placeholder takes the form {@code ${name}}. Using {@code PropertyPlaceholderHelper}
 * these placeholders can be substituted for user-supplied values.
 *
 * <p>Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceholderHelper {

	private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

	// 维护了一组简单的前后缀组合
	private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);

	static {
		wellKnownSimplePrefixes.put("}", "{");
		wellKnownSimplePrefixes.put("]", "[");
		wellKnownSimplePrefixes.put(")", "(");
	}

	// 占位符前缀
	private final String placeholderPrefix;

	// 占位符后缀
	private final String placeholderSuffix;

	private final String simplePrefix;

	// 默认值分割符，譬如当其值为 : 时，对应 ${a:b} 意味着当 a 解析失败时取默认值 b
	@Nullable
	private final String valueSeparator;

	// 解析失败时是否忽略
	private final boolean ignoreUnresolvablePlaceholders;


	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * Unresolvable placeholders are ignored.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
		this(placeholderPrefix, placeholderSuffix, null, true);
	}

	/**
	 * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
	 * @param placeholderPrefix the prefix that denotes the start of a placeholder
	 * @param placeholderSuffix the suffix that denotes the end of a placeholder
	 * @param valueSeparator the separating character between the placeholder variable
	 * and the associated default value, if any
	 * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
	 * be ignored ({@code true}) or cause an exception ({@code false})
	 */
	public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
			@Nullable String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

		Assert.notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
		Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
		this.placeholderPrefix = placeholderPrefix;
		this.placeholderSuffix = placeholderSuffix;
		String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
		if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
			this.simplePrefix = simplePrefixForSuffix;
		}
		else {
			this.simplePrefix = this.placeholderPrefix;
		}
		this.valueSeparator = valueSeparator;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}


	/**
	 * Replaces all placeholders of format {@code ${name}} with the corresponding
	 * property from the supplied {@link Properties}.
	 * @param value the value containing the placeholders to be replaced
	 * @param properties the {@code Properties} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, final Properties properties) {
		Assert.notNull(properties, "'properties' must not be null");
		return replacePlaceholders(value, properties::getProperty);
	}

	/**
	 * Replaces all placeholders of format {@code ${name}} with the value returned
	 * from the supplied {@link PlaceholderResolver}.
	 * @param value the value containing the placeholders to be replaced
	 * @param placeholderResolver the {@code PlaceholderResolver} to use for replacement
	 * @return the supplied value with placeholders replaced inline
	 */
	public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
		Assert.notNull(value, "'value' must not be null");
		return parseStringValue(value, placeholderResolver, null);
	}

	// tag::parseStringValue[]
	/**
	 * 主要逻辑在此：
	 * 1、首先 占位符 是支持嵌套的，譬如 ${a${b}}，因此截取出 占位符 中间的目标字符串后，是需要递归解析的
	 * 2、其次，此处是不支持解析 循环占位符 的，比如当属性源为 a -> ${a} 时，显然是无法解析的，
	 *    此处通过一个 Set<String> visitedPlaceholders 保证该逻辑
	 * 3、占位符解析完成后，就是从 属性源 中解析对应属性了，对于解析为 null 的属性是支持指定默认值的（前提是你指定了默认值分割符）
	 * 4、如果仍然解析失败，根据属性 ignoreUnresolvablePlaceholders 决定是否抛出异常
	 * 5、如果解析成功了，对于解析的结果，如果仍然包含 占位符，则再次进行解析
	 * 6、最后更新 startIndex，便于解析后面的 占位符，如果都解析完了，就是 -1，然后把解析完的 占位符 移出 visitedPlaceholders
	 * 7、最后，分析下用于获取 占位符 后缀下标的方法 findPlaceholderEndIndex
	 */
	protected String parseStringValue(
			String value, PlaceholderResolver placeholderResolver, @Nullable Set<String> visitedPlaceholders) {
		// 如果不包含指定前缀，那就原样返回
		int startIndex = value.indexOf(this.placeholderPrefix);
		if (startIndex == -1) {
			return value;
		}

		StringBuilder result = new StringBuilder(value);
		while (startIndex != -1) {
			// 先找到对应后缀的下标
			int endIndex = findPlaceholderEndIndex(result, startIndex);
			if (endIndex != -1) {
				// 截取前后缀中间的目标字符串
				String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				String originalPlaceholder = placeholder;
				if (visitedPlaceholders == null) {
					visitedPlaceholders = new HashSet<>(4);
				}
				// 先把解析目标字符串保存起来，避免循环解析
				if (!visitedPlaceholders.add(originalPlaceholder)) {
					throw new IllegalArgumentException(
							"Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
				}
				// 开始递归解析目标字符串，因为目标字符串可能也包含占位符，比如 ${a${b}}
				// Recursive invocation, parsing placeholders contained in the placeholder key.
				placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
				// Now obtain the value for the fully resolved key...
				// 解析占位符在这里完成
				String propVal = placeholderResolver.resolvePlaceholder(placeholder);
				// 如果解析结果是 null，那就看是有指定默认值分割符，
				// 如果有且原始值包含该分割符，则先获取分割符前的 key，获取无果返回指定默认值
				if (propVal == null && this.valueSeparator != null) {
					int separatorIndex = placeholder.indexOf(this.valueSeparator);
					if (separatorIndex != -1) {
						String actualPlaceholder = placeholder.substring(0, separatorIndex);
						String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
						propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
						if (propVal == null) {
							propVal = defaultValue;
						}
					}
				}
				// 如果获取成功，则再解析一次
				// 这意味着如果最终解析出来的属性中仍然包含占位符，是可以继续解析的
				if (propVal != null) {
					// Recursive invocation, parsing placeholders contained in the
					// previously resolved placeholder value.
					propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
					// 解析完后整体替换
					result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					if (logger.isTraceEnabled()) {
						logger.trace("Resolved placeholder '" + placeholder + "'");
					}
					// 然后更新 startIndex，
					// 如果后面还有占位符，就更新到下一个占位符前缀下标；
					// 如果没有，就返回 -1，打破循环
					startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else if (this.ignoreUnresolvablePlaceholders) {
					// 到这里就是解析无果了，根据属性 ignoreUnresolvablePlaceholders
					// 决定是否抛出异常 IllegalArgumentException
					// Proceed with unprocessed value.
					startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
				}
				else {
					throw new IllegalArgumentException("Could not resolve placeholder '" +
							placeholder + "'" + " in value \"" + value + "\"");
				}
				// 解析完后从缓存中移除
				visitedPlaceholders.remove(originalPlaceholder);
			}
			else {
				startIndex = -1;
			}
		}
		return result.toString();
	}
	// end::parseStringValue[]

	// tag::findPlaceholderEndIndex[]
	/**
	 * 获取 占位符 后缀下标
	 */
	private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		// 赋值 index
		int index = startIndex + this.placeholderPrefix.length();
		int withinNestedPlaceholder = 0;
		// 从 index 处开始解析
		while (index < buf.length()) {
			/**
			 * 先匹配后缀，如果匹配到，先看下是不是嵌套的后缀,
			 * 如果是嵌套后缀，嵌套层级 -1，重新计算 index；
			 * 否则就是匹配到了，直接返回
			 */
			if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
				if (withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				}
				else {
					return index;
				}
			}
			/**
			 * 如果没匹配到，就看下是否匹配到 simplePrefix，
			 * 如果匹配到了，说明有嵌套 占位符；
			 * 嵌套层级 +1，重新计算 index
			 */
			else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
				withinNestedPlaceholder++;
				index = index + this.simplePrefix.length();
			}
			// 如果都没有，index + 1 即可
			else {
				index++;
			}
		}
		return -1;
	}
	// end::findPlaceholderEndIndex[]


	/**
	 * Strategy interface used to resolve replacement values for placeholders contained in Strings.
	 */
	@FunctionalInterface
	public interface PlaceholderResolver {

		/**
		 * Resolve the supplied placeholder name to the replacement value.
		 * @param placeholderName the name of the placeholder to resolve
		 * @return the replacement value, or {@code null} if no replacement is to be made
		 */
		@Nullable
		String resolvePlaceholder(String placeholderName);
	}

}
