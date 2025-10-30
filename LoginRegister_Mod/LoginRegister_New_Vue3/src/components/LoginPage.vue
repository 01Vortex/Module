<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="app-name">Sun</h1>
      <p class="app-desc">a fine app</p>
    </div>
    
    <!-- 居中的登录卡片 -->
    <div class="login-card">
      <!-- 左侧装饰区 -->
      <div class="left-section">
        <div class="welcome-content">
          <h1 class="welcome-title">欢迎登录</h1>
          <h2 class="module-name">01Vortex的模块</h2>
          
          <div class="qr-code">
            <canvas ref="qrCanvas" width="160" height="160"></canvas>
          </div>
          
          <p class="qr-hint">使用特定app扫码</p>
        </div>
        
        <div class="footer-link">
          <p>还没有账号？请先 <a href="#" @click.prevent="handleRegister">注册</a></p>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="right-section">
      <div class="login-form-wrapper">
        <div class="login-tabs">
          <button 
            :class="['tab', { active: loginType === 'password' }]"
            @click="loginType = 'password'"
          >
            密码登录
          </button>
          <button 
            :class="['tab', { active: loginType === 'code' }]"
            @click="loginType = 'code'"
          >
            验证码登录
          </button>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <input 
              v-model="username" 
              type="text" 
              placeholder="输入用户名或邮箱"
              required
            />
          </div>

          <div class="form-group" v-if="loginType === 'password'">
            <input 
              v-model="password" 
              type="password" 
              placeholder="输入密码"
              required
            />
          </div>

          <div class="form-group" v-else>
            <div class="code-input-group">
              <input 
                v-model="verifyCode" 
                type="text" 
                placeholder="输入验证码"
                required
              />
              <button type="button" class="send-code-btn" @click="sendCode">
                {{ codeButtonText }}
              </button>
            </div>
          </div>

          <div class="forgot-password">
            <a 
              v-if="loginType === 'password'" 
              href="#" 
              @click.prevent="handleForgotPassword"
            >
              忘记密码?
            </a>
          </div>

          <button type="submit" class="login-btn">登录/注册</button>
        </form>

        <div class="divider">
          <span>其他方式登录</span>
        </div>

        <div class="social-login">
          <button class="social-btn wechat" @click="handleSocialLogin('wechat')" title="微信登录">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M8.5 12c0-.8-.7-1.5-1.5-1.5s-1.5.7-1.5 1.5.7 1.5 1.5 1.5 1.5-.7 1.5-1.5zm8 0c0-.8-.7-1.5-1.5-1.5s-1.5.7-1.5 1.5.7 1.5 1.5 1.5 1.5-.7 1.5-1.5zM12 2C6.5 2 2 6 2 11c0 3 1.7 5.7 4.4 7.4l-.9 2.8c-.1.3.2.6.5.5l3.4-1.7c1-.3 2-.4 3.1-.4 5.5 0 10-4 10-9S17.5 2 12 2z"/>
            </svg>
          </button>
          <button class="social-btn qq" @click="handleSocialLogin('qq')" title="QQ登录">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm3.5 13.5c-.3.5-1 .8-1.5.5-.3-.2-.5-.5-.5-.9 0-.4.2-.7.5-.9.5-.3 1.2 0 1.5.5.3.4.3 1 0 1.3zm-7 0c-.3.5-1 .8-1.5.5-.3-.2-.5-.5-.5-.9 0-.4.2-.7.5-.9.5-.3 1.2 0 1.5.5.3.4.3 1 0 1.3z"/>
            </svg>
          </button>
          <button class="social-btn weibo" @click="handleSocialLogin('weibo')" title="微博登录">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M9.5 14c-2.5 0-4.5-1.3-4.5-3s2-3 4.5-3 4.5 1.3 4.5 3-2 3-4.5 3zm0-4.5c-1.4 0-2.5.7-2.5 1.5s1.1 1.5 2.5 1.5 2.5-.7 2.5-1.5-1.1-1.5-2.5-1.5zm7.5-2c-.8 0-1.5-.7-1.5-1.5s.7-1.5 1.5-1.5 1.5.7 1.5 1.5-.7 1.5-1.5 1.5z"/>
            </svg>
          </button>
        </div>

        <div class="privacy-notice">
          <p>登录代表同意<a href="#" @click.prevent="handlePrivacy">隐私政策</a></p>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loginType = ref('password')
