package com.diguage.truman.beans;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

public class BeanUtilsTest {
	@Test
	public void test() {
		Person source = new Person();
		source.setFirstName("D瓜哥");
		source.setSecondName("diguage");
		source.setLastName("www.diguage.com");
		source.setBirthday(new Date());
		source.setSex(1);
		source.setWeight(145.0F);
		source.setHeight(175.0F);
		source.setAddress("https://www.diguage.com");
		source.setSalary(BigDecimal.ZERO);
		source.setCapital(BigDecimal.ONE);

		Person target = new Person();
		BeanUtils.copyProperties(source, target);
		System.out.println(target);
	}

	@Data
	@NoArgsConstructor
	public static class Person {
		private String firstName;
		private String secondName;
		private String lastName;
		private Date birthday;
		private int sex;
		private float weight;
		private float height;
		private String address;
		private BigDecimal salary;
		private BigDecimal capital;
	}
}
