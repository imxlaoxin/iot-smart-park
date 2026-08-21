package com.imxiaoxin.iot.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.imxiaoxin.iot.model.dto.common.PageQueryDTO;
import com.imxiaoxin.iot.model.enums.ExpAlarmLevelEnum;
import com.imxiaoxin.iot.model.enums.ExpDetectProcessStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author imxiaoxin
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "异常检测记录分页参数")
public class ExpDetectPageDto extends PageQueryDTO {

  @Schema(description = "设备ID")
  private Integer camId;

  @Schema(description = "告警类型")
  private ExpAlarmLevelEnum alarmType;

  @Schema(description = "告警级别")
  private String alarmLevel;

  @Schema(description = "置信度")
  private Float confidence;

  @Schema(description = "处理状态")
  private ExpDetectProcessStatusEnum processStatus;

  @Schema(description = "日期时间范围")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private List<LocalDateTime> time;

}
