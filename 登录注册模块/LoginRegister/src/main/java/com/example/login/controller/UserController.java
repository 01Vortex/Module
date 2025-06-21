package com.example.login.controller;

import com.example.login.dto.UserInfoDTO;
import com.example.login.model.User;
import com.example.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;



    @GetMapping("/userinfo")
    @Operation(summary = "获取用户信息")
    @ApiResponse(responseCode = "200", description = "获取用户信息成功")
    public ResponseEntity<UserInfoDTO> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.loadUserInfoByUsername(username);
        UserInfoDTO userInfoDTO = new UserInfoDTO(user);
        return ResponseEntity.ok(userInfoDTO);
    }














}
