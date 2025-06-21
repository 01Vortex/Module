package com.example.login.utils;


import java.security.SecureRandom;

public class RandomCodeUtil {

    // 使用 SecureRandom 实例，确保使用强熵源
    private static final SecureRandom secureRandom = new SecureRandom();


    /**
     * 生成指定长度的纯数字随机字符串（适用于高并发）
     *
     * @param length 随机数长度
     * @return n 位纯数字字符串
     */
    public static String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10)); // 生成 0-9 之间的随机数字
        }
        return sb.toString();
    }



    //生成固定长度的字母数字字符串（不推荐用于加密，可用于用户可见 token）
    public static String generateAlphaNumericKey(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }














}
