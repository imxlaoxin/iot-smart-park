package com.imxiaoxin.iot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.model.dto.ChargerInfoPageDto;
import com.imxiaoxin.iot.model.entity.ChargingPileInfo;

/**
 * <p>
 * 充电桩相关信息 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
public interface IChargingPileInfoService extends IService<ChargingPileInfo> {

  PageR<ChargingPileInfo> getChargingPileInfoByPage(ChargerInfoPageDto chargerInfoPageDto);

  ChargingPileInfo getChargerInfoById(Long chargerId);
}
