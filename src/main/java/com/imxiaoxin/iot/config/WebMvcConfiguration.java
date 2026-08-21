package com.imxiaoxin.iot.config;

import com.imxiaoxin.iot.config.properties.CorProperties;
import com.imxiaoxin.iot.factory.StringToBaseEnumConverterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private CorProperties corProperties;

    /**
     * 配置跨域资源共享（CORS）的映射规则
     *
     * 该方法用于定义全局的CORS策略，以允许来自不同域的请求在浏览器中执行
     * 主要配置了哪些域可以访问服务、允许的HTTP方法、是否支持用户凭证、允许的请求头以及暴露的响应头
     *
     * @param registry CorsRegistry对象，用于注册CORS映射规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("CORS 配置生效");
        registry
            .addMapping("/**")
             .allowedOriginPatterns("*")
//            .allowedOrigins(corProperties.getAllowOrigins())
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
             .allowCredentials(true)
            .allowedHeaders("*")
            .exposedHeaders("*")
            .maxAge(3600);  // 预检请求缓存时间
    }

    @Autowired
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

    /**
     * 添加自定义的枚举转换器，用于将前端传递参数字符串转换为枚举类型
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
    }


}