package com.example.login.controller;

import com.example.login.exception.RedisOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLNonTransientConnectionException;
import java.util.NoSuchElementException;

import org.springframework.dao.DataAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
public class GlobalExceptionController {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionController.class);


    // 捕获 @Valid 校验失败异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error.getObjectName(); // 字段名
            String errorMessage = error.getDefaultMessage(); // 错误信息
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(SQLNonTransientConnectionException.class)
    public ResponseEntity<String> handleDatabaseConnectionException(SQLNonTransientConnectionException ex) {
        logger.error("数据库连接失败: ", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("数据库连接失败，请检查网络或配置");
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
        logger.error("发生空指针异常: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系统错误");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleResourceNotFoundException(NoSuchElementException ex) {
        logger.warn("资源未找到: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("请求资源不存在");
    }



    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        logger.error("数据库操作异常: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("数据库操作失败，请稍后再试");
    }

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<String> handleHttpClientOrServerException(Exception ex) {
        logger.error("调用第三方服务异常: ", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("调用外部服务失败");
    }


    @ExceptionHandler(RedisOperationException.class)
    public ResponseEntity<String> handleRedisOperationFail(RedisOperationException ex) {
        logger.error("未连接到Redis");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("验证码服务出错了");
    }
}



