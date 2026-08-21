package com.imxiaoxin.iot.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.dto.AddOrUpdateParkBillingRecordDto;
import com.imxiaoxin.iot.model.vo.OrderInfoVo;
import com.imxiaoxin.iot.model.vo.TodayOrderSummaryVo;
import com.imxiaoxin.iot.service.IParkBillingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 停车计费订单记录表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 * @since 2026-03-28
 */
@Tag(name = "停车计费订单记录接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/bill/parking")
public class ParkBillingRecordController {

  @Autowired
  private IParkBillingRecordService iParkBillingRecordService;

  @Operation(summary = "新增或修改停车计费订单")
  @PostMapping("addOrUpdateOrder")
  public R<String> addOrUpdateOrder(AddOrUpdateParkBillingRecordDto dto) {
    log.info("新增或修改停车计费订单: {}", dto);
    iParkBillingRecordService.addOrUpdateOrder(dto);
    return R.success("新增停车计费订单成功");
  }

  @Operation(summary = "支付停车订单(包含充电费用)")
  @PostMapping("payOrder")
  public R<String> payOrder(String licensePlate) throws JsonProcessingException {
    String message = iParkBillingRecordService.payOrder(licensePlate);
    return R.success(message);
  }

  @Operation(summary = "查询订单相关信息")
  @PostMapping("getOrderInfo")
  public R<OrderInfoVo> getOrderInfo(String licensePlate) {
    OrderInfoVo orderInfoVo = iParkBillingRecordService.getOrderInfo(licensePlate);
    return R.success(orderInfoVo);
  }

  @Operation(summary = "查询今日所有订单信息")
  @GetMapping("getTodayOrderInfo")
  public R<TodayOrderSummaryVo> getTodayOrderInfo() {
    return R.success(iParkBillingRecordService.getTodayOrderInfo());
  }

}
