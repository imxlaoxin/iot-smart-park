package com.imxiaoxin.iot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    /**
     * 配置OpenAPI文档信息
     * 设置API文档的标题、描述、版本等基本信息
     * Knife4j会自动读取这些信息并生成美观的文档页面
     *
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("物联网API文档后台管理系统") // API文档标题，添加emoji图标
                .description("📚 物联网系统后端接口文档，提供完整的RESTful API服务\n\n" +
                    "✨ 主要功能模块：\n" +
                    "• 🧠 停车场环境信息接口管理\n" +
                    "• 📝 充电桩信息接口管理\n" +
                    "• 🎨 环境异常警报信息接口管理\n" +
                    "• 📊 充电桩异常信息接口管理\n" +
                    "• ⚠️ 异常检测记录接口管理\n"
                ) // API文档描述，使用markdown格式
                .version("v1.0.0"));
    }


    // 1. 停车场环境信息接口管理
    @Bean
    public GroupedOpenApi parkEnvInfoAPI() {

        return GroupedOpenApi.builder().group("停车场环境信息接口管理").
            pathsToMatch(
                "/iot/park-env-info/**"
            ).
            build();
    }

    // 2. 充电桩信息接口管理
    @Bean
    public GroupedOpenApi chargingPileInfoAPI() {

        return GroupedOpenApi.builder().group("充电桩信息接口管理").
            pathsToMatch(
                "/iot/charging-pile-info/**"
            ).
            build();
    }

    // 3. 环境异常警报信息接口管理
    @Bean
    public GroupedOpenApi envExpAlarmInfoAPI() {

        return GroupedOpenApi.builder().group("环境异常警报信息接口管理").
            pathsToMatch(
                "/iot/env-exp-alarm-info/**"
            ).
            build();
    }

    // 4. 充电桩异常信息接口管理
    @Bean
    public GroupedOpenApi chargerExpInfoAPI() {

        return GroupedOpenApi.builder().group("充电桩异常信息接口管理").
            pathsToMatch(
                "/iot/charger-exp-info/**"
            ).
            build();
    }

    // 5. 检测记录接口管理
    // 包括: 常规业务识别记录和火灾/紧急告警记录
    @Bean
    public GroupedOpenApi expDetectRecordAPI() {

        return GroupedOpenApi.builder().group("异常检测记录接口管理").
            pathsToMatch(
                "/iot/detect/**"
            ).
            build();
    }

    // 6. 计费规则配置接口管理
    @Bean
    public GroupedOpenApi billingRuleAPI() {

        return GroupedOpenApi.builder().group("计费规则配置接口管理").
            pathsToMatch(
                "/iot/billing-rule/**"
            ).
            build();
    }

    // 7. 计费管理接口管理
    @Bean
    public GroupedOpenApi billingManageAPI() {

        return GroupedOpenApi.builder().group("计费管理接口管理").
            pathsToMatch(
                "/iot/bill/**"
            ).
            build();
    }

    // 8. 停车场实时车位状态接口管理
    @Bean
    public GroupedOpenApi parkingLotRealTimeStatusAPI() {

        return GroupedOpenApi.builder().group("停车场实时车位状态接口管理").
            pathsToMatch(
                "/iot/park-spot-info/**"
            ).
            build();
    }

}