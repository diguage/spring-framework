package com.diguage.truman.aop.xml;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-08-09 10:27
 */
public class AopXmlTest {
	@Test
	public void test() {
		ClassPathXmlApplicationContext context
				= new ClassPathXmlApplicationContext("classpath:com/diguage/truman/aop/xml/aop-xml.xml");

		UserService bean = context.getBean(UserService.class);

		bean.test();
		bean.getDesc();
		bean.setDesc("This is a test.");

		String user = bean.getById(119);
		System.out.println(user);
	}
}
