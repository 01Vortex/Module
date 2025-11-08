package com.vortex.loginregister_new.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vortex.loginregister_new.config.MinIOConfig;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.MinIOService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.util.ValidationUtils;
import com.vortex.loginregister_new.util.WebUtils;
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
        boolean deleted = userService.removeById(id);
        
        Map<String, Object> result = new HashMap<>();
        if (deleted) {
            result.put("code", 200);
            result.put("message", "删除成功");
        } else {
            result.put("code", 500);
            result.put("message", "删除失败");
        }
        return result;
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
            
            // 更新邮箱（如果允许）
            if (userData.containsKey("email")) {
                String email = userData.get("email");
                if (email != null && !email.trim().isEmpty()) {
                    email = ValidationUtils.normalizeEmail(email);
                    if (!ValidationUtils.isValidEmail(email)) {
                        result.put("code", 400);
                        result.put("message", "邮箱格式不正确");
                        return result;
                    }
                    // 检查邮箱是否被其他用户使用
                    User emailUser = userService.findByEmail(email);
                    if (emailUser != null && !emailUser.getId().equals(user.getId())) {
                        result.put("code", 400);
                        result.put("message", "邮箱已被使用");
                        return result;
                    }
                    user.setEmail(email);
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
                if (oldAvatarUrl != null && oldAvatarUrl.contains(minIOConfig.getBucketName())) {
                    try {
                        // 从URL中提取objectName
                        String oldObjectName = extractObjectNameFromUrl(oldAvatarUrl);
                        if (oldObjectName != null) {
                            minIOService.deleteFile(minIOConfig.getBucketName(), oldObjectName);
                            log.info("删除旧头像: {}", oldObjectName);
                        }
                    } catch (Exception e) {
                        log.warn("删除旧头像失败: {}", oldAvatarUrl, e);
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
            // URL格式: http://localhost:9000/avatar/avatars/filename.jpg
            if (url.contains("/" + minIOConfig.getBucketName() + "/")) {
                int index = url.indexOf("/" + minIOConfig.getBucketName() + "/");
                return url.substring(index + minIOConfig.getBucketName().length() + 2);
            }
        } catch (Exception e) {
            log.warn("提取objectName失败: {}", url, e);
        }
        return null;
    }
}

