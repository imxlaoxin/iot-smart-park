package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 * 告警级别: 0-预警, 1-危险, 2-危急
 */
@Getter
@AllArgsConstructor
public enum ExpAlarmLevelEnum implements BaseEnum {

  WARNING(0, "预警"),
  DANGER(1, "危险"),
  CRITICAL(2, "危急");

  @EnumValue
  @JsonValue
  private final Integer code;
  private final String desc;

}
