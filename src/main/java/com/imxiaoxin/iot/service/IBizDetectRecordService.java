package com.imxiaoxin.iot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.imxiaoxin.iot.common.PageR;
import com.imxiaoxin.iot.model.dto.BizDetectDto;
import com.imxiaoxin.iot.model.dto.BizDetectPageDto;
import com.imxiaoxin.iot.model.entity.BizDetectRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 常规业务识别记录表 服务类
 * </p>
 *
 * @author imxiaoxin
 * @since 2025-12-30
 */
public interface IBizDetectRecordService extends IService<BizDetectRecord> {

  void processBizDetect(BizDetectDto bizDetectDTO) throws JsonProcessingException;

  PageR<BizDetectRecord> getBizDetectRecordByPage(BizDetectPageDto bizDetectPageDTO);

  void updateBizDetectStatus(BizDetectRecord bizDetectRecord);
}
