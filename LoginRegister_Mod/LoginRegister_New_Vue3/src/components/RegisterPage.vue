<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="app-name">Sun</h1>
      <p class="app-desc">a fine app</p>
    </div>
    
    <!-- 居中的注册卡片 -->
    <div class="register-card">
      <!-- 左侧装饰区 -->
      <div class="left-section">
        <div class="welcome-content">
          <h1 class="welcome-title">欢迎注册</h1>
          <h2 class="module-name">01Vortex的模块</h2>
          
          <div class="qr-code">
            <canvas ref="qrCanvas" width="160" height="160"></canvas>
          </div>
          
          <p class="qr-hint">使用特定app扫码</p>
        </div>
        
        <div class="footer-link">
          <p>已有账号？<a href="#" @click.prevent="$emit('switch-to-login')">立即登录</a></p>
        </div>
      </div>

      <!-- 右侧注册表单区 -->
      <div class="right-section">
        <div class="register-form-wrapper">
          <h2 class="form-title">创建新账号</h2>

          <form @submit.prevent="handleRegister" class="register-form">
            <!-- 用户名 -->
            <div class="form-group">
              <input 
                v-model="formData.username" 
                type="text" 
                placeholder="用户名（3-20位字符）"
                :disabled="loading"
                required
                minlength="3"
                maxlength="20"
                @blur="validateUsername"
              />
              <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
            </div>

            <!-- 邮箱或手机号 -->
            <div class="form-group">
              <input 
                v-model="formData.emailOrPhone" 
                type="text" 
                placeholder="邮箱地址或手机号"
                :disabled="loading"
                required
                @blur="validateEmailOrPhone"
              />
              <span v-if="errors.emailOrPhone" class="error-text">{{ errors.emailOrPhone }}</span>
            </div>

            <!-- 密码 -->
            <div class="form-group">
              <input 
                v-model="formData.password" 
                type="password" 
                placeholder="密码（至少6位）"
                :disabled="loading"
                required
                minlength="6"
                @blur="validatePassword"
              />
              <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
            </div>

            <!-- 确认密码 -->
            <div class="form-group">
              <input 
                v-model="formData.confirmPassword" 
                type="password" 
                placeholder="确认密码"
                :disabled="loading"
                required
                @blur="validateConfirmPassword"
              />
              <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
            </div>

            <!-- 验证码 -->
            <div class="form-group">
              <div class="code-input-group">
                <input 
                  v-model="formData.verifyCode" 
                  type="text" 
                  placeholder="验证码"
                  :disabled="loading"
                  required
                  maxlength="6"
                />
                <button 
                  type="button" 
                  class="send-code-btn" 
                  @click="sendCode"
                  :disabled="countdown > 0 || loading || !formData.emailOrPhone"
                >
                  {{ codeButtonText }}
                </button>
              </div>
              <span v-if="errors.verifyCode" class="error-text">{{ errors.verifyCode }}</span>
            </div>

            <!-- 服务条款 -->
            <div class="agreement">
              <label>
                <input type="checkbox" v-model="agreedToTerms" required>
                <span>我已阅读并同意 <a href="#" @click.prevent="showTerms">用户协议</a> 和 <a href="#" @click.prevent="showPrivacy">隐私政策</a></span>
              </label>
            </div>

            <!-- 注册按钮 -->
            <button type="submit" class="register-btn" :disabled="loading || !agreedToTerms">
              <span v-if="!loading">立即注册</span>
              <span v-else>注册中...</span>
            </button>
          </form>

          <div class="divider">
            <span>或使用社交账号注册</span>
          </div>

          <div class="social-login">
            <button class="social-btn wechat" @click="handleSocialRegister('wechat')" title="微信注册">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M8.5 12c0-.8-.7-1.5-1.5-1.5s-1.5.7-1.5 1.5.7 1.5 1.5 1.5 1.5-.7 1.5-1.5zm8 0c0-.8-.7-1.5-1.5-1.5s-1.5.7-1.5 1.5.7 1.5 1.5 1.5 1.5-.7 1.5-1.5zM12 2C6.5 2 2 6 2 11c0 3 1.7 5.7 4.4 7.4l-.9 2.8c-.1.3.2.6.5.5l3.4-1.7c1-.3 2-.4 3.1-.4 5.5 0 10-4 10-9S17.5 2 12 2z"/>
              </svg>
            </button>
            <button class="social-btn qq" @click="handleSocialRegister('qq')" title="QQ注册">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm3.5 13.5c-.3.5-1 .8-1.5.5-.3-.2-.5-.5-.5-.9 0-.4.2-.7.5-.9.5-.3 1.2 0 1.5.5.3.4.3 1 0 1.3zm-7 0c-.3.5-1 .8-1.5.5-.3-.2-.5-.5-.5-.9 0-.4.2-.7.5-.9.5-.3 1.2 0 1.5.5.3.4.3 1 0 1.3z"/>
              </svg>
            </button>
            <button class="social-btn weibo" @click="handleSocialRegister('weibo')" title="微博注册">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M9.5 14c-2.5 0-4.5-1.3-4.5-3s2-3 4.5-3 4.5 1.3 4.5 3-2 3-4.5 3zm0-4.5c-1.4 0-2.5.7-2.5 1.5s1.1 1.5 2.5 1.5 2.5-.7 2.5-1.5-1.1-1.5-2.5-1.5zm7.5-2c-.8 0-1.5-.7-1.5-1.5s.7-1.5 1.5-1.5 1.5.7 1.5 1.5-.7 1.5-1.5 1.5z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 消息弹窗 -->
  <MessageBox 
    :show="showMessageBox" 
    :message="messageText" 
    :type="messageType"
    @close="showMessageBox = false"
  />
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { register, checkUsername, checkEmail, sendVerificationCode } from '../api/auth'
import MessageBox from './MessageBox.vue'

