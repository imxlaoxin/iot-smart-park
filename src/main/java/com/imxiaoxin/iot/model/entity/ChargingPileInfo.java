package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.ChargerPileChargeStatusEnum;
import com.imxiaoxin.iot.model.enums.ChargerPileStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 充电桩相关信息
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("charging_pile_info")
@Schema(description = "充电桩相关信息")
public class ChargingPileInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 充电桩标识
     */
    @Schema(description = "充电桩标识")
    @JsonProperty("charger_id")
    private Integer chargerId;

    /**
     * 充电桩内部温度
     */
    @Schema(description = "充电桩内部温度")
    private Integer temperature;

    /**
     * 充电桩内部空气湿度
     */
    @Schema(description = "充电桩内部空气湿度")
    private Integer humidity;

    /**
     * 充电桩电流
     */
    @Schema(description = "充电桩电流")
    private Double current;

    /**
     * 充电桩电压
     */
    @Schema(description = "充电桩电压")
    private Double voltage;

    /**
     * 充电桩状态(0: 停止; 1: 启动; 2: 异常)
     */
    @Schema(description = "充电桩状态(0: 停止; 1: 启动; 2: 异常)")
    private ChargerPileStatusEnum status;

    /**
     * 充电状态(0: 关闭; 1. 开启)
     */
    @Schema(description = "充电状态(0: 停止; 1. 开启)")
    @JsonProperty("charge_status")
    private ChargerPileChargeStatusEnum chargeStatus;

}
