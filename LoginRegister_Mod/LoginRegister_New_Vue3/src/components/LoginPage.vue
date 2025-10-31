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
          <h2 class="welcome-title">登录</h2>
          
          <div class="qr-code">
            <canvas ref="qrCanvas" width="160" height="160"></canvas>
          </div>
          
          <p class="qr-hint">使用app扫码登录</p>
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
              :placeholder="loginType === 'password' ? '输入用户名或邮箱' : '输入邮箱或手机号'"
              :disabled="loading"
              required
            />
          </div>

          <div class="form-group" v-if="loginType === 'password'">
            <input 
              v-model="password" 
              type="password"
              placeholder="输入密码"
              :disabled="loading"
              required
            />
          </div>

          <div class="form-group" v-else>
            <div class="code-input-group">
              <input 
                v-model="verifyCode" 
                type="text" 
                placeholder="输入验证码"
                :disabled="loading"
                required
              />
              <button 
                type="button" 
                :class="['send-code-btn', shouldShowBlueBtn ? 'is-blue' : '']" 
                @click="sendCode"
                :disabled="countdown > 0 || loading || !isValidContact"
              >
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

          <!-- 用户协议 -->
          <div class="agreement">
            <label>
              <input type="checkbox" v-model="agreedToTerms" required>
              <span>我已阅读并同意 <a href="#" @click.prevent="handleTerms">用户协议</a> 和 <a href="#" @click.prevent="handlePrivacy">隐私政策</a></span>
            </label>
          </div>

          <button type="submit" class="login-btn" :disabled="loading || !agreedToTerms">
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </button>
        </form>

        <div class="divider">
          <span>其他方式登录</span>
        </div>

        <div class="social-login">
          <button class="social-btn wechat" @click="handleSocialLogin('wechat')" title="微信登录">
            <svg width="24" height="24" fill="none" viewBox="0 0 24 24" class="ZDI ZDI--Wechat24">
              <path fill="#07C160" d="M20.314 18.59c1.333-.968 2.186-2.397 2.186-3.986 0-2.91-2.833-5.27-6.325-5.27-3.494 0-6.325 2.36-6.325 5.27 0 2.911 2.831 5.271 6.325 5.271.698.001 1.393-.096 2.064-.288l.186-.029c.122 0 .232.038.336.097l1.386.8.12.04a.21.21 0 0 0 .212-.211l-.034-.154-.285-1.063-.023-.135a.42.42 0 0 1 .177-.343ZM9.09 3.513C4.9 3.514 1.5 6.346 1.5 9.84c0 1.905 1.022 3.622 2.622 4.781a.505.505 0 0 1 .213.412l-.026.16-.343 1.276-.04.185c0 .14.113.254.252.254l.146-.047 1.663-.96a.793.793 0 0 1 .403-.116l.222.032c.806.231 1.64.348 2.478.348l.417-.01a4.888 4.888 0 0 1-.255-1.55c0-3.186 3.1-5.77 6.923-5.77l.411.011c-.57-3.02-3.71-5.332-7.494-5.332Zm4.976 10.248a.843.843 0 1 1 0-1.685.843.843 0 0 1 0 1.684v.001Zm4.217 0a.843.843 0 1 1 0-1.685.843.843 0 0 1 0 1.684v.001ZM6.561 8.827a1.012 1.012 0 1 1 0-2.023 1.012 1.012 0 0 1 0 2.023Zm5.061 0a1.012 1.012 0 1 1 0-2.023 1.012 1.012 0 0 1 0 2.023Z" clip-rule="evenodd"></path>
            </svg>
          </button>
          <button class="social-btn qq" @click="handleSocialLogin('qq')" title="QQ登录">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="#50C8FD" class="ZDI ZDI--Qq24">
              <path fill-rule="evenodd" d="M12.003 2c-2.265 0-6.29 1.364-6.29 7.325v1.195S3.55 14.96 3.55 17.474c0 .665.17 1.025.281 1.025.114 0 .902-.483 1.748-2.072 0 0-.18 2.197 1.904 3.967 0 0-1.77.495-1.77 1.182 0 .686 4.078.43 6.29 0 2.239.425 6.288.687 6.288 0 0-.688-1.77-1.182-1.77-1.182 2.086-1.77 1.906-3.967 1.906-3.967.845 1.588 1.634 2.072 1.746 2.072.111 0 .283-.36.283-1.025 0-2.514-2.165-6.954-2.165-6.954V9.325C18.29 3.364 14.268 2 12.003 2Z" clip-rule="evenodd"></path>
            </svg>
          </button>
          <button class="social-btn weibo" @click="handleSocialLogin('google')" title="Google 登录">
            <svg height="1em" style="flex:none;line-height:1" viewBox="0 0 24 24" width="1em" xmlns="http://www.w3.org/2000/svg"><title>Google</title><path d="M23 12.245c0-.905-.075-1.565-.236-2.25h-10.54v4.083h6.186c-.124 1.014-.797 2.542-2.294 3.569l-.021.136 3.332 2.53.23.022C21.779 18.417 23 15.593 23 12.245z" fill="#4285F4"></path><path d="M12.225 23c3.03 0 5.574-.978 7.433-2.665l-3.542-2.688c-.948.648-2.22 1.1-3.891 1.1a6.745 6.745 0 01-6.386-4.572l-.132.011-3.465 2.628-.045.124C4.043 20.531 7.835 23 12.225 23z" fill="#34A853"></path><path d="M5.84 14.175A6.65 6.65 0 015.463 12c0-.758.138-1.491.361-2.175l-.006-.147-3.508-2.67-.115.054A10.831 10.831 0 001 12c0 1.772.436 3.447 1.197 4.938l3.642-2.763z" fill="#FBBC05"></path><path d="M12.225 5.253c2.108 0 3.529.892 4.34 1.638l3.167-3.031C17.787 2.088 15.255 1 12.225 1 7.834 1 4.043 3.469 2.197 7.062l3.63 2.763a6.77 6.77 0 016.398-4.572z" fill="#EB4335"></path></svg>
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
import { ref, onMounted, watch, computed, onBeforeUnmount } from 'vue'
import { login, loginWithCode, sendVerificationCode, API_BASE_URL } from '../api/auth'
import MessageBox from './MessageBox.vue'

