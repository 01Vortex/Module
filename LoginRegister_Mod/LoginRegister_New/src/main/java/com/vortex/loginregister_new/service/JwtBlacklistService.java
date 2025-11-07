package com.vortex.loginregister_new.service;

/**
 * JWT黑名单服务接口
 * 用于管理已注销的token
 *
 * @author Vortex
 * @since 2024
 */
public interface JwtBlacklistService {
    
    /**
     * 将token添加到黑名单
     *
     * @param token JWT token
     */
    void addToBlacklist(String token);
    
    /**
     * 检查token是否在黑名单中
     *
     * @param token JWT token
     * @return 是否在黑名单中
     */
    boolean isBlacklisted(String token);
    
    /**
     * 清除过期的黑名单条目
     */
    void cleanExpiredEntries();
    
    /**
     * 标记用户的所有token失效（用于密码修改后使token失效）
     *
     * @param userId 用户ID
     * @param expirationDays token失效标记的有效期（天）
     */
    void invalidateUserTokens(Long userId, int expirationDays);
    
    /**
     * 检查用户的token是否已失效
     *
     * @param userId 用户ID
     * @return 是否已失效
     */
    boolean isUserTokenInvalidated(Long userId);
}
