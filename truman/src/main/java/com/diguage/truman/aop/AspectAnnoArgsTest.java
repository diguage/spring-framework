package com.diguage.truman.aop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * 验证 @args 的匹配规则。
 *
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-07-18 21:18:43
 */
@Slf4j
public class AspectAnnoArgsTest {

	@Test
	public void test() {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		DiguageTask task = context.getBean(DiguageTask.class);
		AnnoParam annoParam = new AnnoParam("AnnoParam");
		task.run(annoParam);

		NonAnnoParam nonAnnoParam = new NonAnnoParam("NonAnnoParam");
		task.run(nonAnnoParam);
	}

	@Configuration
	@Import(ArgsImportSelector.class)
	@EnableAspectJAutoProxy // 注意：这行必须加
	public static class Config {
	}

	public static class ArgsImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata metadata) {
			return new String[]{
					DiguageTask.class.getName(),
					TypeAspect.class.getName()
			};
		}
	}

	public static class DiguageTask {
		public void run(Object param) {
			log.info("Diguage.run executing.params[{}]", param);
		}
	}

	@Data
	@NoArgsConstructor
	@TypeAnnotation
	public static class AnnoParam {
		private String name;

		public AnnoParam(String name) {
			this.name = name;
		}
	}

	/**
	 * 这个参数调用时，没有执行增强，所以只会对有注解的参数进行拦截。
	 */
	@Data
	@NoArgsConstructor
	public static class NonAnnoParam {
		private String name;

		public NonAnnoParam(String name) {
			this.name = name;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface TypeAnnotation {
	}

	@Aspect
	public static class TypeAspect {
		@Pointcut("@args(com.diguage.truman.aop." +
				"AspectAnnoArgsTest.TypeAnnotation)")
		public void doType() {
		}

		@Around("doType()")
		public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
			Object[] args = null;
			try {
				args = joinPoint.getArgs();
				return joinPoint.proceed();
			} finally {
				log.info("TypeAspect executing. params[{}]", Arrays.toString(args));
			}
		}
	}
}
