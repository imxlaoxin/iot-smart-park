package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imxiaoxin.iot.config.properties.MqttProperties;
import com.imxiaoxin.iot.exception.BizException;
import com.imxiaoxin.iot.mapper.ParkBillingRecordMapper;
import com.imxiaoxin.iot.model.dto.AddOrUpdateParkBillingRecordDto;
import com.imxiaoxin.iot.model.entity.BillingRule;
import com.imxiaoxin.iot.model.entity.ChargingBillingRecord;
import com.imxiaoxin.iot.model.entity.ParkBillingRecord;
import com.imxiaoxin.iot.model.enums.ChargingBillStatusEnum;
import com.imxiaoxin.iot.model.enums.ParkBillStatusEnum;
import com.imxiaoxin.iot.model.enums.PoleActionEnum;
import com.imxiaoxin.iot.model.vo.OrderInfoVo;
import com.imxiaoxin.iot.model.vo.ParkBillDisplayVo;
import com.imxiaoxin.iot.model.vo.PoleActionVo;
import com.imxiaoxin.iot.model.vo.TodayOrderSummaryVo;
import com.imxiaoxin.iot.mqtt.send.MqttMessageSender;
import com.imxiaoxin.iot.service.IParkBillingRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 停车计费订单记录表 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2026-03-28
 */
@Slf4j
@Service
public class ParkBillingRecordServiceImpl extends ServiceImpl<ParkBillingRecordMapper, ParkBillingRecord> implements IParkBillingRecordService {

  @Autowired
  private MqttMessageSender mqttMessageSender;

