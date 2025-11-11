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
        boolean isValid = true;
        if (redirectUri == null || redirectUri.isBlank() || clientId.isBlank() || clientSecret.isBlank()) {
            log.warn("⚠️ Google OAuth服务未配置或配置不完全");
            isValid = false;
        }
        
        if (isValid) {
            log.info("✅ GoogleOAuth服务正常");
        }
    }
}


