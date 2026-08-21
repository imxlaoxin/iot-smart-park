package com.imxiaoxin.iot.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.imxiaoxin.iot.model.dto.common.PageQueryDTO;
import com.imxiaoxin.iot.model.enums.BizDetectStatusEnum;
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
@Schema(description = "常规业务识别结果分页查询参数")
public class BizDetectPageDto extends PageQueryDTO {

  @Schema(description = "设备ID")
  private Integer camId;

  @Schema(description = "识别类型")
  private String detectType;

  @Schema(description = "最大置信度")
  private Float maxConfidence;

  @Schema(description = "最小置信度")
  private Float minConfidence;

  @Schema(description = "识别状态: 0: 未处理; 1: 正常; 2: 误报; 3: 漏报;")
  private BizDetectStatusEnum detectStatus;

  @Schema(description = "日期时间范围")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private List<LocalDateTime> time;

}
