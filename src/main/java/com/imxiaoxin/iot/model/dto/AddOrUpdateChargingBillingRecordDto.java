package com.imxiaoxin.iot.model.dto;

import com.imxiaoxin.iot.model.enums.ChargingBillStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "新增充电计费订单DTO")
public class AddOrUpdateChargingBillingRecordDto {

  @Schema(description = "充电桩标识")
  private Integer chargerId;

  @Schema(description = "车牌号")
  private String licensePlate;

  @Schema(description = "订单状态")
  private ChargingBillStatusEnum orderStatus;

}
