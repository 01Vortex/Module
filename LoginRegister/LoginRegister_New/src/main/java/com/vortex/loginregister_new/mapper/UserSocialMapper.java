package com.vortex.loginregister_new.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.loginregister_new.entity.UserSocial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserSocialMapper extends BaseMapper<UserSocial> {

    @Select("SELECT * FROM user_social WHERE provider = #{provider} AND openid = #{openid} LIMIT 1")
    UserSocial findByProviderAndOpenId(@Param("provider") String provider, @Param("openid") String openid);

    @Select("SELECT * FROM user_social WHERE provider = #{provider} AND unionid = #{unionid} LIMIT 1")
    UserSocial findByProviderAndUnionId(@Param("provider") String provider, @Param("unionid") String unionid);
}


