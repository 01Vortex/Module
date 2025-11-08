package com.vortex.loginregister_new.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortex.loginregister_new.config.GoogleOAuthProperties;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/oauth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleOAuthProperties props;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/authorize")
    public String authorize(@RequestParam(value = "state", required = false) String state) {
        String finalState = (state == null || state.isBlank()) ? UUID.randomUUID().toString() : state;
        String redirect = URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8);
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=" + props.getClientId() +
                "&redirect_uri=" + redirect +
                "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8) +
                "&state=" + finalState +
                "&access_type=online&include_granted_scopes=true&prompt=select_account";
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

            // Exchange code for token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "authorization_code");
            form.add("code", code);
            form.add("client_id", props.getClientId());
            form.add("client_secret", props.getClientSecret());
            form.add("redirect_uri", props.getRedirectUri());
            HttpEntity<MultiValueMap<String, String>> tokenReq = new HttpEntity<>(form, headers);
            String tokenResp = rest.postForObject(tokenUrl, tokenReq, String.class);

            if (tokenResp == null) {
                return popupResultHtml("error", Map.of("message", "Failed to get token"));
            }
            JsonNode tokenNode = objectMapper.readTree(tokenResp);
            String accessToken = tokenNode.has("access_token") ? tokenNode.get("access_token").asText() : null;
            if (accessToken == null) {
                log.warn("Google token response unexpected: {}", tokenResp);
                return popupResultHtml("error", Map.of("message", "Failed to get access_token"));
            }

            // Fetch userinfo
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userReq = new HttpEntity<>(authHeaders);
            String userInfoUrl = "https://openidconnect.googleapis.com/v1/userinfo";
            String userResp = rest.postForObject(userInfoUrl, userReq, String.class);
            if (userResp == null) {
                return popupResultHtml("error", Map.of("message", "Failed to get userinfo"));
            }
            JsonNode userNode = objectMapper.readTree(userResp);
            String sub = userNode.has("sub") ? userNode.get("sub").asText() : null;
            String email = userNode.has("email") ? userNode.get("email").asText() : null;
            String name = userNode.has("name") ? userNode.get("name").asText() : null;
            String picture = userNode.has("picture") ? userNode.get("picture").asText() : null;

            if (sub == null) {
                return popupResultHtml("error", Map.of("message", "Invalid userinfo"));
            }

            // Find or create user
            String generatedAccount = "google_" + sub;
            User user = userService.findByAccount(generatedAccount);
            if (user == null) {
                user = new User();
                user.setAccount(generatedAccount);
                user.setEmail(email);
                user.setNickname(name != null ? name : generatedAccount);
                user.setAvatar(picture);
                user.setStatus(1);
                userService.register(user);
            }

            String clientIp = getClientIp(request);
            userService.updateLastLoginInfo(user.getId(), clientIp);
            user.setPassword(null);

            String userJson = objectMapper.writeValueAsString(Map.of(
                    "code", 200,
                    "message", "登录成功",
                    "data", user
            ));
            return popupPostMessageHtml(userJson);
        } catch (Exception e) {
            log.error("Google 回调处理失败", e);
            return popupResultHtml("error", Map.of("message", e.getMessage()));
        }
    }

    private String popupPostMessageHtml(String jsonPayload) {
        String escaped = jsonPayload.replace("\\", "\\\\").replace("\"", "\\\"");
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='utf-8'><title>Google 登录</title></head>" +
                "<body><script>" +
                "(function(){" +
                "var data = JSON.parse(\"" + escaped + "\");" +
                "try { window.opener && window.opener.postMessage({ type: 'google_auth', payload: data }, '*'); } catch(e){}" +
                "window.close();" +
                "})();" +
                "</script></body></html>";
    }

    private String popupResultHtml(String status, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("code", status.equals("ok") ? 200 : 500, "message", payload.get("message")));
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


