package com.vortex.loginregister_new.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vortex.loginregister_new.entity.Admin;

/**
 * 管理员服务接口
 *
 * @author Vortex
 * @since 2024
 */
public interface AdminService extends IService<Admin> {

    /**
     * 根据账号查询管理员
     *
     * @param account 账号
     * @return 管理员对象
     */
    Admin findByAccount(String account);

    /**
     * 更新最后登录信息
     *
     * @param adminId 管理员ID
     * @param loginIp 登录IP
     * @return 是否更新成功
     */
    boolean updateLastLoginInfo(Long adminId, String loginIp);

    /**
     * 根据邮箱查询管理员
     *
     * @param email 邮箱
     * @return 管理员对象
     */
    Admin findByEmail(String email);
}

