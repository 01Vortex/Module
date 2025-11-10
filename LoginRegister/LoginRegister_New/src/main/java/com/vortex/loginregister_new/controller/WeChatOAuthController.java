package com.vortex.loginregister_new.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.SocialLoginService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.util.JwtUtil;
import com.vortex.loginregister_new.util.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/oauth/wechat")
@RequiredArgsConstructor
public class WeChatOAuthController {

    private final UserService userService;
    private final SocialLoginService socialLoginService;
    private final JwtUtil jwtUtil;

    @Value("${wechat.oauth.app-id}")
    private String appId;

    @Value("${wechat.oauth.app-secret}")
    private String appSecret;

    @Value("${wechat.oauth.redirect-uri}")
    private String redirectUri;

    // 配置 ObjectMapper 支持 Java 8 时间类型
    private final ObjectMapper objectMapper = createObjectMapper();
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

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
    public ResponseEntity<String> callback(String code, String state, HttpServletRequest request) {
        if (code == null || code.isEmpty()) {
            return buildPostMessageHtml("wechat_auth", 400, null, "缺少code");
        }
        try {
            String clientIp = WebUtils.getClientIp(request);
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
            String email = null; // 微信通常不提供邮箱

            // 检查是否有当前登录用户（绑定模式）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            if (authentication != null && authentication.getName() != null) {
                currentUser = userService.findByAccount(authentication.getName());
            }
            
            if (currentUser != null) {
                // 绑定模式：将第三方账号绑定到当前登录用户
                boolean bound = socialLoginService.bindSocialAccount(
                        currentUser.getId(),
                        "wechat",
                        openid,
                        unionid,
                        nickname,
                        avatar
                );
                
                if (bound) {
                    // 绑定成功
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("code", 200);
                    payload.put("message", "微信账号绑定成功");
                    payload.put("data", currentUser);
                    return buildPostMessageHtml("wechat_auth", 200, currentUser, null, payload);
                } else {
                    return buildPostMessageHtml("wechat_auth", 400, null, "微信账号已被其他用户绑定");
                }
            }

            // 登录模式：使用统一的第三方登录服务处理登录或注册
            User user = socialLoginService.loginOrRegister(
                    "wechat",
                    openid,
                    unionid,
                    email, // 微信通常不提供邮箱
                    nickname,
                    avatar,
                    clientIp // 传入登录IP
            );
            
            if (user == null) {
                log.error("微信用户登录/注册失败");
                return buildPostMessageHtml("wechat_auth", 500, null, "用户登录失败，请重试");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                log.warn("微信用户账号已被禁用 - account: {}", user.getAccount());
                return buildPostMessageHtml("wechat_auth", 403, null, "账号已被禁用");
            }

            // 登录信息已在 loginOrRegister 方法中更新，无需再次更新

            // 获取用户角色
            List<String> roles = userService.getUserRoles(user.getId());
            String role = roles != null && !roles.isEmpty() ? roles.get(0) : "ROLE_USER";

            // 生成JWT token
            String jwtAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getAccount(), role);
            String jwtRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getAccount(), role);

            // 清除敏感信息
            user.setPassword(null);

            // 构建返回数据
            Map<String, Object> payload = new HashMap<>();
            payload.put("code", 200);
            payload.put("message", "登录成功");
            payload.put("data", user);
            payload.put("accessToken", jwtAccessToken);
            payload.put("refreshToken", jwtRefreshToken);
            payload.put("tokenType", "Bearer");
            return buildPostMessageHtml("wechat_auth", 200, user, null, payload);

        } catch (Exception e) {
            log.error("WeChat callback error", e);
            return buildPostMessageHtml("wechat_auth", 500, null, "微信登录失败: " + e.getMessage());
        }
    }

    private ResponseEntity<String> buildPostMessageHtml(String type, int code, User user, String message, Map<String, Object> customPayload) {
        try {
            ObjectMapper mapper = objectMapper;
            Map<String, Object> payload = customPayload != null ? customPayload : new HashMap<>();
            if (payload.isEmpty()) {
                payload.put("code", code);
                if (message != null) payload.put("message", message);
                if (user != null) payload.put("data", user);
            }
            String json = mapper.writeValueAsString(payload);
            // 转义JSON字符串中的特殊字符
            String escaped = json
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            String html = "<!DOCTYPE html>\n" +
                    "<html><head><meta charset=\"utf-8\"><title>WeChat Login</title></head>" +
                    "<body><script>\n" +
                    "(function(){\n" +
                    "  try {\n" +
                    "    var data = JSON.parse(\"" + escaped + "\");\n" +
                    "    if (window.opener) {\n" +
                    "      window.opener.postMessage({ type: '" + type + "', payload: data }, '*');\n" +
                    "    }\n" +
                    "  } catch(e){\n" +
                    "    console.error('Parse error:', e);\n" +
                    "  }\n" +
                    "  setTimeout(function(){window.close();}, 100);\n" +
                    "})();\n" +
                    "</script>" +
                    "<p>正在处理登录，请稍候...</p>" +
                    "</body></html>";
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
        } catch (Exception e) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("WeChat login finished.");
        }
    }
    
    private ResponseEntity<String> buildPostMessageHtml(String type, int code, User user, String message) {
        return buildPostMessageHtml(type, code, user, message, null);
    }
}


