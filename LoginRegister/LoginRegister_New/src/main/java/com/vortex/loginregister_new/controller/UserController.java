package com.vortex.loginregister_new.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vortex.loginregister_new.config.MinIOConfig;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.common.Constants;
import com.vortex.loginregister_new.entity.UserSocial;
import com.vortex.loginregister_new.service.MinIOService;
import com.vortex.loginregister_new.service.RedisService;
import com.vortex.loginregister_new.service.SocialLoginService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.service.UserSocialService;
import com.vortex.loginregister_new.util.ValidationUtils;
import com.vortex.loginregister_new.util.WebUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户控制器
 *
 * @author 01Vortex
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MinIOService minIOService;
    private final MinIOConfig minIOConfig;
    private final PasswordEncoder passwordEncoder;
    private final SocialLoginService socialLoginService;
    private final RedisService redisService;
    private final UserSocialService userSocialService;
    
    // Redis Key 前缀
    private static final String VERIFICATION_CODE_PREFIX = Constants.RedisKey.VERIFICATION_CODE_PREFIX;
    private static final String CODE_ATTEMPT_PREFIX = Constants.RedisKey.CODE_ATTEMPT_PREFIX;
    

    /**
     * 查询用户列表（分页）
     */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        queryWrapper.orderByDesc("create_time");
        
        Page<User> userPage = userService.page(pageParam, queryWrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", userPage);
        return result;
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        
        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("code", 200);
            result.put("data", user);
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
        }
        return result;
    }

    /**
     * 根据账号查询用户
     */
    @GetMapping("/account/{account}")
    public Map<String, Object> getByAccount(@PathVariable String account) {
        User user = userService.findByAccount(account);
        
        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("code", 200);
            result.put("data", user);
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
        }
        return result;
    }

    /**
     * 添加用户
     */
    @PostMapping
    public Map<String, Object> add(@RequestBody User user) {
        boolean saved = userService.save(user);
        
        Map<String, Object> result = new HashMap<>();
        if (saved) {
            result.put("code", 200);
            result.put("message", "添加成功");
            result.put("data", user);
        } else {
            result.put("code", 500);
            result.put("message", "添加失败");
        }
        return result;
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Map<String, Object> update(@RequestBody User user) {
        boolean updated = userService.updateById(user);
        
        Map<String, Object> result = new HashMap<>();
        if (updated) {
            result.put("code", 200);
            result.put("message", "更新成功");
        } else {
            result.put("code", 500);
            result.put("message", "更新失败");
        }
        return result;
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取用户信息
            User user = userService.getById(id);
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 删除用户头像（如果存在）
            deleteUserAvatar(user);
            
            // 逻辑删除
            boolean deleted = userService.removeById(id);
            if (deleted) {
                result.put("code", 200);
                result.put("message", "删除成功");
                log.info("用户 {} 删除成功 - 用户ID: {}", user.getAccount(), id);
            } else {
                result.put("code", 500);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败 - 用户ID: {}", id, e);
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 删除用户头像（从MinIO中删除）
     */
    private void deleteUserAvatar(User user) {
        if (user == null || user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
            return;
        }
        
        String avatarUrl = user.getAvatar();
        try {
            // 检查是否是MinIO中的文件
            if (avatarUrl.contains(minIOConfig.getBucketName()) || avatarUrl.contains("avatars/")) {
                // 从URL中提取objectName
                String objectName = extractObjectNameFromUrl(avatarUrl);
                if (objectName != null && !objectName.trim().isEmpty()) {
                    minIOService.deleteFile(minIOConfig.getBucketName(), objectName);
                    log.info("删除用户头像成功 - 用户ID: {}, objectName: {}", user.getId(), objectName);
                } else {
                    log.warn("无法从URL中提取objectName - 用户ID: {}, avatarUrl: {}", user.getId(), avatarUrl);
                }
            }
        } catch (Exception e) {
            // 删除头像失败不影响用户删除操作，只记录警告
            log.warn("删除用户头像失败 - 用户ID: {}, avatarUrl: {}, 错误: {}", 
                    user.getId(), avatarUrl, e.getMessage());
        }
    }

    /**
     * 检查账号是否存在
     */
    @GetMapping("/check/account/{account}")
    public Map<String, Object> checkAccount(@PathVariable String account) {
        boolean exists = userService.isAccountExists(account);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("exists", exists);
        return result;
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check/email/{email}")
    public Map<String, Object> checkEmail(@PathVariable String email) {
        boolean exists = userService.isEmailExists(email);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("exists", exists);
        return result;
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public Map<String, Object> getCurrentUserProfile() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从SecurityContext获取当前用户账号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }
            
            String account = authentication.getName();
            User user = userService.findByAccount(account);
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 清除密码
            user.setPassword(null);
            
            result.put("code", 200);
            result.put("data", user);
            return result;
            
        } catch (Exception e) {
            log.error("获取用户信息失败: ", e);
            result.put("code", 500);
            result.put("message", "获取用户信息失败");
            return result;
        }
    }
    
    /**
     * 更新当前登录用户个人信息
     */
    @PutMapping("/profile")
    public Map<String, Object> updateCurrentUserProfile(@RequestBody Map<String, String> userData, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从SecurityContext获取当前用户账号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }
            
            String account = authentication.getName();
            User user = userService.findByAccount(account);
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 更新昵称
            if (userData.containsKey("nickname")) {
                String nickname = userData.get("nickname");
                if (nickname != null && !nickname.trim().isEmpty()) {
                    nickname = ValidationUtils.sanitizeInput(nickname.trim());
                    if (nickname.length() > 50) {
                        nickname = nickname.substring(0, 50);
                    }
                    user.setNickname(nickname);
                }
            }
            
            // 更新邮箱（如果允许）- 需要验证码
            if (userData.containsKey("email")) {
                String email = userData.get("email");
                if (email != null && !email.trim().isEmpty()) {
                    email = ValidationUtils.normalizeEmail(email);
                    if (!ValidationUtils.isValidEmail(email)) {
                        result.put("code", 400);
                        result.put("message", "邮箱格式不正确");
                        return result;
                    }
                    
					// 如果邮箱没有变化，则不需要验证码，直接跳过
					String currentEmail = user.getEmail();
					boolean emailChanged = currentEmail == null ? true : !email.equalsIgnoreCase(currentEmail);
					if (emailChanged) {
                    // 检查邮箱是否被其他用户使用
                    User emailUser = userService.findByEmail(email);
                    if (emailUser != null && !emailUser.getId().equals(user.getId())) {
                        result.put("code", 400);
                        result.put("message", "邮箱已被使用");
                        return result;
                    }
                    
						// 验证邮箱验证码（仅当更换邮箱时需要）
                    String code = userData.get("code");
                    if (code == null || code.trim().isEmpty()) {
                        result.put("code", 400);
                        result.put("message", "请输入邮箱验证码");
                        return result;
                    }
                    
                    // 从 Redis 验证验证码（邮箱统一小写）
                    String identifier = email.toLowerCase();
                    String redisKey = VERIFICATION_CODE_PREFIX + identifier;
                    String storedCode = redisService.get(redisKey);
                    
                    if (storedCode == null) {
                        result.put("code", 400);
                        result.put("message", "验证码已过期或不存在，请重新获取");
                        return result;
                    }
                    
                    if (!storedCode.equals(code.trim())) {
                        result.put("code", 400);
                        result.put("message", "验证码错误");
                        return result;
                    }
                    
                    // 验证码正确，清除验证码
                    redisService.delete(redisKey);
                    redisService.delete(CODE_ATTEMPT_PREFIX + identifier);
                    
                    user.setEmail(email);
                    log.info("用户 {} 绑定/更换邮箱成功: {}, IP: {}", account, email, WebUtils.getClientIp(request));
					} // 未更换邮箱则不做任何修改，也不校验验证码
                }
            }
            
            // 更新手机号（如果允许）
            if (userData.containsKey("phone")) {
                String phone = userData.get("phone");
                if (phone != null && !phone.trim().isEmpty()) {
                    user.setPhone(phone.trim());
                }
            }
            
            // 不允许通过此接口修改密码和账号
            boolean updated = userService.updateById(user);
            
            if (updated) {
                user.setPassword(null);
                result.put("code", 200);
                result.put("message", "更新成功");
                result.put("data", user);
                log.info("用户 {} 更新个人信息成功，IP: {}", account, WebUtils.getClientIp(request));
            } else {
                result.put("code", 500);
                result.put("message", "更新失败");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("更新用户信息失败: ", e);
            result.put("code", 500);
            result.put("message", "更新用户信息失败");
            return result;
        }
    }
    
    /**
     * 设置密码（用于第三方登录用户设置密码）
     */
    @PostMapping("/set-password")
    public Map<String, Object> setPassword(@RequestBody Map<String, String> passwordData, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从SecurityContext获取当前用户账号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }
            
            String account = authentication.getName();
            User user = userService.findByAccount(account);
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            String password = passwordData.get("password");
            if (password == null || password.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "密码不能为空");
                return result;
            }
            
            // 验证密码强度
            String passwordError = ValidationUtils.getPasswordValidationError(password);
            if (passwordError != null) {
                result.put("code", 400);
                result.put("message", passwordError);
                return result;
            }
            
            // 判断是设置密码还是重置密码
            boolean hasPassword = user.getPassword() != null && !user.getPassword().trim().isEmpty();
            String oldPassword = passwordData.get("oldPassword");
            String code = passwordData.get("code");
            
            if (hasPassword) {
                // 重置密码：有两种方式
                if (oldPassword != null && !oldPassword.trim().isEmpty()) {
                    // 方式一：使用旧密码重置（不需要验证码）
                    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                        result.put("code", 400);
                        result.put("message", "旧密码错误");
                        return result;
                    }
                    // 旧密码验证通过，直接重置密码
                } else if (code != null && !code.trim().isEmpty()) {
                    // 方式二：使用邮箱验证码重置（不需要旧密码）
                    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                        result.put("code", 400);
                        result.put("message", "您尚未绑定邮箱，无法使用验证码方式重置密码");
                        return result;
                    }
                    
                    // 从 Redis 验证验证码（使用用户邮箱）
                    String identifier = user.getEmail().toLowerCase();
                    String redisKey = VERIFICATION_CODE_PREFIX + identifier;
                    String storedCode = redisService.get(redisKey);
                    
                    if (storedCode == null) {
                        result.put("code", 400);
                        result.put("message", "验证码已过期或不存在，请重新获取");
                        return result;
                    }
                    
                    if (!storedCode.equals(code.trim())) {
                        result.put("code", 400);
                        result.put("message", "验证码错误");
                        return result;
                    }
                    
                    // 验证码正确，清除验证码
                    redisService.delete(redisKey);
                    redisService.delete(CODE_ATTEMPT_PREFIX + identifier);
                } else {
                    // 两种方式都没有提供
                    result.put("code", 400);
                    result.put("message", "重置密码需要提供旧密码或邮箱验证码");
                    return result;
                }
            } else {
                // 设置密码：判断是否是第三方登录用户
                boolean isSocialUser = "SOCIAL".equals(user.getAccountType()) || 
                        userSocialService.lambdaQuery()
                                .eq(UserSocial::getUserId, user.getId())
                                .count() > 0;
                
                if (isSocialUser) {
                    // 第三方登录用户第一次设置密码：不需要邮箱验证码
                    // 直接允许设置密码
                    log.info("第三方用户首次设置密码，无需验证码 - account: {}, accountType: {}", 
                            account, user.getAccountType());
                } else {
                    // 非第三方用户设置密码：需要邮箱验证码
                    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                        result.put("code", 400);
                        result.put("message", "设置密码需要先绑定邮箱，请先绑定邮箱");
                        return result;
                    }
                    
                    if (code == null || code.trim().isEmpty()) {
                        result.put("code", 400);
                        result.put("message", "请输入邮箱验证码");
                        return result;
                    }
                    
                    // 从 Redis 验证验证码（使用用户邮箱）
                    String identifier = user.getEmail().toLowerCase();
                    String redisKey = VERIFICATION_CODE_PREFIX + identifier;
                    String storedCode = redisService.get(redisKey);
                    
                    if (storedCode == null) {
                        result.put("code", 400);
                        result.put("message", "验证码已过期或不存在，请重新获取");
                        return result;
                    }
                    
                    if (!storedCode.equals(code.trim())) {
                        result.put("code", 400);
                        result.put("message", "验证码错误");
                        return result;
                    }
                    
                    // 验证码正确，清除验证码
                    redisService.delete(redisKey);
                    redisService.delete(CODE_ATTEMPT_PREFIX + identifier);
                }
            }
            
            // 加密新密码
            user.setPassword(passwordEncoder.encode(password));
            boolean updated = userService.updateById(user);
            
            if (updated) {
                // 更新账户类型
                socialLoginService.updateAccountType(user.getId());
                
                String clientIp = WebUtils.getClientIp(request);
                boolean wasPasswordSet = user.getPassword() != null && !user.getPassword().trim().isEmpty();
                log.info("用户设置/修改密码成功 - account: {}, IP: {}, wasPasswordSet: {}", account, clientIp, wasPasswordSet);
                
                // 重新查询用户以获取最新的账户类型
                user = userService.getById(user.getId());
                
                result.put("code", 200);
                result.put("message", "密码设置成功");
                result.put("data", Map.of("accountType", user != null ? user.getAccountType() : "UNKNOWN"));
            } else {
                result.put("code", 500);
                result.put("message", "设置密码失败");
            }
            
        } catch (Exception e) {
            log.error("设置密码失败: ", e);
            result.put("code", 500);
            result.put("message", "设置密码失败");
        }
        
        return result;
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从SecurityContext获取当前用户账号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }
            
            String account = authentication.getName();
            User user = userService.findByAccount(account);
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 验证文件
            if (file == null || file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "请选择文件");
                return result;
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("code", 400);
                result.put("message", "只能上传图片文件");
                return result;
            }
            
            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("code", 400);
                result.put("message", "文件大小不能超过5MB");
                return result;
            }
            
            // 生成文件名（统一使用jpg格式）
            String filename = UUID.randomUUID().toString() + ".jpg";
            String objectName = "avatars/" + filename;
            
            // 处理图片：压缩和尺寸调整（前端已经裁剪，这里只需要压缩和统一尺寸）
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                Thumbnails.of(file.getInputStream())
                        .outputFormat("jpg") // 统一使用jpg格式
                        .outputQuality(0.85) // 质量85%
                        .size(400, 400) // 统一尺寸400x400
                        .keepAspectRatio(false) // 不保持宽高比，强制400x400（因为前端已经裁剪为1:1）
                        .toOutputStream(outputStream);
            } catch (IOException e) {
                log.error("图片处理失败: ", e);
                result.put("code", 500);
                result.put("message", "图片处理失败: " + e.getMessage());
                return result;
            }
            
            // 上传到MinIO
            byte[] imageData = outputStream.toByteArray();
            String avatarUrl;
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData)) {
                avatarUrl = minIOService.uploadFile(
                        minIOConfig.getBucketName(),
                        objectName,
                        inputStream,
                        "image/jpeg",
                        imageData.length
                );
            }
            
            // 保存旧头像URL（用于删除）
            String oldAvatarUrl = user.getAvatar();
            
            // 更新用户头像
            user.setAvatar(avatarUrl);
            boolean updated = userService.updateById(user);
            
            if (updated) {
                // 删除旧头像（如果存在且是MinIO中的文件）
                if (oldAvatarUrl != null && 
                    (oldAvatarUrl.contains(minIOConfig.getBucketName()) || oldAvatarUrl.contains("avatars/"))) {
                    try {
                        // 从URL中提取objectName
                        String oldObjectName = extractObjectNameFromUrl(oldAvatarUrl);
                        if (oldObjectName != null && !oldObjectName.trim().isEmpty()) {
                            minIOService.deleteFile(minIOConfig.getBucketName(), oldObjectName);
                            log.info("删除旧头像成功: {}", oldObjectName);
                        } else {
                            log.warn("无法从URL中提取objectName - avatarUrl: {}", oldAvatarUrl);
                        }
                    } catch (Exception e) {
                        // 删除旧头像失败不影响新头像上传，只记录警告
                        log.warn("删除旧头像失败: {}, 错误: {}", oldAvatarUrl, e.getMessage());
                    }
                }
                
                user.setPassword(null);
                Map<String, Object> userData = new HashMap<>();
                userData.put("avatar", avatarUrl);
                
                result.put("code", 200);
                result.put("message", "上传成功");
                result.put("data", userData);
                log.info("用户 {} 上传头像成功，IP: {}", account, WebUtils.getClientIp(request));
            } else {
                result.put("code", 500);
                result.put("message", "更新头像失败");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("上传头像失败: ", e);
            result.put("code", 500);
            result.put("message", "上传头像失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 从URL中提取objectName
     */
    private String extractObjectNameFromUrl(String url) {
        try {
            // URL格式: http://localhost:9000/bucket-name/avatars/filename.jpg
            // 或者: http://localhost:9000/avatar/avatars/filename.jpg
            if (url == null || url.trim().isEmpty()) {
                return null;
            }
            
            // 检查是否包含存储桶名称
            String bucketName = minIOConfig.getBucketName();
            if (url.contains("/" + bucketName + "/")) {
                int index = url.indexOf("/" + bucketName + "/");
                return url.substring(index + bucketName.length() + 2);
            }
            
            // 如果没有包含存储桶名称，尝试从路径中提取（可能是相对路径）
            // 例如：/avatars/filename.jpg 或 avatars/filename.jpg
            if (url.contains("avatars/")) {
                int index = url.indexOf("avatars/");
                return url.substring(index);
            }
            
        } catch (Exception e) {
            log.warn("提取objectName失败: {}", url, e);
        }
        return null;
    }
    
    /**
     * 获取当前用户的第三方账号列表
     */
    @GetMapping("/social-accounts")
    public Map<String, Object> getCurrentUserSocialAccounts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从SecurityContext获取当前用户账号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }
            
            String account = authentication.getName();
            User user = userService.findByAccount(account);
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 查询用户的第三方账号列表
            List<UserSocial> socialAccounts = userSocialService.lambdaQuery()
                    .eq(UserSocial::getUserId, user.getId())
                    .list();
            
            result.put("code", 200);
            result.put("data", socialAccounts);
            return result;
            
        } catch (Exception e) {
            log.error("获取第三方账号列表失败: ", e);
            result.put("code", 500);
            result.put("message", "获取第三方账号列表失败");
            return result;
        }
    }
    
    /**
     * 解绑第三方账号
     */
    @DeleteMapping("/social-accounts/{provider}")
    public Map<String, Object> unbindSocialAccount(@PathVariable String provider, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从SecurityContext获取当前用户账号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return result;
            }
            
            String account = authentication.getName();
            User user = userService.findByAccount(account);
            
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 查询要解绑的第三方账号
            UserSocial userSocial = userSocialService.lambdaQuery()
                    .eq(UserSocial::getUserId, user.getId())
                    .eq(UserSocial::getProvider, provider)
                    .one();
            
            if (userSocial == null) {
                result.put("code", 404);
                result.put("message", "未绑定该第三方账号");
                return result;
            }
            
            // 检查是否还有其他登录方式（密码或其他第三方账号）
            boolean hasPassword = user.getPassword() != null && !user.getPassword().trim().isEmpty();
            long otherSocialCount = userSocialService.lambdaQuery()
                    .eq(UserSocial::getUserId, user.getId())
                    .ne(UserSocial::getProvider, provider)
                    .count();
            
            if (!hasPassword && otherSocialCount == 0) {
                result.put("code", 400);
                result.put("message", "至少需要保留一种登录方式，无法解绑最后一个第三方账号");
                return result;
            }
            
            // 删除第三方账号绑定
            boolean deleted = userSocialService.removeById(userSocial.getId());
            
            if (deleted) {
                // 更新账户类型
                socialLoginService.updateAccountType(user.getId());
                
                result.put("code", 200);
                result.put("message", "解绑成功");
                log.info("用户 {} 解绑第三方账号成功 - provider: {}, IP: {}", account, provider, WebUtils.getClientIp(request));
            } else {
                result.put("code", 500);
                result.put("message", "解绑失败");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("解绑第三方账号失败: ", e);
            result.put("code", 500);
            result.put("message", "解绑第三方账号失败");
            return result;
        }
    }
}

