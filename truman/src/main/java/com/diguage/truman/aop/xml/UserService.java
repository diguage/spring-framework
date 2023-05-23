package com.diguage.truman.aop.xml;

import com.diguage.truman.aop.AopTest;
import org.springframework.aop.framework.AopContext;

import jakarta.annotation.Resource;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-08-09 10:31
 */
public class UserService {
	private String desc = "testBean";

	@Resource
	private UserDao userDao;

	public String getDesc() {
		System.out.println("getDesc");
		this.test();
		System.out.println("--this----------getDesc");
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
		// 使用 @EnableAspectJAutoProxy(exposeProxy = true) 打开 exposeProxy = true
		// 则必须这样写，才能获取到当前的代理对象，然后调用的方法才是被 AOP 处理后的方法。
		// 使用 this.methodName() 调用，依然调用的是原始的、未经 AOP 处理的方法
		((AopTest.UserService) AopContext.currentProxy()).test();
		System.out.println("--AopContext----setDesc");
	}

	public void test() {
		System.out.println("----------------test");
	}

	public String getById(int id) {
		return userDao.getById(id);
	}
}
