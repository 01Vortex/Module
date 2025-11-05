package com.vortex.loginregister_new.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务
 * 用于记录登录失败次数，防止暴力破解
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisService redisService;

    // Redis Key 前缀
    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final String ACCOUNT_LOCKED_PREFIX = "account_locked:";

    // 最大尝试次数
    private static final int MAX_ATTEMPT = 5;
    // 锁定时间（分钟）
    private static final int LOCK_TIME_MINUTES = 15;
    // 尝试记录过期时间（分钟）
    private static final int ATTEMPT_EXPIRE_MINUTES = 30;

    /**
     * 记录登录成功，清除失败记录
     */
    public void loginSucceeded(String identifier) {
        String key = LOGIN_ATTEMPT_PREFIX + identifier.toLowerCase();
        redisService.delete(key);
        redisService.delete(ACCOUNT_LOCKED_PREFIX + identifier.toLowerCase());
        log.info("用户 {} 登录成功，清除失败记录", identifier);
    }

    /**
     * 记录登录失败
     */
    public void loginFailed(String identifier) {
        String key = LOGIN_ATTEMPT_PREFIX + identifier.toLowerCase();
        String attemptsStr = redisService.get(key);
        int attempts = attemptsStr == null ? 0 : Integer.parseInt(attemptsStr);
        attempts++;

        if (attempts >= MAX_ATTEMPT) {
            // 锁定账户
            lockAccount(identifier);
            log.warn("用户 {} 登录失败次数过多，账户已锁定", identifier);
        } else {
            // 更新失败次数
            redisService.set(key, String.valueOf(attempts), ATTEMPT_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.warn("用户 {} 登录失败，当前失败次数: {}/{}", identifier, attempts, MAX_ATTEMPT);
        }
    }

    /**
     * 检查账户是否被锁定
     */
    public boolean isBlocked(String identifier) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + identifier.toLowerCase();
        String locked = redisService.get(lockKey);
        return locked != null;
    }

    /**
     * 获取剩余尝试次数
     */
    public int getRemainingAttempts(String identifier) {
        String key = LOGIN_ATTEMPT_PREFIX + identifier.toLowerCase();
        String attemptsStr = redisService.get(key);
        int attempts = attemptsStr == null ? 0 : Integer.parseInt(attemptsStr);
        return Math.max(0, MAX_ATTEMPT - attempts);
    }

    /**
     * 获取账户锁定剩余时间（分钟）
     */
    public long getLockRemainingTime(String identifier) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + identifier.toLowerCase();
        Long ttl = redisService.getExpire(lockKey, TimeUnit.MINUTES);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 锁定账户
     */
    private void lockAccount(String identifier) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + identifier.toLowerCase();
        String attemptKey = LOGIN_ATTEMPT_PREFIX + identifier.toLowerCase();
        
        // 设置锁定标记
        redisService.set(lockKey, "locked", LOCK_TIME_MINUTES, TimeUnit.MINUTES);
        // 清除失败次数记录（锁定期间不再增加）
        redisService.delete(attemptKey);
    }

    /**
     * 手动解锁账户（管理员操作）
     */
    public void unlockAccount(String identifier) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + identifier.toLowerCase();
        String attemptKey = LOGIN_ATTEMPT_PREFIX + identifier.toLowerCase();
        redisService.delete(lockKey);
        redisService.delete(attemptKey);
        log.info("管理员解锁账户: {}", identifier);
    }
}

