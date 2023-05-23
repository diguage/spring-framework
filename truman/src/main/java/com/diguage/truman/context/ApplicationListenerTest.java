package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 19:33
 */
public class ApplicationListenerTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.addApplicationListener(new LogApplicationListener());
		context.register(Config.class);
		context.refresh();
		UserService service = context.getBean(UserService.class);
		System.out.println(service);
	}

	@Configuration
	@Import(UserService.class)
	public static class Config {
	}

	@Component
	public static class UserService {
	}

	public static class LogApplicationListener implements ApplicationListener<ApplicationEvent> {
		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			System.out.println(event);
		}
	}
}
