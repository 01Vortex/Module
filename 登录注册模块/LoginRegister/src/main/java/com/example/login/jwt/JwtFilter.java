package com.example.login.jwt;

import com.example.login.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;

public class JwtFilter extends OncePerRequestFilter {
    /**
     * 从请求头中提取 token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // 提取 token 并移除所有空白字符
            String token = bearerToken.substring(7).replaceAll("\\s+", "");
            return token;
        }
        return null;
    }

    /**
     * 过滤器处理
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, java.io.IOException {
        String token = extractToken(request);
        // 有token
        if (token != null) {
            String username = JwtUtil.parseToken(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 将请求交给下一个过滤器或对应过滤器处理
        filterChain.doFilter(request, response);
    }


















}
