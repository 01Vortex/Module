package com.example.login.service.Impl;

import com.example.login.mapper.UserMapper;
import com.example.login.model.User;
import com.example.login.service.UserService;
import com.example.login.util.DataValidationUtil;
import com.example.login.util.RandomCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    // 将 userMapper 和 passwordEncoder 声明为 final，并在构造函数中初始化。这样可以确保注入后不可变，提高类的安全性和可读性。
    //实例调用函数用用final
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);



    @Autowired
    public UserServiceImpl(PasswordEncoder passwordEncoder,UserMapper userMapper){
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }


    /**
     * 创建新用户
     * @param user
     * @return void
     */
    @Override
    public void createAccount(User user) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("该用户名已被占用，请选择其他用户名");
        } else if (!DataValidationUtil.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名格式不正确");
        }
        // 检查邮箱是否已被注册
        if (userMapper.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("该邮箱已被注册，请输入其他邮箱地址");
        }
        // 检查手机号是否已被注册
        if (userMapper.findByPhone(user.getPhoneNumber()) != null) {
            throw new IllegalArgumentException("该手机号已被注册，请输入其他手机号");
        }

        /**
         * 设置User属性并插入到数据库
         * 前端设置了头像,性别不需要在这里使用setter方法
         */
        user.setNickname("用户"+RandomCodeUtil.generateNumericCode(5));
        user.setPassword(passwordEncoder.encode(user.getPassword()));  //加密密码
        user.setGender("2");
        user.setDescription("留下你的自述...");
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        userMapper.createNewAccount(user);
        logger.info("用户:{}创建成功,账号为:{}", user.getNickname(),user.getUsername());
    }

    /**
     * 根据邮箱/手机号修改密码
     * @param email_phone
     * @param newPassword
     * @return void
     */
    @Override
    public void resetPassword(String email_phone, String newPassword){
        // 判断是邮箱还是手机号
        if(email_phone.contains("@")){
            userMapper.updatePasswordByEmail(email_phone,passwordEncoder.encode(newPassword));
            logger.info("邮箱 {} 的密码已重置", email_phone);
        }
        else{
            userMapper.updatePasswordByPhone(email_phone,passwordEncoder.encode(newPassword));
            logger.info("手机号 {} 的密码已重置", email_phone);
        }
    }

    /**
     * 实现SpringSecurity的获取取用户的详细信息，包括权限列表
     * @param username
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("正在加载UserDetails对象...");
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                // 创建权限列表
                AuthorityUtils.createAuthorityList(user.getRole()) // 角色转为 GrantedAuthority
        );
    }


    /**
     * 获取用户信息
     * @param username
     * @return User
     */
    @Override
    public User loadUserInfoByUsername(String username) {
        return userMapper.loadUserInfoByUsername(username);
    }
























}
