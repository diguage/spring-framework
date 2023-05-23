package com.diguage.truman.mybatis;

import java.util.Date;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2020-05-29 17:24
 */
public class Employees {
  public Integer empNo;
  public Date birthDate;
  public String firstName;
  public String lastName;
  public String gender;
  public Date hireDate;

  @Override
  public String toString() {
    return "Employees{" +
        "empNo=" + empNo +
        ", birthDate=" + birthDate +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", gender='" + gender + '\'' +
        ", hireDate=" + hireDate +
        '}';
  }
}
