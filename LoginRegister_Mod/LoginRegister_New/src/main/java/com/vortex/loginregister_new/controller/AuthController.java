package com.vortex.loginregister_new.controller;

import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.EmailService;
import com.vortex.loginregister_new.service.LoginAttemptService;
import com.vortex.loginregister_new.service.RateLimitService;
import com.vortex.loginregister_new.service.RedisService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
    private final RedisService redisService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final RateLimitService rateLimitService;
    
    // Redis Key 前缀
    private static final String VERIFICATION_CODE_PREFIX = "verification_code:";
    private static final String CODE_ATTEMPT_PREFIX = "code_attempt:";
    
    // 密码策略：至少8位，包含字母和数字，允许常见特殊字符
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&._\\-+=()\\[\\]{}]{8,}$");
    // 验证码最大尝试次数
    private static final int MAX_CODE_ATTEMPTS = 5;

    /**
     * 用户登录（密码方式）
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        String clientIp = getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        // 请求频率限制：每个IP每分钟最多5次登录尝试
        if (rateLimitService.isRateLimited("login:" + clientIp, 5, 60)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        if (username == null || password == null) {
            result.put("code", 400);
            result.put("message", "用户名和密码不能为空");
            return result;
        }
        
        // 标准化标识（邮箱统一小写）
        String identifier = username.contains("@") ? username.toLowerCase() : username;
        
        try {
            // 检查账户是否被锁定
            if (loginAttemptService.isBlocked(identifier)) {
                long remainingTime = loginAttemptService.getLockRemainingTime(identifier);
                result.put("code", 423);
                result.put("message", String.format("账户已被锁定，请%d分钟后重试", remainingTime));
                return result;
            }
            
            // 支持用户名或邮箱登录
            User user = userService.findByUsername(identifier);
            if (user == null && identifier.contains("@")) {
                user = userService.findByEmail(identifier);
            }
            
            // 无论用户是否存在，都进行密码验证（防止用户枚举攻击）
            boolean passwordValid = false;
            if (user != null) {
                passwordValid = passwordEncoder.matches(password, user.getPassword());
            }
            
            if (user == null || !passwordValid) {
                // 记录登录失败
                if (user != null) {
                    loginAttemptService.loginFailed(identifier);
                    int remaining = loginAttemptService.getRemainingAttempts(identifier);
                    result.put("code", 401);
                    result.put("message", "用户名或密码错误");
                    if (remaining > 0) {
                        result.put("remainingAttempts", remaining);
                    }
                } else {
                    // 用户不存在时也记录失败（防止用户枚举）
                    loginAttemptService.loginFailed(identifier);
                    result.put("code", 401);
                    result.put("message", "用户名或密码错误");
                }
                return result;
            }
            
            // 检查用户状态
            if (user.getStatus() == 0) {
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }
            
            // 登录成功，清除失败记录
            loginAttemptService.loginSucceeded(identifier);
            
            // 更新登录信息
            userService.updateLastLoginInfo(user.getId(), clientIp);
            
            // 生成JWT token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            
            // 返回用户信息（不包含密码）
            user.setPassword(null);
            
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            
            log.info("用户 {} 登录成功，IP: {}", username, clientIp);
            
        } catch (Exception e) {
            log.error("登录失败: ", e);
            result.put("code", 500);
            result.put("message", "登录失败，请稍后重试");
        }
        
        return result;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerData, HttpServletRequest request) {
        String nickname = registerData.get("nickname");
        String password = registerData.get("password");
        String email = registerData.get("email");
        String phone = registerData.get("phone");
        String verifyCode = registerData.get("verifyCode");
        String clientIp = getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        // 请求频率限制：每个IP每小时最多10次注册尝试
        if (rateLimitService.isRateLimited("register:" + clientIp, 10, 3600)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        // 验证密码强度
        if (password == null || password.length() < 8) {
            result.put("code", 400);
            result.put("message", "密码长度至少为8位");
            return result;
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            result.put("code", 400);
            result.put("message", "密码必须包含字母和数字，长度至少8位，可包含常见特殊字符（@$!%*#?&._-+=()[]{}）");
            return result;
        }
        
        try {
            // 判断是邮箱注册还是手机号注册
            String identifier = email != null ? email.toLowerCase() : phone;
            
            if (identifier == null || identifier.isEmpty()) {
                result.put("code", 400);
                result.put("message", "邮箱或手机号不能为空");
                return result;
            }
            
            // 从 Redis 验证验证码
            String redisKey = VERIFICATION_CODE_PREFIX + identifier;
            String storedCode = redisService.get(redisKey);
            
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
            
            // 创建用户对象（用户名为10位随机数字，需保证唯一）
            User user = new User();
            String generatedUsername = generateUniqueUsername(10);
            user.setUsername(generatedUsername);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setPhone(phone);
            
            // 使用前端传入的昵称（可为空时给一个默认昵称）
            if (nickname == null || nickname.trim().isEmpty()) {
                nickname = "新用户";
            }
            // 防止XSS：清理昵称
            user.setNickname(sanitizeInput(nickname.trim()));
            
            // 设置默认状态
            user.setStatus(1);
            
            // 注册用户
            boolean saved = userService.register(user);
            
            if (saved) {
                // 清除 Redis 中已使用的验证码
                redisService.delete(redisKey);
                
                user.setPassword(null);
                result.put("code", 200);
                result.put("message", "注册成功");
                result.put("data", user);
                log.info("用户 {} 注册成功，昵称: {}，IP: {}", user.getUsername(), user.getNickname(), clientIp);
            } else {
                result.put("code", 500);
                result.put("message", "注册失败，请稍后重试");
            }
            
        } catch (Exception e) {
            log.error("注册失败: ", e);
            result.put("code", 500);
            result.put("message", "注册失败，请稍后重试");
        }
        
        return result;
    }
    
    /**
     * 生成随机纯数字字符串
     * @param length 数字长度
     * @return 纯数字字符串
     */
    private String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成唯一的10位数字用户名（如发生碰撞则重试）
     */
    private String generateUniqueUsername(int length) {
        String candidate;
        int attempts = 0;
        do {
            candidate = generateRandomNumber(length);
            attempts++;
            if (attempts > 10) {
                // 极端情况下增加一位长度降碰撞风险
                candidate = generateRandomNumber(length + 1);
            }
        } while (userService.isUsernameExists(candidate));
        return candidate;
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> data, HttpServletRequest request) {
        String username = data.get("username");
        if (username != null) {
            username = username.trim();
        }
        String clientIp = getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        if (username == null || username.isEmpty()) {
            result.put("code", 400);
            result.put("message", "用户名或邮箱/手机号不能为空");
            return result;
        }
        
        // 标准化标识（邮箱统一小写，其他原样）
        String identifier = username.contains("@") ? username.toLowerCase() : username;
        
        // 频率限制：每个标识每分钟最多发送1次，每个IP每分钟最多5次
        if (rateLimitService.isRateLimited("send_code:" + identifier, 1, 60)) {
            result.put("code", 429);
            result.put("message", "验证码发送过于频繁，请稍后再试");
            return result;
        }
        
        if (rateLimitService.isRateLimited("send_code_ip:" + clientIp, 5, 60)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        try {
            // 生成6位数字验证码
            String code = String.format("%06d", new Random().nextInt(1000000));
            
            // Redis 存储验证码（5分钟有效）
            String redisKey = VERIFICATION_CODE_PREFIX + identifier;
            redisService.set(redisKey, code, 5, TimeUnit.MINUTES);
            
            // 清除验证码尝试次数
            redisService.delete(CODE_ATTEMPT_PREFIX + identifier);
            
            log.info("验证码已生成并存储到Redis: {} (用户: {}, 有效期: 5分钟)", code, username);
            
            // 判断是邮箱还是手机号
            if (username.contains("@")) {
                // 邮箱：发送邮件验证码
                try {
                    emailService.sendVerificationCode(username, code);
                    result.put("code", 200);
                    result.put("message", "验证码已发送到邮箱，请查收");
                    // 开发环境：在日志中显示验证码（查看后端日志）
                    log.info("✅ 验证码邮件已发送到: {}，验证码: {}", username, code);
                } catch (Exception e) {
                    log.error("❌ 邮件发送失败: ", e);
                    result.put("code", 500);
                    result.put("message", "邮件发送失败，请稍后重试");
                    // 删除 Redis 中的验证码
                    redisService.delete(redisKey);
                }
            } else {
                // 手机号：暂不支持
                result.put("code", 400);
                result.put("message", "暂不支持手机号注册/登录");
                // 删除 Redis 中的验证码
                redisService.delete(redisKey);
            }
            
        } catch (Exception e) {
            log.error("发送验证码失败: ", e);
            result.put("code", 500);
            result.put("message", "发送失败，请稍后重试");
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
        if (username != null) username = username.trim();
        if (code != null) code = code.trim();
        String clientIp = getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        // 请求频率限制
        if (rateLimitService.isRateLimited("login_code:" + clientIp, 5, 60)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        if (username == null || username.isEmpty() || code == null || code.isEmpty()) {
            result.put("code", 400);
            result.put("message", "账号和验证码不能为空");
            return result;
        }
        
        try {
            // 从 Redis 验证验证码（邮箱统一小写）
            String identifier = username.contains("@") ? username.toLowerCase() : username;
            String redisKey = VERIFICATION_CODE_PREFIX + identifier;
            String storedCode = redisService.get(redisKey);
            
            // 检查验证码尝试次数
            String attemptKey = CODE_ATTEMPT_PREFIX + identifier;
            String attemptStr = redisService.get(attemptKey);
            int attempts = attemptStr == null ? 0 : Integer.parseInt(attemptStr);
            
            if (storedCode == null) {
                result.put("code", 400);
                result.put("message", "验证码已过期或不存在");
                return result;
            }
            
            if (!storedCode.equals(code)) {
                attempts++;
                if (attempts >= MAX_CODE_ATTEMPTS) {
                    // 超过最大尝试次数，清除验证码
                    redisService.delete(redisKey);
                    redisService.delete(attemptKey);
                    result.put("code", 400);
                    result.put("message", "验证码错误次数过多，请重新获取验证码");
                    return result;
                }
                redisService.set(attemptKey, String.valueOf(attempts), 5, TimeUnit.MINUTES);
                result.put("code", 400);
                result.put("message", "验证码错误");
                result.put("remainingAttempts", MAX_CODE_ATTEMPTS - attempts);
                return result;
            }
            
            // 查询用户（用户名或邮箱）
            User user = userService.findByUsername(identifier);
            if (user == null && identifier.contains("@")) {
                user = userService.findByEmail(identifier);
            }
            
            if (user == null) {
                result.put("code", 401);
                if (identifier.contains("@")) {
                    result.put("message", "邮箱未注册");
                } else if (identifier.matches("^\\d{11}$")) {
                    result.put("message", "手机号未注册");
                } else {
                    result.put("message", "用户不存在");
                }
                return result;
            }
            
            // 检查用户状态
            if (user.getStatus() == 0) {
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }
            
            // 登录成功，清除验证码和尝试次数
            redisService.delete(redisKey);
            redisService.delete(attemptKey);
            
            // 更新登录信息
            userService.updateLastLoginInfo(user.getId(), clientIp);
            
            // 生成JWT token
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            
            // 返回用户信息
            user.setPassword(null);
            
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            
            log.info("用户 {} 通过验证码登录成功，IP: {}", username, clientIp);
            
        } catch (Exception e) {
            log.error("验证码登录失败: ", e);
            result.put("code", 500);
            result.put("message", "登录失败，请稍后重试");
        }
        
        return result;
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(@RequestBody Map<String, String> data) {
        String refreshToken = data.get("refreshToken");
        Map<String, Object> result = new HashMap<>();
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            result.put("code", 400);
            result.put("message", "刷新令牌不能为空");
            return result;
        }
        
        try {
            // 验证刷新token
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                result.put("code", 401);
                result.put("message", "无效的刷新令牌");
                return result;
            }
            
            // 获取用户信息
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            
            if (username == null || userId == null) {
                result.put("code", 401);
                result.put("message", "无效的刷新令牌");
                return result;
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateAccessToken(userId, username);
            
            result.put("code", 200);
            result.put("message", "刷新成功");
            result.put("accessToken", newAccessToken);
            result.put("tokenType", "Bearer");
            
        } catch (Exception e) {
            log.error("刷新令牌失败: ", e);
            result.put("code", 500);
            result.put("message", "刷新失败，请稍后重试");
        }
        
        return result;
    }
    
    /**
     * 清理输入，防止XSS攻击
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // 移除HTML标签和特殊字符
        return input.replaceAll("<[^>]*>", "")
                   .replaceAll("&[^;]+;", "")
                   .replaceAll("[<>\"']", "")
                   .trim();
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
        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 发送重置密码验证码
     */
    @PostMapping("/forgot-password/send-code")
    public Map<String, Object> sendResetPasswordCode(@RequestBody Map<String, String> data) {
        String emailOrPhone = data.get("emailOrPhone");
        
        log.info("发送重置密码验证码请求: {}", emailOrPhone);
        
        Map<String, Object> result = new HashMap<>();
        
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "邮箱或手机号不能为空");
            return result;
        }
        
        try {
            // 查找用户
            User user = null;
            if (emailOrPhone.contains("@")) {
                user = userService.findByEmail(emailOrPhone);
            } else {
                user = userService.findByPhone(emailOrPhone);
            }
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 生成6位数字验证码
            String code = String.format("%06d", new Random().nextInt(1000000));
            
            // Redis 存储验证码（15分钟有效）
            String redisKey = "reset_password_code:" + emailOrPhone;
            redisService.set(redisKey, code, 15, TimeUnit.MINUTES);
            
            log.info("重置密码验证码已生成并存储到Redis: {} (用户: {}, 有效期: 15分钟)", code, emailOrPhone);
            
            // 判断是邮箱还是手机号
            if (emailOrPhone.contains("@")) {
                // 邮箱：发送邮件验证码
                try {
                    emailService.sendResetPasswordCode(emailOrPhone, code);
                    result.put("code", 200);
                    result.put("message", "验证码已发送到邮箱，请查收");
                    log.info("✅ 重置密码验证码邮件已发送到: {}，验证码: {}", emailOrPhone, code);
                } catch (Exception e) {
                    log.error("❌ 邮件发送失败: ", e);
                    result.put("code", 500);
                    result.put("message", "邮件发送失败: " + e.getMessage());
                    // 删除 Redis 中的验证码
                    redisService.delete(redisKey);
                }
            } else {
                // 手机号：暂不支持
                result.put("code", 400);
                result.put("message", "暂不支持手机号重置密码");
                // 删除 Redis 中的验证码
                redisService.delete(redisKey);
            }
            
        } catch (Exception e) {
            log.error("发送重置密码验证码失败: ", e);
            result.put("code", 500);
            result.put("message", "发送失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 重置密码
     */
    @PostMapping("/forgot-password/reset")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> data) {
        String emailOrPhone = data.get("emailOrPhone");
        String code = data.get("code");
        String newPassword = data.get("newPassword");
        
        log.info("重置密码请求: {}", emailOrPhone);
        
        Map<String, Object> result = new HashMap<>();
        
        if (emailOrPhone == null || code == null || newPassword == null) {
            result.put("code", 400);
            result.put("message", "参数不完整");
            return result;
        }
        
        if (newPassword.length() < 6) {
            result.put("code", 400);
            result.put("message", "密码长度至少为6位");
            return result;
        }
        
        try {
            // 验证验证码
            String redisKey = "reset_password_code:" + emailOrPhone;
            String storedCode = redisService.get(redisKey);
            
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
            
            // 查找用户
            User user = null;
            if (emailOrPhone.contains("@")) {
                user = userService.findByEmail(emailOrPhone);
            } else {
                user = userService.findByPhone(emailOrPhone);
            }
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateById(user);
            
            // 清除 Redis 中的验证码
            redisService.delete(redisKey);
            
            result.put("code", 200);
            result.put("message", "密码重置成功");
            log.info("用户 {} 密码重置成功", emailOrPhone);
            
        } catch (Exception e) {
            log.error("重置密码失败: ", e);
            result.put("code", 500);
            result.put("message", "重置失败: " + e.getMessage());
        }
        
        return result;
    }
}

