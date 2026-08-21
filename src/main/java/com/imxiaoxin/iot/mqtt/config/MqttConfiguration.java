package com.imxiaoxin.iot.mqtt.config;

import com.imxiaoxin.iot.config.properties.MqttProperties;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfiguration {

    @Bean
    public MqttPahoClientFactory mqttClientFactory(MqttProperties mqttProperties){

        // 创建客户端工厂
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        // 创建MqttConnectOptions对象
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        options.setServerURIs(new String[]{mqttProperties.getUrl()});
        factory.setConnectionOptions(options);

        // 返回
        return factory;
    }

}