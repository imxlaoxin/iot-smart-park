package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 充电桩异常信息表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("charger_exp_info")
@Schema(description = "充电桩异常信息表")
public class ChargerExpInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 充电桩标识
     */
    @Schema(description = "充电桩标识")
    private Integer chargerId;

    /**
     * 异常信息
     */
    @Schema(description = "异常信息")
    private String expInfo;

}
