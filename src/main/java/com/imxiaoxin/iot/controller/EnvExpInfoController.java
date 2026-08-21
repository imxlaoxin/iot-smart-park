package com.imxiaoxin.iot.controller;


import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.dto.EnvExpAlarmInfoPageDto;
import com.imxiaoxin.iot.model.entity.EnvExpInfo;
import com.imxiaoxin.iot.service.IEnvExpInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 环境异常警报信息表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "环境异常警报信息接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/env-exp-alarm-info")
public class EnvExpInfoController {

  @Autowired
  private IEnvExpInfoService expAlarmInfoService;

  /**
   *
   * @param envExpAlarmInfoPageDto
   * @return
   */
  @GetMapping("/page")
  @Operation(summary = "环境异常警报信息条件查询接口")
  public R<PageR<EnvExpInfo>> getEnvExpAlarmInfoByPage(EnvExpAlarmInfoPageDto envExpAlarmInfoPageDto) {
    return R.success(expAlarmInfoService.getEnvExpAlarmInfoByPage(envExpAlarmInfoPageDto));
  }

  /**
   * 环境异常警报信息批量删除接口
   */
  @DeleteMapping("/delete/{ids}")
  @Operation(summary = "环境异常警报信息批量删除接口")
  public R<String> deleteEnvExpAlarmInfo(@PathVariable List<Long> ids) {
    log.info("环境异常警报信息批量删除");

    expAlarmInfoService.removeByIds(ids);
    return R.success("环境异常警报信息批量删除成功");
  }

}
