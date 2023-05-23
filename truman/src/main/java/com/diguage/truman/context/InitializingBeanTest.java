package com.diguage.truman.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-26 17:37
 */
public class InitializingBeanTest {
	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		UserService service = context.getBean(UserService.class);
		System.out.println(service.getCount());
	}

	@Configuration
	@Import(DefaultImportSelector.class)
	public static class Config {
	}

	public static class DefaultImportSelector implements ImportSelector {
		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			return new String[]{
					UserDao.class.getName(),
					OrderDao.class.getName(),
					UserService.class.getName()
			};
		}

	}

	@Repository
	public static class UserDao {
		public String getById(Long id) {
			return "Name-" + id;
		}
	}

	@Repository
	public static class OrderDao {
		public String getById(Long id) {
			return "Order-" + id;
		}
	}

	@Service
	public static class UserService implements InitializingBean {
		@Autowired
		UserDao userDao;

		@Autowired
		OrderDao orderDao;

		static int count = 0;

		@Override
		public void afterPropertiesSet() throws Exception {
			// 根据最新代码测试来看，这里只会被执行一次
			System.out.println("\nuserDao =" + userDao + "\norderDao=" + orderDao);
			count++;
		}

		String getUserById(Long id) {
			return userDao.getById(id);
		}

		String getOrderById(Long id) {
			return orderDao.getById(id);
		}

		int getCount() {
			return count;
		}
	}
}
