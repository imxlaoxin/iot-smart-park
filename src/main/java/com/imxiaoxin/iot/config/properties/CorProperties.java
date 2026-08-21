package com.imxiaoxin.iot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author imxiaoxin
 *
 */
@Component
@Data
@ConfigurationProperties(prefix = "cors")
public class CorProperties {
  private String[] allowOrigins;
}
