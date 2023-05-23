package com.diguage.truman.beans.env;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import static com.diguage.truman.util.Constans.BASE_CLASS_PATH;

/**
 * 研究变量替换
 * <p>
 * {@link PropertyPlaceholderConfigurerTest} 被启用。启用 {@link PropertySourcesPlaceholderConfigurer}.
 */
public class PropertyPlaceholderConfigurerTest {


	@Test
	public void test() {
		String config = BASE_CLASS_PATH + "/beans/env/" +
				"PropertyPlaceholderConfigurerTest.xml";
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(config);
//		PropertyPlaceholderConfigurer placeholderConfigurer = ctx.getBean(PropertyPlaceholderConfigurer.class);
//		assertThat(placeholderConfigurer).isNotNull();

		CfgOption dataSource = ctx.getBean(CfgOption.class);
		System.out.println("cmdCfg=" + dataSource.getCmdCfg());
		System.out.println("fileCfg=" + dataSource.getFileCfg());
		System.out.println("envCfg=" + dataSource.getEnvCfg());
	}

	@Configuration
	@EnableAspectJAutoProxy
	public static class Config {
//		@Bean
//		public PropertyPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
//			String path = "com/diguage/truman/beans/env/" +
//					"PropertyPlaceholderConfigurerTest.properties";
//			PropertyPlaceholderConfigurer result = new PropertyPlaceholderConfigurer();
//			result.setLocations(new ClassPathResource(path));
//			return result;
//		}

		/**
		 * 如果不创建这个 Bean，则在 XML 中配置的 ${url} 和 ${javaHome} 就不会解析！
		 */
		@Bean
		public PropertySourcesPlaceholderConfigurer getPSPC() {
			PropertySourcesPlaceholderConfigurer result
					= new PropertySourcesPlaceholderConfigurer();
			String path = "com/diguage/truman/beans/env/" +
					"PropertyPlaceholderConfigurerTest.properties";
			result.setLocations(new ClassPathResource(path));
			return result;
		}
	}

	@Data
	public static class CfgOption {
		/**
		 * 命令行 -D 配置项
		 */
		private String cmdCfg;
		/**
		 * 配置文件配置项
		 */
		private String fileCfg;
		/**
		 * 环境变量配置项
		 */
		private String envCfg;
	}
}
