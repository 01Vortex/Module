package com.example.login.service;

import com.example.login.model.User;
import org.springframework.security.core.GrantedAuthority;


import java.util.Collection;



public interface UserService {
    // 密码重置
    void resetPassword(String email_phone, String newPassword);

    // 创建账号
    void createAccount(User user);

    // 获取用户信息
    User loadUserInfoByUsername(String username);




    }
