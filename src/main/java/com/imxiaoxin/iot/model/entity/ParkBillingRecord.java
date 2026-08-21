package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.ParkBillStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 停车计费订单记录表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("park_billing_record")
public class ParkBillingRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 入场时间
     */
    private LocalDateTime entryTime;

    /**
     * 出场时间
     */
    private LocalDateTime exitTime;

    /**
     * 停车总时长(分钟)
     */
    private Integer parkingDuration;

    /**
     * 应收费用(元)
     */
    private BigDecimal totalFee;

    /**
     * 订单状态(0: 停车中, 1: 待缴费, 2: 已完成)
     */
    private ParkBillStatusEnum orderStatus;

}
