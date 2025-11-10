package com.vortex.loginregister_new.service.impl;

import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.entity.UserSocial;
import com.vortex.loginregister_new.service.SocialLoginService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * 第三方登录服务实现类
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginServiceImpl implements SocialLoginService {

    private final UserService userService;
    private final UserSocialService userSocialService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User loginOrRegister(String provider, String providerUserId, String unionId,
                                String email, String nickname, String avatar, String loginIp) {
        log.info("第三方登录/注册开始 - provider: {}, providerUserId: {}, email: {}", 
                provider, providerUserId, email);

        // 1. 检查第三方关联表是否存在该 provider 和 providerUserId 的记录
        // 对于有 unionid 的情况（如微信），优先使用 unionid 查询（更稳定）
        UserSocial userSocial = null;
        if (unionId != null && !unionId.trim().isEmpty()) {
            userSocial = userSocialService.findByProviderAndUnionId(provider, unionId);
            if (userSocial != null) {
                log.debug("通过 unionid 找到关联记录 - provider: {}, unionId: {}", provider, unionId);
            }
        }
        // 如果没有 unionid 或通过 unionid 未找到，则使用 openid 查询
        if (userSocial == null) {
            userSocial = userSocialService.findByProviderAndOpenId(provider, providerUserId);
            if (userSocial != null) {
                log.debug("通过 openid 找到关联记录 - provider: {}, openid: {}", provider, providerUserId);
            }
        }
        
        if (userSocial != null) {
            // 已存在关联记录（第二次及以上登录），只更新登录信息
            log.info("找到已关联的第三方账号（非首次登录） - provider: {}, userId: {}, account: {}", 
                    provider, userSocial.getUserId(), userSocial.getOpenid());
            User user = userService.getById(userSocial.getUserId());
            if (user != null) {
                // 更新第三方账号信息（昵称、头像、unionid、openid 等可能有变化）
                boolean needUpdate = false;
                // 更新 openid（如果不同，可能是同一用户在不同应用下的 openid）
                if (providerUserId != null && !providerUserId.equals(userSocial.getOpenid())) {
                    userSocial.setOpenid(providerUserId);
                    needUpdate = true;
                }
                // 更新 unionid（如果提供了但当前记录没有，或者不同）
                if (unionId != null && !unionId.trim().isEmpty()) {
                    if (userSocial.getUnionid() == null || !unionId.equals(userSocial.getUnionid())) {
                        userSocial.setUnionid(unionId);
                        needUpdate = true;
                    }
                }
                if (nickname != null && !nickname.equals(userSocial.getSocialName())) {
                    userSocial.setSocialName(nickname);
                    needUpdate = true;
                }
                if (avatar != null && !avatar.equals(userSocial.getAvatar())) {
                    userSocial.setAvatar(avatar);
                    needUpdate = true;
                }
                if (needUpdate) {
                    userSocialService.updateById(userSocial);
                    log.debug("更新第三方账号关联信息 - provider: {}, userId: {}", provider, userSocial.getUserId());
                }
                
                // 第二次及以上登录：只更新用户表的最后登录时间和IP，不更新其他字段
                if (loginIp != null && !loginIp.trim().isEmpty()) {
                    userService.updateLastLoginInfo(user.getId(), loginIp);
                    log.debug("更新用户登录信息 - userId: {}, loginIp: {}", user.getId(), loginIp);
                }
                
                return user;
            }
        }

        // 2. 如果不存在关联记录，检查用户表是否存在该邮箱的用户
        User existingUser = null;
        if (email != null && !email.trim().isEmpty()) {
            existingUser = userService.findByEmail(email.toLowerCase());
            if (existingUser != null) {
                log.info("找到相同邮箱的现有用户 - email: {}, userId: {}", email, existingUser.getId());
                // 直接将第三方账号与现有用户关联
                UserSocial newUserSocial = new UserSocial();
                newUserSocial.setUserId(existingUser.getId());
                newUserSocial.setProvider(provider);
                newUserSocial.setOpenid(providerUserId);
                if (unionId != null && !unionId.trim().isEmpty()) {
                    newUserSocial.setUnionid(unionId);
                }
                newUserSocial.setSocialName(nickname);
                newUserSocial.setAvatar(avatar);
                userSocialService.save(newUserSocial);
                
                // 更新账户类型（首次关联时）
                updateAccountType(existingUser.getId());
                
                // 首次关联第三方账号：更新登录信息
                if (loginIp != null && !loginIp.trim().isEmpty()) {
                    userService.updateLastLoginInfo(existingUser.getId(), loginIp);
                }
                
                return existingUser;
            }
        }

        // 3. 如果不存在，创建新用户（password 为 NULL）
        log.info("创建新的第三方登录用户 - provider: {}, email: {}", provider, email);
        User newUser = new User();
        
        // 生成唯一10位数字账号
        String generatedAccount = generateUniqueNumericAccount(10);
        newUser.setAccount(generatedAccount);
        newUser.setPassword(null); // 第三方登录用户密码为 NULL
        newUser.setEmail(email != null ? email.toLowerCase() : null);
        // 首次登录时，使用第三方昵称设置到 user 表的 nickname 字段
        newUser.setNickname(nickname);
        newUser.setAvatar(avatar);
        newUser.setStatus(1); // 默认启用
        newUser.setAccountType("SOCIAL"); // 仅第三方登录
        
        boolean saved = userService.save(newUser);
        if (!saved) {
            log.error("创建用户失败 - provider: {}, account: {}", provider, generatedAccount);
            throw new RuntimeException("创建用户失败");
        }
        
        // 重新查询用户以获取ID
        newUser = userService.findByAccount(generatedAccount);
        if (newUser == null) {
            log.error("创建用户后查询失败 - provider: {}, account: {}", provider, generatedAccount);
            throw new RuntimeException("创建用户失败");
        }
        
        // 4. 在第三方关联表中创建记录
        UserSocial newUserSocial = new UserSocial();
        newUserSocial.setUserId(newUser.getId());
        newUserSocial.setProvider(provider);
        newUserSocial.setOpenid(providerUserId);
        if (unionId != null && !unionId.trim().isEmpty()) {
            newUserSocial.setUnionid(unionId);
        }
        newUserSocial.setSocialName(nickname);
        newUserSocial.setAvatar(avatar);
        userSocialService.save(newUserSocial);
        
        // 首次注册：更新登录信息
        if (loginIp != null && !loginIp.trim().isEmpty()) {
            userService.updateLastLoginInfo(newUser.getId(), loginIp);
        }
        
        log.info("第三方登录/注册成功（首次注册） - provider: {}, userId: {}, account: {}", 
                provider, newUser.getId(), newUser.getAccount());
        
        return newUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccountType(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return;
        }
        
        boolean hasPassword = user.getPassword() != null && !user.getPassword().trim().isEmpty();
        boolean hasSocial = userSocialService.lambdaQuery()
                .eq(UserSocial::getUserId, userId)
                .count() > 0;
        
        String accountType;
        if (hasPassword && hasSocial) {
            accountType = "BOTH";
        } else if (hasPassword) {
            accountType = "PASSWORD";
        } else if (hasSocial) {
            accountType = "SOCIAL";
        } else {
            // 理论上不应该出现这种情况，但为了安全，设置为 PASSWORD
            accountType = "PASSWORD";
        }
        
        if (!accountType.equals(user.getAccountType())) {
            user.setAccountType(accountType);
            userService.updateById(user);
            log.info("更新账户类型 - userId: {}, accountType: {}", userId, accountType);
        }
    }

    @Override
    public boolean hasPassword(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return user.getPassword() != null && !user.getPassword().trim().isEmpty();
    }

    /**
     * 生成唯一的10位数字账号
     */
    private String generateUniqueNumericAccount(int length) {
        if (length < 1 || length > 19) {
            throw new IllegalArgumentException("账号长度必须在1-19位之间");
        }
        
        Random random = new Random();
        int maxAttempts = 100; // 最大尝试次数
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            // 生成指定长度的数字账号
            // 第一位不能为0，确保是length位数字
            StringBuilder sb = new StringBuilder(length);
            sb.append(random.nextInt(9) + 1); // 第一位：1-9
            for (int i = 1; i < length; i++) {
                sb.append(random.nextInt(10)); // 其他位：0-9
            }
            
            String candidate = sb.toString();
            
            // 检查账号是否已存在
            if (!userService.isAccountExists(candidate)) {
                return candidate;
            }
            
            attempts++;
        }
        
        // 如果100次尝试后仍然冲突，使用时间戳+随机数的方式
        log.warn("生成{}位数字账号失败，使用备用方案", length);
        String fallback = String.valueOf(System.currentTimeMillis() % (long) Math.pow(10, length));
        // 确保是length位（前面补0）
        while (fallback.length() < length) {
            fallback = "0" + fallback;
        }
        // 如果还是冲突，添加随机后缀
        if (userService.isAccountExists(fallback)) {
            fallback = fallback.substring(0, length - 4) + String.valueOf(random.nextInt(10000));
        }
        return fallback;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindSocialAccount(Long userId, String provider, String providerUserId,
                                    String unionId, String nickname, String avatar) {
        log.info("绑定第三方账号开始 - userId: {}, provider: {}, providerUserId: {}", 
                userId, provider, providerUserId);

        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            log.error("用户不存在 - userId: {}", userId);
            return false;
        }

        // 检查该第三方账号是否已被其他用户绑定
        UserSocial existingSocial = null;
        if (unionId != null && !unionId.trim().isEmpty()) {
            existingSocial = userSocialService.findByProviderAndUnionId(provider, unionId);
        }
        if (existingSocial == null) {
            existingSocial = userSocialService.findByProviderAndOpenId(provider, providerUserId);
        }

        if (existingSocial != null) {
            // 如果已绑定到当前用户，直接返回成功
            if (existingSocial.getUserId().equals(userId)) {
                log.info("第三方账号已绑定到当前用户 - userId: {}, provider: {}", userId, provider);
                // 更新第三方账号信息
                boolean needUpdate = false;
                if (nickname != null && !nickname.equals(existingSocial.getSocialName())) {
                    existingSocial.setSocialName(nickname);
                    needUpdate = true;
                }
                if (avatar != null && !avatar.equals(existingSocial.getAvatar())) {
                    existingSocial.setAvatar(avatar);
                    needUpdate = true;
                }
                if (needUpdate) {
                    userSocialService.updateById(existingSocial);
                }
                // 更新账户类型
                updateAccountType(userId);
                return true;
            } else {
                // 已绑定到其他用户
                log.warn("第三方账号已被其他用户绑定 - provider: {}, providerUserId: {}, existingUserId: {}", 
                        provider, providerUserId, existingSocial.getUserId());
                return false;
            }
        }

        // 创建新的绑定记录
        UserSocial newUserSocial = new UserSocial();
        newUserSocial.setUserId(userId);
        newUserSocial.setProvider(provider);
        newUserSocial.setOpenid(providerUserId);
        if (unionId != null && !unionId.trim().isEmpty()) {
            newUserSocial.setUnionid(unionId);
        }
        newUserSocial.setSocialName(nickname);
        newUserSocial.setAvatar(avatar);
        
        boolean saved = userSocialService.save(newUserSocial);
        if (saved) {
            // 更新账户类型
            updateAccountType(userId);
            log.info("第三方账号绑定成功 - userId: {}, provider: {}", userId, provider);
            return true;
        } else {
            log.error("第三方账号绑定失败 - userId: {}, provider: {}", userId, provider);
            return false;
        }
    }
}

