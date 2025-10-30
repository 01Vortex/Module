package com.vortex.loginregister_new.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vortex.loginregister_new.entity.User;

/**
 * 用户服务接口
 *
 * @author Vortex
 * @since 2024
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User findByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象
     */
    User findByPhone(String phone);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 是否更新成功
     */
    boolean updateLastLoginInfo(Long userId, String loginIp);

    /**
     * 用户注册
     *
     * @param user 用户对象
     * @return 是否注册成功
     */
    boolean register(User user);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExists(String email);
}

