package com.imxiaoxin.iot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.model.entity.ChargingBillingRecord;

/**
 * <p>
 * 充电计费订单记录表 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2026-03-28
 */
public interface IChargingBillingRecordService extends IService<ChargingBillingRecord> {

  void startCharging(String licensePlate, Integer chargerId) throws JsonProcessingException;
  void finishCharging(Integer chargerId) throws JsonProcessingException;

}
