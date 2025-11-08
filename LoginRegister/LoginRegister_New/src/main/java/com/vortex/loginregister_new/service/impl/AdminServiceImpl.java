package com.vortex.loginregister_new.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vortex.loginregister_new.entity.Admin;
import com.vortex.loginregister_new.mapper.AdminMapper;
import com.vortex.loginregister_new.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员服务实现类
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public Admin findByAccount(String account) {
        return baseMapper.findByAccount(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastLoginInfo(Long adminId, String loginIp) {
        int rows = baseMapper.updateLastLoginInfo(adminId, loginIp);
        return rows > 0;
    }

    @Override
    public Admin findByEmail(String email) {
        return baseMapper.findByEmail(email);
    }
}

