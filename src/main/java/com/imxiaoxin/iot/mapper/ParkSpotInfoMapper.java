package com.imxiaoxin.iot.mapper;

import com.imxiaoxin.iot.model.entity.ParkSpotInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imxiaoxin.iot.model.vo.statsParkSpotStatusVo;

/**
 * <p>
 * 停车场实时车位状态表 Mapper 接口
 * </p>
 *
 * @author imxiaoxin
 */
public interface ParkSpotInfoMapper extends BaseMapper<ParkSpotInfo> {

  statsParkSpotStatusVo countParkSpotStatus();
}
