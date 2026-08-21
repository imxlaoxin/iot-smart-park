package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.ParkSpotStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 停车场实时车位状态表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("park_spot_info")
public class ParkSpotInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 车位编号(如: P1, P2)
     */
    private String spotCode;

    /**
     * 车位状态(0: 空闲, 1: 占用, 2: 故障)
     */
    private ParkSpotStatusEnum status;

    /**
     * 绑定的硬件传感器ID/MAC地址
     */
    private String sensorId;

}

