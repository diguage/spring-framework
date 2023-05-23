package com.diguage.truman.core;

import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionTest {
	interface SomeRepository {
		<T> T someMethod1(Class<T> arg0, Class<?> arg1, Class<Object> arg2);
	}

	@Test
	public void test() {
		Method method = ReflectionUtils.findMethod(SomeRepository.class, "someMethod1", Class.class, Class.class, Class.class);

		ResolvableType returnType = ResolvableType.forMethodReturnType(method, SomeRepository.class);

		ResolvableType arg0 = ResolvableType.forMethodParameter(method, 0, SomeRepository.class); // generic[0]=T
		ResolvableType arg1 = ResolvableType.forMethodParameter(method, 1, SomeRepository.class); // generic[0]=?
		ResolvableType arg2 = ResolvableType.forMethodParameter(method, 2, SomeRepository.class); // generic[0]=java.lang.Object

		assertThat(returnType.isAssignableFrom(arg0.as(Class.class).getGeneric(0))).isTrue();
		assertThat(returnType.isAssignableFrom(arg1.as(Class.class).getGeneric(0))).isFalse();
		assertThat(returnType.isAssignableFrom(arg2.as(Class.class).getGeneric(0))).isFalse();
	}
}
