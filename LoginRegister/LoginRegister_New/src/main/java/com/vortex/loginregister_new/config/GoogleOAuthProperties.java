package com.vortex.loginregister_new.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    
    @PostConstruct
    public void validate() {
        if (clientId == null || clientId.isBlank()) {
            log.warn("⚠️ Google OAuth client_id 未配置");
        } else {
            log.info("✅ Google OAuth client_id 已配置: {}...", 
                    clientId.substring(0, Math.min(30, clientId.length())));
        }
        
        if (clientSecret == null || clientSecret.isBlank()) {
            log.warn("⚠️ Google OAuth client_secret 未配置");
        } else {
            log.info("✅ Google OAuth client_secret 已配置");
        }
        
        if (redirectUri == null || redirectUri.isBlank()) {
            log.warn("⚠️ Google OAuth redirect_uri 未配置");
        } else {
            log.info("✅ Google OAuth redirect_uri 已配置: {}", redirectUri);
        }
    }
}


