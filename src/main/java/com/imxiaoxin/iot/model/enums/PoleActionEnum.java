package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 *  0: 降杆；1: 升杆
 */
@Getter
@AllArgsConstructor
public enum PoleActionEnum implements BaseEnum {

  DOWN(0, "降杆"),
  UP(1, "升杆");

  @JsonValue
  @EnumValue
  private final Integer code;
  private final String desc;

}
