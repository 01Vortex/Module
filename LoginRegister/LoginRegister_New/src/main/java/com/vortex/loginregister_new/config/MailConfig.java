package com.vortex.loginregister_new.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮件配置类
 * 确保在配置文件中存在邮件配置时创建 JavaMailSender Bean
 * 这个配置类会覆盖 Spring Boot 的自动配置，确保邮件服务可用
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port:465}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.protocol:smtps}")
    private String protocol;

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        // 检查必要配置
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalStateException("邮件配置错误：spring.mail.host 不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("邮件配置错误：spring.mail.username 不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalStateException("邮件配置错误：spring.mail.password 不能为空");
        }
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol(protocol);

        // 设置邮件属性
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", "true");
        
        // SSL/TLS 配置
        if ("smtps".equals(protocol) || port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", host);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else if (port == 587) {
            // STARTTLS 配置
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        
        // 超时设置
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        
        // 调试模式（开发环境可以启用）
        // props.put("mail.debug", "true");
        
        log.info("✅ 邮件发送服务配置成功(host: {}, port: {}, username: {}, protocol: {})",
                host, port, username, protocol);
        
        return mailSender;
    }
}

