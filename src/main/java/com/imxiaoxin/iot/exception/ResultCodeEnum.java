package com.imxiaoxin.iot.exception;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    
   	// 登录段 1~49
    NEED_LOGIN(1,"需要登录后操作"),
    LOGIN_USER_NOT_EXIST(2,"用户不存在"),
    LOGIN_PASSWORD_ERROR(3,"密码错误"),
    // TOKEN 401
    TOKEN_INVALID(401,"无效的TOKEN"),
    TOKEN_EXPIRED(401,"TOKEN已过期"),
    TOKEN_REQUIRE(401,"TOKEN是必须的"),
    // SIGN验签 100~120
    SIGN_INVALID(100,"无效的SIGN"),
    SIG_TIMEOUT(101,"SIGN已过期"),
    // 参数错误 500~1000
    PARAM_REQUIRE(500,"缺少参数"),
    PARAM_INVALID(501,"无效参数"),
    PARAM_IMAGE_FORMAT_ERROR(502,"图片格式有误"),
    SERVER_ERROR(503,"服务器内部错误111111111111"),
    // 数据错误 1000~2000
    DATA_EXIST(1000,"数据已经存在"),
    AP_USER_DATA_NOT_EXIST(1001,"ApUser数据不存在"),
    DATA_NOT_EXIST(1002,"数据不存在"),
    // 权限错误 3000~3500
    NO_OPERATOR_AUTH(3000,"无权限操作"),
    NEED_ADMIN(3001,"需要管理员权限"),
  
  	// 业务异常规定在10000~50000之间
    ORDER_CLOSED(10001, "订单已关闭"),
    ORDER_NOT_EXIST(10002, "订单不存在"),
    ORDER_TIMEOUT(10003, "订单超时"),

    PRODUCT_STOCK_NOT_ENOUGH(20003, "库存不足"),
    PRODUCT_HAS_SOLD(20002, "商品已售完"),
    PRODUCT_HAS_CLOSED(20001, "商品已下架");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}