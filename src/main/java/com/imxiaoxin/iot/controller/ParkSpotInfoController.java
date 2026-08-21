package com.imxiaoxin.iot.controller;


import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.vo.statsParkSpotStatusVo;
import com.imxiaoxin.iot.service.IParkSpotInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 停车场实时车位状态表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "停车场实时车位状态接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/park-spot-info")
public class ParkSpotInfoController {

  @Autowired
  private IParkSpotInfoService parkSpotInfoService;

  @Operation(summary = "统计车位使用状态")
  @GetMapping("/statsParkSpotStatus")
  public R<statsParkSpotStatusVo> statsParkSpotStatus() {
    return R.success(parkSpotInfoService.countParkSpotStatus());
  }

}
