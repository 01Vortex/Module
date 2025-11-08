package com.vortex.loginregister_new.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // 构建 MinIO 的域名（用于 CSP）
        String minioDomain = extractDomain(minioEndpoint);
        
        // Content Security Policy (CSP) - 防止XSS攻击
        // 允许同源资源，内联脚本和样式需要nonce或hash
        // 允许从 MinIO 服务器加载图片
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https: " + minioDomain + "; " +
            "font-src 'self' data:; " +
            "connect-src 'self' " + minioDomain + "; " +
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
    
    /**
     * 从URL中提取域名
     * 例如: http://localhost:9000 -> http://localhost:9000
     *      https://example.com -> https://example.com
     */
    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return "http://localhost:9000";
        }
        // 移除末尾的斜杠
        url = url.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
