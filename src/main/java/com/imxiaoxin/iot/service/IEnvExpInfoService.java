package com.imxiaoxin.iot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.model.dto.EnvExpAlarmInfoPageDto;
import com.imxiaoxin.iot.model.entity.EnvExpInfo;

/**
 * <p>
 * 环境异常警报信息表 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
public interface IEnvExpInfoService extends IService<EnvExpInfo> {

  PageR<EnvExpInfo> getEnvExpAlarmInfoByPage(EnvExpAlarmInfoPageDto envExpAlarmInfoPageDto);
}
