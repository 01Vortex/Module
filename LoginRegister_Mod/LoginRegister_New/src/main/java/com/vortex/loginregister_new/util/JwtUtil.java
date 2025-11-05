package com.vortex.loginregister_new.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成、验证和刷新 JWT token
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌（Access Token）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT token
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新令牌（Refresh Token）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT refresh token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("从token中获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
            return null;
        } catch (Exception e) {
            log.warn("从token中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证token是否为刷新token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object type = claims.get("type");
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中解析Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查token是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 检查token是否即将过期（在指定时间内）
     *
     * @param token        token
     * @param millisBefore 提前多少毫秒认为即将过期
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, long millisBefore) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return true;
            }
            long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
            return timeUntilExpiration < millisBefore;
        } catch (Exception e) {
            return true;
        }
    }
}

