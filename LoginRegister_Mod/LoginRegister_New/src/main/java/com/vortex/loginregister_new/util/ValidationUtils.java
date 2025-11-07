package com.vortex.loginregister_new.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 *
 * @author Vortex
 * @since 2024
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new UnsupportedOperationException("ValidationUtils class cannot be instantiated");
    }

    /**
     * 密码正则表达式：至少8位，必须包含字母和数字（仅允许字母和数字）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"
    );

    /**
     * 账号正则表达式：字母、数字、下划线，长度3-50
     */
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * 手机号正则表达式（中国大陆）
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * HTML标签正则表达式
     */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    /**
     * Script标签正则表达式
     */
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>");

    /**
     * 验证密码强度
     * 要求：至少8位，必须包含字母和数字（仅允许字母和数字）
     *
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * 获取密码验证错误信息
     *
     * @param password 密码
     * @return 错误信息，如果密码有效则返回null
     */
    public static String getPasswordValidationError(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        if (password.length() < 8) {
            return "密码长度至少为8位";
        }
        if (password.length() > 128) {
            return "密码长度不能超过128位";
        }
        if (!password.matches(".*[A-Za-z].*")) {
            return "密码必须包含字母";
        }
        if (!password.matches(".*\\d.*")) {
            return "密码必须包含数字";
        }
        if (!password.matches("^[A-Za-z\\d]+$")) {
            return "密码只能包含字母和数字";
        }
        return null;
    }

    /**
     * 验证账号格式
     *
     * @param account 账号
     * @return 是否有效
     */
    public static boolean isValidAccount(String account) {
        if (account == null || account.length() < 3 || account.length() > 50) {
            return false;
        }
        return ACCOUNT_PATTERN.matcher(account).matches();
    }

    /**
     * 验证邮箱格式
     *
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return 是否有效
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 清理输入，防止XSS攻击
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // 移除HTML标签和脚本
        String sanitized = HTML_TAG_PATTERN.matcher(input).replaceAll("");
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        // 移除危险字符
        sanitized = sanitized.replaceAll("[<>\"'&]", "");
        return sanitized.trim();
    }

    /**
     * 标准化邮箱（转小写）
     *
     * @param email 邮箱
     * @return 标准化后的邮箱
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * 标准化账号（去除空格）
     *
     * @param account 账号
     * @return 标准化后的账号
     */
    public static String normalizeAccount(String account) {
        if (account == null) {
            return null;
        }
        return account.trim();
    }

    /**
     * 验证输入长度（防止DoS攻击）
     *
     * @param input 输入字符串
     * @param maxLength 最大长度
     * @return 是否有效
     */
    public static boolean isValidLength(String input, int maxLength) {
        if (input == null) {
            return true;
        }
        return input.length() <= maxLength;
    }

    /**
     * 验证输入长度范围
     *
     * @param input 输入字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 是否有效
     */
    public static boolean isValidLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return false;
        }
        int length = input.length();
        return length >= minLength && length <= maxLength;
    }
}
