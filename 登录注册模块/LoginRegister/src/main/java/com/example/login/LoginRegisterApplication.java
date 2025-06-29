package com.example.login;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
@MapperScan("com.example.login.mapper")
public class LoginRegisterApplication {
	public static void main(String[] args) {
		SpringApplication.run(LoginRegisterApplication.class, args);
	}
}
     