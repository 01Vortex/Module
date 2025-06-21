package com.example.login.dto;

import lombok.Data;

@Data
public class Response<T> {
    private boolean success;
    private String message;
    private T data;


    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.success = true;
        response.message = "操作成功";
        response.data = data;
        return response;
    }

    public static <T> Response<T> error(T data) {
        Response<T> response = new Response<>();
        response.success = false;
        response.message = "操作失败";
        response.data = data;
        return response;
    }
}
