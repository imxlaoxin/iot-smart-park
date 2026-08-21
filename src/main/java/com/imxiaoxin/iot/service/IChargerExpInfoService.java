package com.imxiaoxin.iot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.model.dto.ChargerExpInfoPageDto;
import com.imxiaoxin.iot.model.entity.ChargerExpInfo;

/**
 * <p>
 * 充电桩异常信息表 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
public interface IChargerExpInfoService extends IService<ChargerExpInfo> {

  PageR<ChargerExpInfo> getChargerExpInfoByPage(ChargerExpInfoPageDto chargerExpInfoPageDto);
}
