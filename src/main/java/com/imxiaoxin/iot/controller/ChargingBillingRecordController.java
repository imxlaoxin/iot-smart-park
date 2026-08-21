package com.imxiaoxin.iot.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.service.IChargingBillingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 充电计费订单记录表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "充电计费订单记录接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/bill/charging")
public class ChargingBillingRecordController {

  @Autowired
  private IChargingBillingRecordService iChargingBillingRecordService;

  @Operation(summary = "新增充电中订单-根据车牌号启动充电桩")
  @PostMapping("startCharging")
  public R<String> startCharging(String licensePlate, Integer chargerId) throws JsonProcessingException {
    iChargingBillingRecordService.startCharging(licensePlate, chargerId);
    return R.success("启动充电桩成功");
  }
  @Operation(summary = "新增待缴费订单-结束充电")
  @PostMapping("finishCharging")
  public R<String> finishCharging(Integer chargerId) throws JsonProcessingException {
    iChargingBillingRecordService.finishCharging(chargerId);
    return R.success("充电完毕，修改充电计费订单-待缴费");
  }

}
