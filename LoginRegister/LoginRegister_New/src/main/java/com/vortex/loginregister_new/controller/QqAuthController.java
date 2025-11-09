package com.vortex.loginregister_new.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vortex.loginregister_new.config.QqOAuthProperties;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.SocialLoginService;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/oauth/qq")
@RequiredArgsConstructor
public class QqAuthController {

    private final QqOAuthProperties props;
    private final UserService userService;
    private final SocialLoginService socialLoginService;
    private final JwtUtil jwtUtil;
    
    // 配置 ObjectMapper 支持 Java 8 时间类型
    private final ObjectMapper objectMapper = createObjectMapper();
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @GetMapping("/authorize")
    public String authorize(@RequestParam(value = "state", required = false) String state) throws UnsupportedEncodingException {
        String finalState = (state == null || state.isBlank()) ? UUID.randomUUID().toString() : state;
        String redirect = URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8);
        String url = "https://graph.qq.com/oauth2.0/authorize" +
                "?response_type=code" +
                "&client_id=" + props.getAppId() +
                "&redirect_uri=" + redirect +
                "&state=" + finalState +
                "&display=page";
        return "redirect:" + url;
    }

    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String callback(@RequestParam(name = "code", required = false) String code,
                           @RequestParam(name = "state", required = false) String state,
                           HttpServletRequest request) {
        if (code == null || code.isBlank()) {
            return popupResultHtml("error", Map.of("message", "Missing code"));
        }

        try {
            RestTemplate rest = new RestTemplate();

            String tokenUrl = "https://graph.qq.com/oauth2.0/token" +
                    "?grant_type=authorization_code" +
                    "&client_id=" + props.getAppId() +
                    "&client_secret=" + props.getAppKey() +
                    "&code=" + code +
                    "&redirect_uri=" + URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8);

            String tokenResp = rest.getForObject(tokenUrl, String.class);
            if (tokenResp == null || !tokenResp.contains("access_token=")) {
                log.warn("QQ token response unexpected: {}", tokenResp);
                return popupResultHtml("error", Map.of("message", "Failed to get access_token"));
            }
            String accessToken = extractQueryParam(tokenResp, "access_token");

            String meUrl = "https://graph.qq.com/oauth2.0/me?access_token=" + accessToken;
            String meResp = rest.getForObject(meUrl, String.class);
            // response like: callback( {"client_id":"APPID","openid":"OPENID"} );
            String json = meResp;
            if (json != null) {
                int start = json.indexOf("{");
                int end = json.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    json = json.substring(start, end + 1);
                }
            }
            JsonNode meNode = objectMapper.readTree(json);
            String openId = meNode.get("openid").asText();
            String unionId = meNode.has("unionid") ? meNode.get("unionid").asText() : null;

            // Optional: fetch user info
            String infoUrl = "https://graph.qq.com/user/get_user_info?access_token=" + accessToken +
                    "&oauth_consumer_key=" + props.getAppId() +
                    "&openid=" + openId;
            String infoResp = rest.getForObject(infoUrl, String.class);
            String nickname = null;
            String avatar = null;
            String email = null; // QQ 通常不提供邮箱
            try {
                JsonNode infoNode = objectMapper.readTree(infoResp);
                if (infoNode.has("nickname")) nickname = infoNode.get("nickname").asText();
                if (infoNode.has("figureurl_qq_2")) {
                    avatar = infoNode.get("figureurl_qq_2").asText();
                } else if (infoNode.has("figureurl_2")) {
                    avatar = infoNode.get("figureurl_2").asText();
                }
            } catch (Exception ignored) { }

            String clientIp = getClientIp(request);
            
            // 使用统一的第三方登录服务处理登录或注册
            User user = socialLoginService.loginOrRegister(
                    "qq",
                    openId,
                    unionId,
                    email, // QQ 通常不提供邮箱
                    nickname,
                    avatar,
                    clientIp // 传入登录IP
            );
            
            if (user == null) {
                log.error("QQ用户登录/注册失败");
                return popupResultHtml("error", Map.of("message", "用户登录失败，请重试"));
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                log.warn("QQ用户账号已被禁用 - account: {}", user.getAccount());
                return popupResultHtml("error", Map.of("message", "账号已被禁用"));
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
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("code", 200);
            responseData.put("message", "登录成功");
            responseData.put("data", user);
            responseData.put("accessToken", jwtAccessToken);
            responseData.put("refreshToken", jwtRefreshToken);
            responseData.put("tokenType", "Bearer");

            String userJson = objectMapper.writeValueAsString(responseData);
            log.info("QQ登录成功 - account: {}, IP: {}", user.getAccount(), clientIp);
            return popupPostMessageHtml(userJson);
        } catch (Exception e) {
            log.error("QQ 回调处理失败", e);
            return popupResultHtml("error", Map.of("message", e.getMessage()));
        }
    }

    private String extractQueryParam(String queryString, String key) {
        String[] parts = queryString.split("&");
        for (String part : parts) {
            if (part.startsWith(key + "=")) {
                return part.substring((key + "=").length());
            }
        }
        return null;
    }

    private String popupPostMessageHtml(String jsonPayload) {
        String escaped = jsonPayload.replace("\\", "\\\\").replace("\"", "\\\"");
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='utf-8'><title>QQ 登录</title></head>" +
                "<body><script>" +
                "(function(){" +
                "var data = JSON.parse(\"" + escaped + "\");" +
                "try { window.opener && window.opener.postMessage({ type: 'qq_auth', payload: data }, '*'); } catch(e){}" +
                "window.close();" +
                "})();" +
                "</script></body></html>";
    }

    private String popupResultHtml(String status, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("code", status.equals("ok") ? 200 : 500, "payload", payload));
            return popupPostMessageHtml(json);
        } catch (Exception e) {
            return "登录失败，请关闭此窗口";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        return (ip == null || ip.isBlank()) ? request.getRemoteAddr() : ip;
    }
}


