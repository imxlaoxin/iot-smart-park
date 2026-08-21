package com.imxiaoxin.iot.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.imxiaoxin.iot.model.dto.common.PageQueryDTO;
import com.imxiaoxin.iot.model.enums.ExpEnvLevelEnum;
import com.imxiaoxin.iot.model.enums.ExpEnvTypeEnum;
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
@Schema(description = "环境异常警报信息分页查询参数")
public class EnvExpAlarmInfoPageDto extends PageQueryDTO {

  @Schema(description = "报警类型(fire)")
  private ExpEnvTypeEnum envType;

  @Schema(description = "报警等级")
  private ExpEnvLevelEnum level;

  @Schema(description = "日期时间范围")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private List<LocalDateTime> time;

}
