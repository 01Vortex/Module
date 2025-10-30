package com.example.mybatisdemo.controller;

import com.example.mybatisdemo.entity.User;
import com.example.mybatisdemo.service.UserServiceImpl;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserServiceImpl userServiceImpl;

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userServiceImpl.getUserById(id);
    }

    @PostMapping("/register")
    public void createUser(@RequestBody User user) {
        userServiceImpl.createUser(user);
    }

    @GetMapping("/pageInfo")
    public PageInfo<User> getUsersPageInfo(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return userServiceImpl.getUsersPageInfo(pageNum, pageSize);
    }

}