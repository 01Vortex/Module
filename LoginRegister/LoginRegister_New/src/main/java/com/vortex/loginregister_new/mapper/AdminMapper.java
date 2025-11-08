package com.vortex.loginregister_new.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.loginregister_new.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员 Mapper
 *
 * @author Vortex
 * @since 2024
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据账号查询管理员
     */
    Admin findByAccount(@Param("account") String account);

    /**
     * 更新最后登录信息
     */
    int updateLastLoginInfo(@Param("adminId") Long adminId, @Param("loginIp") String loginIp);

    /**
     * 根据邮箱查询管理员
     */
    Admin findByEmail(@Param("email") String email);
}

