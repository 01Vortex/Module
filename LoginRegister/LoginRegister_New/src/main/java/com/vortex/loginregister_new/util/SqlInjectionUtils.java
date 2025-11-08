package com.vortex.loginregister_new.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SQL注入防护工具类
 *
 * @author Vortex
 * @since 2024
 */
public final class SqlInjectionUtils {

    private SqlInjectionUtils() {
        throw new UnsupportedOperationException("SqlInjectionUtils class cannot be instantiated");
    }

    /**
     * SQL关键字列表
     */
    private static final List<String> SQL_KEYWORDS = Arrays.asList(
        "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TRUNCATE",
        "EXEC", "EXECUTE", "UNION", "SCRIPT", "DECLARE", "CAST", "CONVERT",
        "EXECUTE", "EXEC", "SP_", "XP_", "/*", "*/", "--", ";", "'", "\""
    );

    /**
     * SQL注入模式
     */
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|declare|cast|convert|sp_|xp_|/\\*|\\*/|--|;|'|\")"
    );

    /**
     * 检查输入是否包含SQL注入攻击
     *
     * @param input 输入字符串
     * @return 是否包含SQL注入
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String upperInput = input.toUpperCase();
        
        // 检查是否包含SQL关键字
        for (String keyword : SQL_KEYWORDS) {
            if (upperInput.contains(keyword.toUpperCase())) {
                // 进一步检查是否是真正的SQL注入（而不是正常的业务数据）
                if (SQL_INJECTION_PATTERN.matcher(input).find()) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 清理输入，移除潜在的SQL注入字符
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String sanitizeSqlInput(String input) {
        if (input == null) {
            return null;
        }
        
        // 移除SQL注入相关字符
        String sanitized = input
            .replaceAll("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute)", "")
            .replaceAll("['\";]", "")
            .replaceAll("/\\*.*?\\*/", "")
            .replaceAll("--.*", "");
        
        return sanitized.trim();
    }

    /**
     * 验证输入是否安全（不包含SQL注入）
     *
     * @param input 输入字符串
     * @return 是否安全
     */
    public static boolean isSafeInput(String input) {
        return !containsSqlInjection(input);
    }
}
