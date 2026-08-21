package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imxiaoxin.iot.mapper.ParkSpotInfoMapper;
import com.imxiaoxin.iot.model.dto.ParkSpotUpdateDto;
import com.imxiaoxin.iot.model.entity.ParkSpotInfo;
import com.imxiaoxin.iot.model.enums.ParkSpotStatusEnum;
import com.imxiaoxin.iot.model.vo.statsParkSpotStatusVo;
import com.imxiaoxin.iot.service.IParkSpotInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 停车场实时车位状态表 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2026-03-28
 */
@Service
public class ParkSpotInfoServiceImpl extends ServiceImpl<ParkSpotInfoMapper, ParkSpotInfo> implements IParkSpotInfoService {

  /**
   * 更新车位状态
   * @param parkSpotUpdateDto
   */
  @Override
  public void updateParkSpotStatus(ParkSpotUpdateDto parkSpotUpdateDto) {
    ParkSpotInfo parkSpotInfo = BeanUtil.copyProperties(parkSpotUpdateDto, ParkSpotInfo.class);
    lambdaUpdate().eq(ParkSpotInfo::getSpotCode, parkSpotUpdateDto.getSpotCode()).update(parkSpotInfo);
  }

  /**
   * 统计车位使用状态
   * @return
   */
  @Override
  public statsParkSpotStatusVo countParkSpotStatus() {
    // 1. 一次性查出所有车位的当前状态 (避免 N+1 或复杂的 Join)
    // 根据 iot-smart-park.sql，按 spot_code 升序排一下，方便前端按顺序画车位图
    List<ParkSpotInfo> allSpots = lambdaQuery().orderByAsc(ParkSpotInfo::getSpotCode).list();

    // 2. 在 Java 内存中进行统计
    int totalCount = allSpots.size();

    // 依据数据库设计：status=0 代表空闲
    long freeCount = allSpots.stream()
        .filter(spot -> spot.getStatus() != null && spot.getStatus() == ParkSpotStatusEnum.IDLE)
        .count();

    // 3. 组装 VO 给前端
    statsParkSpotStatusVo vo = new statsParkSpotStatusVo();
    vo.setTotalParkingCount(totalCount);
    vo.setFreeParkingCount((int) freeCount);
    vo.setSpotList(allSpots);

    return vo;
  }
}
