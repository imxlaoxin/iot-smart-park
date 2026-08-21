package com.imxiaoxin.iot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imxiaoxin.iot.agent.SmartParkAgent;
import com.imxiaoxin.iot.config.properties.MqttProperties;
import com.imxiaoxin.iot.constant.RedisConstant;
import com.imxiaoxin.iot.model.dto.ParkSpotUpdateDto;
import com.imxiaoxin.iot.model.entity.ChargingPileInfo;
import com.imxiaoxin.iot.model.entity.EnvExpInfo;
import com.imxiaoxin.iot.model.entity.ParkEnvInfo;
import com.imxiaoxin.iot.model.vo.statsParkSpotStatusVo;
import com.imxiaoxin.iot.mqtt.send.MqttMessageSender;
import com.imxiaoxin.iot.service.IChargingPileInfoService;
import com.imxiaoxin.iot.service.IEnvExpInfoService;
import com.imxiaoxin.iot.service.IParkEnvInfoService;
import com.imxiaoxin.iot.service.IParkSpotInfoService;
import com.imxiaoxin.iot.utils.RedisUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "MQTT Webhook 接口", description = "接收 EMQX 规则引擎的系统级事件推送")
@Slf4j
@RestController
@RequestMapping("/iot/webhook")
public class MqttWebhookController {

  @Autowired
  private IChargingPileInfoService chargingPileInfoService;

  @Autowired
  private IParkEnvInfoService parkEnvInfoService;

  @Autowired
  private IParkSpotInfoService iParkSpotInfoService;

  @Autowired
  private IEnvExpInfoService expAlarmInfoService;

  @Autowired
  private RedisUtils redisUtils;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MqttProperties mqttProperties;

  @Autowired
  private MqttMessageSender mqttMessageSender;

  @Autowired
  private SmartParkAgent smartParkAgent;

  @Operation(summary = "处理充电桩信息上报")
  @PostMapping("/charging_pile_info")
  public ResponseEntity<Void> handleChargingPileInfo(@RequestBody List<ChargingPileInfo> chargingPileInfoList) {
    if (chargingPileInfoList == null || chargingPileInfoList.isEmpty()) {
      return ResponseEntity.ok().build();
    }

    log.debug("🔌 收到 EMQX Webhook 充电桩上报，数量: {}", chargingPileInfoList.size());

    // 1. ⚡ 【热数据处理】遍历集合，写入 Redis 缓存
    for (ChargingPileInfo info : chargingPileInfoList) {
      if (info.getChargerId() != null) {
        String redisKey = RedisConstant.CHARGER_STATUS_KEY + info.getChargerId();
        try {
          redisUtils.set(redisKey, objectMapper.writeValueAsString(info));
        } catch (JsonProcessingException e) {
          log.error("写入 Redis 序列化失败", e);
        }
      }
    }

    // 2. 【冷数据处理】批量写入 MySQL 流水表
    chargingPileInfoService.saveBatch(chargingPileInfoList);

    return ResponseEntity.ok().build();
  }

  @Operation(summary = "处理停车场环境信息上报")
  @PostMapping("/park_env_info")
  public ResponseEntity<Void> handleParkEnvInfo(@RequestBody ParkEnvInfo parkEnvInfo) {
    log.debug("☁️ 收到 EMQX Webhook 环境信息上报: {}", parkEnvInfo);
    parkEnvInfoService.save(parkEnvInfo);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "处理车位状态更新上报")
  @PostMapping("/park_spot_info")
  public ResponseEntity<Void> handleParkSpotUpdate(@RequestBody ParkSpotUpdateDto parkSpotUpdateDto) {
    log.info("收到 EMQX Webhook 车位状态更新: {}", parkSpotUpdateDto);

    // 1. 更新数据库中的车位状态
    iParkSpotInfoService.updateParkSpotStatus(parkSpotUpdateDto);

    // 2. 重新统计车位大盘数据，并通过 MQTT (或者WebSocket) 广播给前端大屏
    try {
      statsParkSpotStatusVo statsParkSpotStatusVo = iParkSpotInfoService.countParkSpotStatus();
      mqttMessageSender.send(mqttProperties.getPubTopics().getParkingStatus(), objectMapper.writeValueAsString(statsParkSpotStatusVo));
      log.info("已将最新车位统计数据广播至前端");
    } catch (Exception e) {
      log.error("广播车位状态异常", e);
    }

    return ResponseEntity.ok().build();
  }

