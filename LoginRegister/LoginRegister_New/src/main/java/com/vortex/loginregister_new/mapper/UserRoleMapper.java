package com.vortex.loginregister_new.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vortex.loginregister_new.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联 Mapper
 *
 * @author Vortex
 * @since 2024
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);
}