const username = ref('')
const password = ref('')
const verifyCode = ref('')
const codeButtonText = ref('发送验证码')
const countdown = ref(0)
const qrCanvas = ref(null)

const handleLogin = () => {
  if (loginType.value === 'password') {
    console.log('密码登录:', { username: username.value, password: password.value })
  } else {
    console.log('验证码登录:', { username: username.value, code: verifyCode.value })
  }
  alert('登录功能待实现')
}

const handleRegister = () => {
  console.log('跳转到注册页面')
  alert('注册功能待实现')
}

const handleForgotPassword = () => {
  console.log('跳转到忘记密码页面')
  alert('忘记密码功能待实现')
}

const sendCode = () => {
  if (!username.value) {
    alert('请先输入用户名或邮箱')
    return
  }
  
  if (countdown.value > 0) {
    return
  }
  
  console.log('发送验证码到:', username.value)
  alert('验证码已发送')
  
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    codeButtonText.value = `${countdown.value}秒后重发`
    if (countdown.value <= 0) {
      clearInterval(timer)
      codeButtonText.value = '发送验证码'
    }
  }, 1000)
}

const handleSocialLogin = (platform) => {
  console.log('使用', platform, '登录')
  alert(`${platform}登录功能待实现`)
}

const handlePrivacy = () => {
  console.log('查看隐私政策')
  alert('隐私政策页面待实现')
}

// 生成随机二维码图案
const generateQRCode = () => {
  if (!qrCanvas.value) return
  
  const canvas = qrCanvas.value
  const ctx = canvas.getContext('2d')
  const size = 160
  const gridSize = 8 // 8x8 网格
  const cellSize = size / gridSize
  
  // 清空画布
  ctx.fillStyle = 'white'
  ctx.fillRect(0, 0, size, size)
  
  // 随机生成二维码图案
  ctx.fillStyle = '#2196f3'
  
  for (let i = 0; i < gridSize; i++) {
    for (let j = 0; j < gridSize; j++) {
      // 四个角保持固定的定位点
      const isCorner = 
        (i < 3 && j < 3) || // 左上角
        (i < 3 && j >= gridSize - 3) || // 右上角
        (i >= gridSize - 3 && j < 3) // 左下角
      
      if (isCorner) {
        ctx.fillRect(j * cellSize, i * cellSize, cellSize, cellSize)
        // 在定位点中间留白
        if ((i === 1 && j === 1) || 
            (i === 1 && j === gridSize - 2) || 
            (i === gridSize - 2 && j === 1)) {
          ctx.fillStyle = 'white'
          ctx.fillRect(j * cellSize + cellSize * 0.25, i * cellSize + cellSize * 0.25, 
                      cellSize * 0.5, cellSize * 0.5)
          ctx.fillStyle = '#2196f3'
        }
      } else {
        // 其他位置随机填充
        if (Math.random() > 0.5) {
          ctx.fillRect(j * cellSize, i * cellSize, cellSize, cellSize)
        }
      }
    }
  }
}

// 组件挂载时生成二维码
onMounted(() => {
  generateQRCode()
  // 每10秒更新一次二维码
  setInterval(generateQRCode, 10000)
})
</script>

<style scoped>
/* 页面容器 - 全屏居中 */
.page-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  min-height: 100vh;
  background: linear-gradient(135deg, #4a90e2 0%, #1e88e5 100%);
  padding: 30px 20px 40px;
  position: relative;
  overflow: hidden;
  gap: 25px;
}

/* 添加背景装饰 */
.page-container::before {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  top: -150px;
  left: -150px;
  animation: float 6s ease-in-out infinite;
}

.page-container::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  bottom: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 页面标题 */
.page-header {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
  animation: fadeIn 0.8s ease-out;
}

.page-header .app-name {
  font-size: 64px;
  font-weight: 300;
  margin: 0;
  letter-spacing: 3px;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
}

