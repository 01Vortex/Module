package com.vortex.loginregister_new.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.entity.UserSocial;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/oauth/wechat")
@RequiredArgsConstructor
public class WeChatOAuthController {

    private final UserService userService;
    private final UserSocialService userSocialService;

    @Value("${wechat.oauth.app-id}")
    private String appId;

    @Value("${wechat.oauth.app-secret}")
    private String appSecret;

    @Value("${wechat.oauth.redirect-uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize() {
        String state = String.valueOf(System.currentTimeMillis());
        String encodedRedirect = UriUtils.encode(redirectUri, StandardCharsets.UTF_8);
        String url = "https://open.weixin.qq.com/connect/qrconnect" +
                "?appid=" + appId +
                "&redirect_uri=" + encodedRedirect +
                "&response_type=code" +
                "&scope=snsapi_login" +
                "&state=" + state +
                "#wechat_redirect";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", url);
        return ResponseEntity.status(302).headers(headers).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(String code, String state) {
        if (code == null || code.isEmpty()) {
            return buildPostMessageHtml("wechat_auth", 400, null, "缺少code");
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=" + appId +
                    "&secret=" + appSecret +
                    "&code=" + code +
                    "&grant_type=authorization_code";
            String tokenResp = restTemplate.getForObject(tokenUrl, String.class);
            JsonNode tokenJson = objectMapper.readTree(tokenResp);
            if (tokenJson.has("errcode")) {
                log.warn("WeChat token error: {}", tokenJson);
                return buildPostMessageHtml("wechat_auth", 500, null, tokenJson.path("errmsg").asText("获取access_token失败"));
            }

            String accessToken = tokenJson.path("access_token").asText();
            String openid = tokenJson.path("openid").asText();
            String unionid = tokenJson.path("unionid").asText("");

            // 获取用户信息
            String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=" + accessToken +
                    "&openid=" + openid;
            String userResp = restTemplate.getForObject(userInfoUrl, String.class);
            JsonNode userJson = objectMapper.readTree(userResp);
            if (userJson.has("errcode")) {
                log.warn("WeChat userinfo error: {}", userJson);
                return buildPostMessageHtml("wechat_auth", 500, null, userJson.path("errmsg").asText("获取用户信息失败"));
            }

            String nickname = userJson.path("nickname").asText("微信用户");
            String avatar = userJson.path("headimgurl").asText("");

            // 绑定或创建用户
            User user = resolveOrCreateUser(openid, unionid, nickname, avatar);
            user.setPassword(null);

            Map<String, Object> payload = new HashMap<>();
            payload.put("code", 200);
            payload.put("message", "登录成功");
            payload.put("data", user);
            return buildPostMessageHtml("wechat_auth", 200, user, null);

        } catch (Exception e) {
            log.error("WeChat callback error", e);
            return buildPostMessageHtml("wechat_auth", 500, null, "微信登录失败: " + e.getMessage());
        }
    }

    private User resolveOrCreateUser(String openid, String unionid, String nickname, String avatar) {
        UserSocial bind = null;
        if (unionid != null && !unionid.isEmpty()) {
            bind = userSocialService.findByProviderAndUnionId("wechat", unionid);
        }
        if (bind == null) {
            bind = userSocialService.findByProviderAndOpenId("wechat", openid);
        }
        if (bind != null) {
            User user = userService.getById(bind.getUserId());
            if (user != null) {
                // 同步最新的头像昵称（可选）
                bind.setNickname(nickname).setAvatar(avatar);
                userSocialService.updateById(bind);
                return user;
            }
        }

        // 创建新用户：用户名10位随机数字，昵称取三方昵称
        User user = new User();
        user.setAccount(generateUniqueAccount(10));
        user.setPassword("");
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setStatus(1);
        userService.save(user);

        UserSocial social = new UserSocial();
        social.setUserId(user.getId())
                .setProvider("wechat")
                .setOpenid(openid)
                .setUnionid(unionid)
                .setNickname(nickname)
                .setAvatar(avatar);
        userSocialService.save(social);
        return user;
    }

    private String generateUniqueAccount(int length) {
        String candidate;
        int attempts = 0;
        do {
            candidate = generateRandomDigits(length);
            attempts++;
            if (attempts > 10) {
                candidate = generateRandomDigits(length + 1);
            }
        } while (userService.isAccountExists(candidate));
        return candidate;
    }

    private String generateRandomDigits(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private ResponseEntity<String> buildPostMessageHtml(String type, int code, User user, String message) {
        try {
            ObjectMapper mapper = objectMapper;
            Map<String, Object> payload = new HashMap<>();
            payload.put("code", code);
            if (message != null) payload.put("message", message);
            if (user != null) payload.put("data", user);
            String json = mapper.writeValueAsString(payload);
            String html = "<!DOCTYPE html>\n" +
                    "<html><head><meta charset=\"utf-8\"><title>WeChat Login</title></head>" +
                    "<body><script>\n" +
                    "(function(){\n" +
                    "  try {\n" +
                    "    var payload = " + json + ";\n" +
                    "    if (window.opener) {\n" +
                    "      window.opener.postMessage({ type: '" + type + "', payload: payload }, '*');\n" +
                    "    }\n" +
                    "  } catch(e){}\n" +
                    "  window.close();\n" +
                    "})();\n" +
                    "</script></body></html>";
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
        } catch (Exception e) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("WeChat login finished.");
        }
    }
}


