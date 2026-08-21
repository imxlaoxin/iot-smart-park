package com.imxiaoxin.iot.controller;


import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.dto.ChargerExpInfoPageDto;
import com.imxiaoxin.iot.model.entity.ChargerExpInfo;
import com.imxiaoxin.iot.service.IChargerExpInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 充电桩异常信息表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "充电桩异常信息接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/charger-exp-info")
public class ChargerExpInfoController {

  @Autowired
  private IChargerExpInfoService chargerExpInfoService;

  /**
   * 充电桩异常信息条件分页查询接口
   * @param chargerExpInfoPageDto
   * @return
   */
  @Operation(summary = "充电桩异常信息条件分页查询接口")
  @GetMapping("/page")
  public R<PageR<ChargerExpInfo>> getChargerExpInfoByPage(ChargerExpInfoPageDto chargerExpInfoPageDto) {
    return R.success(chargerExpInfoService.getChargerExpInfoByPage(chargerExpInfoPageDto));
  }

  /**
   * 充电桩异常信息批量删除接口
   */
  @DeleteMapping("/delete/{ids}")
  @Operation(summary = "充电桩异常信息批量删除接口")
  public R<Boolean> delChargerExpInfo(@PathVariable("ids") List<Long> ids) {
    log.info("充电桩异常信息批量删除");
    chargerExpInfoService.removeByIds(ids);
    return R.success("充电桩异常信息批量删除成功");
  }

}
