package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.config.properties.MqttProperties;
import com.imxiaoxin.iot.mapper.BizDetectRecordMapper;
import com.imxiaoxin.iot.model.dto.AddOrUpdateParkBillingRecordDto;
import com.imxiaoxin.iot.model.dto.BizDetectDto;
import com.imxiaoxin.iot.model.dto.BizDetectPageDto;
import com.imxiaoxin.iot.model.entity.BizDetectRecord;
import com.imxiaoxin.iot.model.enums.BizDetectStatusEnum;
import com.imxiaoxin.iot.model.enums.ParkBillStatusEnum;
import com.imxiaoxin.iot.mqtt.send.MqttMessageSender;
import com.imxiaoxin.iot.service.DetectService;
import com.imxiaoxin.iot.service.FileService;
import com.imxiaoxin.iot.service.IBizDetectRecordService;
import com.imxiaoxin.iot.service.IParkBillingRecordService;
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
 * 常规业务识别记录表 服务实现类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-30
 */
@Slf4j
@Service
public class BizDetectRecordServiceImpl extends ServiceImpl<BizDetectRecordMapper, BizDetectRecord> implements IBizDetectRecordService {

  @Autowired
  private FileService fileUploadService;

  @Autowired
  private MqttMessageSender mqttMessageSender;

  @Autowired
  private MqttProperties mqttProperties;

  @Autowired
  @Qualifier("taskExecutor")
  private TaskExecutor taskExecutor;

  @Autowired
  private DetectService detectService;

  @Autowired
  private IParkBillingRecordService parkBillingRecordService;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * 处理常规业务识别数据
   * @param bizDetectDTO
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void processBizDetect(BizDetectDto bizDetectDTO) throws JsonProcessingException {
    // 1. 保存常规业务识别数据
    BizDetectRecord bizDetectRecord = BeanUtil.copyProperties(bizDetectDTO, BizDetectRecord.class);
    bizDetectRecord.setDetectStatus(BizDetectStatusEnum.UNPROCESSED);
    save(bizDetectRecord);
    // 2. 触发计费流程
    if ("license-plate".equals(bizDetectRecord.getDetectType()) && StrUtil.isNotBlank(bizDetectRecord.getDetectResult())) {
      try {
        // 从 JSON 结果中解析出车牌号 (例如把 {"car-license-number": "粤A88888"} 提取出 "粤A88888")
        String licensePlate = JSONUtil.parseObj(bizDetectRecord.getDetectResult()).getStr("car-license-number");
        String licensePlateColor = JSONUtil.parseObj(bizDetectRecord.getDetectResult()).getStr("color");
        if (StrUtil.isNotBlank(licensePlate)) {
          AddOrUpdateParkBillingRecordDto billingDto = new AddOrUpdateParkBillingRecordDto();
          billingDto.setLicensePlate(licensePlate);
          billingDto.setLicensePlateColor(licensePlateColor);
          Long camId = bizDetectRecord.getCamId();
          if (camId != null && camId == 3L) {
            // 【入口】：触发入场计费流程
            log.info("[入口监控] 识别到车牌: {}，触发入场计费流程...", licensePlate);
            parkBillingRecordService.addOrUpdateOrder(billingDto);
          } else if (camId != null && camId == 2L) {
            // 【出口】：明确设置待缴费状态，触发合并结算流程
            log.info("[出口监控] 识别到车牌: {}，触发合并结算流程...", licensePlate);
            billingDto.setOrderStatus(ParkBillStatusEnum.AWAITING_PAYMENT);
            parkBillingRecordService.addOrUpdateOrder(billingDto);
          }
        }
      } catch (Exception e) {
        log.error("解析车牌或触发计费流程失败", e);
      }
    }
    // 3. 发送MQTT消息
//    mqttMessageSender.send(mqttProperties.getPubTopics().getBizDetect(), JSONUtil.toJsonStr(bizDetectRecord));
    log.info("bizDetectRecord: " + objectMapper.writeValueAsString(bizDetectRecord));
    // camId为入口(3)才发送
    if (bizDetectRecord.getCamId() == 3) {
      mqttMessageSender.send(mqttProperties.getPubTopics().getBizDetect(), objectMapper.writeValueAsString(bizDetectRecord));
    }
    // 4. 异步上传图片
    try {
      // 将文件内容提取到内存中，避免 MultipartFile 临时文件被删除
      byte[] originalBytes = bizDetectDTO.getOriginalFile().getBytes();
      byte[] detectBytes = bizDetectDTO.getDetectFile().getBytes();
      // 获取原始文件名和类型
      String originalName = bizDetectDTO.getOriginalFile().getOriginalFilename();
      String detectName = bizDetectDTO.getDetectFile().getOriginalFilename();
      String originalType = bizDetectDTO.getOriginalFile().getContentType();
      String detectType = bizDetectDTO.getDetectFile().getContentType();
      // 3. 异步处理上传（传入 byte[] 而不是 MultipartFile）
      CompletableFuture.runAsync(() -> {
        try {
          log.info("开始异步上传告警图片，线程名: {}", Thread.currentThread().getName());
          // 注意：你需要重载或者修改 FileUploadService.upload 方法，使其支持接收 byte[]
          String originalURL = fileUploadService.upload(originalBytes, "exp-detect-original", originalName, originalType);
          String detectURL = fileUploadService.upload(detectBytes, "exp-detect-detect", detectName, detectType);
          bizDetectRecord.setOriginalUrl(originalURL);
          bizDetectRecord.setDetectUrl(detectURL);
          updateById(bizDetectRecord);
          log.info("异步上传成功，ID: {}", bizDetectRecord.getId());
        } catch (Exception e) {
          log.error("异步上传失败，ID: {}", bizDetectRecord.getId(), e);
        }
      }, taskExecutor);
    } catch (IOException e) {
      log.error("读取业务识别文件失败", e);
      throw new RuntimeException("文件预处理失败");
    }
  }