const emit = defineEmits(['switch-to-login', 'register-success'])

const qrCanvas = ref(null)
const loading = ref(false)
const agreedToTerms = ref(false)

// 验证码相关
const codeButtonText = ref('发送验证码')
const countdown = ref(0)

// 表单数据
const formData = reactive({
  username: '',
  emailOrPhone: '',
  password: '',
  confirmPassword: '',
  verifyCode: ''
})

// 错误信息
const errors = reactive({
  username: '',
  emailOrPhone: '',
  password: '',
  confirmPassword: '',
  verifyCode: ''
})

// 消息弹窗
const showMessageBox = ref(false)
const messageText = ref('')
const messageType = ref('info')

// 验证用户名
const validateUsername = async () => {
  errors.username = ''
  
  if (!formData.username) {
    return
  }
  
  if (formData.username.length < 3 || formData.username.length > 20) {
    errors.username = '用户名长度应为3-20位字符'
    return
  }
  
  // 检查用户名格式（字母、数字、下划线）
  if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
    errors.username = '用户名只能包含字母、数字和下划线'
    return
  }
  
  try {
    const result = await checkUsername(formData.username)
    if (result.exists) {
      errors.username = '该用户名已被使用'
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 验证邮箱或手机号
const validateEmailOrPhone = async () => {
  errors.emailOrPhone = ''
  
  if (!formData.emailOrPhone) {
    return
  }
  
  const value = formData.emailOrPhone.trim()
  
  // 判断是邮箱还是手机号
  if (value.includes('@')) {
    // 邮箱验证
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailPattern.test(value)) {
      errors.emailOrPhone = '请输入有效的邮箱地址'
      return
    }
    
    try {
      const result = await checkEmail(value)
      if (result.exists) {
        errors.emailOrPhone = '该邮箱已被注册'
      }
    } catch (error) {
      console.error('检查邮箱失败:', error)
    }
  } else {
    // 手机号验证
    const phonePattern = /^1[3-9]\d{9}$/
    if (!phonePattern.test(value)) {
      errors.emailOrPhone = '请输入有效的手机号（11位）'
      return
    }
  }
}

// 验证密码
const validatePassword = () => {
  errors.password = ''
  
  if (!formData.password) {
    return
  }
  
  if (formData.password.length < 6) {
    errors.password = '密码长度至少为6位'
    return
  }
  
  // 验证确认密码
  if (formData.confirmPassword) {
    validateConfirmPassword()
  }
}

// 验证确认密码
const validateConfirmPassword = () => {
  errors.confirmPassword = ''
  
  if (!formData.confirmPassword) {
    return
  }
  
  if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
  }
}

// 发送验证码
const sendCode = async () => {
  if (!formData.emailOrPhone) {
    showMessage('请先输入邮箱或手机号', 'error')
    return
  }
  
  // 验证邮箱或手机号格式
  await validateEmailOrPhone()
  if (errors.emailOrPhone) {
    showMessage(errors.emailOrPhone, 'error')
    return
  }
  
  if (countdown.value > 0) {
    return
  }
  
  try {
    const result = await sendVerificationCode(formData.emailOrPhone)
    
    if (result.code === 200) {
      // 开发环境显示验证码
      if (result.verificationCode) {
        console.log('验证码:', result.verificationCode)
        showMessage(`验证码已发送！验证码: ${result.verificationCode}`, 'success')
      } else {
        showMessage('验证码已发送，请查收', 'success')
      }
      
      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        codeButtonText.value = `${countdown.value}秒后重发`
        if (countdown.value <= 0) {
          clearInterval(timer)
          codeButtonText.value = '发送验证码'
        }
      }, 1000)
    } else {
      showMessage(result.message || '发送失败', 'error')
    }
  } catch (error) {
    console.error('发送验证码错误:', error)
    showMessage('网络错误，请稍后重试', 'error')
  }
}