.page-header .app-desc {
  font-size: 20px;
  margin: 8px 0 0 0;
  font-weight: 300;
  opacity: 0.95;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 登录卡片 - 居中显示 */
.login-card {
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 520px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 左侧装饰区 */
.left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px 30px;
  color: #333;
  background: white;
  position: relative;
  border-right: 1px solid #e0e0e0;
}

.welcome-content,
.footer-link {
  position: relative;
  z-index: 2;
}

.welcome-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.welcome-title {
  font-size: 36px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: #333;
}

.module-name {
  font-size: 22px;
  font-weight: 400;
  margin: 0 0 30px 0;
  color: #666;
}

.qr-code {
  width: 150px;
  height: 150px;
  background: white;
  border-radius: 12px;
  padding: 10px;
  margin: 0 0 12px 0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s;
}

.qr-code:hover {
  transform: scale(1.05);
}

.qr-code canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.qr-hint {
  font-size: 16px;
  margin: 0;
  color: #666;
}

.footer-link {
  font-size: 15px;
  text-align: center;
  color: #666;
}

.footer-link a {
  color: #2196f3;
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.3s;
}

.footer-link a:hover {
  text-decoration: underline;
}

/* 右侧登录表单区 */
.right-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 35px 30px;
  background: white;
}

.login-form-wrapper {
  width: 100%;
  max-width: 380px;
}

.login-tabs {
  display: flex;
  gap: 28px;
  margin-bottom: 28px;
  border-bottom: 1px solid #e0e0e0;
}

.tab {
  background: none;
  border: none;
  padding: 10px 0;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
}

.tab:hover {
  color: #333;
}

.tab.active {
  color: #2196f3;
  font-weight: 500;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #2196f3;
}

.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #2196f3;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input-group input {
  flex: 1;
}

.send-code-btn {
  padding: 0 24px;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.send-code-btn:hover {
  background: #e8e8e8;
}

.forgot-password {
  text-align: right;
  margin-bottom: 12px;
  margin-top: -4px;
  min-height: 20px;
}

.forgot-password a {
  color: #2196f3;
  text-decoration: none;
  font-size: 13px;
  transition: opacity 0.3s;
}

.forgot-password a:hover {
  opacity: 0.8;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
}

.login-btn:hover {
  background: #1976d2;
}

.divider {
  text-align: center;
  margin: 24px 0;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background: #e0e0e0;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  background: white;
  padding: 0 16px;
  color: #999;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.social-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.social-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.social-btn svg {
  width: 22px;
  height: 22px;
}

.social-btn.wechat {
  background: #09bb07;
  color: white;
}

.social-btn.qq {
  background: #12b7f5;
  color: white;
}

.social-btn.weibo {
  background: #e6162d;
  color: white;
}

.privacy-notice {
  text-align: center;
  font-size: 13px;
  color: #999;
}

.privacy-notice a {
  color: #2196f3;
  text-decoration: none;
}

.privacy-notice a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .page-container {
    gap: 20px;
    padding: 25px 20px 30px;
  }

  .page-header .app-name {
    font-size: 52px;
  }

  .page-header .app-desc {
    font-size: 18px;
  }

  .login-card {
    flex-direction: column;
    max-width: 500px;
  }

  .left-section {
    padding: 40px 30px;
    min-height: auto;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }

  .welcome-title {
    font-size: 32px;
  }

  .module-name {
    font-size: 22px;
    margin-bottom: 30px;
  }

  .qr-code {
    width: 150px;
    height: 150px;
  }

  .footer-link {
    margin-top: 20px;
  }

  .right-section {
    padding: 40px 30px;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 20px 10px 20px;
    gap: 12px;
  }

  .page-header .app-name {
    font-size: 42px;
  }

  .page-header .app-desc {
    font-size: 16px;
  }

  .login-card {
    border-radius: 15px;
  }

  .left-section {
    padding: 30px 20px;
  }

  .welcome-title {
    font-size: 28px;
  }

  .module-name {
    font-size: 18px;
  }

  .qr-code {
    width: 130px;
    height: 130px;
  }

  .login-form-wrapper {
    max-width: 100%;
  }

  .right-section {
    padding: 30px 20px;
  }
}
</style>

