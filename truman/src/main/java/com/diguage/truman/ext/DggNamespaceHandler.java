package com.diguage.truman.ext;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-06-10 00:11
 */
public class DggNamespaceHandler extends NamespaceHandlerSupport {
	@Override
	public void init() {
		registerBeanDefinitionParser("user", new UserBeanDefinitionParser());
	}
}