const props = defineProps({
  prefilledData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['switch-to-register', 'switch-to-forgot-password'])

const loginType = ref('password')
const username = ref('')
const password = ref('')
const verifyCode = ref('')
const codeButtonText = ref('发送验证码')
const countdown = ref(0)
const qrCanvas = ref(null)
const loading = ref(false)
const agreedToTerms = ref(false)

// 消息弹窗
const showMessageBox = ref(false)
const messageText = ref('')
const messageType = ref('info')

// 按钮变色校验（邮箱或11位手机号）
const isEmailFormat = computed(() => {
  const v = (username.value || '').trim()
  if (!v.includes('@')) return false
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)
})

const isPhoneFormat = computed(() => {
  const v = (username.value || '').trim()
  return /^\d{11}$/.test(v)
})

const isValidContact = computed(() => isEmailFormat.value || isPhoneFormat.value)

const shouldShowBlueBtn = computed(() => {
  const base = countdown.value === 0 && !loading.value
  return loginType.value === 'code' ? (isValidContact.value && base) : (isValidContact.value && base)
})
// 密码登录仅允许：用户名 或 邮箱
const isValidUsername = (v) => /^[a-zA-Z0-9_]{3,20}$/.test(v)
const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)

const validatePasswordLoginIdentity = (v) => {
  const value = (v || '').trim()
  if (!value) return false
  if (value.includes('@')) return isEmail(value)
  return isValidUsername(value)
}


// 监听预填充数据
watch(() => props.prefilledData, (data) => {
  if (data) {
    username.value = data.username || ''
    password.value = data.password || ''
    loginType.value = 'password'
  }
}, { immediate: true })

