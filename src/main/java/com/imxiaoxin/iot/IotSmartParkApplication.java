package com.imxiaoxin.iot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author imxiaoxin
 *
 */
@EnableAsync
@SpringBootApplication
@MapperScan("com.imxiaoxin.iot.mapper")
public class IotSmartParkApplication {
  public static void main(String[] args) {
    SpringApplication.run(IotSmartParkApplication.class, args);
  }

}
