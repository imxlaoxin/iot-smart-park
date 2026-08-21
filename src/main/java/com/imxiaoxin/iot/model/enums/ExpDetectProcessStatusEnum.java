package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 * 处理状态: 0-未处理, 1-确认属实, 2-误报, 3-漏报; 4-已解决
 */
@Getter
@AllArgsConstructor
public enum ExpDetectProcessStatusEnum implements BaseEnum {

  UNPROCESSED(0, "未处理"),
  CONFIRMED(1, "确认属实"),
  MISREPORTED(2, "误报"),
  LOSS(3, "漏报"),
  SOLVED(4, "已解决");

  @EnumValue
  @JsonValue
  private final Integer code;
  private final String desc;

}
