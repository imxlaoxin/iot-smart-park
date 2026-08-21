package com.imxiaoxin.iot.config;

import com.imxiaoxin.iot.config.properties.MinioProperties;
import com.imxiaoxin.iot.utils.MinIOFileStorageUtil;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("minio.endpoint")
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

  @Bean
  public MinioClient minioClient(MinioProperties minioProperties) {
    return MinioClient
        .builder()
        .endpoint(minioProperties.getEndpoint())
        .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
        .build();
  }

  @Bean
  public MinIOFileStorageUtil minIOFileStorageUtil(MinioClient minioClient, MinioProperties minioProperties) {
    return new MinIOFileStorageUtil(minioClient, minioProperties);
  }
  
}