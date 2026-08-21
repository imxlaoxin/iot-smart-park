package com.imxiaoxin.iot.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "今日订单信息")
public class TodayOrderSummaryVo {
  private List<OrderInfoVo> orderInfoVoList;
  private BigDecimal todayFee;
}
