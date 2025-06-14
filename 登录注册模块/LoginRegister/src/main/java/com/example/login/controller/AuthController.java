package com.example.login.controller;


import com.example.login.dto.LoginRequest;
import com.example.login.dto.RegisterRequest;
import com.example.login.dto.ResetPasswordRequest;
import com.example.login.mapper.UserMapper;
import com.example.login.model.User;
import com.example.login.service.VerificationCodeService;
import com.example.login.service.UserService;
import com.example.login.util.DataValidationUtil;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


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


    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名和密码进行身份验证并生成 JWT Token")
    @ApiResponse(responseCode = "200", description = "登录成功，返回 JWT Token")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        // 创建一个未认证的认证对象 authenticationToken
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        try {
            // 传入未认证对象authenticationToken,返回已认证的对象authentication
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            // 将已认证对象的身份信息存储到 SpringSecurity 安全上下文中
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = JwtUtil.generateToken(loginRequest.getUsername());
            return ResponseEntity.ok().header("Authorization", "Bearer " + token).body("登录成功");
        } catch (org.springframework.security.authentication.AuthenticationServiceException e) {
            // 处理认证失败的情况
            return ResponseEntity.status(400).body("用户名或密码错误");
        }
    }


    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "通过邮箱和验证码完成新用户注册")
    @ApiResponse(responseCode = "200", description = "注册成功")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        if (Boolean.TRUE.equals(userMapper.checkEmailAndUsernameExist(request.getUsername(), request.getEmail()))) {
            return ResponseEntity.status(400).body("用户名或邮箱已存在");
        }
        if(!DataValidationUtil.isValidUsername(request.getUsername())){
            return ResponseEntity.status(400).body("用户名格式不正确");
        }
        if(!DataValidationUtil.isValidPassword(request.getPassword())){
            return ResponseEntity.status(400).body("密码必须包含大小写字母、数字和特殊字符，且长度不少于8位");
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
        return ResponseEntity.ok("注册成功");

    }


    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过邮箱和验证码重置用户密码")
    @ApiResponse(responseCode = "200", description = "密码重置成功")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!verificationCodeService.validateVerificationCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.status(400).body("验证码错误");
        }

        if(!DataValidationUtil.isValidPassword(request.getNewPassword())){
            return ResponseEntity.status(400).body("密码必须包含大小写字母、数字和特殊字符，且长度不少于8位");
        }
        userService.resetPassword(request.getEmail(),request.getNewPassword());
        return ResponseEntity.ok("密码已重置");
    }









}
