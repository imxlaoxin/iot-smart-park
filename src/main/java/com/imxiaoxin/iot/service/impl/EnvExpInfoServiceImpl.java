package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.mapper.EnvExpInfoMapper;
import com.imxiaoxin.iot.model.dto.EnvExpAlarmInfoPageDto;
import com.imxiaoxin.iot.model.entity.EnvExpInfo;
import com.imxiaoxin.iot.service.IEnvExpInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 环境异常警报信息表 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
@Service
public class EnvExpInfoServiceImpl extends ServiceImpl<EnvExpInfoMapper, EnvExpInfo> implements IEnvExpInfoService {

  /**
   * 条件分页查询
   * @param envExpAlarmInfoPageDto
   * @return
   */
  @Override
  public PageR<EnvExpInfo> getEnvExpAlarmInfoByPage(EnvExpAlarmInfoPageDto envExpAlarmInfoPageDto) {
    Page<EnvExpInfo> p = envExpAlarmInfoPageDto.toMpPageDefaultSortByCreateTimeDesc();
    var query = lambdaQuery()
        // 报警类型查询
        .eq(envExpAlarmInfoPageDto.getEnvType() != null, EnvExpInfo::getEnvType, envExpAlarmInfoPageDto.getEnvType())
        // 报警等级查询
        .eq(envExpAlarmInfoPageDto.getLevel() != null, EnvExpInfo::getLevel, envExpAlarmInfoPageDto.getLevel());
    // 时间范围查询
    List<LocalDateTime> timeList = envExpAlarmInfoPageDto.getTime();
    if (CollUtil.isNotEmpty(timeList) && timeList.get(0) != null) {
      query.ge(EnvExpInfo::getCreateTime, timeList.get(0));
    }
    if (CollUtil.isNotEmpty(timeList) && timeList.get(1) != null) {
      query.le(EnvExpInfo::getCreateTime, timeList.get(1));
    }
    return PageR.of(query.page(p), EnvExpInfo.class);
  }

}
