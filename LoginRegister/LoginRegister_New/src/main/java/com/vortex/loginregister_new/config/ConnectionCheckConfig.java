package com.vortex.loginregister_new.config;

import com.vortex.loginregister_new.service.MinIOService;
import io.minio.MinioClient;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
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
    private final Environment environment;

    public ConnectionCheckConfig(
            DataSource dataSource,
            org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory,
            @Autowired(required = false) JavaMailSender mailSender,
            MinioClient minioClient,
            MinIOConfig minIOConfig,
            MinIOService minIOService,
            Environment environment) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.mailSender = mailSender;
        this.minioClient = minioClient;
        this.minIOConfig = minIOConfig;
        this.minIOService = minIOService;
        this.environment = environment;
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
        // 先从环境配置中读取邮件配置
        String mailHost = environment.getProperty("spring.mail.host");
        String mailUsername = environment.getProperty("spring.mail.username");
        String mailPassword = environment.getProperty("spring.mail.password");
        String mailPort = environment.getProperty("spring.mail.port", "465");
        
        // 检查配置文件中的邮件配置
        if (mailHost == null || mailHost.trim().isEmpty()) {
            log.error("❌ 邮件服务未配置：请在配置文件中配置 spring.mail.host");
            log.error("   当前配置 - host: {}, username: {}, password: {}", 
                    mailHost != null ? "已配置" : "未配置",
                    mailUsername != null && !mailUsername.trim().isEmpty() ? "已配置" : "未配置",
                    mailPassword != null && !mailPassword.trim().isEmpty() ? "已配置" : "未配置");
            return false;
        }
        
        if (mailUsername == null || mailUsername.trim().isEmpty()) {
            log.error("❌ 邮件配置不完整：spring.mail.username 为空");
            log.error("   当前配置 - host: {}, username: 未配置, password: {}", 
                    mailHost,
                    mailPassword != null && !mailPassword.trim().isEmpty() ? "已配置" : "未配置");
            return false;
        }
        
        if (mailPassword == null || mailPassword.trim().isEmpty()) {
            log.error("❌ 邮件配置不完整：spring.mail.password 为空");
            log.error("   当前配置 - host: {}, username: {}, password: 未配置", 
                    mailHost, mailUsername);
            return false;
        }
        
        // 如果 JavaMailSender bean 存在，使用它进行连接测试
        JavaMailSenderImpl mailSenderImpl = null;
        if (mailSender != null && mailSender instanceof JavaMailSenderImpl) {
            mailSenderImpl = (JavaMailSenderImpl) mailSender;
        } else if (mailSender == null) {
            // 如果 bean 不存在，但配置存在，手动创建一个临时的 JavaMailSender 来测试连接
            log.info("📧 检测到邮件配置，但 JavaMailSender bean 未创建，正在手动创建测试实例...");
            mailSenderImpl = new JavaMailSenderImpl();
            mailSenderImpl.setHost(mailHost);
            mailSenderImpl.setPort(Integer.parseInt(mailPort));
            mailSenderImpl.setUsername(mailUsername);
            mailSenderImpl.setPassword(mailPassword);
            
            // 设置邮件属性
            java.util.Properties props = mailSenderImpl.getJavaMailProperties();
            props.put("mail.transport.protocol", environment.getProperty("spring.mail.protocol", "smtps"));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", mailHost);
            
            // 如果配置了其他邮件属性，也设置上
            String protocol = environment.getProperty("spring.mail.protocol");
            if (protocol != null && protocol.equals("smtps")) {
                props.put("mail.smtp.ssl.enable", "true");
            }
        } else {
            log.error("❌ 邮件发送器类型不正确");
            return false;
        }
        
        // 测试邮件服务器连接
        try {
            mailSenderImpl.testConnection();
            log.info("✅ 邮件服务器连接成功 (host: {}, port: {}, username: {})", 
                    mailHost, mailPort, mailUsername);
            return true;
        } catch (Exception e) {
            log.error("❌ 邮件服务器连接失败: {}", e.getMessage());
            log.error("   配置信息 - host: {}, port: {}, username: {}", 
                    mailHost, mailPort, mailUsername);
            log.error("   错误详情: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("   原因: {}", e.getCause().getMessage());
            }
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
