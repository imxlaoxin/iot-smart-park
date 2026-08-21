package com.imxiaoxin.iot.model.vo;

import com.imxiaoxin.iot.model.enums.ChargerActionEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "抬杆vo")
public class ChargerActionVo {

  @Schema(description = "动作")
   private ChargerActionEnum action;
//  private Integer action;

  @Schema(description = "充电桩ID")
  private Integer chargerId;

}
