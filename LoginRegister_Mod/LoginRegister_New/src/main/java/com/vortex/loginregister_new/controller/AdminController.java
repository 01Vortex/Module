package com.vortex.loginregister_new.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vortex.loginregister_new.common.Constants;
import com.vortex.loginregister_new.common.Result;
import com.vortex.loginregister_new.common.ResultCode;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.exception.BusinessException;
import com.vortex.loginregister_new.service.JwtBlacklistService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.util.ValidationUtils;
import com.vortex.loginregister_new.util.WebUtils;
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
    private final JwtBlacklistService jwtBlacklistService;

    /**
     * 用户统计图表数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        String clientIp = WebUtils.getClientIp(request);
        log.info("管理员 {} 获取统计信息，IP: {}", adminAccount, clientIp);
        
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
                    .eq("status", Constants.UserStatus.DISABLED));
            
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
                    .eq("status", Constants.UserStatus.ENABLED)));
            statusDistribution.put("禁用", disabledUsers);
            
            Map<String, Object> data = new HashMap<>();
            data.put("totalUsers", totalUsers);
            data.put("activeUsers", activeUsers);
            data.put("todayNewUsers", todayNewUsers);
            data.put("disabledUsers", disabledUsers);
            data.put("dailyNewUsers", dailyNewUsers);
            data.put("statusDistribution", statusDistribution);
            
            return Result.success("获取统计成功", data);
            
        } catch (Exception e) {
            log.error("获取统计信息失败: ", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "获取统计信息失败");
        }
    }

    /**
     * 获取用户列表（分页，支持搜索）
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        String adminAccount = getCurrentAdminAccount();
        String clientIp = WebUtils.getClientIp(request);
        
        // 参数验证
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        
        // 清理关键词，防止XSS
        final String finalKeyword = keyword != null ? ValidationUtils.sanitizeInput(keyword) : null;
        
        log.info("管理员 {} 获取用户列表 - page: {}, size: {}, keyword: {}, status: {}, IP: {}", 
                adminAccount, page, size, finalKeyword != null ? "***" : null, status, clientIp);
        
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
            
            // 构建分页数据
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("records", userPage.getRecords());
            pageData.put("total", userPage.getTotal());
            pageData.put("size", userPage.getSize());
            pageData.put("current", userPage.getCurrent());
            pageData.put("pages", userPage.getPages());
            pageData.put("hasNext", userPage.hasNext());
            pageData.put("hasPrevious", userPage.hasPrevious());
            
            log.info("管理员 {} 获取用户列表成功 - 总记录数: {}, 当前页记录数: {}", 
                    adminAccount, userPage.getTotal(), userPage.getRecords().size());
            
            return Result.success("获取用户列表成功", pageData);
            
        } catch (Exception e) {
            log.error("获取用户列表失败: ", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "获取用户列表失败");
        }
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        String clientIp = WebUtils.getClientIp(request);
        log.info("管理员 {} 获取用户详情 - 用户ID: {}, IP: {}", adminAccount, id, clientIp);
        
        try {
            User user = userService.getById(id);
            if (user != null) {
                user.setPassword(null);
                return Result.success("获取用户成功", user);
            } else {
                return Result.fail(ResultCode.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("获取用户详情失败: ", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "获取用户详情失败");
        }
    }

    /**
     * 创建用户（管理员操作）
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@RequestBody User user, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        String clientIp = WebUtils.getClientIp(request);
        
        // 验证必填字段
        if (user.getAccount() == null || user.getAccount().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账号不能为空");
        }
        
        // 清理输入，防止XSS
        user.setAccount(ValidationUtils.sanitizeInput(ValidationUtils.normalizeAccount(user.getAccount())));
        
        // 账号格式验证
        if (!ValidationUtils.isValidAccount(user.getAccount())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, 
                    "账号格式不正确，只能包含字母、数字和下划线，长度3-50位");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "密码不能为空");
        }
        
        // 密码强度验证
        if (!ValidationUtils.isValidPassword(user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, 
                    "密码必须包含字母和数字，长度8-128位，可包含常见特殊字符");
        }
        
        try {
            // 检查账号是否已存在
            if (userService.isAccountExists(user.getAccount())) {
                throw new BusinessException(ResultCode.ACCOUNT_EXISTS);
            }
            
            // 检查邮箱是否已存在
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                String email = ValidationUtils.normalizeEmail(user.getEmail());
                if (!ValidationUtils.isValidEmail(email)) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "邮箱格式不正确");
                }
                user.setEmail(email);
                if (userService.isEmailExists(email)) {
                    throw new BusinessException(ResultCode.EMAIL_EXISTS);
                }
            }
            
            // 清理昵称，防止XSS
            if (user.getNickname() != null) {
                String nickname = ValidationUtils.sanitizeInput(user.getNickname().trim());
                user.setNickname(nickname.length() > 50 ? nickname.substring(0, 50) : nickname);
            }
            
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus(Constants.UserStatus.ENABLED);
            }
            
            // 保存用户
            boolean saved = userService.save(user);
            if (!saved) {
                throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "创建用户失败");
            }
            
            user.setPassword(null);
            log.info("管理员 {} 创建用户: {}, IP: {}", adminAccount, user.getAccount(), clientIp);
            return Result.success("创建用户成功", user);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建用户失败: ", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "创建用户失败");
        }
    }

    /**
     * 更新用户信息（管理员操作）
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        String clientIp = WebUtils.getClientIp(request);
        log.info("管理员 {} 更新用户 - 用户ID: {}, IP: {}", adminAccount, id, clientIp);
        
        try {
            User existingUser = userService.getById(id);
            if (existingUser == null) {
                return Result.fail(ResultCode.NOT_FOUND);
            }
            
            // 不允许修改账号
            user.setAccount(null);
            
            // 如果提供了新密码，则加密
            boolean passwordChanged = false;
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                if (!ValidationUtils.isValidPassword(user.getPassword())) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, 
                            "密码必须包含字母和数字，长度8-128位，可包含常见特殊字符");
                }
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                passwordChanged = true;
            } else {
                // 不修改密码
                user.setPassword(null);
            }
            
            // 检查邮箱是否与其他用户冲突
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                String email = ValidationUtils.normalizeEmail(user.getEmail());
                if (!ValidationUtils.isValidEmail(email)) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "邮箱格式不正确");
                }
                user.setEmail(email);
                User emailUser = userService.findByEmail(email);
                if (emailUser != null && !emailUser.getId().equals(id)) {
                    throw new BusinessException(ResultCode.EMAIL_EXISTS);
                }
            }
            
            // 清理昵称，防止XSS
            if (user.getNickname() != null) {
                String nickname = ValidationUtils.sanitizeInput(user.getNickname().trim());
                user.setNickname(nickname.length() > 50 ? nickname.substring(0, 50) : nickname);
            }
            
            user.setId(id);
            boolean updated = userService.updateById(user);
            
            if (!updated) {
                throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新用户失败");
            }
            
            // 如果修改了密码，标记用户的所有token失效（7天内密码修改后使token失效）
            if (passwordChanged) {
                jwtBlacklistService.invalidateUserTokens(id, 7);
                log.info("管理员 {} 修改用户 {} 密码成功，已使该用户所有token失效", adminAccount, id);
            }
            
            User updatedUser = userService.getById(id);
            updatedUser.setPassword(null);
            log.info("管理员 {} 更新用户成功 - 用户ID: {}, IP: {}", adminAccount, id, clientIp);
            return Result.success("更新用户成功", updatedUser);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户失败: ", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新用户失败");
        }
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        String clientIp = WebUtils.getClientIp(request);
        log.warn("管理员 {} 删除用户 - 用户ID: {}, IP: {}", adminAccount, id, clientIp);
        
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.fail(ResultCode.NOT_FOUND);
            }
            
            // 逻辑删除
            boolean deleted = userService.removeById(id);
            if (!deleted) {
                throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "删除用户失败");
            }
            
            log.warn("管理员 {} 删除用户成功 - 用户ID: {}, IP: {}", adminAccount, id, clientIp);
            return Result.<Void>success("删除用户成功", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户失败: ", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "删除用户失败");
        }
    }

    /**
     * 启用/禁用用户
     */
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data, HttpServletRequest request) {
        String adminAccount = getCurrentAdminAccount();
        String clientIp = WebUtils.getClientIp(request);
        
        try {
            Integer status = data.get("status");
            if (status == null || (status != Constants.UserStatus.DISABLED && status != Constants.UserStatus.ENABLED)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "状态值无效，必须为0（禁用）或1（启用）");
            }
            
            User user = userService.getById(id);
            if (user == null) {
                return Result.fail(ResultCode.NOT_FOUND);
            }
            
            user.setStatus(status);
            boolean updated = userService.updateById(user);
            
            if (!updated) {
                throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新用户状态失败");
            }
            
            String action = status == Constants.UserStatus.ENABLED ? "启用" : "禁用";
            log.info("管理员 {} {}用户 - 用户ID: {}, IP: {}", adminAccount, action, id, clientIp);
            return Result.<Void>success(action + "用户成功", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户状态失败: ", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新用户状态失败");
        }
    }

    /**
     * 获取当前管理员账号
     */
    private String getCurrentAdminAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "unknown";
    }
}

