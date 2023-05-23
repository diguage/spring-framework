package com.diguage.truman.context;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2021-08-08 22:26:20
 */
@Slf4j
public class AnnoLookupTest {

	@Test
	public void test() {
		AnnotationConfigApplicationContext context
				= new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();
		UserService service = context.getBean(UserService.class);
		service.get();
	}

	@Configuration
	@Import(LookupImportSelector.class)
	public static class Config {
	}

	public static class LookupImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata metadata) {
			return new String[]{
					UserDao.class.getName(),
					UserService.class.getName()
			};
		}
	}

	@Repository
	public static class UserDao {
		public String getUser() {
			log.info("execute UserDao.getUser");
			return "D瓜哥";
		}
	}

	@Component
	public static abstract class UserService {
		public void get() {
			UserDao userDao = getUserDao();
			log.info("invoke userDao...");
			log.info("result: {}", userDao.getUser());
		}

		/**
		 * TODO 这里使用抽象方法，让人费解。
		 */
		@Lookup
		public abstract UserDao getUserDao();
	}
}
