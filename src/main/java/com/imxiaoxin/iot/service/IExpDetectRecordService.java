package com.imxiaoxin.iot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.model.dto.ExpDetectDto;
import com.imxiaoxin.iot.model.dto.ExpDetectPageDto;
import com.imxiaoxin.iot.model.entity.ExpDetectRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 火灾/紧急告警记录表 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-30
 */
public interface IExpDetectRecordService extends IService<ExpDetectRecord> {

  void processExpDetectRecord(ExpDetectDto expDetectDTO) throws JsonProcessingException;

  PageR<ExpDetectRecord> getExpDetectRecordByPage(ExpDetectPageDto expDetectPageDto);

  void updateProcessStatus(ExpDetectRecord expDetectRecord);
}
