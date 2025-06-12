package com.example.login.controller;

import com.example.login.dto.ApiResponse;
import com.example.login.dto.LoginRequest;
import com.example.login.dto.RegisterRequest;
import com.example.login.dto.ResetPasswordRequest;
import com.example.login.mapper.UserMapper;
import com.example.login.model.User;
import com.example.login.service.VerificationCodeService;
import com.example.login.service.UserService;
import com.example.login.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private AuthenticationManager authenticationManager ;

    @Autowired
    public AuthController(UserService userService, VerificationCodeService verificationCodeService, UserMapper userMapper,AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
    }


    // 登录用户
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        // 这里通常由 Spring Security 处理认证逻辑，也可自定义 Token 登录
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);  //将认证后的身份信息存储到 Spring 安全上下文中
        String token = JwtUtil.generateToken(loginRequest.getUsername());
        return ResponseEntity.ok().header("Authorization", "Bearer " + token).build();
    }

    // 注册用户
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        if (Boolean.TRUE.equals(userMapper.checkEmailAndUsernameExist(request.getUsername(), request.getEmail()))) {
            return ResponseEntity.status(400).body("用户名或邮箱已存在");
        }

        // 使用else if 表示"如果前面条件不成立才执行"
         if (!verificationCodeService.validateVerificationCode(request.getEmail(), request.getCode())){
            return ResponseEntity.status(400).body("验证码错误");
         }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        userService.createAccount(user);
        return ResponseEntity.ok("用户已注册");

    }



    // 重置密码
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!verificationCodeService.validateVerificationCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.status(400).body("验证码错误");
        }

        userService.resetPassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("密码已重置");
    }









}
