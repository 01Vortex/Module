package com.vortex.loginregister_new.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 安全审计日志工具类
 * 记录安全相关的重要操作
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Component
public class SecurityAuditLogger {

    /**
     * 记录登录成功
     */
    public void logLoginSuccess(String account, String clientIp, String userAgent) {
        log.info("[安全审计] 登录成功 - 账号: {}, IP: {}, User-Agent: {}", account, clientIp, userAgent);
    }

    /**
     * 记录登录失败
     */
    public void logLoginFailure(String account, String clientIp, String reason) {
        log.warn("[安全审计] 登录失败 - 账号: {}, IP: {}, 原因: {}", account, clientIp, reason);
    }

    /**
     * 记录密码重置
     */
    public void logPasswordReset(String account, String clientIp) {
        log.warn("[安全审计] 密码重置 - 账号: {}, IP: {}", account, clientIp);
    }

    /**
     * 记录权限拒绝
     */
    public void logAccessDenied(String account, String uri, String clientIp) {
        log.warn("[安全审计] 权限拒绝 - 账号: {}, URI: {}, IP: {}", account, uri, clientIp);
    }

    /**
     * 记录敏感操作
     */
    public void logSensitiveOperation(String account, String operation, String clientIp) {
        log.warn("[安全审计] 敏感操作 - 账号: {}, 操作: {}, IP: {}", account, operation, clientIp);
    }

    /**
     * 记录异常访问
     */
    public void logSuspiciousActivity(String account, String activity, String clientIp, HttpServletRequest request) {
        log.error("[安全审计] 可疑活动 - 账号: {}, 活动: {}, IP: {}, URI: {}, User-Agent: {}", 
                account, activity, clientIp, request.getRequestURI(), request.getHeader("User-Agent"));
    }

    /**
     * 记录Token相关操作
     */
    public void logTokenOperation(String account, String operation, String clientIp) {
        log.info("[安全审计] Token操作 - 账号: {}, 操作: {}, IP: {}", account, operation, clientIp);
    }
}
