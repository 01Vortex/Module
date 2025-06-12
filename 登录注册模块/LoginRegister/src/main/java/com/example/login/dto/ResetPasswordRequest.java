package com.example.login.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(min = 8, message = "密码至少为8位")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    private String code;

}
