package com.imxiaoxin.iot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    private String username;
    private String password;
    private String url;
    private String subClientId;
    private String pubClientId;

    /**
     * 发布主题配置
     */
    private PubTopics pubTopics = new PubTopics();

    @Data
    public static class PubTopics {
        /**
         * 常规业务识别结果发布主题
         */
        private String bizDetect = "biz_detect";    // 默认值

        /**
         * 紧急告警识别结果发布主题
         */
        private String expDetect = "exp_detect";

        /**
         * 车位状态更新
         */
        private String parkingStatus = "/park/spot/status";

        /**
         * 操作充电桩
         */
        private String chargerAction = "/park/charger/action";

        /**
         * 出口订单显示
         */
        private String exitBillDisplay = "/park/billing/display";

        /**
         * 操作出口抬/降杆
         */
        private String poleAction = "/park/pole/action";

        /**
         * 环境阈值异常ai分析
         */
        private String envExpAiAdvice = "exp_env_advice";

    }
}