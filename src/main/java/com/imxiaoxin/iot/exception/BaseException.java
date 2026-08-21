package com.imxiaoxin.iot.exception;


import lombok.Data;

/**
 * 基础异常
 */
@Data
public class BaseException extends RuntimeException {

    private Integer code;
    private String message;

    public BaseException() {
    }

    public BaseException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseException(ResultCodeEnum resultCodeEnum) {
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}