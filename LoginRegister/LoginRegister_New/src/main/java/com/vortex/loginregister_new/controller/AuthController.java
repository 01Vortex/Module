package com.vortex.loginregister_new.controller;

import com.vortex.loginregister_new.common.Constants;
import com.vortex.loginregister_new.entity.Admin;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.AdminService;
import com.vortex.loginregister_new.service.EmailService;
import com.vortex.loginregister_new.service.JwtBlacklistService;
import com.vortex.loginregister_new.service.LoginAttemptService;
import com.vortex.loginregister_new.service.RateLimitService;
import com.vortex.loginregister_new.service.RedisService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.util.JwtUtil;
import com.vortex.loginregister_new.util.ValidationUtils;
import com.vortex.loginregister_new.util.VerificationCodeUtils;
import com.vortex.loginregister_new.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器 - 处理登录、注册等认证相关功能
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;
    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final RateLimitService rateLimitService;
    private final JwtBlacklistService jwtBlacklistService;

    public AuthController(
            UserService userService,
            AdminService adminService,
            PasswordEncoder passwordEncoder,
            RedisService redisService,
            @Autowired(required = false) EmailService emailService,
            JwtUtil jwtUtil,
            LoginAttemptService loginAttemptService,
            RateLimitService rateLimitService,
            JwtBlacklistService jwtBlacklistService) {
        this.userService = userService;
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
        this.rateLimitService = rateLimitService;
        this.jwtBlacklistService = jwtBlacklistService;
    }
    
    // Redis Key 前缀
    private static final String VERIFICATION_CODE_PREFIX = Constants.RedisKey.VERIFICATION_CODE_PREFIX;
    private static final String CODE_ATTEMPT_PREFIX = Constants.RedisKey.CODE_ATTEMPT_PREFIX;

    /**
     * 用户登录（密码方式）
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String account = loginData.get("account");
        String password = loginData.get("password");
        String clientIp = WebUtils.getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        // 请求频率限制：每个IP每分钟最多5次登录尝试
        if (rateLimitService.isRateLimited("login:" + clientIp, 5, 60)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        if (account == null || password == null) {
            result.put("code", 400);
            result.put("message", "账号和密码不能为空");
            return result;
        }
        
        // 标准化标识（邮箱统一小写）
        String identifier = account.contains("@") ? account.toLowerCase() : account;
        
        try {
            // 检查账户是否被锁定
            if (loginAttemptService.isBlocked(identifier)) {
                long remainingTime = loginAttemptService.getLockRemainingTime(identifier);
                result.put("code", 423);
                result.put("message", String.format("账户已被锁定，请%d分钟后重试", remainingTime));
                return result;
            }
            
            // 支持账号或邮箱登录
            User user = userService.findByAccount(identifier);
            if (user == null && identifier.contains("@")) {
                user = userService.findByEmail(identifier);
            }
            
            // 检查用户是否设置了密码
            if (user != null && (user.getPassword() == null || user.getPassword().trim().isEmpty())) {
                // 用户没有设置密码，只能通过第三方登录
                log.warn("用户尝试使用密码登录，但该用户未设置密码 - account: {}", identifier);
                result.put("code", 400);
                result.put("message", "该账号未设置密码，请使用第三方登录");
                return result;
            }
            
            // 无论用户是否存在，都进行密码验证（防止用户枚举攻击）
            boolean passwordValid = false;
            if (user != null && user.getPassword() != null) {
                passwordValid = passwordEncoder.matches(password, user.getPassword());
            }
            
            if (user == null || !passwordValid) {
                // 记录登录失败
                if (user != null) {
                    loginAttemptService.loginFailed(identifier);
                    int remaining = loginAttemptService.getRemainingAttempts(identifier);
                    result.put("code", 401);
                    result.put("message", "账号或密码错误");
                    if (remaining > 0) {
                        result.put("remainingAttempts", remaining);
            }
                } else {
                    // 用户不存在时也记录失败（防止用户枚举）
                    loginAttemptService.loginFailed(identifier);
                result.put("code", 401);
                    result.put("message", "账号或密码错误");
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
            
            // 获取用户角色（默认为普通用户）
            List<String> roles = userService.getUserRoles(user.getId());
            String role = roles != null && !roles.isEmpty() ? roles.get(0) : "ROLE_USER";
            
            // 生成JWT token（包含角色信息）
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getAccount(), role);
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getAccount(), role);
            
            // 返回用户信息（不包含密码）
            user.setPassword(null);
            
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            
            log.info("用户 {} 登录成功，IP: {}", account, clientIp);
            
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
        String clientIp = WebUtils.getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        // 请求频率限制：每个IP每小时最多10次注册尝试
        if (rateLimitService.isRateLimited("register:" + clientIp, 10, 3600)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        // 验证密码强度
        String passwordError = ValidationUtils.getPasswordValidationError(password);
        if (passwordError != null) {
            result.put("code", 400);
            result.put("message", passwordError);
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
            
            // 创建用户对象（账号为10位随机数字，需保证唯一）
            User user = new User();
            String generatedAccount = generateUniqueAccount(10);
            user.setAccount(generatedAccount);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setPhone(phone);
            
            // 使用前端传入的昵称（可为空时给一个默认昵称）
            if (nickname == null || nickname.trim().isEmpty()) {
                nickname = "新用户";
            }
            // 防止XSS：清理昵称
            user.setNickname(ValidationUtils.sanitizeInput(nickname.trim()));
            
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
                log.info("用户 {} 注册成功，昵称: {}，IP: {}", user.getAccount(), user.getNickname(), clientIp);
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
     * 生成唯一的10位数字账号（如发生碰撞则重试）
     */
    private String generateUniqueAccount(int length) {
        String candidate;
        int attempts = 0;
        do {
            candidate = VerificationCodeUtils.generateCode(length);
            attempts++;
            if (attempts > 10) {
                // 极端情况下增加一位长度降碰撞风险
                candidate = VerificationCodeUtils.generateCode(length + 1);
            }
        } while (userService.isAccountExists(candidate));
        return candidate;
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Map<String, Object> sendVerificationCode(@RequestBody Map<String, String> data, HttpServletRequest request) {
        String account = data.get("account");
        if (account != null) {
            account = account.trim();
        }
        String clientIp = WebUtils.getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        if (account == null || account.isEmpty()) {
            result.put("code", 400);
            result.put("message", "账号或邮箱/手机号不能为空");
            return result;
        }
        
        // 标准化标识（邮箱统一小写，其他原样）
        String identifier = account.contains("@") ? account.toLowerCase() : account;
        
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
            String code = VerificationCodeUtils.generateSixDigitCode();
            
            // Redis 存储验证码（5分钟有效）
            String redisKey = VERIFICATION_CODE_PREFIX + identifier;
            redisService.set(redisKey, code, 5, TimeUnit.MINUTES);
            
            // 清除验证码尝试次数
            redisService.delete(CODE_ATTEMPT_PREFIX + identifier);
            
            log.info("验证码已生成并存储到Redis: {} (用户: {}, 有效期: 5分钟)", code, account);
            
            // 判断是邮箱还是手机号
            if (account.contains("@")) {
                // 邮箱：发送邮件验证码
                if (emailService == null) {
                    result.put("code", 503);
                    result.put("message", "邮件服务未配置，无法发送验证码");
                    // 删除 Redis 中的验证码
                    redisService.delete(redisKey);
                } else {
                    try {
                        emailService.sendVerificationCode(account, code);
                        result.put("code", 200);
                        result.put("message", "验证码已发送到邮箱，请查收");
                        // 开发环境：在日志中显示验证码（查看后端日志）
                        log.info("✅ 验证码邮件已发送到: {}，验证码: {}", account, code);
                    } catch (Exception e) {
                        log.error("❌ 邮件发送失败: ", e);
                        result.put("code", 500);
                        result.put("message", "邮件发送失败，请稍后重试");
                        // 删除 Redis 中的验证码
                        redisService.delete(redisKey);
                    }
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
        String account = loginData.get("account");
        String code = loginData.get("code");
        if (account != null) account = account.trim();
        if (code != null) code = code.trim();
        String clientIp = WebUtils.getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        // 请求频率限制
        if (rateLimitService.isRateLimited("login_code:" + clientIp, 5, 60)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        if (account == null || account.isEmpty() || code == null || code.isEmpty()) {
            result.put("code", 400);
            result.put("message", "账号和验证码不能为空");
            return result;
        }
        
        try {
            // 从 Redis 验证验证码（邮箱统一小写）
            String identifier = account.contains("@") ? account.toLowerCase() : account;
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
                if (attempts >= Constants.VerificationCode.MAX_ATTEMPTS) {
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
                result.put("remainingAttempts", Constants.VerificationCode.MAX_ATTEMPTS - attempts);
                return result;
            }
            
            // 查询用户（账号或邮箱）
            User user = userService.findByAccount(identifier);
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
            
            // 获取用户角色（默认为普通用户）
            List<String> roles = userService.getUserRoles(user.getId());
            String role = roles != null && !roles.isEmpty() ? roles.get(0) : "ROLE_USER";
            
            // 生成JWT token（包含角色信息）
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getAccount(), role);
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getAccount(), role);
            
            // 返回用户信息
            user.setPassword(null);
            
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            
            log.info("用户 {} 通过验证码登录成功，IP: {}", account, clientIp);
            
        } catch (Exception e) {
            log.error("验证码登录失败: ", e);
            result.put("code", 500);
            result.put("message", "登录失败，请稍后重试");
        }
        
        return result;
    }

    /**
     * 管理员登录（只能登录，不能注册）
     */
    @PostMapping("/admin/login")
    public Map<String, Object> adminLogin(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String account = loginData.get("account");
        String password = loginData.get("password");
        String clientIp = WebUtils.getClientIp(request);
        
        Map<String, Object> result = new HashMap<>();
        
        // 请求频率限制
        if (rateLimitService.isRateLimited("admin_login:" + clientIp, 5, 60)) {
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后再试");
            return result;
        }
        
        if (account == null || password == null) {
            result.put("code", 400);
            result.put("message", "账号和密码不能为空");
            return result;
        }
        
        try {
            // 从admin表查询管理员
            Admin admin = adminService.findByAccount(account);
            
            log.debug("管理员登录尝试 - 账号: {}, 查询结果: {}", account, admin != null ? "找到" : "未找到");
            
            if (admin == null) {
                log.warn("管理员登录失败 - 账号不存在: {}", account);
                result.put("code", 401);
                result.put("message", "账号或密码错误");
                return result;
            }
            
            log.debug("管理员登录 - 账号: {}, 状态: {}, 密码hash: {}", account, admin.getStatus(), admin.getPassword() != null ? "已设置" : "未设置");
            
            // 验证密码
            boolean passwordMatches = passwordEncoder.matches(password, admin.getPassword());
            log.debug("密码验证结果: {}", passwordMatches);
            
            if (!passwordMatches) {
                log.warn("管理员登录失败 - 密码错误: {}", account);
                result.put("code", 401);
                result.put("message", "账号或密码错误");
                return result;
            }
            
            // 检查管理员状态
            if (admin.getStatus() == 0) {
                result.put("code", 403);
                result.put("message", "账号已被禁用");
                return result;
            }
            
            // 更新登录信息
            adminService.updateLastLoginInfo(admin.getId(), clientIp);
            
            // 生成JWT token（管理员固定为ROLE_ADMIN）
            String role = "ROLE_ADMIN";
            String accessToken = jwtUtil.generateAccessToken(admin.getId(), admin.getAccount(), role);
            String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), admin.getAccount(), role);
            
            // 返回管理员信息（不包含密码）
            admin.setPassword(null);
            
            // 构建返回数据（兼容前端）
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", admin.getId());
            adminData.put("account", admin.getAccount());
            adminData.put("name", admin.getName());
            adminData.put("email", admin.getEmail());
            adminData.put("phone", admin.getPhone());
            adminData.put("status", admin.getStatus());
            adminData.put("lastLoginTime", admin.getLastLoginTime());
            adminData.put("lastLoginIp", admin.getLastLoginIp());
            
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", adminData);
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("role", role);
            
            log.info("管理员 {} 登录成功，IP: {}", account, clientIp);
            
        } catch (Exception e) {
            log.error("管理员登录失败: ", e);
            result.put("code", 500);
            result.put("message", "登录失败，请稍后重试");
        }
        
        return result;
    }

    /**
     * 管理员发送重置密码验证码（仅支持邮箱）
     */
    @PostMapping("/admin/forgot-password/send-code")
    public Map<String, Object> sendAdminResetPasswordCode(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        
        log.info("管理员发送重置密码验证码请求: {}", email);
        
        Map<String, Object> result = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "邮箱不能为空");
            return result;
        }
        
        // 验证邮箱格式
        if (!email.contains("@")) {
            result.put("code", 400);
            result.put("message", "邮箱格式不正确");
            return result;
        }
        
        try {
            // 通过邮箱查找管理员
            Admin admin = adminService.findByEmail(email);
            
            if (admin == null) {
                result.put("code", 404);
                result.put("message", "该邮箱未绑定管理员账号");
                return result;
            }
            
            // 生成6位数字验证码
            String code = VerificationCodeUtils.generateSixDigitCode();
            
            // Redis 存储验证码（15分钟有效）
            String redisKey = "admin_reset_password_code:" + email;
            redisService.set(redisKey, code, 15, TimeUnit.MINUTES);
            
            log.info("管理员重置密码验证码已生成并存储到Redis: {} (邮箱: {}, 有效期: 15分钟)", code, email);
            
            // 发送邮件
            if (emailService == null) {
                result.put("code", 503);
                result.put("message", "邮件服务未配置，无法发送验证码");
            } else {
                try {
                    emailService.sendResetPasswordCode(email, code);
                    result.put("code", 200);
                    result.put("message", "验证码已发送到您的邮箱，请查收");
                } catch (Exception e) {
                    log.error("发送管理员重置密码验证码邮件失败: ", e);
                    result.put("code", 500);
                    result.put("message", "发送验证码失败，请稍后重试");
                }
            }
            
        } catch (Exception e) {
            log.error("发送管理员重置密码验证码失败: ", e);
            result.put("code", 500);
            result.put("message", "发送验证码失败，请稍后重试");
        }
        
        return result;
    }

    /**
     * 管理员重置密码（仅支持邮箱）
     */
    @PostMapping("/admin/forgot-password/reset")
    public Map<String, Object> resetAdminPassword(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        String code = data.get("code");
        String newPassword = data.get("newPassword");
        
        log.info("管理员重置密码请求: {}", email);
        
        Map<String, Object> result = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "邮箱不能为空");
            return result;
        }
        
        if (code == null || code.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "验证码不能为空");
            return result;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "新密码不能为空");
            return result;
        }
        
        // 验证密码强度
            String passwordError = ValidationUtils.getPasswordValidationError(newPassword);
            if (passwordError != null) {
                result.put("code", 400);
                result.put("message", passwordError);
                return result;
            }
        
        try {
            // 验证验证码
            String redisKey = "admin_reset_password_code:" + email;
            String storedCode = redisService.get(redisKey);
            
            if (storedCode == null || !storedCode.equals(code)) {
                result.put("code", 400);
                result.put("message", "验证码错误或已过期");
                return result;
            }
            
            // 通过邮箱查找管理员
            Admin admin = adminService.findByEmail(email);
            
            if (admin == null) {
                result.put("code", 404);
                result.put("message", "该邮箱未绑定管理员账号");
                return result;
            }
            
            // 加密新密码
            String encodedPassword = passwordEncoder.encode(newPassword);
            admin.setPassword(encodedPassword);
            
            // 更新密码
            boolean updated = adminService.updateById(admin);
            
            if (updated) {
                // 删除验证码
                redisService.delete(redisKey);
                
                result.put("code", 200);
                result.put("message", "密码重置成功，请重新登录");
                log.info("管理员 {} 密码重置成功", email);
            } else {
                result.put("code", 500);
                result.put("message", "密码重置失败，请稍后重试");
            }
            
        } catch (Exception e) {
            log.error("管理员重置密码失败: ", e);
            result.put("code", 500);
            result.put("message", "密码重置失败，请稍后重试");
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
            String account = jwtUtil.getAccountFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String role = jwtUtil.getRoleFromToken(refreshToken);
            
            if (account == null || userId == null) {
                result.put("code", 401);
                result.put("message", "无效的刷新令牌");
                return result;
            }
            
            // 检查用户token是否已失效（密码修改后）
            // 只检查用户token，不检查管理员token
            if (!"ROLE_ADMIN".equals(role) && jwtBlacklistService.isUserTokenInvalidated(userId)) {
                result.put("code", 401);
                result.put("message", "密码已修改，请重新登录");
                return result;
            }
            
            // 生成新的访问令牌（包含角色信息）
            String newAccessToken = jwtUtil.generateAccessToken(userId, account, role);
            
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
            String code = VerificationCodeUtils.generateSixDigitCode();
            
            // Redis 存储验证码（15分钟有效）
            String redisKey = "reset_password_code:" + emailOrPhone;
            redisService.set(redisKey, code, 15, TimeUnit.MINUTES);
            
            log.info("重置密码验证码已生成并存储到Redis: {} (用户: {}, 有效期: 15分钟)", code, emailOrPhone);
            
            // 判断是邮箱还是手机号
            if (emailOrPhone.contains("@")) {
                // 邮箱：发送邮件验证码
                if (emailService == null) {
                    result.put("code", 503);
                    result.put("message", "邮件服务未配置，无法发送验证码");
                    // 删除 Redis 中的验证码
                    redisService.delete(redisKey);
                } else {
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
        
        // 验证密码强度
        String passwordError = ValidationUtils.getPasswordValidationError(newPassword);
        if (passwordError != null) {
            result.put("code", 400);
            result.put("message", passwordError);
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
            
            // 标记用户的所有token失效（7天内密码修改后使token失效）
            jwtBlacklistService.invalidateUserTokens(user.getId(), 7);
            
            // 清除 Redis 中的验证码
            redisService.delete(redisKey);
            
            result.put("code", 200);
            result.put("message", "密码重置成功");
            log.info("用户 {} 密码重置成功，已使所有token失效", emailOrPhone);
            
        } catch (Exception e) {
            log.error("重置密码失败: ", e);
            result.put("code", 500);
            result.put("message", "重置失败: " + e.getMessage());
        }
        
        return result;
    }
}

