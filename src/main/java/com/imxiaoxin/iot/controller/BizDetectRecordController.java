package com.imxiaoxin.iot.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.dto.BizDetectDto;
import com.imxiaoxin.iot.model.dto.BizDetectPageDto;
import com.imxiaoxin.iot.model.entity.BizDetectRecord;
import com.imxiaoxin.iot.service.DetectService;
import com.imxiaoxin.iot.service.IBizDetectRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 常规业务识别记录表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "常规业务识别记录接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/detect/biz-detect-record")
public class BizDetectRecordController {

  @Autowired
  private IBizDetectRecordService bizDetectRecordService;

  @Autowired
  private DetectService detectService;

  @GetMapping("/error-count")
  @Operation(summary = "获取纠错素材库当前张数接口")
  public R<Integer> getErrorCount() {
    return R.success(detectService.getErrorImageCount());
  }

  /**
   * 上传现场真实视频段接口
   */
  @PostMapping("/upload-video-case")
  @Operation(summary = "上传现场真实视频段接口")
  public R<String> uploadVideoCase(@RequestParam("file") MultipartFile file, @RequestParam("camId") Long camId) {
    if (file.isEmpty() || file.getOriginalFilename() == null) {
      return R.fail("视频文件不能为空");
    }
    try {
      // 1. 格式校验
      String originalFilename = file.getOriginalFilename().toLowerCase();
      if (!originalFilename.endsWith(".mp4") && !originalFilename.endsWith(".avi")) {
        return R.fail("仅支持 MP4 或 AVI 格式的视频");
      }
      // 2. 生成唯一的视频文件名
      String fileName = "hard-case_video_" + camId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".mp4";
      // 3. 将视频保存到服务器本地磁盘
      detectService.saveVideoFile(file, fileName);
      log.info("视频保存成功");
      return R.success("视频上传成功");
    } catch (Exception e) {
      log.error("视频捕获与保存失败: ", e);
      return R.fail("视频处理失败，请联系管理员");
    }
  }

  /**
   * 捕获难例-漏报(视而不见)图片接口
   * @param file
   * @param camId
   * @return
   */
  @PostMapping("/capture-hard-case")
  @Operation(summary = "捕获难例-漏报(视而不见)图片接口")
  public R<String> captureHardCase(@RequestParam("file") MultipartFile file, @RequestParam("camId") Long camId) {
    try {
      // 生成文件名：hard-case_data-loss_设备ID_时间戳.jpg
      String fileName = "hard-case_data-loss" + "_" + camId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
      // 复用下载保存逻辑，直接传入字节数组
      detectService.saveErrorImageByBytesForTraining(file.getBytes(), fileName);
      return R.success("难例图片捕获成功");
    } catch (IOException e) {
      return R.fail("捕获失败");
    }
  }

  /**
   * 接收识别结果并保存常规业务识别数据接口
   * @param bizDetectDTO
   * @return
   */
  @PostMapping
  @Operation(summary = "接收识别结果并保存常规业务识别数据接口")
  public R<String> processBizDetect(BizDetectDto bizDetectDTO) throws JsonProcessingException {
    // Spring 会自动将 multipart 中的 data 字段和文件字段映射到 DTO 中
    log.info("接收并保存常规业务识别数据");
    bizDetectRecordService.processBizDetect(bizDetectDTO);
    return R.success("业务识别数据接收成功");
  }

  /**
   * 常规业务识别结果条件分页查询接口
   * @param bizDetectPageDTO
   * @return
   */
  @GetMapping("/page")
  @Operation(summary = "常规业务识别结果条件分页查询接口")
  public R<PageR<BizDetectRecord>> getBizDetectRecordByPage(BizDetectPageDto bizDetectPageDTO) {
    log.debug("常规业务识别结果分页查询");
    return R.success(bizDetectRecordService.getBizDetectRecordByPage(bizDetectPageDTO));
  }

  /**
   * 修改常规业务识别状态记录接口
   */
  @PutMapping("/update-status")
  @Operation(summary = "修改常规业务识别状态记录接口")
  public R<String> updateBizDetectStatus(@RequestBody BizDetectRecord bizDetectRecord) {
    log.info("修改常规业务识别状态");
    bizDetectRecordService.updateBizDetectStatus(bizDetectRecord);
    return R.success("修改常规业务识别状态成功");
  }

  /**
   * 批量删除常规业务识别结果记录接口
   */
  @DeleteMapping("/delete/{ids}")
  @Operation(summary = "批量删除常规业务识别结果记录接口")
  public R<String> deleteBizDetectRecord(@PathVariable("ids") List<Long> ids) {
    log.info("批量删除常规业务识别结果记录");
    bizDetectRecordService.removeBatchByIds(ids);
    return R.success("批量删除常规业务识别结果记录成功");
  }

}
