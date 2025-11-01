package com.vortex.loginregister_new.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vortex.loginregister_new.entity.UserSocial;

public interface UserSocialService extends IService<UserSocial> {
    UserSocial findByProviderAndOpenId(String provider, String openid);
    UserSocial findByProviderAndUnionId(String provider, String unionid);
}


