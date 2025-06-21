package com.example.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^(?![-_])[a-zA-Z0-9_-]{1,12}(?<![-_])$",
            message = "用户名格式不正确")
    private String username;

    @NotBlank(message = "密码不能为空")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*/_+.,?;:']).{8,}$",
            message = "密码必须包含大小写字母、数字和特殊字符(!@#$%^&*/_+.,?;:')，且长度不少于8位")
    private String password;
}
