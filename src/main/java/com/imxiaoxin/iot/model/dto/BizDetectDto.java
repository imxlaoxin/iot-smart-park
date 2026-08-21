package com.imxiaoxin.iot.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "常规业务识别结果")
public class BizDetectDto {

    @Schema(description = "设备ID")
    private String cam_id;

    @Schema(description = "识别类型(例如: 'car', 'license-plate')")
    private String detect_type;

    @Schema(description = "结构化结果(例如: {car-license-number: \"湘E 845135\";car-license-color: \"blue\"})")
    private String detect_result;

    @Schema(description = "置信度")
    private String confidence;

    @Schema(description = "原始图片")
    private MultipartFile originalFile;

    @Schema(description = "标注图片")
    private MultipartFile detectFile;

}