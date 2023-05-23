package com.diguage.truman.mybatis;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2020-05-29 17:23
 */
public interface EmployeesMapper {

  @MapperAop
  @Select("SELECT * FROM employees WHERE emp_no = #{id}")
  Employees getById(@Param("id") Integer id);

  @MapperAop
  @Insert("INSERT INTO employees(emp_no, birth_date, first_name, last_name, gender, hire_date) " +
      "VALUES(#{empNo}, #{birthDate, jdbcType=TIMESTAMP}, #{firstName}, #{lastName}, #{gender}, " +
      "       #{hireDate, jdbcType=TIMESTAMP})")
  int insert(Employees employees);
}
