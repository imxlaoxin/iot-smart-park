package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.constant.RedisConstant;
import com.imxiaoxin.iot.mapper.ChargingPileInfoMapper;
import com.imxiaoxin.iot.model.dto.ChargerInfoPageDto;
import com.imxiaoxin.iot.model.entity.ChargingPileInfo;
import com.imxiaoxin.iot.service.IChargingPileInfoService;
import com.imxiaoxin.iot.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 充电桩相关信息 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-26
 */
@Slf4j
@Service
public class ChargingPileInfoServiceImpl extends ServiceImpl<ChargingPileInfoMapper, ChargingPileInfo> implements IChargingPileInfoService {

  @Autowired
  private RedisUtils redisUtils;

  /**
   * 充电桩信息条件分页查询
   * @param chargerInfoPageDto
   * @return
   */
  @Override
  public PageR<ChargingPileInfo> getChargingPileInfoByPage(ChargerInfoPageDto chargerInfoPageDto) {
    Page<ChargingPileInfo> p = chargerInfoPageDto.toMpPageDefaultSortByCreateTimeDesc();
    var query = lambdaQuery()
        // 充电桩标识查询
        .eq(chargerInfoPageDto.getChargerId() != null, ChargingPileInfo::getChargerId, chargerInfoPageDto.getChargerId())
        // 充电桩内部温度范围查询
        .ge(chargerInfoPageDto.getMinTemp() != null, ChargingPileInfo::getTemperature, chargerInfoPageDto.getMinTemp())
        .le(chargerInfoPageDto.getMaxTemp() != null, ChargingPileInfo::getTemperature, chargerInfoPageDto.getMaxTemp())
        // 充电桩内部湿度范围查询
        .ge(chargerInfoPageDto.getMinHum() != null, ChargingPileInfo::getHumidity, chargerInfoPageDto.getMinHum())
        .le(chargerInfoPageDto.getMaxHum() != null, ChargingPileInfo::getHumidity, chargerInfoPageDto.getMaxHum())
        // 充电桩内部电流范围查询
        .ge(chargerInfoPageDto.getMinCurrent() != null, ChargingPileInfo::getCurrent, chargerInfoPageDto.getMinCurrent())
        .le(chargerInfoPageDto.getMaxCurrent() != null, ChargingPileInfo::getCurrent, chargerInfoPageDto.getMaxCurrent())
        // 充电桩内部电压范围查询
        .ge(chargerInfoPageDto.getMinVoltage() != null, ChargingPileInfo::getVoltage, chargerInfoPageDto.getMinVoltage())
        .le(chargerInfoPageDto.getMaxVoltage() != null, ChargingPileInfo::getVoltage, chargerInfoPageDto.getMaxVoltage())
        // 充电桩运行状态查询
        .eq(chargerInfoPageDto.getStatus() != null, ChargingPileInfo::getStatus, chargerInfoPageDto.getStatus())
        // 充电状态查询
        .eq(chargerInfoPageDto.getChargeStatus() != null, ChargingPileInfo::getChargeStatus, chargerInfoPageDto.getChargeStatus());

    // 时间范围查询
    List<LocalDateTime> timeList = chargerInfoPageDto.getTime();
    if (CollUtil.isNotEmpty(timeList) && timeList.get(0) != null) {
      query.ge(ChargingPileInfo::getCreateTime, timeList.get(0));
    }
    if (CollUtil.isNotEmpty(timeList) && timeList.get(1) != null) {
      query.le(ChargingPileInfo::getCreateTime, timeList.get(1));
    }
    return PageR.of(query.page(p), ChargingPileInfo.class);
  }

  /**
   * 根据id查询充电桩实时状态 (冷热分离架构)
   * @param chargerId 充电桩的业务ID (如 1, 2, 3)
   * @return
   */
  @Override
  public ChargingPileInfo getChargerInfoById(Long chargerId) {
    if (chargerId == null) {
      return null;
    }

    // 1. 定义 Redis 的 Key，例如: "iot:charger:info:1"
    String redisKey = RedisConstant.CHARGER_STATUS_KEY + chargerId;

    // 2. ⚡【热数据读取】优先尝试从 Redis 内存中获取
    Object cachedData = redisUtils.get(redisKey);
    if (cachedData != null) {
      log.info("命中 Redis 缓存，瞬间返回 {} 号充电桩实时状态", chargerId);
      // 将 Redis 里的 JSON 字符串反序列化为对象 (假设你存的是 JSON 字符串)
      return JSONUtil.toBean(cachedData.toString(), ChargingPileInfo.class);
    }

    // 3. 🐢【冷数据兜底】如果 Redis 没查到 (缓存未命中)，再去 MySQL 流水表里捞最新的一条
    log.warn("Redis 未命中，前往 MySQL 检索 {} 号充电桩最新快照...", chargerId);
    ChargingPileInfo latestInfo = lambdaQuery()
        .eq(ChargingPileInfo::getChargerId, chargerId)
        .orderByDesc(ChargingPileInfo::getCreateTime) // 根据创建时间降序
        .last("limit 1") // 只取最新的一条
        .one();

    // 4. 【缓存回写】如果 MySQL 查到了，把数据写回 Redis，方便下次极速读取！
    if (latestInfo != null) {
      // 设置到 Redis，并给一个 5 分钟 (300秒) 的过期时间作为安全兜底
      redisUtils.set(redisKey, JSONUtil.toJsonStr(latestInfo), 300);
    }

    return latestInfo;
  }
}
