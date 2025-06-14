package com.example.login.controller;

import com.example.login.dto.ApiResponse;
import com.example.login.service.VerificationCodeService;
import com.example.login.util.DataValidationUtil;
import com.example.login.util.RandomCodeUtil;
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

    /**
     * 发送验证码到邮箱
     * @param email
     * @return
     */
    @GetMapping("/send-code-email")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String email) {
        if (!DataValidationUtil.isValidEmail(email)) {
            return ResponseEntity.status(400).body("邮箱格式错误");
        }
            try {
                String code = RandomCodeUtil.generateNumericCode(6);
                verificationCodeService.sendVerificationCodeWithEmail(email, code);
                verificationCodeService.storeVerificationCodeToRedis(email, code);
                return ResponseEntity.ok(new ApiResponse<>(true, null, "验证码已发送"));  //等价于 ResponseEntity.status(200).body(new ApiResponse<>());
            } catch (Exception e) {
                logger.error("验证码发送失败: {}", e.getMessage());
                return ResponseEntity.status(500).body("验证码发送失败");
            }
    }
































}
