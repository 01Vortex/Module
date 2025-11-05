-- 数据库迁移脚本：将username字段改为account
-- 执行时间：根据实际情况执行
-- 注意：执行前请备份数据库！

USE login_register_new;

-- 1. 修改user表：将username字段改为account
ALTER TABLE `user` 
CHANGE COLUMN `username` `account` VARCHAR(50) NOT NULL COMMENT '账号' AFTER `id`;

-- 2. 修改索引：删除旧的username索引，添加新的account索引
ALTER TABLE `user` DROP INDEX IF EXISTS `uk_username`;
ALTER TABLE `user` ADD UNIQUE INDEX `uk_account` (`account`);

-- 3. 修改login_log表：将username字段改为account（如果存在）
ALTER TABLE `login_log` 
CHANGE COLUMN `username` `account` VARCHAR(50) NULL DEFAULT NULL COMMENT '账号' AFTER `user_id`;

-- 4. 验证修改结果
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'login_register_new' 
  AND TABLE_NAME IN ('user', 'login_log')
  AND COLUMN_NAME IN ('account', 'username');

