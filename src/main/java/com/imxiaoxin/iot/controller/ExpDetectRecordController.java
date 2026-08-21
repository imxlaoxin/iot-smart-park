package com.imxiaoxin.iot.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.common.R;
import com.imxiaoxin.iot.model.dto.ExpDetectDto;
import com.imxiaoxin.iot.model.dto.ExpDetectPageDto;
import com.imxiaoxin.iot.model.entity.ExpDetectRecord;
import com.imxiaoxin.iot.service.IExpDetectRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 火灾/紧急告警记录表 前端控制器
 * </p>
 *
 * @author imxiaoxin
 */
@Tag(name = "火灾/紧急告警记录接口管理")
@Slf4j
@RestController
@RequestMapping("/iot/detect/exp-detect-record")
public class ExpDetectRecordController {

  @Autowired
  private IExpDetectRecordService expDetectRecordService;

  /**
   * 接收识别结果并处理火灾告警接口
   * @param expDetectDTO
   * @return
   */
  @PostMapping
  @Operation(summary = "接收识别结果并处理火灾告警接口")
  public R<String> processExpDetectRecord(ExpDetectDto expDetectDTO) throws JsonProcessingException {
    // Spring 会自动将 multipart 中的 data 字段和文件字段映射到 DTO 中
    log.warn("处理火灾/紧急告警数据");
    expDetectRecordService.processExpDetectRecord(expDetectDTO);
    return R.success("告警识别数据接收成功");
  }

  /**
   * 火灾/紧急告警条件分页查询接口
   * @param expDetectPageDto
   * @return
   */
  @GetMapping("/page")
  @Operation(summary = "火灾/紧急告警条件分页查询接口")
  public R<PageR<ExpDetectRecord>> getExpDetectRecordByPage(ExpDetectPageDto expDetectPageDto) {
    log.debug("条件分页查询火灾/紧急告警数据");
    return R.success(expDetectRecordService.getExpDetectRecordByPage(expDetectPageDto));
  }

  /**
   * 修改告警记录接口
   */
  @PutMapping("/update-process-status")
  @Operation(summary = "修改告警记录接口")
  public R<String> updateProcessStatus(@RequestBody ExpDetectRecord expDetectRecord) {
    log.info("修改告警处理状态");
    expDetectRecordService.updateProcessStatus(expDetectRecord);
    return R.success("修改告警记录成功");
  }

  /**
   * 删除告警记录接口
   */
  @DeleteMapping("/delete/{ids}")
  @Operation(summary = "删除告警记录接口")
  public R<String> deleteExpDetectRecord(@PathVariable List<Long> ids) {
    log.info("删除告警记录");
    expDetectRecordService.removeByIds(ids);
    return R.success("删除告警记录成功");
  }

}
