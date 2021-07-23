package com.diguage.truman.ext;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-06-10 00:26
 */
public class ExtensionTest {
	@Test
	public void test() {
		ClassPathXmlApplicationContext context
				= new ClassPathXmlApplicationContext("classpath:com/diguage/truman/ext/dgg.xml");
		User user = context.getBean(User.class);
		System.out.println(user.getUserName() + " : " + user.getEmail());
	}
}
