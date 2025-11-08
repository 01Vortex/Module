package com.vortex.loginregister_new.service.impl;

import com.vortex.loginregister_new.service.JwtBlacklistService;
import com.vortex.loginregister_new.service.RedisService;
import com.vortex.loginregister_new.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * JWT黑名单服务实现
 * 使用Redis存储已注销的token
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlacklistServiceImpl implements JwtBlacklistService {

    private final RedisService redisService;
    private final JwtUtil jwtUtil;
    
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    public void addToBlacklist(String token) {
        try {
            // 获取token的过期时间
            Date expiration = jwtUtil.getExpirationDateFromToken(token);
            if (expiration != null) {
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    // 将token添加到黑名单，TTL设置为token的剩余有效期
                    String key = BLACKLIST_PREFIX + token;
                    redisService.set(key, "1", ttl / 1000, java.util.concurrent.TimeUnit.SECONDS);
                    log.info("Token已添加到黑名单，TTL: {}秒", ttl / 1000);
                }
            }
        } catch (Exception e) {
            log.error("添加token到黑名单失败: ", e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            String value = redisService.get(key);
            return value != null;
        } catch (Exception e) {
            log.error("检查token黑名单失败: ", e);
            // 发生错误时，为了安全起见，认为token在黑名单中
            return true;
        }
    }

    @Override
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanExpiredEntries() {
        // Redis会自动过期，这里主要用于日志记录
        log.debug("清理过期的黑名单条目（Redis自动过期）");
    }
    
    private static final String USER_TOKEN_INVALID_PREFIX = "user:token:invalid:";
    
    @Override
    public void invalidateUserTokens(Long userId, int expirationDays) {
        try {
            // 在Redis中标记用户token失效，有效期设置为指定天数
            String key = USER_TOKEN_INVALID_PREFIX + userId;
            redisService.set(key, "1", expirationDays, java.util.concurrent.TimeUnit.DAYS);
            log.info("用户 {} 的所有token已标记为失效，有效期: {}天", userId, expirationDays);
        } catch (Exception e) {
            log.error("标记用户token失效失败: ", e);
        }
    }
    
    @Override
    public boolean isUserTokenInvalidated(Long userId) {
        try {
            String key = USER_TOKEN_INVALID_PREFIX + userId;
            String value = redisService.get(key);
            return value != null;
        } catch (Exception e) {
            log.error("检查用户token失效状态失败: ", e);
            // 发生错误时，为了安全起见，认为token已失效
            return true;
        }
    }
}
