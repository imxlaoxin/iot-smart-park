package com.imxiaoxin.iot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.model.dto.ParkEnvInfoPageDto;
import com.imxiaoxin.iot.model.entity.ParkEnvInfo;

/**
 * <p>
 * 停车场环境相关信息 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
public interface IParkEnvInfoService extends IService<ParkEnvInfo> {

  PageR<ParkEnvInfo> getParkEnvInfoByPage(ParkEnvInfoPageDto parkEnvInfoPageDto);
}
