package com.imxiaoxin.iot.common;

import io.swagger.v3.oas.annotations.media.Schema;
import com.imxiaoxin.iot.exception.ResultCodeEnum;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 响应结果类
 *
 * @param <T> 任意类型
 */
@Data
@Schema(name = "R", description = "响应结果")
public class R<T> {
    /**
     * 响应状态码，200是正常，非200表示异常
     */
    @Schema(description = "响应状态码", example = "200")
    private int code;
    /**
     * 响应信息
     */
    @Schema(description = "响应信息", example = "用户名或密码错误")
    private String msg;
    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    // 私有化构造
    private R() {}

    // 返回数据
    public static <T> R<T> build(Integer code, String message, T data) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(data);
        return result;
    }

    // 通过枚举构造R对象
    public static <T> R build(ResultCodeEnum resultCodeEnum, T data) {
        return build(resultCodeEnum.getCode() , resultCodeEnum.getMessage(), data) ;
    }

    public static <T> R<T> success() {
        return success(HttpStatus.OK, null, null);
    }
    public static <T> R<T> success(String message) {
        return success(HttpStatus.OK, message, null);
    }
    public static <T> R<T> success(T data) {
        return success(HttpStatus.OK, null, data);
    }

    public static <T> R<T> success(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }

    public static <T> R<T> success(HttpStatus status, T data) {
        return success(status, null, data);
    }
    public static <T> R<T> success(HttpStatus status, String message, T data) {
        return R.build(status.value(), message, data);
    }
    public static <T> R<T> fail(String message) {
        return fail(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }
    public static <T> R<T> fail(HttpStatus status, String message) {
        return fail(status, message, null);
    }
    public static <T> R<T> fail(HttpStatus status, String message, T data) {
        return R.build(status.value(), message, data);
    }

    public static <T> R<T> fail(Integer status, String message) {
        return R.build(status, message, null);
    }
    public static <T> R<T> fail(Integer status, String message, T data) {
        return R.build(status, message, data);
    }
}