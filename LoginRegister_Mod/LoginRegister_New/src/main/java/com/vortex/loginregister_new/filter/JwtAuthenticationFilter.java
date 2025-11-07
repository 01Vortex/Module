package com.vortex.loginregister_new.filter;

import com.vortex.loginregister_new.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 从请求头中提取JWT token并验证，设置Spring Security认证上下文
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${jwt.header:Authorization}")
    private String tokenHeader;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // 判断是否是管理员接口（排除公开的管理员登录和忘记密码接口）
        boolean isAdminRequest = requestURI.contains("/admin/") 
                && !requestURI.contains("/auth/admin/login")
                && !requestURI.contains("/auth/admin/forgot-password");
        String token = null;

        try {
            token = extractTokenFromRequest(request);

            if (token != null) {
                // 验证token
                boolean isValid = jwtUtil.validateToken(token);
                
                if (!isValid) {
                    // Token无效，记录错误
                    log.warn("Token验证失败 - URI: {}", requestURI);
                }
            }

            if (token != null && jwtUtil.validateToken(token)) {
                // 验证是否为刷新token，刷新token不能用于普通API访问
                if (jwtUtil.isRefreshToken(token)) {
                    log.warn("尝试使用刷新token访问API: {}", request.getRequestURI());
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"无效的访问令牌\"}");
                    response.getWriter().flush();
                    return;
                }

                String account = jwtUtil.getAccountFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                if (account != null && userId != null) {
                    // 检查是否已有认证信息，如果有则清除（避免使用旧认证）
                    if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        SecurityContextHolder.clearContext();
                    }
                    
                    // 根据token中的角色设置权限
                    String authority = role != null ? role : "ROLE_USER";
                    
                    // 确保权限格式正确（Spring Security的hasRole会自动添加ROLE_前缀）
                    if (!authority.startsWith("ROLE_")) {
                        authority = "ROLE_" + authority;
                    }
                    
                    // 创建认证对象 - 使用带权限列表的构造函数会自动标记为已认证
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            account,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(authority))
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 验证权限设置
                    org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth == null) {
                        log.error("认证信息设置失败 - URI: {}", requestURI);
                    } else if (isAdminRequest && !auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        log.warn("管理员接口访问 - 缺少ADMIN权限，当前权限: {}, URI: {}", 
                                auth.getAuthorities(), requestURI);
                    }

                    // 将userId、account和role存储到请求属性中，方便后续使用
                    request.setAttribute("userId", userId);
                    request.setAttribute("account", account);
                    request.setAttribute("role", role);
                } else {
                    log.warn("JWT认证失败 - URI: {}, 账号: {}, 用户ID: {}", 
                            requestURI, account, userId);
                }
            } else {
                // 对于管理员接口，如果没有token或token无效，记录警告
                if (isAdminRequest) {
                    log.warn("管理员接口访问失败 - 未提供有效Token, URI: {}", requestURI);
                }
            }
        } catch (Exception e) {
            log.error("JWT认证过滤器异常 - URI: {}", requestURI, e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            String token = bearerToken.substring(tokenPrefix.length());
            // 去除前后空格，确保token格式正确
            token = token.trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
}

