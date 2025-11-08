package com.vortex.loginregister_new.config;

import com.vortex.loginregister_new.service.MinIOService;
import io.minio.MinioClient;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;

/**
 * 连接检查配置
 * 在应用启动时检查数据库、Redis、邮件服务器和MinIO连接
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Component
@Order(1)
public class ConnectionCheckConfig {

    private final DataSource dataSource;
    private final org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory;
    private final JavaMailSender mailSender;
    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;
    private final MinIOService minIOService;

    public ConnectionCheckConfig(
            DataSource dataSource,
            org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory,
            @Autowired(required = false) JavaMailSender mailSender,
            MinioClient minioClient,
            MinIOConfig minIOConfig,
            MinIOService minIOService) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.mailSender = mailSender;
        this.minioClient = minioClient;
        this.minIOConfig = minIOConfig;
        this.minIOService = minIOService;
    }


    /**
     * 应用启动后检查连接
     */
    @PostConstruct
    public void checkConnections() {
        boolean dbConnected = checkDatabase();
        boolean redisConnected = checkRedis();
        boolean mailConnected = checkMail();
        boolean minioConnected = checkMinIO();
        
        // 数据库或Redis连接失败时阻止启动
        if (!dbConnected || !redisConnected) {
            throw new RuntimeException("应用启动失败：数据库或Redis连接失败");
        }
        
        // 邮件服务器连接失败时阻止启动
        if (!mailConnected) {
            throw new RuntimeException("应用启动失败：邮件服务器未配置或连接失败，请在配置文件中配置 spring.mail.username 和 spring.mail.password");
        }
        
        // MinIO连接失败不阻止启动（仅记录警告），但会尝试创建存储桶
        if (!minioConnected) {
            log.warn("MinIO连接失败，文件上传功能将无法使用");
        } else {
            // 如果MinIO连接成功，确保存储桶存在并设置公开读取策略
            try {
                minIOService.ensureBucketExists(minIOConfig.getBucketName());
                minIOService.setBucketPublicRead(minIOConfig.getBucketName());
                log.info("✅ MinIO存储桶 '{}' 检查/创建成功，已设置公开读取策略", minIOConfig.getBucketName());
            } catch (Exception e) {
                log.warn("MinIO存储桶检查/创建失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 检查数据库连接
     */
    private boolean checkDatabase() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    log.info("✅ 数据库连接成功");
                    return true;
                } else {
                    log.error("❌ 数据库连接失败");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("❌ 数据库连接失败");
            return false;
        }
    }

    /**
     * 检查Redis连接
     */
    private boolean checkRedis() {
        try {
            org.springframework.data.redis.connection.RedisConnection connection = null;
            try {
                connection = redisConnectionFactory.getConnection();
                String result = new String(connection.ping());
                if ("PONG".equals(result) || "pong".equalsIgnoreCase(result)) {
                    log.info("✅ Redis连接成功");
                    return true;
                } else {
                    log.error("❌ Redis连接失败");
                    return false;
                }
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e) {
                        // 忽略关闭异常
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Redis连接失败");
            return false;
        }
    }

    /**
     * 检查邮件服务器连接
     */
    private boolean checkMail() {
        // 如果邮件配置不存在或未配置，抛出异常阻止启动
        if (mailSender == null) {
            log.error("❌ 邮件服务未配置：请在配置文件中配置 spring.mail.host、spring.mail.username 和 spring.mail.password");
            return false;
        }
        
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
                // 检查配置是否完整
                if (mailSenderImpl.getUsername() == null || mailSenderImpl.getUsername().trim().isEmpty() ||
                    mailSenderImpl.getPassword() == null || mailSenderImpl.getPassword().trim().isEmpty()) {
                    log.error("❌ 邮件配置不完整：spring.mail.username 或 spring.mail.password 为空");
                    return false;
                }
                mailSenderImpl.testConnection();
                log.info("✅ 邮件服务器连接成功");
                return true;
            } else {
                log.error("❌ 邮件服务器连接检查失败：邮件发送器类型不正确");
                return false;
            }
        } catch (Exception e) {
            log.error("❌ 邮件服务器连接失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查MinIO连接
     */
    private boolean checkMinIO() {
        try {
            // 尝试检查MinIO服务是否可用（通过检查存储桶是否存在）
            minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(minIOConfig.getBucketName())
                            .build()
            );
            
            // 能够执行操作，说明MinIO服务可达
            log.info("✅ MinIO连接成功 (endpoint: {})", minIOConfig.getEndpoint());
            return true;
        } catch (ErrorResponseException e) {
            // ErrorResponseException通常表示MinIO服务可达，只是存储桶不存在或权限问题
            // 如果错误码是NoSuchBucket，说明服务可达
            if ("NoSuchBucket".equals(e.errorResponse().code())) {
                log.info("✅ MinIO连接成功 (endpoint: {}), 存储桶不存在将自动创建", minIOConfig.getEndpoint());
                return true;
            }
            // 其他错误可能是权限问题，但也说明服务可达
            log.warn("⚠️ MinIO连接检查警告 (endpoint: {}): {}", minIOConfig.getEndpoint(), e.getMessage());
            return true;
        } catch (InsufficientDataException | InternalException | InvalidKeyException | 
                 InvalidResponseException | IOException | NoSuchAlgorithmException | 
                 ServerException | XmlParserException e) {
            log.error("❌ MinIO连接失败 (endpoint: {}): {}", minIOConfig.getEndpoint(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ MinIO连接失败 (endpoint: {}): {}", minIOConfig.getEndpoint(), e.getMessage());
            return false;
        }
    }
}
