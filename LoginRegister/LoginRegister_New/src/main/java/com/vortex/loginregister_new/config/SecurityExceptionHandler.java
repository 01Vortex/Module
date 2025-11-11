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
        
        // 记录403错误信息
        log.warn("403访问被拒绝 - URI: {}, 用户: {}, 权限: {}, 已认证: {}", 
                request.getRequestURI(), username, authorities, 
                authentication != null && authentication.isAuthenticated());
        
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

