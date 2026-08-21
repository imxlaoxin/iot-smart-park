package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.ExpEnvLevelEnum;
import com.imxiaoxin.iot.model.enums.ExpEnvTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 环境异常警报信息表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("env_exp_info")
public class EnvExpInfo extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 告警类型 (0：温度；1：湿度；2：烟雾浓度；3.二氧化碳浓度；4.光照强度。)
     */
    private ExpEnvTypeEnum envType;

    /**
     * 告警等级 (0：预警；1：危险；2：危急)
     */
    private ExpEnvLevelEnum level;

    /**
     * 异常信息
     */
    private String expInfo;

    /**
     * ai分析
     */
    private String aiAnalysis;

}
