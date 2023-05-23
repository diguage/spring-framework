package com.diguage.truman.dubbo;

import lombok.Data;

import java.util.Date;

@Data
public class Book {
  private Long id;
  private String name;
  private Date publishDate;
}
