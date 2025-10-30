package com.vortex.loginregister_new.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.mapper.UserMapper;
import com.vortex.loginregister_new.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findByUsername(String username) {
        return baseMapper.findByUsername(username);
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
        // 检查用户名是否已存在
        if (isUsernameExists(user.getUsername())) {
            log.warn("用户名已存在: {}", user.getUsername());
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

        // 保存用户
        boolean saved = this.save(user);
        if (saved) {
            log.info("用户注册成功: {}", user.getUsername());
        }
        return saved;
    }

    @Override
    public boolean isUsernameExists(String username) {
        User user = findByUsername(username);
        return user != null;
    }

    @Override
    public boolean isEmailExists(String email) {
        User user = findByEmail(email);
        return user != null;
    }
}