  @Operation(summary = "处理环境异常告警上报")
  @PostMapping("/env_exp")
  public ResponseEntity<Void> handleEnvExpInfo(@RequestBody EnvExpInfo envExpAlarmInfo) {
    log.info("收到 EMQX Webhook 环境异常告警: {}", envExpAlarmInfo);

    // 1. 先保存基础告警信息 (快速落盘)
    expAlarmInfoService.save(envExpAlarmInfo);

    // 2. 开启异步线程处理大模型分析
    CompletableFuture.runAsync(() -> {
      String coolDownKey = RedisConstant.EXP_ENV_COOLDOWN_KEY + envExpAlarmInfo.getEnvType().getDesc();

      // 触发冷却机制时的“无限 Loading”兜底降级处理
      if (redisUtils.hasKey(coolDownKey)) {
        log.info("AI 分析正在冷却中，下发历史缓存建议以解除前端 Loading...");
        try {
          // 从数据库中查找【该告警类型】最近一条有 AI 建议的历史记录
          EnvExpInfo lastInfo = expAlarmInfoService.lambdaQuery()
              .eq(EnvExpInfo::getEnvType, envExpAlarmInfo.getEnvType())
              .isNotNull(EnvExpInfo::getAiAnalysis)
              .ne(EnvExpInfo::getId, envExpAlarmInfo.getId()) // 排除当前刚插入的这条新记录
              .orderByDesc(EnvExpInfo::getCreateTime)
              .last("limit 1")
              .one();

          // 组装降级文案
          String fallbackAdvice = (lastInfo != null && lastInfo.getAiAnalysis() != null)
              ? "【系统持续告警】" + lastInfo.getAiAnalysis()
              : "【系统持续告警】传感器高频触发保护机制，请安保人员立刻前往现场处置！";

          // 将兜底建议更新到数据库，保持流水数据的完整性

          envExpAlarmInfo.setAiAnalysis(fallbackAdvice);
          expAlarmInfoService.updateById(envExpAlarmInfo);

          // 立即通过 MQTT 发送兜底消息给前端，瞬间解除 Loading 状态！
          String aiTopic = mqttProperties.getPubTopics().getEnvExpAiAdvice();
          mqttMessageSender.send(aiTopic, objectMapper.writeValueAsString(envExpAlarmInfo));

        } catch (Exception e) {
          log.error("下发冷却期兜底 AI 建议失败", e);
        }
        return; // 彻底结束本次任务，不再请求大模型！
      }
      // =========================================================

      // 如果没有被冷却拦截，正常走大模型请求逻辑并加锁
      redisUtils.set(coolDownKey, "LOCK", 60);

      try {
        // 3. 组装发给大模型的话
        String prompt = String.format(
            "当前停车场发生环境告警：告警类型【%s】，告警级别【%s】，传感器上报的异常原因：【%s】。请立刻给出处置建议。",
            envExpAlarmInfo.getEnvType().getDesc(),
            envExpAlarmInfo.getLevel().getDesc(),
            envExpAlarmInfo.getExpInfo()
        );

        // 4. 召唤大模型 Agent！
        log.info("正在请求大模型 Agent 分析...");
        String aiResult = smartParkAgent.chat(prompt);
        log.info("🤖 AI 专家回复: {}", aiResult);

        // 5. 更新数据库
        envExpAlarmInfo.setAiAnalysis(aiResult);
        expAlarmInfoService.updateById(envExpAlarmInfo);

        // 6. 通过新主题广播给前端
        String aiTopic = mqttProperties.getPubTopics().getEnvExpAiAdvice();
        mqttMessageSender.send(aiTopic, objectMapper.writeValueAsString(envExpAlarmInfo));

      } catch (Exception e) {
        log.error("大模型分析失败", e);
        redisUtils.delete(coolDownKey); // 发生异常记得释放锁
      }
    });

    return ResponseEntity.ok().build();
  }

}