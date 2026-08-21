package com.imxiaoxin.iot.exception;

import lombok.Data;

/**
 * 业务异常
 */
@Data
public class BizException extends RuntimeException {

  private Integer code; // 业务异常码
  private String message; // 业务异常信息

  public BizException(String message) {
    this.code = 500;
    this.message = message;
  }

  public BizException(Integer code, String message) {
    this.code = code;
    this.message = message;
  }
  public BizException(ResultCodeEnum exceptionEnum) {
    this.code = exceptionEnum.getCode();
    this.message = exceptionEnum.getMessage();
  }

}