package com.vortex.loginregister_new.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具
 * 用于生成BCrypt密码哈希，用于初始化管理员账号
 *
 * @author Vortex
 * @since 2024
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        // 生成admin123的哈希
        String password = "admin123";
        
        // 验证数据库中现有的hash
        String existingHash = "$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("==========================================");
        System.out.println("验证数据库中现有的hash:");
        System.out.println("Hash: " + existingHash);
        System.out.println("密码: " + password);
        System.out.println("验证结果: " + existingMatches);
        System.out.println("==========================================");
        System.out.println();
        
        // 生成新的hash
        String newHash = encoder.encode(password);
        boolean newMatches = encoder.matches(password, newHash);
        
        System.out.println("==========================================");
        System.out.println("生成新的hash:");
        System.out.println("密码: " + password);
        System.out.println("新Hash: " + newHash);
        System.out.println("验证结果: " + newMatches);
        System.out.println("==========================================");
        System.out.println();
        
        System.out.println("==========================================");
        System.out.println("SQL更新语句（复制执行）:");
        System.out.println("==========================================");
        System.out.println("UPDATE `admin` SET `password` = '" + newHash + "' WHERE `account` = 'admin';");
        System.out.println();
    }
}

