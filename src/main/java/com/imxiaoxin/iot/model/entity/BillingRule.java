package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.BillingRuleOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 停车场计费规则配置表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("billing_rule")
public class BillingRule extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则代码(如: PARKING_FEE_PER_HOUR)
     */
    private String ruleCode;

    /**
     * 规则名称(如: 停车费单价/小时)
     */
    private String ruleName;

    /**
     * 规则数值(如: 5.00)
     */
    private BigDecimal ruleValue;

    /**
     * 单位(如: 元/小时, 元/度)
     */
    private String unit;

    /**
     * 规则描述及备注
     */
    private String description;

    /**
     * 状态(1:启用, 0:停用)
     */
    private BillingRuleOperation status;

}
