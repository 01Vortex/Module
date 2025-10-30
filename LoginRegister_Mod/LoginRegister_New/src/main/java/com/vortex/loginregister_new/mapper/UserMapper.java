package com.vortex.loginregister_new.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.loginregister_new.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis Plus 的 BaseMapper，自动拥有 CRUD 方法
 *
 * @author Vortex
 * @since 2024
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象
     */
    User findByPhone(@Param("phone") String phone);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 影响行数
     */
    int updateLastLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);
}

