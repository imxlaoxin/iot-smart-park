package com.imxiaoxin.iot.model.dto;

import com.imxiaoxin.iot.model.enums.ParkBillStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "新增停车计费订单DTO")
public class AddOrUpdateParkBillingRecordDto {

  @Schema(description = "车牌号")
  private String licensePlate;
  @Schema(description = "车牌颜色")
  private String licensePlateColor;
  @Schema(description = "订单状态")
  private ParkBillStatusEnum orderStatus;

}
