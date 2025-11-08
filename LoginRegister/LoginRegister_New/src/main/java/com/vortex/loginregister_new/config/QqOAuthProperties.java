package com.vortex.loginregister_new.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qq.oauth")
public class QqOAuthProperties {
    private String appId;
    private String appKey;
    private String redirectUri;
}


