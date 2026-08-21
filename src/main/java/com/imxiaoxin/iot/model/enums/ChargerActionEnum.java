package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 *  0: 关闭；1: 开启
 */
@Getter
@AllArgsConstructor
public enum ChargerActionEnum implements BaseEnum {

  CLOSE(0, "关闭"),
  OPEN(1, "开启");

  @JsonValue
  @EnumValue
  private final Integer code;
  private final String desc;

}
