package com.vortex.loginregister_new.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求大小限制配置
 * 防止DoS攻击和过大的请求
 *
 * @author Vortex
 * @since 2024
 */
@Configuration
public class RequestSizeLimitConfig {

    /**
     * 最大请求体大小（10MB）
     */
    private static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024;

    /**
     * 最大URL长度（2048字符）
     */
    private static final int MAX_URL_LENGTH = 2048;

    @Bean
    public FilterRegistrationBean<RequestSizeLimitFilter> requestSizeLimitFilter() {
        FilterRegistrationBean<RequestSizeLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestSizeLimitFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    /**
     * 请求大小限制过滤器
     */
    public static class RequestSizeLimitFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                       @NonNull HttpServletResponse response, 
                                       @NonNull FilterChain filterChain)
                throws ServletException, IOException {
            
            // 检查URL长度
            String requestURI = request.getRequestURI();
            String queryString = request.getQueryString();
            int urlLength = requestURI.length() + (queryString != null ? queryString.length() : 0);
            
            if (urlLength > MAX_URL_LENGTH) {
                response.setStatus(HttpServletResponse.SC_REQUEST_URI_TOO_LONG);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":414,\"message\":\"请求URL过长\"}");
                response.getWriter().flush();
                return;
            }
            
            // 检查Content-Length
            String contentLength = request.getHeader("Content-Length");
            if (contentLength != null) {
                try {
                    long length = Long.parseLong(contentLength);
                    if (length > MAX_REQUEST_SIZE) {
                        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":413,\"message\":\"请求体过大\"}");
                        response.getWriter().flush();
                        return;
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的Content-Length
                }
            }
            
            filterChain.doFilter(request, response);
        }
    }
}
