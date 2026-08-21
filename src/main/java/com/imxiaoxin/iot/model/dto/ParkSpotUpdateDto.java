package com.imxiaoxin.iot.model.dto;

import com.imxiaoxin.iot.model.enums.ParkSpotStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "车位状态更新参数DTO")
public class ParkSpotUpdateDto {

  @Schema(description = "车位编号(如: P1, P2)")
  private String spotCode;
  @Schema(description = "车位状态(0: 空闲, 1: 占用, 2: 故障)")
  private ParkSpotStatusEnum status;

}
