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
        
        // 生成新的hash
        String newHash = encoder.encode(password);
        
        // 输出SQL更新语句
        System.out.println("UPDATE `admin` SET `password` = '" + newHash + "' WHERE `account` = 'admin';");
    }
}

