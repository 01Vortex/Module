package com.example.login.mapper;

import com.example.login.model.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(nickname,username,gender,description,avatar,password,phoneNumber,email,role) VALUES(#{nickname},#{username},#{gender},#{description},#{avatar},#{password},#{phoneNumber},#{email},#{role})")
    void createNewAccount(User user);

    @Update("UPDATE user SET password = #{password} WHERE email = #{email}")
    void updatePasswordByEmail(String email, String password);

    @Update("UPDATE user SET password = #{password} WHERE phoneNumber = #{phone}")
    void updatePasswordByPhone(String phone, String password);

    // 有返回值 说明有数据 该email的用户存在
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM user WHERE phoneNumber = #{phone}")
    User findByPhone(String phone);

    @Select("SELECT EXISTS (SELECT 1 FROM user WHERE username = #{username} OR email = #{email})")
    // 可能为空
    Boolean checkEmailAndUsernameExist(String username, String email);

    // 加载用户信息
    @Select("SELECT * FROM user WHERE username = #{username}")
    User loadUserByUsername(String username);




}