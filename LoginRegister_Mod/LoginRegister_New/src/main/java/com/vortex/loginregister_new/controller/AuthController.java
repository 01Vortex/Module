package com.vortex.loginregister_new.controller;

import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

/**
 * 认证控制器 - 处理登录、注册等认证相关功能
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    // 临时存储验证码（生产环境应使用Redis）
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    /**
     * 用户登录（密码方式）
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        log.info("用户登录请求: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        
        if (username == null || password == null) {
            result.put("code", 400);
            result.put("message", "用户名和密码不能为空");
            return result;
        }
        
        try {
            // 支持用户名或邮箱登录
            User user = userService.findByUsername(username);
            if (user == null) {
                user = userService.findByEmail(username);
            }
            
            if (user == null) {
                result.put("code", 401);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                result.put("code", 401);
                result.put("message", "密码错误");
                return result;
            }
            
            // 检查用户状态
            if (user.getStatus() == 0) {
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }
            
            // 更新登录信息
            String clientIp = getClientIp(request);
            userService.updateLastLoginInfo(user.getId(), clientIp);
            
            // 返回用户信息（不包含密码）
            user.setPassword(null);
            
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
            
            log.info("用户 {} 登录成功", username);
            
        } catch (Exception e) {
            log.error("登录失败: ", e);
            result.put("code", 500);
            result.put("message", "登录失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerData) {
        String username = registerData.get("username");
        String password = registerData.get("password");
        String email = registerData.get("email");
        String phone = registerData.get("phone");
        String verifyCode = registerData.get("verifyCode");
        
        log.info("用户注册请求: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证用户名
            if (userService.isUsernameExists(username)) {
                result.put("code", 400);
                result.put("message", "用户名已存在");
                return result;
            }
            
            // 判断是邮箱注册还是手机号注册
            String identifier = email != null ? email : phone;
            
            // 验证验证码
            String storedCode = verificationCodes.get(identifier);
            if (storedCode == null) {
                result.put("code", 400);
                result.put("message", "验证码已过期或不存在");
                return result;
            }
            
            if (!storedCode.equals(verifyCode)) {
                result.put("code", 400);
                result.put("message", "验证码错误");
                return result;
            }
            
            // 验证邮箱或手机号是否已注册
            if (email != null && userService.isEmailExists(email)) {
                result.put("code", 400);
                result.put("message", "邮箱已被注册");
                return result;
            }
            
            // 创建用户对象
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setPhone(phone);
            
            // 生成随机昵称: Sun + 7位随机数字字母组合
            String randomSuffix = generateRandomCode(7);
            user.setNickname("Sun" + randomSuffix);
            
            // 设置默认状态
            user.setStatus(1);
            
            // 注册用户
            boolean saved = userService.register(user);
            
            if (saved) {
                // 清除已使用的验证码
                verificationCodes.remove(identifier);
                
                user.setPassword(null);
                result.put("code", 200);
                result.put("message", "注册成功");
                result.put("data", user);
                log.info("用户 {} 注册成功，昵称: {}", user.getUsername(), user.getNickname());
            } else {
                result.put("code", 500);
                result.put("message", "注册失败");
            }
            
        } catch (Exception e) {
            log.error("注册失败: ", e);
            result.put("code", 500);
            result.put("message", "注册失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 生成随机字符串（数字和字母组合）
     */
    private String generateRandomCode(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        
        log.info("发送验证码请求: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        
        if (username == null || username.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "用户名或邮箱不能为空");
            return result;
        }
        
        try {
            // 生成6位验证码
            String code = String.format("%06d", new Random().nextInt(1000000));
            
            // 存储验证码（5分钟有效）
            verificationCodes.put(username, code);
            
            // 实际应用中这里应该发送邮件或短信
            // 这里仅作演示，返回验证码（生产环境不应该这样做）
            log.info("验证码: {} (用户: {})", code, username);
            
            result.put("code", 200);
            result.put("message", "验证码已发送");
            result.put("verificationCode", code); // 仅用于开发测试
            
            // 5分钟后自动清除
            new Thread(() -> {
                try {
                    Thread.sleep(5 * 60 * 1000);
                    verificationCodes.remove(username);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } catch (Exception e) {
            log.error("发送验证码失败: ", e);
            result.put("code", 500);
            result.put("message", "发送失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 验证码登录
     */
    @PostMapping("/login/code")
    public Map<String, Object> loginWithCode(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String username = loginData.get("username");
        String code = loginData.get("code");
        
        log.info("验证码登录请求: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        
        if (username == null || code == null) {
            result.put("code", 400);
            result.put("message", "用户名和验证码不能为空");
            return result;
        }
        
        try {
            // 验证验证码
            String storedCode = verificationCodes.get(username);
            if (storedCode == null) {
                result.put("code", 400);
                result.put("message", "验证码已过期或不存在");
                return result;
            }
            
            if (!storedCode.equals(code)) {
                result.put("code", 400);
                result.put("message", "验证码错误");
                return result;
            }
            
            // 查询用户
            User user = userService.findByUsername(username);
            if (user == null) {
                user = userService.findByEmail(username);
            }
            
            if (user == null) {
                result.put("code", 401);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 检查用户状态
            if (user.getStatus() == 0) {
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }
            
            // 更新登录信息
            String clientIp = getClientIp(request);
            userService.updateLastLoginInfo(user.getId(), clientIp);
            
            // 清除已使用的验证码
            verificationCodes.remove(username);
            
            // 返回用户信息
            user.setPassword(null);
            
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
            
            log.info("用户 {} 通过验证码登录成功", username);
            
        } catch (Exception e) {
            log.error("验证码登录失败: ", e);
            result.put("code", 500);
            result.put("message", "登录失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

