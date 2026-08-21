package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.mapper.ParkEnvInfoMapper;
import com.imxiaoxin.iot.model.dto.ParkEnvInfoPageDto;
import com.imxiaoxin.iot.model.entity.ParkEnvInfo;
import com.imxiaoxin.iot.service.IParkEnvInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 停车场环境相关信息 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
@Service
public class ParkEnvInfoServiceImpl extends ServiceImpl<ParkEnvInfoMapper, ParkEnvInfo> implements IParkEnvInfoService {

  /**
   * 条件分页查询
   * @param parkEnvInfoPageDto
   * @return
   */
  @Override
  public PageR<ParkEnvInfo> getParkEnvInfoByPage(ParkEnvInfoPageDto parkEnvInfoPageDto) {
    Page<ParkEnvInfo> p = parkEnvInfoPageDto.toMpPageDefaultSortByCreateTimeDesc();
    var query = lambdaQuery()
        // 环境温度范围查询
        .ge(parkEnvInfoPageDto.getMinEnvTemp() != null, ParkEnvInfo::getTemperature, parkEnvInfoPageDto.getMinEnvTemp())
        .le(parkEnvInfoPageDto.getMaxEnvTemp() != null, ParkEnvInfo::getTemperature, parkEnvInfoPageDto.getMaxEnvTemp())
        // 环境湿度范围查询
        .ge(parkEnvInfoPageDto.getMinEnvHumidity() != null, ParkEnvInfo::getHumidity, parkEnvInfoPageDto.getMinEnvHumidity())
        .le(parkEnvInfoPageDto.getMaxEnvHumidity() != null, ParkEnvInfo::getHumidity, parkEnvInfoPageDto.getMaxEnvHumidity())
        // 环境光照强度范围查询
        .ge(parkEnvInfoPageDto.getMinEnvLight() != null, ParkEnvInfo::getLightIntensity, parkEnvInfoPageDto.getMinEnvLight())
        .le(parkEnvInfoPageDto.getMaxEnvLight() != null, ParkEnvInfo::getLightIntensity, parkEnvInfoPageDto.getMaxEnvLight())
        // 环境烟雾浓度范围查询
        .ge(parkEnvInfoPageDto.getMinEnvSmoke() != null, ParkEnvInfo::getSmokeDensity, parkEnvInfoPageDto.getMinEnvSmoke())
        .le(parkEnvInfoPageDto.getMaxEnvSmoke() != null, ParkEnvInfo::getSmokeDensity, parkEnvInfoPageDto.getMaxEnvSmoke())
        // 环境二氧化碳浓度范围查询
        .ge(parkEnvInfoPageDto.getMinEnvCo2() != null, ParkEnvInfo::getCarbonDioxideDensity, parkEnvInfoPageDto.getMinEnvCo2())
        .le(parkEnvInfoPageDto.getMaxEnvCo2() != null, ParkEnvInfo::getCarbonDioxideDensity, parkEnvInfoPageDto.getMaxEnvCo2());
    // 时间范围查询
    List<LocalDateTime> timeList = parkEnvInfoPageDto.getTime();
    if (CollUtil.isNotEmpty(timeList) && timeList.get(0) != null) {
      query.ge(ParkEnvInfo::getCreateTime, timeList.get(0));
    }
    if (CollUtil.isNotEmpty(timeList) && timeList.get(1) != null) {
      query.le(ParkEnvInfo::getCreateTime, timeList.get(1));
    }
    return PageR.of(query.page(p), ParkEnvInfo.class);
  }
}
