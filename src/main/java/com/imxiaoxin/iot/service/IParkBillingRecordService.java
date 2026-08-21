package com.imxiaoxin.iot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.model.dto.AddOrUpdateParkBillingRecordDto;
import com.imxiaoxin.iot.model.entity.ParkBillingRecord;
import com.imxiaoxin.iot.model.vo.OrderInfoVo;
import com.imxiaoxin.iot.model.vo.TodayOrderSummaryVo;

/**
 * <p>
 * 停车计费订单记录表 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2026-03-28
 */
public interface IParkBillingRecordService extends IService<ParkBillingRecord> {

  void addOrUpdateOrder(AddOrUpdateParkBillingRecordDto addParkBillingRecordDto);

  String payOrder(String licensePlate) throws JsonProcessingException;

  OrderInfoVo getOrderInfo(String licensePlate);

  TodayOrderSummaryVo getTodayOrderInfo();
}
