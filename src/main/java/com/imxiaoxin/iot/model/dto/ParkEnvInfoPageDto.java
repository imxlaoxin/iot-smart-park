package com.imxiaoxin.iot.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.imxiaoxin.iot.model.dto.common.PageQueryDTO;
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
@Schema(description = "停车场环境信息分页查询参数")
public class ParkEnvInfoPageDto extends PageQueryDTO {

  @Schema(description = "最大环境温度")
  private Integer maxEnvTemp;

  @Schema(description = "最小环境温度")
  private Integer minEnvTemp;

  @Schema(description = "最大环境湿度")
  private Integer maxEnvHumidity;

  @Schema(description = "最小环境湿度")
  private Integer minEnvHumidity;

  @Schema(description = "最大环境光照强度")
  private Integer maxEnvLight;

  @Schema(description = "最小环境光照强度")
  private Integer minEnvLight;

  @Schema(description = "最大环境烟雾浓度")
  private Integer maxEnvSmoke;

  @Schema(description = "最小环境烟雾浓度")
  private Integer minEnvSmoke;

  @Schema(description = "最大环境二氧化碳浓度")
  private Integer maxEnvCo2;

  @Schema(description = "最小环境二氧化碳浓度")
  private Integer minEnvCo2;

  @Schema(description = "日期时间范围")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private List<LocalDateTime> time;

}
