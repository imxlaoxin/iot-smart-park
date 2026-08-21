package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 * 识别状态: 0: 未处理; 1: 正常; 2: 误报; 3: 漏报;
 */
@Getter
@AllArgsConstructor
public enum BizDetectStatusEnum implements BaseEnum {

  UNPROCESSED(0, "未处理"),
  NORMAL(1, "正常"),
  MISREPORT(2, "误报"),
  LOSS(3, "漏报");

  @EnumValue
  @JsonValue
  private final Integer code;
  private final String desc;

}
