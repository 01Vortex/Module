package com.vortex.loginregister_new.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 请求频率限制服务
 * 用于防止暴力破解和DoS攻击
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisService redisService;

    // Redis Key 前缀
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * 检查是否超过频率限制
     *
     * @param key          限制键（如IP地址或用户标识）
     * @param maxRequests  最大请求次数
     * @param timeWindow   时间窗口（秒）
     * @return true表示超过限制，false表示未超过
     */
    public boolean isRateLimited(String key, int maxRequests, int timeWindow) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        String countStr = redisService.get(redisKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= maxRequests) {
            log.warn("频率限制触发: key={}, count={}, max={}", key, count, maxRequests);
            return true;
        }

        // 增加计数
        count++;
        if (count == 1) {
            // 第一次请求，设置过期时间
            redisService.set(redisKey, String.valueOf(count), timeWindow, TimeUnit.SECONDS);
        } else {
            // 更新计数（保持原有过期时间）
            redisService.set(redisKey, String.valueOf(count), timeWindow, TimeUnit.SECONDS);
        }

        return false;
    }

    /**
     * 获取剩余请求次数
     */
    public int getRemainingRequests(String key, int maxRequests) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        String countStr = redisService.get(redisKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        return Math.max(0, maxRequests - count);
    }

    /**
     * 清除频率限制记录
     */
    public void clearRateLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisService.delete(redisKey);
    }
}

