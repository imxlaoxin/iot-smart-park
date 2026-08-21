package com.imxiaoxin.iot.model.vo;

import com.imxiaoxin.iot.model.enums.PoleActionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "抬杆vo")
public class PoleActionVo {

  @Schema(description = "动作")
   private PoleActionEnum action;
//  private Integer action;

}
