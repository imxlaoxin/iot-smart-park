package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.config.properties.MqttProperties;
import com.imxiaoxin.iot.mapper.ExpDetectRecordMapper;
import com.imxiaoxin.iot.model.dto.ExpDetectDto;
import com.imxiaoxin.iot.model.dto.ExpDetectPageDto;
import com.imxiaoxin.iot.model.entity.ExpDetectRecord;
import com.imxiaoxin.iot.model.enums.ExpAlarmLevelEnum;
import com.imxiaoxin.iot.model.enums.ExpDetectProcessStatusEnum;
import com.imxiaoxin.iot.mqtt.send.MqttMessageSender;
import com.imxiaoxin.iot.service.DetectService;
import com.imxiaoxin.iot.service.FileService;
import com.imxiaoxin.iot.service.IExpDetectRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 火灾/紧急告警记录表 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-30
 */
@Slf4j
@Service
public class ExpDetectRecordServiceImpl extends ServiceImpl<ExpDetectRecordMapper, ExpDetectRecord> implements IExpDetectRecordService {

  @Autowired
  private FileService fileUploadService;

  @Autowired
  private MqttMessageSender mqttMessageSender;

  @Autowired
  private MqttProperties mqttProperties;

  @Autowired
  @Qualifier("taskExecutor") // 明确指定使用 AsyncConfig 中定义的线程池
  private TaskExecutor taskExecutor;

  @Autowired
  private DetectService detectService;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void processExpDetectRecord(ExpDetectDto expDetectDTO) throws JsonProcessingException {

    // 控制指令拦截器 (防历史记录污染)
    // 1. 如果是解除警报的指令，只负责通过 MQTT 下发给硬件和前端，绝对不存入数据库！
    if ("fire_clear".equals(expDetectDTO.getAlarm_type())) {
      // 伪造一个极简的 JSON 发给 MQTT。
      // 只要你的硬件和前端监听到 alarmType 是 "fire_clear"，就立马停止警报！
      String clearMsg = "{\"alarmType\": \"fire_clear\"}";
      mqttMessageSender.send(mqttProperties.getPubTopics().getExpDetect(), clearMsg);
      log.info("接收到 Python 边缘侧火灾消亡信号，已通过 MQTT 下发解除警报指令，不记入数据库！");
      return; // 直接 return，彻底切断后续的存库、传图、大模型分析逻辑！
    }

    // 2. 同步保存基础记录
    ExpDetectRecord expDetectRecord = BeanUtil.copyProperties(expDetectDTO, ExpDetectRecord.class);
    expDetectRecord.setAlarmLevel(ExpAlarmLevelEnum.CRITICAL);
    expDetectRecord.setProcessStatus(ExpDetectProcessStatusEnum.UNPROCESSED);
    save(expDetectRecord);

    // 3. 发送 MQTT 消息
//    mqttMessageSender.send(mqttProperties.getPubTopics().getExpDetect(), JSONUtil.toJsonStr(expDetectRecord));
    mqttMessageSender.send(mqttProperties.getPubTopics().getExpDetect(), objectMapper.writeValueAsString(expDetectRecord));

    // --- 关键修改点：在主线程读取文件字节 ---
    try {
      // 将文件内容提取到内存中，避免 MultipartFile 临时文件被删除
      byte[] originalBytes = expDetectDTO.getOriginalFile().getBytes();
      byte[] detectBytes = expDetectDTO.getDetectFile().getBytes();
      // 获取原始文件名和类型
      String originalName = expDetectDTO.getOriginalFile().getOriginalFilename();
      String detectName = expDetectDTO.getDetectFile().getOriginalFilename();
      String originalType = expDetectDTO.getOriginalFile().getContentType();
      String detectType = expDetectDTO.getDetectFile().getContentType();

      // 4. 异步处理上传（传入 byte[] 而不是 MultipartFile）
      CompletableFuture.runAsync(() -> {
        try {
          log.info("开始异步上传告警图片，线程名: {}", Thread.currentThread().getName());

          // 注意：你需要重载或者修改 FileUploadService.upload 方法，使其支持接收 byte[]
          String originalURL = fileUploadService.upload(originalBytes, "exp-detect-original", originalName, originalType);
          String detectURL = fileUploadService.upload(detectBytes, "exp-detect-detect", detectName, detectType);

          expDetectRecord.setOriginalUrl(originalURL);
          expDetectRecord.setDetectUrl(detectURL);
          updateById(expDetectRecord);

          log.info("异步上传成功，ID: {}", expDetectRecord.getId());
        } catch (Exception e) {
          log.error("异步上传失败，ID: {}", expDetectRecord.getId(), e);
        }
      }, taskExecutor);

    } catch (IOException e) {
      log.error("读取告警文件失败", e);
      throw new RuntimeException("文件预处理失败");
    }
  }

