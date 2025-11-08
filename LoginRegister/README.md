# LoginRegister_New

基于 **Spring Boot 3.2.5** 和 **Java 21** 的现代化登录注册模块，支持多种登录方式、虚拟线程、第三方 OAuth 集成、管理员系统和文件存储。

## ✨ 核心特性

- 🔐 **多样登录**：密码、验证码、第三方（微信/QQ/Google）
- 👨‍💼 **管理员系统**：管理员登录、用户管理、数据统计
- 🎨 **现代 UI**：Vue 3 + 响应式设计 + 粒子动画
- 🚀 **高性能**：Java 21 虚拟线程支持，提升并发处理能力
- 💾 **数据持久化**：MyBatis Plus + MySQL 8.3
- 🔒 **安全防护**：Spring Security + BCrypt 密码加密 + 逻辑删除 + XSS 防护
- 📧 **邮箱验证**：SMTP 邮件验证码
- ⚡ **缓存加速**：Redis 存储验证码、会话、令牌黑名单
- 📸 **头像上传**：MinIO 对象存储 + 图片压缩处理
- 📚 **API 文档**：SpringDoc OpenAPI (Swagger) 自动生成
- 🔄 **Token 刷新**：JWT Access Token + Refresh Token 机制
- 📊 **数据统计**：用户统计图表（折线图、饼图）
- 🛡️ **安全审计**：登录日志、操作日志记录

## 🛠️ 技术栈

### 后端
- **Java 21** + Spring Boot 3.2.5
- MyBatis Plus 3.5.5
- MySQL 8.3
- Redis 6.0+（验证码、会话、令牌黑名单）
- Spring Security + BCrypt 密码加密
- Java JWT 0.12.5（Access Token + Refresh Token）
- MinIO 8.5.7（对象存储）
- Thumbnailator 0.4.20（图片处理）
- SpringDoc OpenAPI 2.3.0（API 文档）
- Spring Boot Actuator（监控和健康检查）
- Lombok 1.18.30（代码简化）
- Apache Commons Lang3（工具类）
- Logback（日志框架）

### 前端
- Vue 3.5.22 + Vite 7.1.7
- CropperJS 1.5.13（图片裁剪）
- QRCode 1.5.3（二维码生成）
- 响应式 UI + 粒子动画

## 📦 快速开始

### 环境要求
- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- MinIO（对象存储，可选）
- Node.js 18+
- Maven 3.6+

### 后端启动
```bash
cd LoginRegister_New
mvn clean install
mvn spring-boot:run
```

### 前端启动
```bash
cd LoginRegister_New_Vue3
npm install
npm run dev
```

### 数据库管理员
账号:admin
密码:Qwerasdf12.

或者直接在 MySQL 客户端中执行 `LoginRegister_New/mysql/login_register_new.sql` 文件。

## 📝 配置说明

项目采用多环境配置，主配置文件为 `application.yml`，环境特定配置为 `application-dev.yml`（开发环境）和 `application-prod.yml`（生产环境）。

### 数据库配置（`application-dev.yml`）
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/login_register_new?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### Redis 配置（`application-dev.yml`）
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果有密码则填写
```

### 邮箱配置（`application-dev.yml`）
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your_email@qq.com
    password: your_auth_code  # QQ 邮箱授权码
```

### MinIO 图床配置（`application-dev.yml`）
```yaml
minio:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
  bucket-name: your_bucket_name
  secure: false  # true 表示使用 HTTPS
```

### JWT 配置（`application-dev.yml`）
```yaml
jwt:
  secret: your_secret_key_here  # 请使用强密钥，建议长度 64 位以上
  expiration: 86400000  # Access Token 过期时间（毫秒），默认 24 小时
  refresh-expiration: 604800000  # Refresh Token 过期时间（毫秒），默认 7 天
```

### 第三方登录配置（`application-dev.yml`）

#### 微信 OAuth
```yaml
wechat:
  oauth:
    app-id: YOUR_WECHAT_APP_ID
    app-secret: YOUR_WECHAT_APP_SECRET
    redirect-uri: http://localhost:8080/api/oauth/wechat/callback
```

#### QQ OAuth
```yaml
qq:
  oauth:
    app-id: YOUR_QQ_APP_ID
    app-key: YOUR_QQ_APP_KEY
    redirect-uri: http://localhost:8080/api/oauth/qq/callback
```

#### Google OAuth
```yaml
google:
  oauth:
    client-id: YOUR_GOOGLE_CLIENT_ID
    client-secret: YOUR_GOOGLE_CLIENT_SECRET
    redirect-uri: http://localhost:8080/api/oauth/google/callback
```

### 环境切换

在 `application.yml` 中修改 `spring.profiles.active` 来切换环境：
```yaml
spring:
  profiles:
    active: dev  # dev 或 prod
```

### API 文档访问

启动项目后，访问以下地址查看 API 文档：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs

## 🎯 功能清单

### 用户功能
- ✅ 用户注册（邮箱/手机号 + 验证码）
- ✅ 密码登录
- ✅ 验证码登录
- ✅ 忘记密码（邮箱验证）
- ✅ Token 刷新机制（Access Token + Refresh Token）
- ✅ 用户信息管理（查看/编辑个人资料）
- ✅ 头像上传（支持图片裁剪、压缩、MinIO 存储）
- ✅ 账号/邮箱唯一性验证
- ✅ 验证码倒计时
- ✅ 用户协议/隐私政策

### 第三方登录(申请未通过qaq)
- ❌微信 OAuth 登录（二维码扫码）
- ❌ QQ OAuth 登录
- ❌ Google OAuth 登录
- ❌ 第三方账号绑定

### 管理员功能
- ✅ 管理员登录
- ✅ 管理员忘记密码
- ✅ 用户管理（增删改查）
- ✅ 用户状态管理（启用/禁用）
- ✅ 用户搜索（支持账号、邮箱、昵称）
- ✅ 数据统计（总用户数、活跃用户、今日新增、状态分布）
- ✅ 统计图表（折线图、饼图）
- ✅ 分页查询

### 安全功能
- ✅ Spring Security 安全框架
- ✅ BCrypt 密码加密
- ✅ JWT Token 认证
- ✅ Token 黑名单机制
- ✅ XSS 防护（输入验证和清理）
- ✅ SQL 注入防护
- ✅ 逻辑删除
- ✅ 登录日志记录
- ✅ 操作日志记录
- ✅ 安全审计日志

### 其他功能
- ✅ 多环境配置（dev/prod）
- ✅ API 文档自动生成（Swagger）
- ✅ 虚拟线程支持（Java 21）
- ✅ 文件上传限制（大小、类型验证）
- ✅ 图片处理（压缩、尺寸调整）
- ✅ 日志记录（Logback）
- ✅ 健康检查（Spring Boot Actuator）

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

