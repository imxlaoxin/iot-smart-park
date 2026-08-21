package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.ExpAlarmLevelEnum;
import com.imxiaoxin.iot.model.enums.ExpDetectProcessStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 火灾/紧急告警记录表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exp_detect_record")
@Schema(description = "火灾/紧急告警记录表")

@SuppressWarnings("serial")

public class ExpDetectRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private Long camId;

    /**
     * 告警类型: fire
     */
    @Schema(description = "告警类型: fire")
    private String alarmType;

    /**
     * 告警级别: 0-预警, 1-危险, 2-危急
     */
    @Schema(description = "告警级别: 1-预警, 2-危险, 3-危机")
    private ExpAlarmLevelEnum alarmLevel;

    /**
     * MinIO原始图URL
     */
    @Schema(description = "MinIO原始图URL")
    private String originalUrl;

    /**
     * MinIO标注图URL
     */
    @Schema(description = "MinIO标注图URL")
    private String detectUrl;

    /**
     * 置信度
     */
    @Schema(description = "置信度")
    private Float confidence;

    /**
     * 处理状态: 0-未处理, 1-确认属实, 2-误报, 3-已解决
     */
    @Schema(description = "处理状态: 0-未处理, 1-确认属实, 2-误报, 3-漏报; 4-已解决")
    private ExpDetectProcessStatusEnum processStatus;

    /**
     * 处理意见/备注
     */
    @Schema(description = "处理意见/备注")
    private String handlerRemark;

}
