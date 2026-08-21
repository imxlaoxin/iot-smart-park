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
 * 停车场环境相关信息
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("park_env_info")
@Schema(description = "停车场环境相关信息")
public class ParkEnvInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 停车场空气湿度
     */
    @Schema(description = "停车场空气湿度")
    private Integer humidity;

    /**
     * 停车场烟雾浓度
     */
    @Schema(description = "停车场烟雾浓度")
    private Integer smokeDensity;

    /**
     * 停车场空气温度
     */
    @Schema(description = "停车场空气温度")
    private String temperature;

    /**
     * 停车场二氧化碳浓度
     */
    @Schema(description = "停车场二氧化碳浓度")
    private Integer carbonDioxideDensity;

    /**
     * 停车场光照强度
     */
    @Schema(description = "停车场光照强度")
    private String lightIntensity;

}
