package com.diguage.truman.web.undertow.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
  @RequestMapping("/hello")
  public String hello() {
    return "Hello, D瓜哥";
  }
}
