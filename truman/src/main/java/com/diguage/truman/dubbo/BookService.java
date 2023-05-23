package com.diguage.truman.dubbo;

public interface BookService {
  Book getById(long id);

  Long save(Book book);
}
