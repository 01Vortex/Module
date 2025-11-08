package com.vortex.loginregister_new.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义访问拒绝处理器
 * 处理403 Forbidden错误
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Component
public class SecurityExceptionHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        String authorities = authentication != null ? authentication.getAuthorities().toString() : "none";
        String authHeader = request.getHeader("Authorization");
        
        // 详细记录403错误信息
        String tokenInfo = "无";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 尝试解析token获取信息（这里只是用于日志，不验证）
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    tokenInfo = payload.length() > 100 ? payload.substring(0, 100) + "..." : payload;
                }
            } catch (Exception e) {
                tokenInfo = "解析失败";
            }
        }
        
        log.error("========== 403访问被拒绝 ==========");
        log.error("URI: {}", request.getRequestURI());
        log.error("请求方法: {}", request.getMethod());
        log.error("用户: {}", username);
        log.error("权限: {}", authorities);
        log.error("是否已认证: {}", authentication != null && authentication.isAuthenticated());
        log.error("Authorization头存在: {}", authHeader != null);
        log.error("Token信息: {}", tokenInfo);
        
        // 检查SecurityContext中的认证信息
        org.springframework.security.core.context.SecurityContext securityContext = 
                org.springframework.security.core.context.SecurityContextHolder.getContext();
        if (securityContext != null && securityContext.getAuthentication() != null) {
            org.springframework.security.core.Authentication auth = securityContext.getAuthentication();
            log.error("SecurityContext中的认证信息:");
            log.error("  - 用户名: {}", auth.getName());
            log.error("  - 是否已认证: {}", auth.isAuthenticated());
            log.error("  - 权限列表: {}", auth.getAuthorities());
        } else {
            log.error("SecurityContext中没有认证信息！");
        }
        
        log.error("异常信息: {}", accessDeniedException.getMessage());
        log.error("===================================");
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            String errorMessage = "权限不足，需要管理员权限";
            if (authentication == null || !authentication.isAuthenticated()) {
                errorMessage = "未认证或认证已失效";
            } else if (authorities == null || !authorities.contains("ROLE_ADMIN")) {
                errorMessage = "权限不足，当前权限: " + authorities;
            }
            response.getWriter().write("{\"code\":403,\"message\":\"" + errorMessage + "\"}");
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("写入响应失败", e);
        }
    }
}

