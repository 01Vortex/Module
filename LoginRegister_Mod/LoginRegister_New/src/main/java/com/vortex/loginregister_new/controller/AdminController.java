package com.vortex.loginregister_new.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 管理员控制器
 * 提供用户统计和用户管理功能
 * 所有接口都需要管理员权限
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    // XSS防护：清理HTML标签和特殊字符
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?i)<script[^>]*>.*?</script>");

    /**
     * 用户统计图表数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getStatistics(HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        log.info("管理员 {} 获取统计信息，IP: {}", adminAccount, getClientIp(request));
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 总用户数（MyBatis Plus 的 @TableLogic 会自动处理逻辑删除）
            long totalUsers = userService.count();
            
            // 活跃用户数（最近30天登录）
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long activeUsers = userService.count(new QueryWrapper<User>()
                    .ge("last_login_time", thirtyDaysAgo));
            
            // 今日新增用户数
            LocalDate today = LocalDate.now();
            long todayNewUsers = userService.count(new QueryWrapper<User>()
                    .ge("create_time", today.atStartOfDay()));
            
            // 禁用用户数
            long disabledUsers = userService.count(new QueryWrapper<User>()
                    .eq("status", 0));
            
            // 最近7天每日新增用户数（用于折线图）
            Map<String, Long> dailyNewUsers = new HashMap<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                
                long count = userService.count(new QueryWrapper<User>()
                        .ge("create_time", startOfDay)
                        .lt("create_time", endOfDay));
                
                dailyNewUsers.put(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), count);
            }
            
            // 用户状态分布（用于饼图）
            Map<String, Long> statusDistribution = new HashMap<>();
            statusDistribution.put("正常", userService.count(new QueryWrapper<User>()
                    .eq("status", 1)));
            statusDistribution.put("禁用", disabledUsers);
            
            result.put("code", 200);
            result.put("message", "获取统计成功");
            Map<String, Object> data = new HashMap<>();
            data.put("totalUsers", totalUsers);
            data.put("activeUsers", activeUsers);
            data.put("todayNewUsers", todayNewUsers);
            data.put("disabledUsers", disabledUsers);
            data.put("dailyNewUsers", dailyNewUsers);
            data.put("statusDistribution", statusDistribution);
            result.put("data", data);
            
        } catch (Exception e) {
            log.error("获取统计信息失败: ", e);
            result.put("code", 500);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取用户列表（分页，支持搜索）
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        String adminAccount = getCurrentAdminAccount();
        
        // 参数验证
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        
        // 清理关键词，防止XSS
        final String finalKeyword;
        if (keyword != null) {
            finalKeyword = sanitizeInput(keyword);
        } else {
            finalKeyword = null;
        }
        
        log.info("管理员 {} 获取用户列表 - page: {}, size: {}, keyword: {}, status: {}, IP: {}", 
                adminAccount, page, size, finalKeyword != null ? "***" : null, status, getClientIp(request));
        
        try {
            Page<User> pageParam = new Page<>(page, size);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            // MyBatis Plus 的 @TableLogic 会自动处理逻辑删除，无需手动添加 deleted = 0
            
            // 关键词搜索（账号、邮箱、昵称）
            if (finalKeyword != null && !finalKeyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like("account", finalKeyword)
                        .or()
                        .like("email", finalKeyword)
                        .or()
                        .like("nickname", finalKeyword)
                );
            }
            
            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            
            queryWrapper.orderByDesc("create_time");
            
            // 执行查询
            Page<User> userPage = userService.page(pageParam, queryWrapper);
            
            // 清除密码信息
            userPage.getRecords().forEach(user -> user.setPassword(null));
            
            // 构建分页数据（确保JSON序列化正确）
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", userPage.getRecords());
            pageData.put("total", userPage.getTotal());
            pageData.put("size", userPage.getSize());
            pageData.put("current", userPage.getCurrent());
            pageData.put("pages", userPage.getPages());
            pageData.put("hasNext", userPage.hasNext());
            pageData.put("hasPrevious", userPage.hasPrevious());
            
            result.put("code", 200);
            result.put("message", "获取用户列表成功");
            result.put("data", pageData);
            
            log.info("管理员 {} 获取用户列表成功 - 总记录数: {}, 当前页记录数: {}", 
                    adminAccount, userPage.getTotal(), userPage.getRecords().size());
            
        } catch (Exception e) {
            log.error("获取用户列表失败: ", e);
            result.put("code", 500);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUserById(@PathVariable Long id, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        log.info("管理员 {} 获取用户详情 - 用户ID: {}, IP: {}", adminAccount, id, getClientIp(request));
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userService.getById(id);
            // MyBatis Plus 的 @TableLogic 会自动过滤已删除的用户
            if (user != null) {
                user.setPassword(null);
                result.put("code", 200);
                result.put("message", "获取用户成功");
                result.put("data", user);
            } else {
                result.put("code", 404);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户详情失败: ", e);
            result.put("code", 500);
            result.put("message", "获取用户详情失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 创建用户（管理员操作）
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> createUser(@RequestBody User user, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String adminAccount = getCurrentAdminAccount();
        
        try {
            // 验证必填字段
            if (user.getAccount() == null || user.getAccount().trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "账号不能为空");
                return result;
            }
            
            // 清理输入，防止XSS
            user.setAccount(sanitizeInput(user.getAccount().trim()));
            
            // 账号格式验证（只允许字母、数字、下划线，长度3-50）
            if (!user.getAccount().matches("^[a-zA-Z0-9_]{3,50}$")) {
                result.put("code", 400);
                result.put("message", "账号格式不正确，只能包含字母、数字和下划线，长度3-50位");
                return result;
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "密码不能为空");
                return result;
            }
            
            // 密码强度验证
            String password = user.getPassword();
            if (password.length() < 8 || password.length() > 128) {
                result.put("code", 400);
                result.put("message", "密码长度必须在8-128位之间");
                return result;
            }
            
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&._\\-+=()\\[\\]{}]{8,}$")) {
                result.put("code", 400);
                result.put("message", "密码必须包含字母和数字，可包含常见特殊字符");
                return result;
            }
            
            // 检查账号是否已存在
            if (userService.isAccountExists(user.getAccount())) {
                result.put("code", 400);
                result.put("message", "账号已存在");
                return result;
            }
            
            // 检查邮箱是否已存在
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                // 邮箱格式验证
                String email = user.getEmail().trim().toLowerCase();
                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    result.put("code", 400);
                    result.put("message", "邮箱格式不正确");
                    return result;
                }
                user.setEmail(email);
                if (userService.isEmailExists(email)) {
                    result.put("code", 400);
                    result.put("message", "邮箱已存在");
                    return result;
                }
            }
            
            // 清理昵称，防止XSS
            if (user.getNickname() != null) {
                user.setNickname(sanitizeInput(user.getNickname().trim()));
                if (user.getNickname().length() > 50) {
                    user.setNickname(user.getNickname().substring(0, 50));
                }
            }
            
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            
            // 保存用户
            boolean saved = userService.save(user);
            if (saved) {
                user.setPassword(null);
                result.put("code", 200);
                result.put("message", "创建用户成功");
                result.put("data", user);
                log.info("管理员 {} 创建用户: {}, IP: {}", adminAccount, user.getAccount(), getClientIp(request));
            } else {
                result.put("code", 500);
                result.put("message", "创建用户失败");
            }
            
        } catch (Exception e) {
            log.error("创建用户失败: ", e);
            result.put("code", 500);
            result.put("message", "创建用户失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 更新用户信息（管理员操作）
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        log.info("管理员 {} 更新用户 - 用户ID: {}, IP: {}", adminAccount, id, getClientIp(request));
        Map<String, Object> result = new HashMap<>();
        
        try {
            User existingUser = userService.getById(id);
            // MyBatis Plus 的 @TableLogic 会自动过滤已删除的用户
            if (existingUser == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 不允许修改账号
            user.setAccount(null);
            
            // 如果提供了新密码，则加密
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                String password = user.getPassword();
                // 密码强度验证
                if (password.length() < 8 || password.length() > 128) {
                    result.put("code", 400);
                    result.put("message", "密码长度必须在8-128位之间");
                    return result;
                }
                if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&._\\-+=()\\[\\]{}]{8,}$")) {
                    result.put("code", 400);
                    result.put("message", "密码必须包含字母和数字，可包含常见特殊字符");
                    return result;
                }
                user.setPassword(passwordEncoder.encode(password));
            } else {
                // 不修改密码
                user.setPassword(null);
            }
            
            // 检查邮箱是否与其他用户冲突
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                String email = user.getEmail().trim().toLowerCase();
                // 邮箱格式验证
                if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    result.put("code", 400);
                    result.put("message", "邮箱格式不正确");
                    return result;
                }
                user.setEmail(email);
                User emailUser = userService.findByEmail(email);
                if (emailUser != null && !emailUser.getId().equals(id)) {
                    result.put("code", 400);
                    result.put("message", "邮箱已被其他用户使用");
                    return result;
                }
            }
            
            // 清理昵称，防止XSS
            if (user.getNickname() != null) {
                user.setNickname(sanitizeInput(user.getNickname().trim()));
                if (user.getNickname().length() > 50) {
                    user.setNickname(user.getNickname().substring(0, 50));
                }
            }
            
            user.setId(id);
            boolean updated = userService.updateById(user);
            
            if (updated) {
                User updatedUser = userService.getById(id);
                updatedUser.setPassword(null);
                result.put("code", 200);
                result.put("message", "更新用户成功");
                result.put("data", updatedUser);
                log.info("管理员 {} 更新用户成功 - 用户ID: {}, IP: {}", adminAccount, id, getClientIp(request));
            } else {
                result.put("code", 500);
                result.put("message", "更新用户失败");
            }
            
        } catch (Exception e) {
            log.error("更新用户失败: ", e);
            result.put("code", 500);
            result.put("message", "更新用户失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        log.warn("管理员 {} 删除用户 - 用户ID: {}, IP: {}", adminAccount, id, getClientIp(request));
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userService.getById(id);
            // MyBatis Plus 的 @TableLogic 会自动过滤已删除的用户
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 注意：这里删除的是普通用户，管理员账号在admin表中，不会出现在这里
            // removeById 会执行逻辑删除（将 deleted 设置为 1）
            boolean deleted = userService.removeById(id);
            if (deleted) {
                result.put("code", 200);
                result.put("message", "删除用户成功");
                log.warn("管理员 {} 删除用户成功 - 用户ID: {}, IP: {}", adminAccount, id, getClientIp(request));
            } else {
                result.put("code", 500);
                result.put("message", "删除用户失败");
            }
            
        } catch (Exception e) {
            log.error("删除用户失败: ", e);
            result.put("code", 500);
            result.put("message", "删除用户失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 启用/禁用用户
     */
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        Map<String, Object> result = new HashMap<>();
        
        try {
            Integer status = data.get("status");
            if (status == null || (status != 0 && status != 1)) {
                result.put("code", 400);
                result.put("message", "状态值无效，必须为0（禁用）或1（启用）");
                return result;
            }
            
            User user = userService.getById(id);
            // MyBatis Plus 的 @TableLogic 会自动过滤已删除的用户
            if (user == null) {
                result.put("code", 404);
                result.put("message", "用户不存在");
                return result;
            }
            
            // 注意：这里操作的是普通用户，管理员账号在admin表中，不会出现在这里
            
            user.setStatus(status);
            boolean updated = userService.updateById(user);
            
            if (updated) {
                result.put("code", 200);
                result.put("message", status == 1 ? "启用用户成功" : "禁用用户成功");
                log.info("管理员 {} {}用户 - 用户ID: {}, IP: {}", adminAccount, status == 1 ? "启用" : "禁用", id, getClientIp(request));
            } else {
                result.put("code", 500);
                result.put("message", "更新用户状态失败");
            }
            
        } catch (Exception e) {
            log.error("更新用户状态失败: ", e);
            result.put("code", 500);
            result.put("message", "更新用户状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取当前管理员账号
     */
    private String getCurrentAdminAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "unknown";
    }
    
    /**
     * 清理输入，防止XSS攻击
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // 移除HTML标签和脚本
        String sanitized = HTML_TAG_PATTERN.matcher(input).replaceAll("");
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        // 移除危险字符
        sanitized = sanitized.replaceAll("[<>\"'&]", "");
        return sanitized.trim();
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
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

