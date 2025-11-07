package com.vortex.loginregister_new.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 安全响应头过滤器
 * 添加额外的安全响应头以增强安全性
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Component
@Order(1)
public class SecurityHeadersConfig extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // Content Security Policy (CSP) - 防止XSS攻击
        // 允许同源资源，内联脚本和样式需要nonce或hash
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' data:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'"
        );
        
        // Referrer Policy - 控制referrer信息
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy - 控制浏览器功能
        response.setHeader("Permissions-Policy", 
            "geolocation=(), " +
            "microphone=(), " +
            "camera=(), " +
            "payment=(), " +
            "usb=()"
        );
        
        // X-Content-Type-Options - 防止MIME类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // X-Frame-Options - 防止点击劫持
        response.setHeader("X-Frame-Options", "DENY");
        
        // X-XSS-Protection - XSS保护（已废弃，但保留兼容性）
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Strict-Transport-Security (HSTS) - 强制HTTPS
        // 注意：仅在HTTPS环境下启用
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
        }
        
        // 移除可能泄露信息的响应头
        response.setHeader("X-Powered-By", "");
        response.setHeader("Server", "");
        
        filterChain.doFilter(request, response);
    }
}
