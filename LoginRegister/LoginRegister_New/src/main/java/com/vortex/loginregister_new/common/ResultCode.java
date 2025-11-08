package com.vortex.loginregister_new.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author Vortex
 * @since 2024
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAIL(500, "操作失败"),

    /**
     * 参数错误
     */
    BAD_REQUEST(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权或认证已失效"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "方法不允许"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    /**
     * 账户被锁定
     */
    LOCKED(423, "账户已被锁定"),

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 账号或密码错误
     */
    LOGIN_FAILED(401, "账号或密码错误"),

    /**
     * 账号已被禁用
     */
    ACCOUNT_DISABLED(403, "账号已被禁用"),

    /**
     * 验证码错误
     */
    VERIFICATION_CODE_ERROR(400, "验证码错误"),

    /**
     * 验证码已过期
     */
    VERIFICATION_CODE_EXPIRED(400, "验证码已过期"),

    /**
     * 账号已存在
     */
    ACCOUNT_EXISTS(400, "账号已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_EXISTS(400, "邮箱已存在"),

    /**
     * 手机号已存在
     */
    PHONE_EXISTS(400, "手机号已存在");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}
