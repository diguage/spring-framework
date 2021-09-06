package com.diguage.truman.dubbo;

import org.apache.dubbo.config.annotation.DubboService;

import java.util.Date;

@DubboService
public class BookServiceImpl implements BookService {
  @Override
  public Book getById(long id) {
    Book result = new Book();
    result.setId(id);
    result.setName("diguage");
    result.setPublishDate(new Date());
    return result;
  }

  @Override
  public Long save(Book book) {
    return System.currentTimeMillis();
  }
}
