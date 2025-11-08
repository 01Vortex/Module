package com.vortex.loginregister_new.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vortex.loginregister_new.entity.UserSocial;
import com.vortex.loginregister_new.mapper.UserSocialMapper;
import com.vortex.loginregister_new.service.UserSocialService;
import org.springframework.stereotype.Service;

@Service
public class UserSocialServiceImpl extends ServiceImpl<UserSocialMapper, UserSocial> implements UserSocialService {
    @Override
    public UserSocial findByProviderAndOpenId(String provider, String openid) {
        return baseMapper.findByProviderAndOpenId(provider, openid);
    }

    @Override
    public UserSocial findByProviderAndUnionId(String provider, String unionid) {
        return baseMapper.findByProviderAndUnionId(provider, unionid);
    }
}


