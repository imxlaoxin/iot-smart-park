package com.imxiaoxin.iot.model.vo;

import com.imxiaoxin.iot.model.entity.ParkSpotInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author imxiaoxin
 *
 */
@Data
@Schema(description = "停车场实时车位状态统计Vo")
public class statsParkSpotStatusVo {

  @Schema(description = "车位总数")
  private Integer totalParkingCount;

  @Schema(description = "空闲车位数")
  private Integer freeParkingCount;

  @Schema(description = "所有车位详细状态列表")
  private List<ParkSpotInfo> spotList;

}
