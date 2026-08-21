package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.ChargingBillStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 充电计费订单记录表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("charging_billing_record")
public class ChargingBillingRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 充电桩标识(关联 charging_pile_info中的chargerId)
     */
    private Integer chargerId;

    /**
     * 关联的停车订单ID(用于出口合并结算)
     */
    private Long parkBillingId;

    /**
     * 车牌号(可选，用于账单追溯)
     */
    private String licensePlate;

    /**
     * 开始充电时间
     */
    private LocalDateTime startTime;

    /**
     * 结束充电时间
     */
    private LocalDateTime endTime;

    /**
     * 累计消耗电量(度/kWh)
     */
    private BigDecimal powerConsumed;

    /**
     * 充电总费用(元)
     */
    private BigDecimal totalFee;

    /**
     * 订单状态(0: 充电中, 1: 待缴费, 2: 已完成)
     */
    private ChargingBillStatusEnum orderStatus;

}
