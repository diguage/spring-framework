package com.diguage.truman.mybatis;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author D瓜哥, https://www.diguage.com/
 * @since 2020-05-29 17:23
 */
public interface EmployeesMapper {
	@Select("SELECT * FROM employees WHERE emp_no = #{id}")
	Employees getById(@Param("id") Integer id);
}
