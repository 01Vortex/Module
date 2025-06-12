package com.example.login.controller;

import com.example.login.dto.UserInfoDTO;
import com.example.login.model.User;
import com.example.login.service.Impl.UserServiceImpl;
import com.example.login.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 获取用户信息给前端
     * @param authentication
     * @return
     */
    @GetMapping("/userinfo")
    public ResponseEntity<UserInfoDTO> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.loadUserInfoByUsername(username);
        UserInfoDTO userInfoDTO = new UserInfoDTO(user);
        return ResponseEntity.ok(userInfoDTO);
    }














}