  @Autowired
  private MqttProperties mqttProperties;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * 新增或更新停车计费订单
   * @param dto
   */
  @Override
  public void addOrUpdateOrder(AddOrUpdateParkBillingRecordDto dto) {
    // 只查询当前处于“停车中/待缴费”状态的最新停车订单
    ParkBillingRecord parkBillingRecord = lambdaQuery()
        .eq(ParkBillingRecord::getLicensePlate, dto.getLicensePlate())  // 根据车牌号查询
        .ne(ParkBillingRecord::getOrderStatus, ParkBillStatusEnum.COMPLETED)  // 排除已完成的订单
        .orderByDesc(ParkBillingRecord::getId)  // 按ID降序排序
        .last("limit 1")
        .one();
    if (parkBillingRecord == null) {
      // 车辆进入停车场，新建订单
      ParkBillingRecord po = BeanUtil.copyProperties(dto, ParkBillingRecord.class);
      po.setPlateColor(dto.getLicensePlateColor());
      po.setEntryTime(LocalDateTime.now()); // 入场瞬间记录时间
      po.setOrderStatus(ParkBillStatusEnum.PARKING);
      save(po);
    } else {
      // 摄像头在出口抓拍到车辆，请求将订单转为“待缴费”状态进行结算
      if (dto.getOrderStatus() == ParkBillStatusEnum.AWAITING_PAYMENT) {
        // 车辆驶出，结算计费
        parkBillingRecord.setExitTime(LocalDateTime.now());

        // 计算停车总时长
        long minutes = java.time.Duration.between(parkBillingRecord.getEntryTime(), parkBillingRecord.getExitTime()).toMinutes();
        parkBillingRecord.setParkingDuration((int) minutes);

        // 获取停车费率并计算
        BillingRule parkingRule = Db.lambdaQuery(BillingRule.class).eq(BillingRule::getRuleCode, "PARKING_FEE_PER_HOUR").one();
        BigDecimal totalFee = BigDecimal.valueOf(minutes).multiply(parkingRule.getRuleValue());

        parkBillingRecord.setTotalFee(totalFee);

        // 状态顺理成章地更新为“待缴费”
        parkBillingRecord.setOrderStatus(ParkBillStatusEnum.AWAITING_PAYMENT);
        updateById(parkBillingRecord);

        // 1. 拿到刚才算好的停车费
        BigDecimal parkFee = parkBillingRecord.getTotalFee() != null ? parkBillingRecord.getTotalFee() : BigDecimal.ZERO;

        // 2. 跨表查询：有没有属于这辆车的所有待缴费充电订单? (可能充了多次)
        List<ChargingBillingRecord> chargingOrders = Db.lambdaQuery(ChargingBillingRecord.class)
            .eq(ChargingBillingRecord::getLicensePlate, dto.getLicensePlate())  // 根据车牌号查询
            .eq(ChargingBillingRecord::getOrderStatus, ChargingBillStatusEnum.AWAITING_PAYMENT) // 查询待缴费充电订单
            .list();

        // 使用 Stream 流将所有充电订单的费用累加
        BigDecimal chargeFee = chargingOrders.stream()
            .map(order -> order.getTotalFee() != null ? order.getTotalFee() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 组装发给前端大屏的 JSON 数据
        ParkBillDisplayVo parkBillDisplayVo = new ParkBillDisplayVo();
        parkBillDisplayVo.setLicensePlate(dto.getLicensePlate());
        parkBillDisplayVo.setParkFee(parkFee);
        parkBillDisplayVo.setChargeFee(chargeFee);
        parkBillDisplayVo.setTotalFee(parkFee.add(chargeFee));

        // 4. 发送 MQTT 消息给大屏
        String topic = mqttProperties.getPubTopics().getExitBillDisplay();
        mqttMessageSender.send(topic, JSONUtil.toJsonStr(parkBillDisplayVo));
        log.info("已向岗亭大屏推送合并账单: {}", JSONUtil.toJsonStr(parkBillDisplayVo));
      }
    }
  }

  /**
   * 查询订单信息
   * @param licensePlate
   * @return
   */
  @Override
  public OrderInfoVo getOrderInfo(String licensePlate) {
    // 1. 查询停车订单
    ParkBillingRecord parkBillingRecord = lambdaQuery()
        .eq(ParkBillingRecord::getLicensePlate, licensePlate)
        .ne(ParkBillingRecord::getOrderStatus, ParkBillStatusEnum.COMPLETED)
        .orderByDesc(ParkBillingRecord::getId)
        .last("limit 1")
        .one();

    // 如果没找到停车订单，直接返回空或抛出业务异常 (避免后续代码崩溃)
    if (parkBillingRecord == null) {
      throw new BizException("未找到车牌 [" + licensePlate + "] 的在场订单信息");
    }

    OrderInfoVo orderInfoVo = new OrderInfoVo();
    orderInfoVo.setLicensePlate(licensePlate);
    // 安全获取停车费用，如果是 null 则取 0
    BigDecimal parkFee = parkBillingRecord.getTotalFee() != null ? parkBillingRecord.getTotalFee() : BigDecimal.ZERO;
    orderInfoVo.setParkDuration(parkBillingRecord.getParkingDuration() != null ? parkBillingRecord.getParkingDuration() : 0);
    orderInfoVo.setParkFee(parkFee);

    // 停车订单的状态作为网页展示状态！
    orderInfoVo.setOrderStatus(parkBillingRecord.getOrderStatus());

    // 2. 跨表查询所有相关联的未完成的充电订单
    List<ChargingBillingRecord> chargingBillingRecords = Db.lambdaQuery(ChargingBillingRecord.class)
        .eq(ChargingBillingRecord::getLicensePlate, licensePlate)
        .eq(ChargingBillingRecord::getParkBillingId, parkBillingRecord.getId())
        .ne(ChargingBillingRecord::getOrderStatus, ChargingBillStatusEnum.COMPLETED)
        .orderByDesc(ChargingBillingRecord::getId)
        .list();

    // 3. 极其关键的充电数据判空防御逻辑
    BigDecimal chargeFee = BigDecimal.ZERO;
    int chargeDuration = 0;

    for (ChargingBillingRecord record : chargingBillingRecords) {
      chargeFee = chargeFee.add(record.getTotalFee() != null ? record.getTotalFee() : BigDecimal.ZERO);
      if (record.getStartTime() != null && record.getEndTime() != null) {
        chargeDuration += (int) Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
      }
    }

    orderInfoVo.setChargeDuration(chargeDuration);
    orderInfoVo.setChargeFee(chargeFee);

    // 4. 计算总金额
    orderInfoVo.setOrderTotalAmount(parkFee.add(chargeFee));

    return orderInfoVo;
  }

  /**
   * 查询今日订单总费用
   * @return
   */
  @Override
  public TodayOrderSummaryVo getTodayOrderInfo() {
    // 1. 获取今日时间的边界 (00:00:00 到 23:59:59)
    LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

    // 2. 查询今日所有的停车订单 (以入场时间为准)
    List<ParkBillingRecord> parkRecords = lambdaQuery()
        .ge(ParkBillingRecord::getEntryTime, startOfDay)
        .le(ParkBillingRecord::getEntryTime, endOfDay)
        .orderByDesc(ParkBillingRecord::getCreateTime)
        .list();

    // 如果今天一辆车都没来，直接返回空列表
    if (CollUtil.isEmpty(parkRecords)) {
      TodayOrderSummaryVo emptyVo = new TodayOrderSummaryVo();
      emptyVo.setOrderInfoVoList(new ArrayList<>());
      emptyVo.setTodayFee(BigDecimal.ZERO);
      return emptyVo;
    }

    // 3. 提取所有停车订单的 ID，一次性批量查出充电订单 (拒绝 for 循环查库)
    List<Long> parkIds = parkRecords.stream().map(ParkBillingRecord::getId).collect(Collectors.toList());

    List<ChargingBillingRecord> chargeRecords = Db.lambdaQuery(ChargingBillingRecord.class)
        .in(ChargingBillingRecord::getParkBillingId, parkIds)
        .list();

    // 将充电订单按 ParkBillingId 进行分组 (因为一辆车一次停车期间，可能充了多次电)
    Map<Long, List<ChargingBillingRecord>> chargeMap = chargeRecords.stream()
        .collect(Collectors.groupingBy(ChargingBillingRecord::getParkBillingId));

    // 4. 组装返回给前端的 VO 列表
    List<OrderInfoVo> resultList = new ArrayList<>();

    // 定义今日总费用累加器
    BigDecimal todayTotalRevenue = BigDecimal.ZERO;

    for (ParkBillingRecord park : parkRecords) {
      OrderInfoVo vo = new OrderInfoVo();

      // --- 组装停车信息 ---
      vo.setLicensePlate(park.getLicensePlate());
      BigDecimal parkFee = park.getTotalFee() != null ? park.getTotalFee() : BigDecimal.ZERO;
      vo.setParkDuration(park.getParkingDuration() != null ? park.getParkingDuration() : 0);
      vo.setParkFee(parkFee);
      vo.setOrderStatus(park.getOrderStatus());

      // --- 组装充电信息 ---
      BigDecimal totalChargeFee = BigDecimal.ZERO;
      int totalChargeDuration = 0;

      // 拿出与这条停车记录关联的所有充电记录
      List<ChargingBillingRecord> relatedChargeRecords = chargeMap.get(park.getId());
      if (CollUtil.isNotEmpty(relatedChargeRecords)) {
        for (ChargingBillingRecord charge : relatedChargeRecords) {
          totalChargeFee = totalChargeFee.add(charge.getTotalFee() != null ? charge.getTotalFee() : BigDecimal.ZERO);
          if (charge.getStartTime() != null && charge.getEndTime() != null) {
            totalChargeDuration += (int) Duration.between(charge.getStartTime(), charge.getEndTime()).toMinutes();
          }
        }
      }

      vo.setChargeDuration(totalChargeDuration);
      vo.setChargeFee(totalChargeFee);

      // --- 计算单笔订单总金额 ---
      BigDecimal orderTotal = parkFee.add(totalChargeFee);
      vo.setOrderTotalAmount(orderTotal);

      // 核心累加：将单笔总金额加到今日总费用中
      todayTotalRevenue = todayTotalRevenue.add(orderTotal);

      resultList.add(vo);
    }

    // 最终组装并返回包装对象
    TodayOrderSummaryVo todayOrderSummaryVo = new TodayOrderSummaryVo();
    todayOrderSummaryVo.setOrderInfoVoList(resultList);
    todayOrderSummaryVo.setTodayFee(todayTotalRevenue);
    return todayOrderSummaryVo;
  }

  /**
   * 支付订单
   * @param licensePlate
   * @return
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public String payOrder(String licensePlate) throws JsonProcessingException {
    BigDecimal totalPaid = BigDecimal.ZERO;

    // 1. 查找停车待缴费订单
    ParkBillingRecord parkOrder = lambdaQuery()
        .eq(ParkBillingRecord::getLicensePlate, licensePlate) // 根据车牌号查询
        .eq(ParkBillingRecord::getOrderStatus, ParkBillStatusEnum.AWAITING_PAYMENT) // 查询待缴费停车订单
        .one();

    if (parkOrder != null) {
      // 更新为已完成 (COMPLETED)
      parkOrder.setOrderStatus(ParkBillStatusEnum.COMPLETED);
      updateById(parkOrder);
      totalPaid = totalPaid.add(parkOrder.getTotalFee() != null ? parkOrder.getTotalFee() : BigDecimal.ZERO);
    }

    // 2. 查找绑定的所有充电待缴费订单
    List<ChargingBillingRecord> chargingOrders = Db.lambdaQuery(ChargingBillingRecord.class)
        .eq(ChargingBillingRecord::getLicensePlate, licensePlate) // 根据车牌号查询
        .eq(ChargingBillingRecord::getOrderStatus, ChargingBillStatusEnum.AWAITING_PAYMENT) // 查询待缴费充电订单
        .list();

    // 遍历所有充电订单，将其状态变更为 COMPLETED 并累加总钱数
    for (ChargingBillingRecord chargingOrder : chargingOrders) {
      chargingOrder.setOrderStatus(ChargingBillStatusEnum.COMPLETED);
      Db.lambdaUpdate(ChargingBillingRecord.class)
          .eq(ChargingBillingRecord::getId, chargingOrder.getId())
          .update(chargingOrder);
      totalPaid = totalPaid.add(chargingOrder.getTotalFee() != null ? chargingOrder.getTotalFee() : BigDecimal.ZERO);
    }

    if (parkOrder == null && chargingOrders.isEmpty()) {
      return "未找到车牌 [" + licensePlate + "] 的待缴费账单！";
    }

    // 3. 通过 MQTT 向出口的 ESP32 发送“开闸抬杆”指令！
    PoleActionVo poleActionVo = new PoleActionVo();
    poleActionVo.setAction(PoleActionEnum.UP);
//    mqttMessageSender.send(mqttProperties.getPubTopics().getPoleAction(), JSONUtil.toJsonStr(poleActionVo));
    mqttMessageSender.send(mqttProperties.getPubTopics().getPoleAction(), objectMapper.writeValueAsString(poleActionVo));

    return "支付成功！共计结清: " + totalPaid + " 元。闸机已开启，祝您一路平安！";
  }

}
