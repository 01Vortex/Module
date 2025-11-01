-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS login_register_new DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE login_register_new;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 第三方账号绑定表
CREATE TABLE IF NOT EXISTS `user_social` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `provider` VARCHAR(50) NOT NULL COMMENT '提供商：wechat/qq/google等',
    `openid` VARCHAR(128) NOT NULL COMMENT '第三方openid',
    `unionid` VARCHAR(128) DEFAULT NULL COMMENT '第三方unionid',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '第三方昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '第三方头像',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_provider_openid` (`provider`, `openid`),
    KEY `idx_provider_unionid` (`provider`, `unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方账号绑定表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(50) NOT NULL COMMENT '权限编码',
    `resource_type` VARCHAR(20) DEFAULT NULL COMMENT '资源类型：menu-菜单，button-按钮，api-接口',
    `url` VARCHAR(200) DEFAULT NULL COMMENT '资源URL',
    `method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法：GET、POST、PUT、DELETE等',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '权限描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 刷新令牌表（用于 JWT 刷新）
CREATE TABLE IF NOT EXISTS `refresh_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `token` VARCHAR(500) NOT NULL COMMENT '刷新令牌',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`(255)),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷新令牌表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    `login_location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    `status` TINYINT NOT NULL COMMENT '登录状态：0-失败，1-成功',
    `message` VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 插入默认角色
INSERT INTO `role` (`role_name`, `role_code`, `description`) VALUES
('管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限'),
('普通用户', 'ROLE_USER', '普通用户，基本权限')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- 插入默认权限
INSERT INTO `permission` (`permission_name`, `permission_code`, `resource_type`, `url`, `method`, `description`) VALUES
('用户管理', 'user:manage', 'menu', '/user/**', NULL, '用户管理模块'),
('用户查询', 'user:query', 'api', '/user/list', 'GET', '查询用户列表'),
('用户新增', 'user:add', 'api', '/user/add', 'POST', '新增用户'),
('用户修改', 'user:edit', 'api', '/user/edit', 'PUT', '修改用户'),
('用户删除', 'user:delete', 'api', '/user/delete', 'DELETE', '删除用户')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