  /**
   * 火灾/紧急告警条件分页查询
   * @param expDetectPageDto
   * @return
   */
  @Override
  public PageR<ExpDetectRecord> getExpDetectRecordByPage(ExpDetectPageDto expDetectPageDto) {
    Page<ExpDetectRecord> p = expDetectPageDto.toMpPageDefaultSortByCreateTimeDesc();
    var query = lambdaQuery()
        // 设备ID查询
        .eq(expDetectPageDto.getCamId() != null, ExpDetectRecord::getCamId, expDetectPageDto.getCamId())
        // 告警类型查询
        .eq(expDetectPageDto.getAlarmType() != null, ExpDetectRecord::getAlarmType, expDetectPageDto.getAlarmType())
        // 告警级别查询
        .eq(StrUtil.isNotBlank(expDetectPageDto.getAlarmLevel()), ExpDetectRecord::getAlarmLevel, expDetectPageDto.getAlarmLevel())
        // 处理状态查询
        .eq(expDetectPageDto.getProcessStatus() != null, ExpDetectRecord::getProcessStatus, expDetectPageDto.getProcessStatus())
        // 置信度范围查询
        .ge(expDetectPageDto.getConfidence() != null, ExpDetectRecord::getConfidence, expDetectPageDto.getConfidence())
        .le(expDetectPageDto.getConfidence() != null, ExpDetectRecord::getConfidence, expDetectPageDto.getConfidence());
    // 时间范围查询
    List<LocalDateTime> timeList = expDetectPageDto.getTime();
    if (CollUtil.isNotEmpty(timeList) && timeList.get(0) != null) {
      query.ge(ExpDetectRecord::getCreateTime, timeList.get(0));
    }
    if (CollUtil.isNotEmpty(timeList) && timeList.get(1) != null) {
      query.le(ExpDetectRecord::getCreateTime, timeList.get(1));
    }
    return PageR.of(query.page(p), ExpDetectRecord.class);
  }

  /**
   * 修改告警处理状态对应记录
   * 若为误报或漏报，则收集图片，保存至本地，用于模型训练
   * @param expDetectRecord
   */
  @Override
  public void updateProcessStatus(ExpDetectRecord expDetectRecord) {
    // 1. 修改数据库记录
    updateById(expDetectRecord);
    // 2. 异步处理训练难例素材收集
    CompletableFuture.runAsync(() ->{
      if (expDetectRecord.getProcessStatus() == ExpDetectProcessStatusEnum.MISREPORTED || expDetectRecord.getProcessStatus() == ExpDetectProcessStatusEnum.LOSS) {
        try {
          // 生成唯一文件名
          String fileName = String.format("%d_%s_%s.jpg",
              expDetectRecord.getId(),
              expDetectRecord.getAlarmType(),
              UUID.randomUUID());
          detectService.saveErrorImageByUrlForTraining(expDetectRecord.getOriginalUrl(), fileName);
          log.info("异步处理训练难例素材收集成功，ID: {}", expDetectRecord.getId());
          int currentCount = detectService.getErrorImageCount();
          log.info("当前纠错素材库进度: {}/100", currentCount);
          if (currentCount >= 100) {
            log.warn("⚠️ 纠错素材已达 {} 张，建议立即启动 Python 端的增量训练 (train.py)！", currentCount);
          }
        } catch (Exception e) {
          log.error("异步处理训练难例素材收集失败", e);
        }
      }
    }, taskExecutor);
  }

}