// 处理注册
const handleRegister = async () => {
  // 清除所有错误
  Object.keys(errors).forEach(key => errors[key] = '')
  
  // 验证所有字段
  await validateUsername()
  await validateEmailOrPhone()
  validatePassword()
  validateConfirmPassword()
  
  // 检查验证码
  if (!formData.verifyCode) {
    errors.verifyCode = '请输入验证码'
  }
  
  // 检查是否有错误
  const hasErrors = Object.values(errors).some(error => error !== '')
  if (hasErrors) {
    showMessage('请检查表单中的错误', 'error')
    return
  }
  
  if (!agreedToTerms.value) {
    showMessage('请阅读并同意用户协议和隐私政策', 'error')
    return
  }
  
  loading.value = true
  
  try {
    // 判断是邮箱还是手机号
    const isEmail = formData.emailOrPhone.includes('@')
    
    const registerData = {
      username: formData.username,
      password: formData.password,
      verifyCode: formData.verifyCode
    }
    
    // 根据是否包含@符号添加email或phone字段
    if (isEmail) {
      registerData.email = formData.emailOrPhone
    } else {
      registerData.phone = formData.emailOrPhone
    }
    
    const result = await register(registerData)
    
    if (result.code === 200) {
      showMessage('注册成功！即将跳转到登录页...', 'success')
      
      // 2秒后跳转到登录页
      setTimeout(() => {
        emit('register-success', { 
          username: formData.username,
          password: formData.password 
        })
        emit('switch-to-login')
      }, 2000)
    } else {
      showMessage(result.message || '注册失败', 'error')
    }
  } catch (error) {
    console.error('注册错误:', error)
    showMessage('网络错误，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

// 社交账号注册
const handleSocialRegister = (platform) => {
  showMessage(`${platform}注册功能开发中...`, 'info')
}

// 查看用户协议
const showTerms = () => {
  showMessage('用户协议页面开发中...', 'info')
}

// 查看隐私政策
const showPrivacy = () => {
  showMessage('隐私政策页面开发中...', 'info')
}

// 消息提示函数
const showMessage = (text, type = 'info') => {
  messageText.value = text
  messageType.value = type
  showMessageBox.value = true
}

// 生成随机二维码图案
const generateQRCode = () => {
  if (!qrCanvas.value) return
  
  const canvas = qrCanvas.value
  const ctx = canvas.getContext('2d')
  const size = 160
  const gridSize = 8
  const cellSize = size / gridSize
  
  ctx.fillStyle = 'white'
  ctx.fillRect(0, 0, size, size)
  
  ctx.fillStyle = '#2196f3'
  
  for (let i = 0; i < gridSize; i++) {
    for (let j = 0; j < gridSize; j++) {
      const isCorner = 
        (i < 3 && j < 3) || 
        (i < 3 && j >= gridSize - 3) || 
        (i >= gridSize - 3 && j < 3)
      
      if (isCorner) {
        ctx.fillRect(j * cellSize, i * cellSize, cellSize, cellSize)
        if ((i === 1 && j === 1) || 
            (i === 1 && j === gridSize - 2) || 
            (i === gridSize - 2 && j === 1)) {
          ctx.fillStyle = 'white'
          ctx.fillRect(j * cellSize + cellSize * 0.25, i * cellSize + cellSize * 0.25, 
                      cellSize * 0.5, cellSize * 0.5)
          ctx.fillStyle = '#2196f3'
        }
      } else {
        if (Math.random() > 0.5) {
          ctx.fillRect(j * cellSize, i * cellSize, cellSize, cellSize)
        }
      }
    }
  }
}

onMounted(() => {
  generateQRCode()
  setInterval(generateQRCode, 10000)
})
</script>

<style scoped>
/* 页面容器 */
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

/* 注册卡片 */
.register-card {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 640px;
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

/* 右侧注册表单区 */
.right-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 35px 30px;
  background: white;
  overflow-y: auto;
}

.register-form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-title {
  font-size: 28px;
  font-weight: 500;
  margin: 0 0 24px 0;
  color: #333;
  text-align: center;
}

.register-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
  position: relative;
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

.form-group input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
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

.send-code-btn:hover:not(:disabled) {
  background: #e8e8e8;
}

.send-code-btn:disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.error-text {
  display: block;
  color: #f44336;
  font-size: 12px;
  margin-top: 4px;
  margin-left: 4px;
}

/* 服务条款 */
.agreement {
  margin: 20px 0;
  font-size: 13px;
}

.agreement label {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  color: #666;
}

.agreement input[type="checkbox"] {
  margin-right: 8px;
  margin-top: 2px;
  cursor: pointer;
}

.agreement a {
  color: #2196f3;
  text-decoration: none;
}

.agreement a:hover {
  text-decoration: underline;
}

/* 注册按钮 */
.register-btn {
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

.register-btn:hover:not(:disabled) {
  background: #1976d2;
}

.register-btn:disabled {
  background: #90caf9;
  cursor: not-allowed;
}

/* 分隔线 */
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

/* 社交登录 */
.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
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

/* 响应式设计 */
@media (max-width: 1024px) {
  .register-card {
    flex-direction: column;
    max-width: 500px;
  }

  .left-section {
    padding: 30px 20px;
    min-height: auto;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }

  .right-section {
    padding: 30px 20px;
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

  .register-card {
    border-radius: 15px;
  }

  .form-title {
    font-size: 24px;
  }

  .register-form-wrapper {
    max-width: 100%;
  }
}
</style>

