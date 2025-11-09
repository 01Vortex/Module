package com.vortex.loginregister_new.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.mapper.UserMapper;
import com.vortex.loginregister_new.mapper.UserRoleMapper;
import com.vortex.loginregister_new.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author 01Vortex
 * @since 2024
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;

    public UserServiceImpl(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public User findByAccount(String account) {
        return baseMapper.findByAccount(account);
    }

    @Override
    public User findByEmail(String email) {
        return baseMapper.findByEmail(email);
    }

    @Override
    public User findByPhone(String phone) {
        return baseMapper.findByPhone(phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastLoginInfo(Long userId, String loginIp) {
        int rows = baseMapper.updateLastLoginInfo(userId, loginIp);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user) {
        // 检查账号是否已存在
        if (isAccountExists(user.getAccount())) {
            log.warn("账号已存在: {}", user.getAccount());
            return false;
        }

        // 检查邮箱是否已存在（如果提供了邮箱）
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            if (isEmailExists(user.getEmail())) {
                log.warn("邮箱已存在: {}", user.getEmail());
                return false;
            }
        }

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认状态为正常
        }
        
        // 设置账户类型（如果未设置）
        if (user.getAccountType() == null || user.getAccountType().isEmpty()) {
            // 如果密码为空，默认为 SOCIAL；否则为 PASSWORD
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                user.setAccountType("SOCIAL");
            } else {
                user.setAccountType("PASSWORD");
            }
        }

        // 保存用户
        boolean saved = this.save(user);
        if (saved) {
            log.info("用户注册成功: {}, accountType: {}", user.getAccount(), user.getAccountType());
        }
        return saved;
    }

    @Override
    public boolean isAccountExists(String account) {
        User user = findByAccount(account);
        return user != null;
    }

    @Override
    public boolean isEmailExists(String email) {
        User user = findByEmail(email);
        return user != null;
    }

    @Override
    public boolean isAdmin(Long userId) {
        List<String> roles = getUserRoles(userId);
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return userRoleMapper.findRoleCodesByUserId(userId);
    }
}

