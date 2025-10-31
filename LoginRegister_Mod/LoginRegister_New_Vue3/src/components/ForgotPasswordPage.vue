<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="app-name">Sun</h1>
      <p class="app-desc">a fine app</p>
    </div>
    
    <!-- 居中的忘记密码卡片 -->
    <div class="forgot-card">
      <!-- 左侧装饰区 -->
      <div class="left-section">
        <div class="welcome-content">
          <h1 class="welcome-title">找回密码</h1>

          <div class="qr-code">
            <canvas ref="qrCanvas" width="160" height="160"></canvas>
          </div>
          
          <p class="qr-hint">遇到问题? 请扫码联系客服</p>
        </div>
        
        <div class="footer-link">
          <p>想起密码了？<a href="#" @click.prevent="$emit('switch-to-login')">返回登录</a></p>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="right-section">
        <div class="forgot-form-wrapper">
          <h2 class="form-title">重置密码</h2>

          <div v-if="!emailSent" class="forgot-form">
            <div class="form-group">
              <input 
                v-model="emailOrPhone" 
                type="text" 
                placeholder="请输入邮箱或手机号"
                :disabled="loading"
                required
              />
              <span v-if="errors.emailOrPhone" class="error-text">{{ errors.emailOrPhone }}</span>
            </div>

            <button type="button" class="forgot-btn" :disabled="loading" @click="sendResetCode">
              <span v-if="!loading">发送验证码</span>
              <span v-else>发送中...</span>
            </button>
          </div>

          <div v-else class="reset-form">
            <div class="form-group">
              <div class="code-input-group">
                <input 
                  v-model="resetCode" 
                  type="text" 
                  placeholder="输入验证码"
                  :disabled="loading"
                  required
                  maxlength="6"
                />
                <button 
                  type="button" 
                  class="send-code-btn" 
                  @click="sendResetCode"
                  :disabled="countdown > 0 || loading"
                >
                  {{ codeButtonText }}
                </button>
              </div>
              <span v-if="errors.resetCode" class="error-text">{{ errors.resetCode }}</span>
            </div>

            <div class="form-group">
              <input 
                v-model="newPassword" 
                type="password"
                placeholder="新密码（至少6位）"
                :disabled="loading"
                required
                minlength="6"
              />
              <span v-if="errors.newPassword" class="error-text">{{ errors.newPassword }}</span>
            </div>

            <div class="form-group">
              <input 
                v-model="confirmPassword" 
                type="password"
                placeholder="确认新密码"
                :disabled="loading"
                required
              />
              <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
            </div>

            <button type="button" class="forgot-btn" :disabled="loading" @click="resetPassword">
              <span v-if="!loading">重置密码</span>
              <span v-else>重置中...</span>
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
import { sendResetPasswordCode, resetPassword as resetPasswordApi } from '../api/auth'
import MessageBox from './MessageBox.vue'

const emit = defineEmits(['switch-to-login'])

const qrCanvas = ref(null)
const loading = ref(false)
const emailSent = ref(false)
const emailOrPhone = ref('')
const resetCode = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const codeButtonText = ref('发送验证码')
const countdown = ref(0)

const errors = reactive({
  emailOrPhone: '',
  resetCode: '',
  newPassword: '',
  confirmPassword: ''
})

const showMessageBox = ref(false)
const messageText = ref('')
const messageType = ref('info')

// 发送重置密码验证码
const sendResetCode = async () => {
  if (!emailOrPhone.value) {
    errors.emailOrPhone = '请输入邮箱或手机号'
    return
  }

  loading.value = true
  errors.emailOrPhone = ''

  try {
    const result = await sendResetPasswordCode(emailOrPhone.value)
    
    if (result && result.code === 200) {
      emailSent.value = true
      showMessage('验证码已发送，请查收', 'success')
      
      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        codeButtonText.value = `${countdown.value}秒后重发`
        if (countdown.value <= 0) {
          clearInterval(timer)
          codeButtonText.value = '重新发送'
        }
      }, 1000)
    } else {
      showMessage(result?.message || '发送失败', 'error')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    showMessage('网络错误，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

// 重置密码
const resetPassword = async () => {
  // 验证
  errors.resetCode = ''
  errors.newPassword = ''
  errors.confirmPassword = ''

  if (!resetCode.value) {
    errors.resetCode = '请输入验证码'
    return
  }

  if (!newPassword.value || newPassword.value.length < 6) {
    errors.newPassword = '密码长度至少为6位'
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    errors.confirmPassword = '两次输入的密码不一致'
    return
  }

  loading.value = true

  try {
    const result = await resetPasswordApi(emailOrPhone.value, resetCode.value, newPassword.value)
    
    if (result && result.code === 200) {
      showMessage('密码重置成功，请重新登录', 'success')
      setTimeout(() => {
        emit('switch-to-login')
      }, 2000)
    } else {
      showMessage(result?.message || '重置失败', 'error')
    }
  } catch (error) {
    console.error('重置密码失败:', error)
    showMessage('网络错误，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const showMessage = (text, type = 'info') => {
  messageText.value = text
  messageType.value = type
  showMessageBox.value = true
}

// 生成随机二维码图案
const generateQRCode = async () => {
  if (!qrCanvas.value) return

  const content = `${window.location.origin}/forgot-password?t=${Date.now()}`
  const QRCode = (await import('qrcode')).default
  await QRCode.toCanvas(qrCanvas.value, content, {
    width: 120,
    margin: 1,
    color: {
      dark: '#4caf50',
      light: '#ffffffff'
    }
  })
}

onMounted(() => {
  generateQRCode()
  setInterval(generateQRCode, 15000)
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

/* 忘记密码卡片 */
.forgot-card {
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
  margin: 6vh 0 12px 0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-code:hover {
  transform: scale(1.05);
}

.qr-code canvas {
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: 100%;
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

/* 右侧表单区 */
.right-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 35px 30px;
  background: white;
}

.forgot-form-wrapper {
  width: 100%;
  max-width: 380px;
}

.form-title {
  font-size: 28px;
  font-weight: 500;
  margin: 0 0 24px 0;
  color: #333;
  text-align: center;
}

.forgot-form,
.reset-form {
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

/* 忘记密码按钮 */
.forgot-btn {
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

.forgot-btn:hover:not(:disabled) {
  background: #1976d2;
}

.forgot-btn:disabled {
  background: #90caf9;
  cursor: not-allowed;
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

  .forgot-card {
    flex-direction: column;
    max-width: 500px;
  }

  .left-section {
    padding: 30px 20px;
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
    margin-top: 4vh;
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

  .forgot-card {
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

  .forgot-form-wrapper {
    max-width: 100%;
  }

  .right-section {
    padding: 30px 20px;
  }
}
</style>
