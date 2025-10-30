package com.vortex.loginregister_new.service;

/**
 * 邮件服务接口
 *
 * @author Vortex
 * @since 2024
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);
}

