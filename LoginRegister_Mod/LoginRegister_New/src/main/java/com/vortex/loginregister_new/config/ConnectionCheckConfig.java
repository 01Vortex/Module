package com.vortex.loginregister_new.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 连接检查配置
 * 在应用启动时检查数据库、Redis和邮件服务器连接
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class ConnectionCheckConfig {

    private final DataSource dataSource;
    private final org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory;
    private final JavaMailSender mailSender;


    /**
     * 应用启动后检查连接
     */
    @PostConstruct
    public void checkConnections() {
        boolean dbConnected = checkDatabase();
        boolean redisConnected = checkRedis();
        boolean mailConnected = checkMail();
        
        // 数据库或Redis连接失败时阻止启动
        if (!dbConnected || !redisConnected) {
            throw new RuntimeException("应用启动失败：数据库或Redis连接失败");
        }
        
        // 邮件服务器连接失败不阻止启动（仅记录警告）
        if (!mailConnected) {
            log.warn("邮件服务器连接失败，邮件功能将无法使用");
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
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
                mailSenderImpl.testConnection();
                log.info("✅ 邮件服务器连接成功");
                return true;
            } else {
                log.error("❌ 邮件服务器连接失败");
                return false;
            }
        } catch (Exception e) {
            log.error("❌ 邮件服务器连接失败");
            return false;
        }
    }
}
