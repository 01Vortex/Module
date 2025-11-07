package com.vortex.loginregister_new.common;

/**
 * 系统常量类
 *
 * @author Vortex
 * @since 2024
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    /**
     * Redis Key 前缀
     */
    public static final class RedisKey {
        private RedisKey() {}

        public static final String VERIFICATION_CODE_PREFIX = "verification_code:";
        public static final String CODE_ATTEMPT_PREFIX = "code_attempt:";
        public static final String RATE_LIMIT_PREFIX = "rate_limit:";
        public static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
        public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    }

    /**
     * 验证码相关常量
     */
    public static final class VerificationCode {
        private VerificationCode() {}

        public static final int MAX_ATTEMPTS = 5;
        public static final int CODE_LENGTH = 6;
        public static final int EXPIRY_MINUTES = 10;
        public static final int CODE_SEND_INTERVAL_SECONDS = 60;
    }

    /**
     * 登录相关常量
     */
    public static final class Login {
        private Login() {}

        public static final int MAX_FAILED_ATTEMPTS = 5;
        public static final int LOCK_TIME_MINUTES = 15;
        public static final int RATE_LIMIT_MAX_REQUESTS = 5;
        public static final int RATE_LIMIT_WINDOW_SECONDS = 60;
    }

    /**
     * 注册相关常量
     */
    public static final class Register {
        private Register() {}

        public static final int RATE_LIMIT_MAX_REQUESTS = 10;
        public static final int RATE_LIMIT_WINDOW_SECONDS = 3600;
    }

    /**
     * 密码相关常量
     */
    public static final class Password {
        private Password() {}

        public static final int MIN_LENGTH = 8;
        public static final int MAX_LENGTH = 128;
    }

    /**
     * 账号相关常量
     */
    public static final class Account {
        private Account() {}

        public static final int MIN_LENGTH = 3;
        public static final int MAX_LENGTH = 50;
    }

    /**
     * 用户状态
     */
    public static final class UserStatus {
        private UserStatus() {}

        public static final int DISABLED = 0;
        public static final int ENABLED = 1;
    }

    /**
     * JWT Token 类型
     */
    public static final class TokenType {
        private TokenType() {}

        public static final String ACCESS = "access";
        public static final String REFRESH = "refresh";
    }

    /**
     * 角色
     */
    public static final class Role {
        private Role() {}

        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}
