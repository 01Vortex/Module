package com.example.login.dto;

import com.example.login.model.User;
import lombok.Data;


@Data
public class UserInfoDTO {
    private String nickname;
    private String username;
    private String avatar;
    private String description;
    private String gender;
    private String phoneNumber;
    private String email;
    private String role;

    public UserInfoDTO(User user) {
        this.nickname = user.getNickname();
        this.username = user.getUsername();
        this.avatar = user.getAvatar();
        this.description = user.getDescription();
        this.gender = user.getGender();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

}


