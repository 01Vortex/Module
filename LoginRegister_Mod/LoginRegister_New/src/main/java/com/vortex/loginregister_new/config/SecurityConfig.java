package com.vortex.loginregister_new.config;

import com.vortex.loginregister_new.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置
 *
 * @author 01Vortex
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 增加BCrypt强度
    }

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（RESTful API使用JWT，不需要CSRF保护）
            // 如果使用Cookie存储token，需要启用CSRF保护
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置Session管理：无状态（使用JWT）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开端点（无需认证）
                .requestMatchers(
                    "/auth/login",
                    "/auth/register",
                    "/auth/send-code",
                    "/auth/login/code",
                    "/auth/forgot-password/**",
                    "/auth/refresh",
                    "/oauth/**",
                    "/user/test",
                    "/user/check/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()
                // 其他端点需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 配置安全响应头
            .headers(headers -> headers
                .contentTypeOptions(contentType -> contentType.and())
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.and())
            );
        
        return http.build();
    }

    /**
     * CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源（生产环境应配置具体域名）
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173"
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 允许的请求头（限制为必要的头部）
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin"
        ));
        
        // 允许携带凭证（Cookie）
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

