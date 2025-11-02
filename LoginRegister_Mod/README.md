# LoginRegister Module

基于 **Spring Boot 3.2.5** 和 **Java 21** 的现代化登录注册模块，支持多种登录方式、虚拟线程、第三方 OAuth 集成。

## ✨ 核心特性

- 🔐 **多样登录**：密码、验证码、第三方（微信/QQ/Google）
- 🎨 **现代 UI**：Vue 3 + 响应式设计
- 🚀 **高性能**：Java 21 虚拟线程支持，提升并发处理能力
- 💾 **数据持久化**：MyBatis Plus + MySQL
- 🔒 **安全防护**：Spring Security + 密码加密 + 逻辑删除
- 📧 **邮箱验证**：SMTP 邮件验证码
- ⚡ **缓存加速**：Redis 存储验证码、会话

## 🛠️ 技术栈

### 后端
- **Java 21** + Spring Boot 3.2.5
- MyBatis Plus 3.5.5
- MySQL 8.3
- Redis（验证码、会话）
- Spring Security + BCrypt
- Java JWT 0.12.5

### 前端
- Vue 3 + Vite
- 响应式 UI + 粒子动画

## 📦 快速开始

### 环境要求
- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+

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

### 数据库初始化
```bash
# 执行 schema.sql 创建表结构
mysql -u root -p < LoginRegister_New/src/main/resources/schema.sql
```

## 📝 配置说明

### 邮箱配置（`application.yml`）
```yaml
spring:
  mail:
    host: smtp.qq.com
    username: your_email@qq.com
    password: your_auth_code
```

### 第三方登录
```yaml
wechat:
  oauth:
    app-id: YOUR_WECHAT_APP_ID
    app-secret: YOUR_WECHAT_APP_SECRET

google:
  oauth:
    client-id: YOUR_GOOGLE_CLIENT_ID
    client-secret: YOUR_GOOGLE_CLIENT_SECRET
```

## 🎯 功能清单

- ✅ 用户注册（邮箱/手机号 + 验证码）
- ✅ 密码登录
- ✅ 验证码登录
- ✅ 忘记密码（邮箱验证）
- ✅ 微信 OAuth 登录
- ✅ QQ OAuth 登录
- ✅ Google OAuth 登录
- ✅ 用户信息管理
- ✅ 验证码倒计时
- ✅ 二维码扫码提示
- ✅ 用户协议/隐私政策

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

