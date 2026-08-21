package com.imxiaoxin.iot.constant;

/**
 * @author imxiaoxin
 *
 */
public interface RedisConstant {

  String CHARGER_STATUS_KEY = "iot:charger:info:";
  // 预防硬件段持续报警
  String EXP_ENV_COOLDOWN_KEY = "iot:ai:cooldown:envType:";
}
