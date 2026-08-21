package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.mapper.ChargerExpInfoMapper;
import com.imxiaoxin.iot.model.dto.ChargerExpInfoPageDto;
import com.imxiaoxin.iot.model.entity.ChargerExpInfo;
import com.imxiaoxin.iot.service.IChargerExpInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 充电桩异常信息表 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
@Service
public class ChargerExpInfoServiceImpl extends ServiceImpl<ChargerExpInfoMapper, ChargerExpInfo> implements IChargerExpInfoService {

  /**
   * 充电桩异常信息条件分页查询
   * @param chargerExpInfoPageDto
   * @return
   */
  @Override
  public PageR<ChargerExpInfo> getChargerExpInfoByPage(ChargerExpInfoPageDto chargerExpInfoPageDto) {
    Page<ChargerExpInfo> p = chargerExpInfoPageDto.toMpPageDefaultSortByCreateTimeDesc();
    var query = lambdaQuery()
        // 充电桩标识查询
        .eq(chargerExpInfoPageDto.getChargerId() != null, ChargerExpInfo::getChargerId, chargerExpInfoPageDto.getChargerId());
    // 时间日期范围查询
    List<LocalDateTime> timeList = chargerExpInfoPageDto.getTime();
    if (CollUtil.isNotEmpty(timeList) && timeList.get(0) != null) {
      query.ge(ChargerExpInfo::getCreateTime, timeList.get(0));
    }
    if (CollUtil.isNotEmpty(timeList) && timeList.get(1) != null) {
      query.le(ChargerExpInfo::getCreateTime, timeList.get(1));
    }
    return PageR.of(query.page(p), ChargerExpInfo.class);
  }

}
