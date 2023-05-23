package com.diguage.truman.jdbc;

import java.util.Date;

/**
 * @author D瓜哥 · https://www.diguage.com
 * @since 2020-05-29 17:24
 */
public class Employees {
  Integer empNo;
  Date birthDate;
  String firstName;
  String lastName;
  String gender;
  Date hireDate;

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
