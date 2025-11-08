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

    // 管理员token有效期：1天（86400000毫秒）
    private static final long ADMIN_EXPIRATION = 86400000L;
    // 用户token有效期：7天（604800000毫秒）
    private static final long USER_EXPIRATION = 604800000L;

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
     * @param account 账号
     * @return JWT token
     */
    public String generateAccessToken(Long userId, String account) {
        return generateAccessToken(userId, account, "ROLE_USER");
    }

    /**
     * 生成访问令牌（Access Token）- 带角色
     *
     * @param userId   用户ID
     * @param account 账号
     * @param role    角色
     * @return JWT token
     */
    public String generateAccessToken(Long userId, String account, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);
        claims.put("role", role);
        claims.put("type", "access");
        
        // 根据角色设置不同的token有效期
        // 管理员：1天，用户：7天
        long tokenExpiration = "ROLE_ADMIN".equals(role) ? ADMIN_EXPIRATION : USER_EXPIRATION;

        return Jwts.builder()
                .claims(claims)
                .subject(account)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新令牌（Refresh Token）
     *
     * @param userId   用户ID
     * @param account 账号
     * @return JWT refresh token
     */
    public String generateRefreshToken(Long userId, String account) {
        return generateRefreshToken(userId, account, "ROLE_USER");
    }

    /**
     * 生成刷新令牌（Refresh Token）- 带角色
     *
     * @param userId   用户ID
     * @param account 账号
     * @param role    角色
     * @return JWT refresh token
     */
    public String generateRefreshToken(Long userId, String account, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);
        claims.put("role", role);
        claims.put("type", "refresh");
        
        // 根据角色设置不同的refresh token有效期
        // 管理员：1天，用户：7天
        long tokenExpiration = "ROLE_ADMIN".equals(role) ? ADMIN_EXPIRATION : USER_EXPIRATION;

        return Jwts.builder()
                .claims(claims)
                .subject(account)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从token中获取账号
     */
    public String getAccountFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("从token中获取账号失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object role = claims.get("role");
            return role != null ? role.toString() : "ROLE_USER";
        } catch (Exception e) {
            log.warn("从token中获取角色失败: {}", e.getMessage());
            return "ROLE_USER";
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

