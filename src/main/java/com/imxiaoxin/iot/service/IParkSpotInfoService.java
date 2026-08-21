package com.imxiaoxin.iot.service;

import com.imxiaoxin.iot.model.dto.ParkSpotUpdateDto;
import com.imxiaoxin.iot.model.entity.ParkSpotInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.imxiaoxin.iot.model.vo.statsParkSpotStatusVo;

/**
 * <p>
 * 停车场实时车位状态表 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2026-03-28
 */
public interface IParkSpotInfoService extends IService<ParkSpotInfo> {

  void updateParkSpotStatus(ParkSpotUpdateDto parkSpotUpdateDto);

  statsParkSpotStatusVo countParkSpotStatus();
}
