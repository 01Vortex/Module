package com.example.login.controller;

import com.example.login.service.VerificationCodeService;
import com.example.login.utils.DataValidationUtil;
import com.example.login.utils.RandomCodeUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/verify")
public class VerifyController {

    private final VerificationCodeService verificationCodeService;
    private static final Logger logger = LoggerFactory.getLogger(VerifyController.class);

    @Autowired
    public VerifyController(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }


    @GetMapping("/send-code-email")
    @Operation(summary = "发送验证码到邮箱")
    @ApiResponse(responseCode = "200", description = "验证码发送成功")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String email) {
        if (!DataValidationUtil.isValidEmail(email)) {
            return ResponseEntity.status(400).body("邮箱格式错误");
        }
        String code = RandomCodeUtil.generateNumericCode(6);
        verificationCodeService.storeVerificationCodeToRedis(email, code);// 存储验证码到 Redis
        verificationCodeService.sendVerificationCodeWithEmail(email, code); // 发送验证码
        return ResponseEntity.status(200).body("验证码已发送");
    }
































}
