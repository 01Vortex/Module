package com.vortex.loginregister_new.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 *
 * @author Vortex
 * @since 2024
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinIOConfig {

    /**
     * MinIO服务地址
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKey;

    /**
     * 访问密钥
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 是否使用HTTPS
     */
    private boolean secure = false;

    /**
     * 创建MinioClient Bean
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}

