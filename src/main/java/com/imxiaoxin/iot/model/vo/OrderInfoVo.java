package com.imxiaoxin.iot.model.vo;

import com.imxiaoxin.iot.model.enums.ParkBillStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "订单信息")
public class OrderInfoVo {
  @Schema(description = "车牌车")
  private String licensePlate;
  @Schema(description = "停车时长")
  private Integer parkDuration;
  @Schema(description = "停车计费")
  private BigDecimal parkFee;
  @Schema(description = "充电时长")
  private Integer chargeDuration;
  @Schema(description = "充电计费")
  private BigDecimal chargeFee;
  @Schema(description = "订单状态")
  private ParkBillStatusEnum orderStatus;
  @Schema(description = "订单总金额")
  private BigDecimal orderTotalAmount;

}
