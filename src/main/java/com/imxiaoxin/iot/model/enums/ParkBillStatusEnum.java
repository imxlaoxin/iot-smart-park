package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 *  订单状态(0: 停车中, 1: 待缴费, 2: 已完成)
 */
@Getter
@AllArgsConstructor
public enum ParkBillStatusEnum implements BaseEnum {
    PARKING(0, "停车中"),
    AWAITING_PAYMENT(1, "待缴费"),
    COMPLETED(2, "已完成");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

}
