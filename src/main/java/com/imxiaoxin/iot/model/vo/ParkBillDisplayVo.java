package com.imxiaoxin.iot.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "停车订单信息，用于设备端展示Vo")
public class ParkBillDisplayVo {

  @Schema(description = "车牌号")
  private String licensePlate;
  @Schema(description = "停车费用")
  private BigDecimal parkFee;
  @Schema(description = "充电费用")
  private BigDecimal chargeFee;
  @Schema(description = "总费用")
  private BigDecimal totalFee;

}
