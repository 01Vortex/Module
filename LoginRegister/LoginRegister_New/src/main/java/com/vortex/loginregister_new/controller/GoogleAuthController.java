package com.vortex.loginregister_new.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vortex.loginregister_new.config.GoogleOAuthProperties;
import com.vortex.loginregister_new.entity.User;
import com.vortex.loginregister_new.service.UserService;
import com.vortex.loginregister_new.util.JwtUtil;
import com.vortex.loginregister_new.util.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/oauth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleOAuthProperties props;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    
    // 配置 ObjectMapper 支持 Java 8 时间类型
    private final ObjectMapper objectMapper = createObjectMapper();
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @GetMapping("/authorize")
    public String authorize(@RequestParam(value = "state", required = false) String state, 
                           jakarta.servlet.http.HttpServletRequest request) {
        log.info("Google OAuth authorize 方法被调用 - URI: {}, State: {}", request.getRequestURI(), state);
        // 验证配置
        if (props.getClientId() == null || props.getClientId().isBlank()) {
            log.error("Google OAuth client_id 未配置，无法进行授权");
            return "redirect:/error?message=Google登录配置错误：client_id未配置";
        }
        if (props.getRedirectUri() == null || props.getRedirectUri().isBlank()) {
            log.error("Google OAuth redirect_uri 未配置，无法进行授权");
            return "redirect:/error?message=Google登录配置错误：redirect_uri未配置";
        }
        
        try {
        String finalState = (state == null || state.isBlank()) ? UUID.randomUUID().toString() : state;
        String redirect = URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8);
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=" + props.getClientId() +
                "&redirect_uri=" + redirect +
                "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8) +
                "&state=" + finalState +
                "&access_type=online&include_granted_scopes=true&prompt=select_account";
            
            log.info("重定向到Google授权页面 - redirect_uri: {}", props.getRedirectUri());
        return "redirect:" + url;
        } catch (Exception e) {
            log.error("构建Google授权URL失败", e);
            return "redirect:/error?message=Google登录配置错误";
        }
    }

    @GetMapping(value = "/callback", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String callback(@RequestParam(name = "code", required = false) String code,
                           @RequestParam(name = "state", required = false) String state,
                           @RequestParam(name = "error", required = false) String error,
                           HttpServletRequest request) {
        // 检查是否有错误
        if (error != null && !error.isBlank()) {
            log.warn("Google OAuth 授权失败: {}", error);
            return popupResultHtml("error", Map.of("message", "Google授权失败: " + error));
        }
        
        if (code == null || code.isBlank()) {
            log.warn("Google OAuth 回调缺少 code 参数");
            return popupResultHtml("error", Map.of("message", "授权失败，请重试"));
        }

        try {
            String clientIp = WebUtils.getClientIp(request);
            log.info("Google OAuth 回调开始处理 - IP: {}", clientIp);

            // 1. 使用授权码换取访问令牌
            String accessToken = exchangeCodeForToken(code);
            if (accessToken == null) {
                log.error("Google OAuth 换取访问令牌失败 - code: {} (前10个字符)", 
                        code != null && code.length() > 10 ? code.substring(0, 10) : code);
                
                // 检查配置
                if (props.getClientId() == null || props.getClientId().isBlank()) {
                    log.error("Google OAuth client_id 未配置");
                    return popupResultHtml("error", Map.of("message", "Google登录配置错误：client_id未配置"));
                }
                if (props.getClientSecret() == null || props.getClientSecret().isBlank()) {
                    log.error("Google OAuth client_secret 未配置");
                    return popupResultHtml("error", Map.of("message", "Google登录配置错误：client_secret未配置"));
                }
                if (props.getRedirectUri() == null || props.getRedirectUri().isBlank()) {
                    log.error("Google OAuth redirect_uri 未配置");
                    return popupResultHtml("error", Map.of("message", "Google登录配置错误：redirect_uri未配置"));
                }
                
                return popupResultHtml("error", Map.of("message", "获取访问令牌失败，请检查Google OAuth配置或重试"));
            }

            // 2. 获取用户信息
            Map<String, String> userInfo = getUserInfoFromGoogle(accessToken);
            if (userInfo == null || userInfo.get("sub") == null) {
                log.error("Google OAuth 获取用户信息失败");
                return popupResultHtml("error", Map.of("message", "获取用户信息失败，请重试"));
            }

            String sub = userInfo.get("sub");
            String email = userInfo.get("email");
            String name = userInfo.get("name");
            String picture = userInfo.get("picture");

            // 3. 查找或创建用户
            String generatedAccount = "google_" + sub;
            User user = userService.findByAccount(generatedAccount);
            
            if (user == null) {
                // 新用户，创建账户
                log.info("创建新的Google用户 - account: {}, email: {}", generatedAccount, email);
                user = new User();
                user.setAccount(generatedAccount);
                user.setEmail(email != null ? email.toLowerCase() : null);
                user.setNickname(name != null && !name.isBlank() ? name : generatedAccount);
                user.setAvatar(picture);
                user.setStatus(1); // 默认启用
                
                // OAuth 用户不需要密码登录，但数据库要求必须有密码字段
                // 生成一个随机密码（OAuth用户不会使用密码登录）
                String randomPassword = UUID.randomUUID().toString() + System.currentTimeMillis();
                user.setPassword(passwordEncoder.encode(randomPassword));
                
                boolean registered = userService.register(user);
                if (!registered) {
                    log.error("Google用户注册失败 - account: {}", generatedAccount);
                    return popupResultHtml("error", Map.of("message", "用户注册失败，请重试"));
                }
                
                // 重新查询用户以获取ID
                user = userService.findByAccount(generatedAccount);
                if (user == null) {
                    log.error("Google用户注册后查询失败 - account: {}", generatedAccount);
                    return popupResultHtml("error", Map.of("message", "用户注册失败，请重试"));
                }
            } else {
                // 已存在用户，更新信息（可选）
                boolean needUpdate = false;
                if (email != null && !email.equals(user.getEmail())) {
                    user.setEmail(email.toLowerCase());
                    needUpdate = true;
                }
                if (name != null && !name.isBlank() && !name.equals(user.getNickname())) {
                    user.setNickname(name);
                    needUpdate = true;
                }
                if (picture != null && !picture.equals(user.getAvatar())) {
                    user.setAvatar(picture);
                    needUpdate = true;
                }
                if (needUpdate) {
                    userService.updateById(user);
                }
            }

            // 4. 检查用户状态
            if (user.getStatus() == 0) {
                log.warn("Google用户账号已被禁用 - account: {}", user.getAccount());
                return popupResultHtml("error", Map.of("message", "账号已被禁用"));
            }

            // 5. 更新登录信息
            userService.updateLastLoginInfo(user.getId(), clientIp);

            // 6. 获取用户角色
            List<String> roles = userService.getUserRoles(user.getId());
            String role = roles != null && !roles.isEmpty() ? roles.get(0) : "ROLE_USER";

            // 7. 生成JWT token
            String jwtAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getAccount(), role);
            String jwtRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getAccount(), role);

            // 8. 清除敏感信息
            user.setPassword(null);

            // 9. 构建返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("code", 200);
            responseData.put("message", "登录成功");
            responseData.put("data", user);
            responseData.put("accessToken", jwtAccessToken);
            responseData.put("refreshToken", jwtRefreshToken);
            responseData.put("tokenType", "Bearer");

            String userJson = objectMapper.writeValueAsString(responseData);
            log.info("Google登录成功 - account: {}, email: {}, IP: {}", user.getAccount(), user.getEmail(), clientIp);
            
            return popupPostMessageHtml(userJson);
            
        } catch (Exception e) {
            log.error("Google OAuth 回调处理失败", e);
            return popupResultHtml("error", Map.of("message", "登录失败: " + e.getMessage()));
        }
    }

    /**
     * 使用授权码换取访问令牌
     */
    private String exchangeCodeForToken(String code) {
        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "authorization_code");
            form.add("code", code);
            form.add("client_id", props.getClientId());
            form.add("client_secret", props.getClientSecret());
            form.add("redirect_uri", props.getRedirectUri());
            
            // 记录请求信息（不记录敏感信息）
            log.debug("Google token 交换请求 - redirect_uri: {}, client_id: {}", 
                    props.getRedirectUri(), 
                    props.getClientId() != null ? props.getClientId().substring(0, Math.min(20, props.getClientId().length())) + "..." : "null");
            
            HttpEntity<MultiValueMap<String, String>> tokenReq = new HttpEntity<>(form, headers);
            
            String tokenResp;
            try {
                tokenResp = restTemplate.postForObject(tokenUrl, tokenReq, String.class);
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // HTTP 4xx 错误
                String responseBody = e.getResponseBodyAsString();
                log.error("Google token 交换 HTTP 错误 - status: {}, response: {}", e.getStatusCode(), responseBody);
                
                // 尝试解析错误信息
                try {
                    JsonNode errorNode = objectMapper.readTree(responseBody);
                    if (errorNode.has("error")) {
                        String error = errorNode.get("error").asText();
                        String errorDescription = errorNode.has("error_description") 
                            ? errorNode.get("error_description").asText() 
                            : "未知错误";
                        log.error("Google OAuth 错误详情 - error: {}, description: {}", error, errorDescription);
                    }
                } catch (Exception parseEx) {
                    log.warn("无法解析错误响应: {}", responseBody);
                }
                
                return null;
            } catch (org.springframework.web.client.HttpServerErrorException e) {
                // HTTP 5xx 错误
                log.error("Google token 交换服务器错误 - status: {}, response: {}", e.getStatusCode(), e.getResponseBodyAsString());
                return null;
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // 网络错误（连接超时、连接拒绝等）
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("Connection timed out")) {
                    log.error("❌ Google token 交换连接超时");
                    log.error("   可能原因: 1) 网络无法访问Google服务 2) 需要配置代理 3) 防火墙阻止连接");
                    log.error("   解决方案: 如果在中国大陆，请配置代理服务器");
                    log.error("   配置位置: application-dev.yml -> rest.template.proxy.*");
                } else if (errorMsg != null && errorMsg.contains("Connection refused")) {
                    log.error("❌ Google token 交换连接被拒绝");
                    log.error("   请检查: 1) 代理服务器是否正在运行 (检查应用启动日志中的代理连接测试结果)");
                    log.error("          2) 代理端口是否正确 (常见端口: Clash 7890, V2Ray 10808)");
                    log.error("          3) 代理软件是否已启动并监听相应端口");
                    log.error("          4) 防火墙是否阻止了连接");
                    log.error("   测试方法: 在命令行执行 'telnet 127.0.0.1 7890' 或使用浏览器访问 Google");
                    log.error("   配置位置: application-dev.yml -> rest.template.proxy.*");
                } else {
                    log.error("❌ Google token 交换网络错误: {}", errorMsg);
                    log.error("   异常详情: {}", e.getClass().getSimpleName());
                }
                return null;
            }

            if (tokenResp == null || tokenResp.trim().isEmpty()) {
                log.error("Google token 响应为空");
                return null;
            }
            
            log.debug("Google token 响应: {}", tokenResp.substring(0, Math.min(200, tokenResp.length())));
            
            JsonNode tokenNode;
            try {
                tokenNode = objectMapper.readTree(tokenResp);
            } catch (Exception e) {
                log.error("Google token 响应解析失败 - response: {}", tokenResp, e);
                return null;
            }
            
            // 检查错误
            if (tokenNode.has("error")) {
                String error = tokenNode.get("error").asText();
                String errorDescription = tokenNode.has("error_description") 
                    ? tokenNode.get("error_description").asText() 
                    : "未知错误";
                String errorUri = tokenNode.has("error_uri") 
                    ? tokenNode.get("error_uri").asText() 
                    : null;
                
                log.error("Google token 交换失败 - error: {}, description: {}, uri: {}", 
                        error, errorDescription, errorUri);
                
                // 根据错误类型提供更详细的日志
                if ("invalid_grant".equals(error)) {
                    log.error("可能的原因: 授权码已过期或已使用，请重新授权");
                } else if ("invalid_client".equals(error)) {
                    log.error("可能的原因: client_id 或 client_secret 配置错误");
                } else if ("redirect_uri_mismatch".equals(error)) {
                    log.error("可能的原因: 重定向URI不匹配 - 配置的URI: {}", props.getRedirectUri());
                }
                
                return null;
            }
            
            if (tokenNode.has("access_token")) {
                String accessToken = tokenNode.get("access_token").asText();
                log.debug("Google access_token 获取成功");
                return accessToken;
            }
            
            log.warn("Google token 响应中缺少 access_token - 完整响应: {}", tokenResp);
            return null;
            
        } catch (Exception e) {
            log.error("Google token 交换异常", e);
            return null;
        }
    }

    /**
     * 从Google获取用户信息
     */
    private Map<String, String> getUserInfoFromGoogle(String accessToken) {
        try {
            String userInfoUrl = "https://openidconnect.googleapis.com/v1/userinfo";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userReq = new HttpEntity<>(authHeaders);
            
            String userResp = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userReq, String.class).getBody();
            
            if (userResp == null) {
                log.error("Google userinfo 响应为空");
                return null;
            }
            
            JsonNode userNode = objectMapper.readTree(userResp);
            
            // 检查错误
            if (userNode.has("error")) {
                String error = userNode.get("error").asText();
                log.error("Google userinfo 获取失败 - error: {}", error);
                return null;
            }
            
            Map<String, String> userInfo = new HashMap<>();
            if (userNode.has("sub")) {
                userInfo.put("sub", userNode.get("sub").asText());
            }
            if (userNode.has("email")) {
                userInfo.put("email", userNode.get("email").asText());
            }
            if (userNode.has("name")) {
                userInfo.put("name", userNode.get("name").asText());
            }
            if (userNode.has("picture")) {
                userInfo.put("picture", userNode.get("picture").asText());
            }
            if (userNode.has("given_name")) {
                userInfo.put("given_name", userNode.get("given_name").asText());
            }
            if (userNode.has("family_name")) {
                userInfo.put("family_name", userNode.get("family_name").asText());
            }
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("Google userinfo 获取异常", e);
            return null;
        }
    }

    private String popupPostMessageHtml(String jsonPayload) {
        try {
            // 转义JSON字符串中的特殊字符
            String escaped = jsonPayload
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            
        return "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset='utf-8'>" +
                    "<title>Google 登录</title>" +
                    "</head>" +
                    "<body>" +
                    "<script>" +
                "(function(){" +
                    "try{" +
                "var data = JSON.parse(\"" + escaped + "\");" +
                    "if(window.opener){" +
                    "window.opener.postMessage({type:'google_auth',payload:data},'*');" +
                    "}" +
                    "}catch(e){" +
                    "console.error('Parse error:',e);" +
                    "}" +
                    "setTimeout(function(){window.close();},100);" +
                "})();" +
                    "</script>" +
                    "<p>正在处理登录，请稍候...</p>" +
                    "</body>" +
                    "</html>";
        } catch (Exception e) {
            log.error("生成popup HTML失败", e);
            return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>错误</title></head>" +
                    "<body><p>登录处理失败，请关闭此窗口并重试。</p></body></html>";
        }
    }

    private String popupResultHtml(String status, Map<String, Object> payload) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("code", "ok".equals(status) ? 200 : 500);
            result.put("message", payload.get("message"));
            String json = objectMapper.writeValueAsString(result);
            return popupPostMessageHtml(json);
        } catch (Exception e) {
            log.error("生成错误HTML失败", e);
            return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>错误</title></head>" +
                    "<body><p>登录失败，请关闭此窗口并重试。</p></body></html>";
        }
    }
}


