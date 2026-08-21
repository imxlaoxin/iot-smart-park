package com.imxiaoxin.iot.controller;


import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.dto.ParkEnvInfoPageDto;
import com.imxiaoxin.iot.model.entity.ParkEnvInfo;
import com.imxiaoxin.iot.service.IParkEnvInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 停车场环境相关信息 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "停车场环境信息接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/park-env-info")
public class ParkEnvInfoController {

  @Autowired
  private IParkEnvInfoService parkEnvInfoService;

  /**
   * 停车场环境信息条件分页查询接口
   * @param parkEnvInfoPageDto
   * @return
   */
  @GetMapping("/page")
  @Operation(summary = "停车场环境信息条件分页查询接口")
  public R<PageR<ParkEnvInfo>> getParkEnvInfoByPage(ParkEnvInfoPageDto parkEnvInfoPageDto) {
    return R.success(parkEnvInfoService.getParkEnvInfoByPage(parkEnvInfoPageDto));
  }

  /**
   * 停车场环境信息批量删除接口
   */
  @DeleteMapping("/delete/{ids}")
  @Operation(summary = "停车场环境信息批量删除接口")
  public R<String> deleteParkEnvInfo(@PathVariable List<Long> ids) {
    log.info("停车场环境信息批量删除");
    parkEnvInfoService.removeByIds(ids);
    return R.success("停车场环境信息批量删除成功");
  }

}
