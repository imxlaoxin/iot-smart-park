package com.imxiaoxin.iot.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.imxiaoxin.iot.model.enums.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author imxiaoxin
 *  状态(1:启用, 0:停用)
 */
@Getter
@AllArgsConstructor
public enum BillingRuleOperation implements BaseEnum {

  ENABLE(1, "启用"),
  DISABLE(0, "停用");

  @JsonValue
  @EnumValue
  private final Integer code;
  private final String desc;

}
