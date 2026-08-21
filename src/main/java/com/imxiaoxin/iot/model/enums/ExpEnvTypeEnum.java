package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 * 告警类型 (0：温度；1：湿度；2：烟雾浓度；3.二氧化碳浓度；4.光照强度。)
 */
@Getter
@AllArgsConstructor
public enum ExpEnvTypeEnum implements BaseEnum {

  TEMPERATURE(0, "温度"),
  HUMIDITY(1, "湿度"),
  SMOKE_DENSITY(2, "烟雾浓度"),
  CO2_DENSITY(3, "二氧化碳浓度"),
  LIGHT_INTENSITY(4, "光照强度");

  @EnumValue
  @JsonValue
  private final Integer code;
  private final String desc;

}
