package com.imxiaoxin.iot.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.imxiaoxin.iot.model.entity.common.BaseEntity;
import com.imxiaoxin.iot.model.enums.BizDetectStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 常规业务识别记录表
 * </p>
 *
 * @author imxiaoxin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("biz_detect_record")
@Schema(description = "常规业务识别记录表")
public class BizDetectRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private Long camId;

    /**
     * 识别类型: car, license_plate, person
     */
    @Schema(description = "识别类型: car, license_plate, person")
    private String detectType;

    /**
     * 结构化结果(如: 车牌号"粤A88888"或人数"5")
     */
    @Schema(description = "结构化结果(如: 车牌号\"粤A88888\"或人数\"5\")")
    private String detectResult;

    /**
     * 置信度
     */
    @Schema(description = "置信度")
    private Float confidence;

    /**
     * 识别状态: 0: 正常; 1: 误报; 2: 漏报;
     */
    @Schema(description = "识别状态: 0: 未处理; 1: 正常; 2: 误报; 3: 漏报;")
     private BizDetectStatusEnum detectStatus;

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

}