  /**
   * 常规业务识别结果条件分页查询
   * @param bizDetectPageDTO
   * @return
   */
  @Override
  public PageR<BizDetectRecord> getBizDetectRecordByPage(BizDetectPageDto bizDetectPageDTO) {
    Page<BizDetectRecord> p = bizDetectPageDTO.toMpPageDefaultSortByCreateTimeDesc();
    var query = lambdaQuery()
        // 设备ID查询
        .eq(bizDetectPageDTO.getCamId() != null, BizDetectRecord::getCamId, bizDetectPageDTO.getCamId())
        // 识别类型查询
        .eq(StrUtil.isNotBlank(bizDetectPageDTO.getDetectType()), BizDetectRecord::getDetectType, bizDetectPageDTO.getDetectType())
        // 置信度范围查询
        .ge(bizDetectPageDTO.getMinConfidence() != null, BizDetectRecord::getConfidence, bizDetectPageDTO.getMinConfidence())
        .le(bizDetectPageDTO.getMaxConfidence() != null, BizDetectRecord::getConfidence, bizDetectPageDTO.getMaxConfidence())
        // 识别状态查询
        .eq(bizDetectPageDTO.getDetectStatus() != null, BizDetectRecord::getDetectStatus, bizDetectPageDTO.getDetectStatus());
    // 日期时间范围查询
    List<LocalDateTime> timeList = bizDetectPageDTO.getTime();
    if (CollUtil.isNotEmpty(timeList) && timeList.get(0) != null) {
      query.ge(BizDetectRecord::getCreateTime, timeList.get(0));
    }

    if (CollUtil.isNotEmpty(timeList) && timeList.get(1) != null) {
      query.le(BizDetectRecord::getCreateTime, timeList.get(1));
    }
    return PageR.of(query.page(p), BizDetectRecord.class);
  }

  /**
   * 修改常规业务识别状态记录接口
   * @param bizDetectRecord
   */
  @Override
  public void updateBizDetectStatus(BizDetectRecord bizDetectRecord) {
    // 1. 修改数据库记录
    updateById(bizDetectRecord);
    CompletableFuture.runAsync(() -> {
      // 2. 异步处理训练难例素材收集
      try {
        if (bizDetectRecord.getDetectStatus() == BizDetectStatusEnum.MISREPORT || bizDetectRecord.getDetectStatus() == BizDetectStatusEnum.LOSS) {
         // 生成唯一文件名
         String fileName = String.format("%d_%s_%s.jpg",
             bizDetectRecord.getId(),
             bizDetectRecord.getDetectType(),
             UUID.randomUUID());
         detectService.saveErrorImageByUrlForTraining(bizDetectRecord.getOriginalUrl(), fileName);
         log.info("纠错图片已同步至本地训练集: {}", fileName);
         // 检查进度并提醒
         int currentCount = detectService.getErrorImageCount();
         log.info("当前纠错素材库进度: {}/100", currentCount);
         if (currentCount >= 100) {
           log.warn("⚠️ 纠错素材已达 {} 张，建议立即启动 Python 端的增量训练 (train.py)！", currentCount);
         }
       }
      } catch (Exception e) {
        log.error("同步纠错图片失败，ID: {}", bizDetectRecord.getId(), e);
      }
    }, taskExecutor);
  }

}
