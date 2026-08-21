package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 *  车位状态(0: 空闲, 1: 占用, 2: 故障)
 */
@Getter
@AllArgsConstructor
public enum ParkSpotStatusEnum implements BaseEnum {
    IDLE(0, "空闲"),
    OCCUPIED(1, "占用"),
    FAULT(2, "故障");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;

}
