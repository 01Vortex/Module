package com.example.login.util;

import java.util.regex.Pattern;

public class DataValidationUtil {


    /**
     * 校证邮箱格式
     * @param email
     * @return true 表示邮箱格式正确，false 表示邮箱格式错误
     */
    public static boolean isValidEmail(String email) {
            if (email == null || email.isEmpty()) {
                return false;
            }
            String emailRegex = "^(?:[\\u4e00-\\u9fa5\\w\\.!#%&'*+/=?^`{|}~-]+)@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,})$";
            Pattern pattern = Pattern.compile(emailRegex);
            return pattern.matcher(email).matches();
      }

    /**
     * 判断字符串是否为空
     * @param input
     * @return 如果字符串为 null 或空则返回 true，否则返回 false
     */
    public static boolean ValidStringEmpty(String input) {
          return input == null || input.trim().isEmpty();
    }


    /**
     * 判断对象是否为空
     * @param obj
     * @return 如果对象为 null 或空则返回 true，否则返回 false
     */
    public static boolean ValidObjectEmpty(Object obj) {
          if (obj == null) {
            return true;
          }
          return false;
    }


    /**
     * 校验手机号格式（这里以中国大陆手机号为例）
     * @param phoneNumber
     * @return
     */

        public static boolean isValidPhoneNumber(String phoneNumber) {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return false;
            }
            String phoneRegex = "^1[3-9]\\d{9}$"; // 中国大陆手机号正则表达式
            Pattern pattern = Pattern.compile(phoneRegex);
            return pattern.matcher(phoneNumber).matches();
        }
        public static boolean isValidPassword(String password) {
            if (password == null || password.isEmpty()) {
                return false;
            }
            String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*+=]).{8,}$";
            Pattern pattern = Pattern.compile(passwordRegex);
            return pattern.matcher(password).matches();
        }

        // 校验用户名格式
        public static boolean isValidUsername(String username) {
            if (username == null || username.isEmpty()) {
                return false;
            }
            // 检查长度是否在 1~12 之间
            if (username.length() < 1 || username.length() > 12) {
                return false;
            }
            // 不允许以 '-' 或 '_' 开头或结尾
            if (username.startsWith("-") || username.startsWith("_") ||
                    username.endsWith("-") || username.endsWith("_")) {
                return false;
            }
            // 正则表达式：只允许 [a-zA-Z0-9_-]
            String regex = "^[a-zA-Z0-9_-]+$";
            return Pattern.matches(regex, username);
        }






}

