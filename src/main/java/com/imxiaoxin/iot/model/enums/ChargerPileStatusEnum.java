package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 *  充电桩状态(0: 停止; 1: 启动; 2: 异常)
 */
@Getter
@AllArgsConstructor
public enum ChargerPileStatusEnum implements BaseEnum {

    STOP(0, "停止"),
    START(1, "启动"),
    FAULT(2, "异常");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

}
