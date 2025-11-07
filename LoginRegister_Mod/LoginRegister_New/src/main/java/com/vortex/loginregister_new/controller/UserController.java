package com.vortex.loginregister_new.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author 01Vortex
 * @since 2024
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}

