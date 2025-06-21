package com.example.login.controller;


import com.example.login.dto.LoginRequest;
import com.example.login.dto.RegisterRequest;
import com.example.login.dto.ResetPasswordRequest;
import com.example.login.dto.Response;
import com.example.login.mapper.UserMapper;
import com.example.login.model.User;
import com.example.login.service.VerificationCodeService;
import com.example.login.service.UserService;
import com.example.login.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
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
    private AuthenticationManager authenticationManager ;

    @Autowired
    public AuthController(UserService userService, VerificationCodeService verificationCodeService, UserMapper userMapper,AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
    }



    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名和密码进行身份验证并生成 JWT Token")
    @ApiResponse(responseCode = "200", description = "登录成功，返回 JWT Token")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        if (userMapper.loadUserByUsername(loginRequest.getUsername()) == null){
            return ResponseEntity.status(400).body("用户未存在,请先注册");
        }
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



    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "通过邮箱和验证码完成新用户注册")
    @ApiResponse(responseCode = "200", description = "注册成功")
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
        return ResponseEntity.ok(Response.success("注册成功"));

    }



    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过邮箱和验证码重置用户密码")
    @ApiResponse(responseCode = "200", description = "密码重置成功")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!verificationCodeService.validateVerificationCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.status(400).body("验证码错误");
        }

        userService.resetPassword(request.getEmail(),request.getNewPassword());
        return ResponseEntity.ok(Response.success("密码已重置"));
    }









}