const handleLogin = async () => {
  if (!username.value) {
    showMessage('请输入用户名或邮箱', 'error')
    return
  }
  
  if (!agreedToTerms.value) {
    showMessage('请阅读并同意用户协议和隐私政策', 'error')
    return
  }
  
  loading.value = true
  
  try {
    let result
    
    if (loginType.value === 'password') {
      // 仅允许用户名或邮箱，不允许手机号
      if (!validatePasswordLoginIdentity(username.value)) {
        showMessage('请输入有效的用户名或邮箱', 'error')
        loading.value = false
        return
      }
      if (!password.value) {
        showMessage('请输入密码', 'error')
        loading.value = false
        return
      }
      result = await login(username.value, password.value)
    } else {
      // 验证码登录：支持邮箱或手机号（不支持用户名）
      if (!isValidContact.value) {
        showMessage('请输入有效的邮箱或手机号', 'error')
        loading.value = false
        return
      }
      if (!verifyCode.value) {
        showMessage('请输入验证码', 'error')
        loading.value = false
        return
      }
      result = await loginWithCode(username.value, verifyCode.value)
    }
    
    if (result.code === 200) {
      console.log('登录成功:', result.data)
      showMessage('登录成功！', 'success')
      // 保存用户信息到localStorage
      localStorage.setItem('user', JSON.stringify(result.data))
      // 跳转到主页或其他页面
      // setTimeout(() => {
      //   window.location.href = '/dashboard'
      // }, 1000)
    } else {
      showMessage(result.message || '登录失败', 'error')
    }
  } catch (error) {
    console.error('登录错误:', error)
    showMessage('网络错误，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  emit('switch-to-register')
}

const handleForgotPassword = () => {
  emit('switch-to-forgot-password')
}

const sendCode = async () => {
  if (!username.value) {
    showMessage('请先输入邮箱或手机号', 'error')
    return
  }
  
  if (countdown.value > 0) {
    return
  }
  
  try {
    // 验证码登录支持邮箱或手机号
    if (!isValidContact.value) {
      showMessage('请输入有效的邮箱或手机号', 'error')
      return
    }
    const result = await sendVerificationCode(username.value)
    
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

let qqMessageHandler = null
const handleSocialLogin = (platform) => {
  const w = 520
  const h = 600
  const left = window.screenX + Math.max(0, (window.outerWidth - w) / 2)
  const top = window.screenY + Math.max(0, (window.outerHeight - h) / 3)
  const features = `width=${w},height=${h},left=${left},top=${top},resizable=yes,scrollbars=yes`

  if (platform === 'qq') {
    const url = `${API_BASE_URL}/oauth/qq/authorize`
    const popup = window.open(url, 'qq_login', features)
    if (!popup) {
      showMessage('请允许弹出窗口以完成QQ登录', 'error')
      return
    }

    qqMessageHandler = (event) => {
      try {
        const data = event.data || {}
        if (data && data.type === 'qq_auth') {
          const payload = data.payload || {}
          if (payload.code === 200) {
            const user = payload.data
            localStorage.setItem('user', JSON.stringify(user))
            showMessage('QQ登录成功！', 'success')
          } else {
            showMessage(payload.message || 'QQ登录失败', 'error')
          }
          window.removeEventListener('message', qqMessageHandler)
          qqMessageHandler = null
        }
      } catch (e) {}
    }
    window.addEventListener('message', qqMessageHandler)
    return
  }

  if (platform === 'google') {
    const url = `${API_BASE_URL}/oauth/google/authorize`
    const popup = window.open(url, 'google_login', features)
    if (!popup) {
      showMessage('请允许弹出窗口以完成Google登录', 'error')
      return
    }

    qqMessageHandler = (event) => {
      try {
        const data = event.data || {}
        if (data && data.type === 'google_auth') {
          const payload = data.payload || {}
          if (payload.code === 200) {
            const user = payload.data
            localStorage.setItem('user', JSON.stringify(user))
            showMessage('Google登录成功！', 'success')
          } else {
            showMessage(payload.message || 'Google登录失败', 'error')
          }
          window.removeEventListener('message', qqMessageHandler)
          qqMessageHandler = null
        }
      } catch (e) {}
    }
    window.addEventListener('message', qqMessageHandler)
    return
  }

  showMessage(`${platform}登录功能开发中...`, 'info')
}

onBeforeUnmount(() => {
  if (qqMessageHandler) {
    window.removeEventListener('message', qqMessageHandler)
    qqMessageHandler = null
  }
})

const handleTerms = () => {
  console.log('查看用户协议')
  showMessage('用户协议页面开发中...', 'info')
}

const handlePrivacy = () => {
  console.log('查看隐私政策')
  showMessage('隐私政策页面开发中...', 'info')
}

// 统一的消息提示函数
const showMessage = (text, type = 'info') => {
  messageText.value = text
  messageType.value = type
  showMessageBox.value = true
}

// 生成随机二维码图案
const generateQRCode = async () => {
  if (!qrCanvas.value) return

  // 真实二维码内容：使用当前页面 URL + 时间戳，保证每次刷新内容变化
  const content = `${window.location.origin}/login?t=${Date.now()}`

  const QRCode = (await import('qrcode')).default
  await QRCode.toCanvas(qrCanvas.value, content, {
    width: 120,
    margin: 1,
    color: {
      dark: '#2196f3',      // 蓝色深色模块
      light: '#ffffffff'    // 纯白背景
    }
  })
}

// 组件挂载时生成二维码
onMounted(() => {
  generateQRCode()
  // 每15秒更新一次二维码
  setInterval(generateQRCode, 15000)
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
  max-width: 760px;
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
  padding: 32px 24px;
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

/* 右侧登录表单区 */
.right-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 24px;
  background: white;
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;
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

.send-code-btn:hover:not(:disabled) {
  background: #e8e8e8;
}

.send-code-btn.is-blue {
  background: #2196f3 !important;
  border-color: #2196f3 !important;
  color: #fff !important;
}

.send-code-btn.is-blue:hover:not(:disabled) {
  background: #1976d2 !important;
}

.send-code-btn:disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.forgot-password {
  text-align: right;
  margin-bottom: 12px;
  margin-top: -4px;
  height: 20px;
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

.login-btn:hover:not(:disabled) {
  background: #1976d2;
}

.login-btn:disabled {
  background: #90caf9;
  cursor: not-allowed;
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
  background: #f5f5f5;
  color: inherit;
}

.social-btn.qq {
  background: #f5f5f5;
  color: inherit;
}

.social-btn.weibo {
  background: #f5f5f5;
  color: inherit;
}

/* 用户协议 */
.agreement {
  margin: 16px 0;
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
    margin-top: 4vh;
  }

  .login-form-wrapper {
    max-width: 100%;
  }

  .right-section {
    padding: 30px 20px;
  }
}
</style>

