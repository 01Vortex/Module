package com.vortex.loginregister_new.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("user_social")
public class UserSocial implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /**
     * 第三方提供商：wechat/qq/google 等
     */
    @TableField("provider")
    private String provider;

    /**
     * openid（微信必有）
     */
    @TableField("openid")
    private String openid;

    /**
     * unionid（如有）
     */
    @TableField("unionid")
    private String unionid;

    /**
     * 第三方昵称
     */
    @TableField("social_name")
    private String socialName;

    @TableField("avatar")
    private String avatar;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


