package com.vortex.loginregister_new.util;

import java.security.SecureRandom;

/**
 * 验证码工具类
 *
 * @author Vortex
 * @since 2024
 */
public final class VerificationCodeUtils {

    private VerificationCodeUtils() {
        throw new UnsupportedOperationException("VerificationCodeUtils class cannot be instantiated");
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DIGITS = "0123456789";

    /**
     * 生成数字验证码
     *
     * @param length 验证码长度
     * @return 验证码
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("验证码长度必须大于0");
        }
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return code.toString();
    }

    /**
     * 生成6位数字验证码
     *
     * @return 验证码
     */
    public static String generateSixDigitCode() {
        return generateCode(6);
    }
}
