package com.imxiaoxin.iot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imxiaoxin.iot.config.properties.MqttProperties;
import com.imxiaoxin.iot.exception.BizException;
import com.imxiaoxin.iot.mapper.ChargingBillingRecordMapper;
import com.imxiaoxin.iot.model.entity.BillingRule;
import com.imxiaoxin.iot.model.entity.ChargingBillingRecord;
import com.imxiaoxin.iot.model.entity.ParkBillingRecord;
import com.imxiaoxin.iot.model.enums.ChargerActionEnum;
import com.imxiaoxin.iot.model.enums.ChargingBillStatusEnum;
import com.imxiaoxin.iot.model.enums.ParkBillStatusEnum;
import com.imxiaoxin.iot.model.vo.ChargerActionVo;
import com.imxiaoxin.iot.mqtt.send.MqttMessageSender;
import com.imxiaoxin.iot.service.IChargingBillingRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * <p>
 * 充电计费订单记录表 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2026-03-28
 */
@Slf4j
@Service
public class ChargingBillingRecordServiceImpl extends ServiceImpl<ChargingBillingRecordMapper, ChargingBillingRecord> implements IChargingBillingRecordService {

  @Autowired
  private MqttProperties mqttProperties;

  @Autowired
  private MqttMessageSender mqttMessageSender;

  @Autowired
  private ObjectMapper objectMapper;


  /**
   * 启动充电桩 (H5 前端扫码触发)
   * @param licensePlate 车牌号
   * @param chargerId 充电桩标识
   */
  @Override
  @Transactional(rollbackFor = Exception.class) // 开启事务
  public void startCharging(String licensePlate, Integer chargerId) throws JsonProcessingException {
    // 1. 🛡业务校验：检查该充电桩是否正在被使用
    Long activeCount = lambdaQuery()
        .eq(ChargingBillingRecord::getChargerId, chargerId)
        .eq(ChargingBillingRecord::getOrderStatus, ChargingBillStatusEnum.CHARGING)
        .count();
    if (activeCount > 0) {
      throw new BizException("该充电桩正在使用中，请选择其他空闲充电桩！");
    }

    // 2. 业务校验：检查该车牌是否已经在充电中
    Long carChargingCount = lambdaQuery()
        .eq(ChargingBillingRecord::getLicensePlate, licensePlate)
        .eq(ChargingBillingRecord::getOrderStatus, ChargingBillStatusEnum.CHARGING)
        .count();
    if (carChargingCount > 0) {
      throw new BizException("您的车辆当前已有正在进行的充电订单，切勿重复启动！");
    }

    // 3. 关联当前的停车订单 (只有在场的车才能充电)
    ParkBillingRecord parkBillingRecord = Db.lambdaQuery(ParkBillingRecord.class)
        .eq(ParkBillingRecord::getLicensePlate, licensePlate)
        .ne(ParkBillingRecord::getOrderStatus, ParkBillStatusEnum.COMPLETED)
        .orderByDesc(ParkBillingRecord::getId)
        .last("limit 1")
        .one();

    if (parkBillingRecord == null) {
      throw new BizException("未查询到车牌 [" + licensePlate + "] 的入场记录，无法启动充电！");
    }

    // 4. 燃油车防占位拦截
    String plateColor = parkBillingRecord.getPlateColor();
    if (plateColor == null || "blue".equalsIgnoreCase(plateColor)) {
      log.warn("燃油车违规尝试充电被拦截！车牌: {}, 颜色: {}", licensePlate, plateColor);
      throw new BizException("抱歉，系统识别该车辆为非新能源牌照，此专属充电桩仅限绿牌汽车使用！");
    }

    // 5. 数据库落盘：生成处于“充电中”的订单记录
    ChargingBillingRecord record = new ChargingBillingRecord();
    record.setChargerId(chargerId);
    record.setLicensePlate(licensePlate);
    record.setParkBillingId(parkBillingRecord.getId());
    record.setStartTime(LocalDateTime.now());
    record.setOrderStatus(ChargingBillStatusEnum.CHARGING); // 状态：充电中
    record.setPowerConsumed(BigDecimal.ZERO); // 防御性编程：初始电量设为0
    save(record);
    log.info("车牌 [{}] 已成功启动 {} 号充电桩，订单生成完成", licensePlate, chargerId);

    // 6. ⚡ 硬件下发：通过 MQTT 发送启动指令给物理充电桩
    ChargerActionVo chargerActionVo = new ChargerActionVo();
    chargerActionVo.setAction(ChargerActionEnum.OPEN);
    chargerActionVo.setChargerId(chargerId);
    // 发送启动充电指令
//    mqttMessageSender.send(mqttProperties.getPubTopics().getChargerAction(), JSONUtil.toJsonStr(chargerActionVo));
    mqttMessageSender.send(mqttProperties.getPubTopics().getChargerAction(), objectMapper.writeValueAsString(chargerActionVo));

    log.info("车牌 [{}] 已成功启动 {} 号充电桩，启动充电桩指令下发完成", licensePlate, chargerId);
  }

  /**
   * 硬件上报充电结束 (专职负责结账算钱)
   * 触发时机：硬件传感器检测到拔枪/充满，通过 MQTT 传给 Java，Java 调用此方法
   * @param chargerId 充电桩编号 (硬件只认得自己是谁)
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void finishCharging(Integer chargerId) throws JsonProcessingException {
    // 1. 🔍 核心转变：通过【充电桩编号】去大海捞针，找出当前正在这个桩上充电的订单！
    ChargingBillingRecord activeOrder = lambdaQuery()
        .eq(ChargingBillingRecord::getChargerId, chargerId)
        .eq(ChargingBillingRecord::getOrderStatus, ChargingBillStatusEnum.CHARGING)
        .last("limit 1")
        .one();

    if (activeOrder == null) {
      log.warn("⚠️ 收到 {} 号桩的结束指令，但数据库中没有发现正在进行的充电订单！", chargerId);
      return;
    }

    // 2. 结束计费与状态流转
    activeOrder.setEndTime(LocalDateTime.now());

    // 3. 正确的计费逻辑：时间(分钟) * 单价(元/分钟)
    BillingRule chargingRule = Db.lambdaQuery(BillingRule.class).eq(BillingRule::getRuleCode, "CHARGING_FEE_PER_KWH").one();
    Long minutes = Duration.between(activeOrder.getStartTime(), activeOrder.getEndTime()).toMinutes();
    BigDecimal totalFee = BigDecimal.valueOf(minutes).multiply(chargingRule.getRuleValue());

    activeOrder.setTotalFee(totalFee);

    // 状态流转为“待缴费”，等待出口大屏一并结算！
    activeOrder.setOrderStatus(ChargingBillStatusEnum.AWAITING_PAYMENT);

    updateById(activeOrder);
    log.info("🔌 {} 号桩充电结束！产生充电费用: {} 元", chargerId, totalFee);
    // 4. ⚡ 硬件下发：通过 MQTT 告诉硬件，充电结束，请关闭充电桩！
    ChargerActionVo chargerActionVo = new ChargerActionVo();
    chargerActionVo.setAction(ChargerActionEnum.CLOSE);
    chargerActionVo.setChargerId(chargerId);
//    mqttMessageSender.send(mqttProperties.getPubTopics().getChargerAction(), JSONUtil.toJsonStr(chargerActionVo));
    mqttMessageSender.send(mqttProperties.getPubTopics().getChargerAction(), objectMapper.writeValueAsString(chargerActionVo));
    log.info("🔌 {} 号桩充电结束，关闭充电桩指令下发完成", chargerId);

  }


}

