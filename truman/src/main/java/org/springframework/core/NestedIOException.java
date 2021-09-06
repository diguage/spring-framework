package org.springframework.core;

// TODO dgg 等 mybatis-spring 兼容版发版后，记得删除类！
public class NestedIOException extends NestedCheckedException{
  private static final long serialVersionUID = 123L;

  public NestedIOException(String msg) {
    super(msg);
  }

  public NestedIOException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
