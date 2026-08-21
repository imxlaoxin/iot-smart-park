package com.imxiaoxin.iot.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "异常检测记录表")
public class ExpDetectDto {

    @Schema(description = "设备ID")
    private String cam_id;

    @Schema(description = "告警类型: fire，fire_clear")
    private String alarm_type;

    @Schema(description = "原始图片")
    private MultipartFile originalFile;

    @Schema(description = "标注图片")
    private MultipartFile detectFile;

    @Schema(description = "置信度")
    private Float confidence;

}