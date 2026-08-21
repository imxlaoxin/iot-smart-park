package com.imxiaoxin.iot.agent.tools;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.imxiaoxin.iot.model.entity.ChargingPileInfo;
import com.imxiaoxin.iot.model.entity.ParkEnvInfo;
import com.imxiaoxin.iot.model.enums.ChargerPileStatusEnum;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author imxiaoxin
 *
 */
@Slf4j
@Component
public class ExpEnvTools {

  /**
   * 工具 1：获取停车场全局环境大盘数据 (用于基准线对比与突变分析)
   */
  @Tool(name="获取停车场全局环境大盘数据", value="获取停车场最近一次的全局环境常规快照。注意：由于常规数据是定时上报的，此数据代表告警发生前的【历史基准线】。用于判断告警是长期累积的全局异常，还是瞬间爆发的局部突变。")
  public String getLatestParkEnvStatus() {
    log.info("🤖 AI Agent 正在调用工具: 获取停车场全局环境快照...");
    // 查询最近的一条全局环境记录
    ParkEnvInfo latestEnv = Db.lambdaQuery(ParkEnvInfo.class)
        .orderByDesc(ParkEnvInfo::getCreateTime)
        .last("limit 1")
        .one();

    if (latestEnv != null) {
      String ret = String.format("最近一次全局环境快照（上报时间：%s）：温度 %s℃, 湿度 %s%%, 烟雾浓度 %sppm, CO2浓度 %sppm, 光照强度 %slux。",
          latestEnv.getCreateTime(),
          latestEnv.getTemperature(), latestEnv.getHumidity(),
          latestEnv.getSmokeDensity(), latestEnv.getCarbonDioxideDensity(),
          latestEnv.getLightIntensity());
      log.info("getLatestParkEnvStatus工具调用，返回: " + ret);
      return ret;
    }
    return "暂无全局环境数据。";
  }

  /**
   * 工具 2：检查充电桩内部硬件健康状态 (寻找火灾/高温源头)
   */
  @Tool("当发生高温、烟雾或CO2危险告警时，极有可能是充电桩或新能源车辆电池热失控引起。必须调用此工具检查所有正在运行的充电桩的内部温度、电压、电流情况，以寻找具体发热源。")
  public String checkChargingPileHealth() {
    log.info("AI Agent 正在调用工具: 扫描充电桩硬件最新健康状态...");

    // 1. 🔍 获取当前数据库中所有的充电桩 ID (利用 Group By 去重)
    List<ChargingPileInfo> distinctPiles = Db.lambdaQuery(ChargingPileInfo.class)
        .select(ChargingPileInfo::getChargerId)
        .groupBy(ChargingPileInfo::getChargerId)
        .list();

    if (distinctPiles.isEmpty()) {
      return "当前系统无充电桩数据。";
    }

    StringBuilder report = new StringBuilder("当前各活跃充电桩最新状态如下：");
    boolean hasWarning = false;
    boolean hasActive = false;

    // 2. 遍历每个充电桩，分别查出它们【最新】的一条记录
    for (ChargingPileInfo p : distinctPiles) {
      if (p.getChargerId() == null) continue;

      ChargingPileInfo latest = Db.lambdaQuery(ChargingPileInfo.class)
          .eq(ChargingPileInfo::getChargerId, p.getChargerId())
          .orderByDesc(ChargingPileInfo::getCreateTime)
          .last("limit 1") // 只要最新的一条！
          .one();

      // 3. 过滤：只向大模型汇报处于“启动/异常”状态的桩 (排除 STOP 停止状态)
      if (latest != null && latest.getStatus() != ChargerPileStatusEnum.STOP) {
        hasActive = true;
        report.append(String.format("[桩号%d: 内部温度%d℃]; ",
            latest.getChargerId(), latest.getTemperature()));

        // 如果温度大于等于 60 度，打上高危标记辅助 AI 判定
        if (latest.getTemperature() != null && latest.getTemperature() >= 60) {
          hasWarning = true;
        }
      }
    }

    if (!hasActive) {
      log.info("checkChargingPileHealth工具调用，返回: 当前所有充电桩均处于关闭/停止状态，暂无充电设备过载引起的电气火灾风险。");
      return "当前所有充电桩均处于关闭/停止状态，暂无充电设备过载引起的电气火灾风险。";
    }

    if (hasWarning) {
      log.info("checkChargingPileHealth工具调用，返回: 【高危提醒：存在充电桩内部温度过高，极大概率是火灾源头！】");
      report.append("【高危提醒：存在充电桩内部温度过高，极大概率是火灾源头！】");
    }
    log.info("checkChargingPileHealth工具调用，返回: " + report);
    return report.toString();
  }

}
