package com.imxiaoxin.iot.controller;


import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.dto.ChargerInfoPageDto;
import com.imxiaoxin.iot.model.entity.ChargingPileInfo;
import com.imxiaoxin.iot.service.IChargingPileInfoService;
import com.imxiaoxin.iot.utils.RedisUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 充电桩相关信息 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "充电桩信息接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/charging-pile-info")
public class ChargingPileInfoController {

  @Autowired
  private IChargingPileInfoService chargingPileInfoService;

  @Autowired
  private RedisUtils redisUtils;

  /**
   * 充电桩信息条件分页查询接口
   * @param chargerInfoPageDto
   * @return
   */
  @GetMapping("/page")
  @Operation(summary = "充电桩信息条件分页查询接口")
  public R<PageR<ChargingPileInfo>> getChargingPileInfoByPage(ChargerInfoPageDto chargerInfoPageDto) {
    return R.success(chargingPileInfoService.getChargingPileInfoByPage(chargerInfoPageDto));
  }

  /**
   * 充电桩信息批量删除接口
   */
  @DeleteMapping("/delete/{ids}")
  @Operation(summary = "充电桩信息批量删除接口")
  public R<String> delChargingPileInfo(@PathVariable List<Long> ids) {
    log.info("充电桩信息批量删除");
    chargingPileInfoService.removeByIds(ids);
    return R.success("充电桩信息批量删除成功");
  }

  /**
   * 根据id查询充电桩信息
   */
  @GetMapping("/getChargerInfo/{chargerId}")
  @Operation(summary = "根据id查询充电桩状态")
  public R<ChargingPileInfo> getChargerPileInfoById(@PathVariable Long chargerId) {
    return R.success(chargingPileInfoService.getChargerInfoById(chargerId));
  }

}
