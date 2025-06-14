package com.example.login.dto;

import lombok.Data;

/**
 * 通用的响应封装类 ApiResponse<T>，用于统一返回格式
 * 用法2:封装工具类 ApiResponseUtil
 */
@Data
public class ApiResponse<T> {
    private boolean success;  //success:表示本次请求是否成功。通常为 true 表示业务逻辑执行正常；false 表示出现错误（如参数错误、资源冲突等）。
    private T data;           //data:泛型类型，表示接口返回的具体数据内容。例如用户信息、列表、字符串提示等，根据实际业务而定。
    private String message;   //message:描述本次请求结果的附加信息，通常用于携带错误描述或操作提示（如“用户名或邮箱已被占用”、“注册成功，请登录”等）。

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    /*用法: return ResponseEntity.ok(ApiResponseUtil.success(token, "登录成功"));
      结果:
    {
        "success": true,
            "data": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "message": "登录成功"
    }*/


}
