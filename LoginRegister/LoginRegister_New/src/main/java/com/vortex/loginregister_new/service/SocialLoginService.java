package com.vortex.loginregister_new.service;

import com.vortex.loginregister_new.entity.User;

/**
 * 第三方登录服务接口
 *
 * @author Vortex
 * @since 2024
 */
public interface SocialLoginService {

    /**
     * 第三方登录或注册
     *
     * @param provider 第三方提供商（google/qq/wechat）
     * @param providerUserId 第三方用户ID（openid/sub等）
     * @param unionId 第三方unionid（可选）
     * @param email 邮箱（可选）
     * @param nickname 昵称（可选）
     * @param avatar 头像URL（可选）
     * @param loginIp 登录IP（用于更新最后登录信息）
     * @return 用户对象
     */
    User loginOrRegister(String provider, String providerUserId, String unionId, 
                        String email, String nickname, String avatar, String loginIp);

    /**
     * 更新账户类型
     *
     * @param userId 用户ID
     */
    void updateAccountType(Long userId);

    /**
     * 检查用户是否已设置密码
     *
     * @param userId 用户ID
     * @return 是否已设置密码
     */
    boolean hasPassword(Long userId);
}

