package com.vortex.loginregister_new.service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 服务接口
 *
 * @author Vortex
 * @since 2024
 */
public interface RedisService {

    /**
     * 设置键值对
     *
     * @param key 键
     * @param value 值
     */
    void set(String key, String value);

    /**
     * 设置键值对（带过期时间）
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String key, String value, long timeout, TimeUnit unit);

    /**
     * 获取值
     *
     * @param key 键
     * @return 值
     */
    String get(String key);

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否删除成功
     */
    Boolean delete(String key);

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 获取过期时间
     *
     * @param key 键
     * @param unit 时间单位
     * @return 过期时间（秒）
     */
    Long getExpire(String key, TimeUnit unit);
}

