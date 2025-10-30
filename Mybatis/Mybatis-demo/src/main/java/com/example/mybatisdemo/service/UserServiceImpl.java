package com.example.mybatisdemo.service;

import com.example.mybatisdemo.entity.User;
import com.example.mybatisdemo.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl {
    @Autowired
    private UserMapper userMapper;

    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    public void createUser(User user) {
        userMapper.insertUser(user);
    }
    @Transactional(readOnly = true)
    public PageInfo<User> getUsersPageInfo(int pageNum, int pageSize) {
        // 启动分页
        PageHelper.startPage(pageNum, pageSize);


        // 查询数据
        List<User> users = userMapper.selectAllUsers();

        // 返回分页信息
        return new PageInfo<>(users);
    }


}
