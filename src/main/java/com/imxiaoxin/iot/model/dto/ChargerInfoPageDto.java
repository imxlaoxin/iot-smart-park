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
@Schema(description = "充电桩信息分页查询参数")
public class ChargerInfoPageDto extends PageQueryDTO {

  @Schema(description = "充电桩标识")
  private String chargerId;

  @Schema(description = "充电桩内部最大温度")
  private Integer maxTemp;

  @Schema(description = "充电桩内部最小温度")
  private Integer minTemp;

  @Schema(description = "充电桩内部最大湿度")
  private Integer maxHum;
  @Schema(description = "充电桩内部最小湿度")
  private Integer minHum;

  @Schema(description = "充电桩内部最大电流")
  private Integer maxCurrent;

  @Schema(description = "充电桩内部最小电流")
  private Integer minCurrent;

  @Schema(description = "充电桩内部最大电压")
  private Integer maxVoltage;

  @Schema(description = "充电桩内部最小电压")
  private Integer minVoltage;

  @Schema(description = "充电桩运行状态")
  private Integer status;

  @Schema(description = "充电状态")
  private Integer chargeStatus;

  @Schema(description = "日期时间范围")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private List<LocalDateTime> time;

}
