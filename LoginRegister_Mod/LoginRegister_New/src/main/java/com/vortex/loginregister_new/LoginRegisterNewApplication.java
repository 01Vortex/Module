package com.vortex.loginregister_new;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 登录注册模块主应用类
 * 基于 Spring Boot 3.2.5 + Java 21 + MyBatis Plus
 * 
 * @author Vortex
 * @since 2024
 */
@SpringBootApplication
@MapperScan("com.vortex.loginregister_new.mapper")
public class LoginRegisterNewApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginRegisterNewApplication.class, args);
    }

}
