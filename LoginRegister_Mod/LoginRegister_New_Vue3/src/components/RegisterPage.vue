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
        <div class="left-art-wrap">
          <canvas ref="artCanvas" class="left-art"></canvas>
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
            <!-- 昵称 -->
            <div class="form-group">
              <input 
                v-model="formData.nickname" 
                type="text" 
                placeholder="昵称（显示名称）"
                :disabled="loading"
                required
                minlength="1"
                maxlength="20"
                @blur="validateNickname"
              />
              <span v-if="errors.nickname" class="error-text">{{ errors.nickname }}</span>
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
                placeholder="密码（至少8位，包含字母和数字，可包含符号）"
                :disabled="loading"
                required
                minlength="8"
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
                  :class="['send-code-btn', shouldShowBlueBtn ? 'is-blue' : '']" 
                  @click="sendCode"
                  :disabled="countdown > 0 || loading || !isValidContact"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { register, checkEmail, sendVerificationCode } from '../api/auth'
import MessageBox from './MessageBox.vue'

const emit = defineEmits(['switch-to-login', 'register-success'])

const qrCanvas = ref(null)
const artCanvas = ref(null)
const loading = ref(false)
const agreedToTerms = ref(false)

// 验证码相关
const codeButtonText = ref('发送验证码')
const countdown = ref(0)

// 表单数据
const formData = reactive({
  nickname: '',
  emailOrPhone: '',
  password: '',
  confirmPassword: '',
  verifyCode: ''
})

// 错误信息
const errors = reactive({
  nickname: '',
  emailOrPhone: '',
  password: '',
  confirmPassword: '',
  verifyCode: ''
})

// 消息弹窗
const showMessageBox = ref(false)
const messageText = ref('')
const messageType = ref('info')

// 简单格式校验（仅用于按钮变色，不做占用后端校验）
const isEmailFormat = computed(() => {
  const v = (formData.emailOrPhone || '').trim()
  if (!v.includes('@')) return false
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)
})

const isPhoneFormat = computed(() => {
  const v = (formData.emailOrPhone || '').trim()
  return /^\d{11}$/.test(v)
})

const isValidContact = computed(() => isEmailFormat.value || isPhoneFormat.value)

const shouldShowBlueBtn = computed(() => {
  return isValidContact.value && countdown.value === 0 && !loading.value
})

// 验证昵称
const validateNickname = () => {
  errors.nickname = ''
  if (!formData.nickname) return
  if (formData.nickname.length < 1 || formData.nickname.length > 20) {
    errors.nickname = '昵称长度应为1-20个字符'
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
    // 手机号验证（11位纯数字）
    const phonePattern = /^\d{11}$/
    if (!phonePattern.test(value)) {
      errors.emailOrPhone = '请输入有效的手机号（11位数字）'
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
  
  if (formData.password.length < 8) {
    errors.password = '密码长度至少为8位'
    return
  }
  
  // 检查是否包含字母
  if (!/[A-Za-z]/.test(formData.password)) {
    errors.password = '密码必须包含字母'
    return
  }
  
  // 检查是否包含数字
  if (!/\d/.test(formData.password)) {
    errors.password = '密码必须包含数字'
    return
  }
  
  // 密码可以包含字母、数字和符号，不再限制字符类型
  
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
    
    if (result && result.code === 200) {
      showMessage('验证码已发送，请查收', 'success')
      
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
      showMessage(result?.message || '发送失败', 'error')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    showMessage('网络错误，请稍后重试', 'error')
  }
}

// 处理注册
const handleRegister = async () => {
  // 清除所有错误
  Object.keys(errors).forEach(key => errors[key] = '')
  
  // 验证所有字段
  validateNickname()
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
      nickname: formData.nickname,
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
        const isEmail = formData.emailOrPhone.includes('@')
        emit('register-success', { 
          account: isEmail ? formData.emailOrPhone : '',
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

// 已移除社交账号注册

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

// 左侧画布动画 - 粒子缓缓漂浮
const startLeftCanvas = () => {
  if (!artCanvas.value) return
  const canvas = artCanvas.value
  const ctx = canvas.getContext('2d')

  const parent = canvas.parentElement
  const dpr = Math.min(window.devicePixelRatio || 1, 2)

  const resize = () => {
    const w = parent.clientWidth
    const h = parent.clientHeight
    canvas.style.width = w + 'px'
    canvas.style.height = h + 'px'
    canvas.width = Math.floor(w * dpr)
    canvas.height = Math.floor(h * dpr)
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  }

  resize()
  window.addEventListener('resize', resize)

  // 云朵（漂浮白云）
  const clouds = Array.from({ length: 5 }).map(() => ({
    x: Math.random(),
    y: Math.random(),
    s: 0.7 + Math.random() * 1.0,   // 缩放
    v: 0.02 + Math.random() * 0.05, // 速度
    a: 0.25 + Math.random() * 0.15  // 透明度
  }))

  let angle = 0
  const draw = () => {
    const w = canvas.clientWidth
    const h = canvas.clientHeight
    ctx.clearRect(0, 0, w, h)

    // 天空（白底→浅蓝天）
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, w, h)
    const sky = ctx.createLinearGradient(0, 0, 0, h * 0.55)
    sky.addColorStop(0, '#e3f2fd')
    sky.addColorStop(1, '#ffffff')
    ctx.fillStyle = sky
    ctx.fillRect(0, 0, w, h * 0.6)

    // 太阳
    const sunX = w * 0.15
    const sunY = h * 0.18
    const sunR = Math.min(w, h) * 0.08
    const sunGrad = ctx.createRadialGradient(sunX, sunY, sunR * 0.2, sunX, sunY, sunR)
    sunGrad.addColorStop(0, '#fff59d')
    sunGrad.addColorStop(1, '#ffeb3b')
    ctx.fillStyle = sunGrad
    ctx.beginPath(); ctx.arc(sunX, sunY, sunR, 0, Math.PI * 2); ctx.fill()
    // 太阳光晕
    const glow = ctx.createRadialGradient(sunX, sunY, sunR, sunX, sunY, sunR * 2.2)
    glow.addColorStop(0, 'rgba(255,235,59,0.35)')
    glow.addColorStop(1, 'rgba(255,235,59,0)')
    ctx.fillStyle = glow
    ctx.beginPath(); ctx.arc(sunX, sunY, sunR * 2.2, 0, Math.PI * 2); ctx.fill()

    // 云绘制（温和）
    for (const c of clouds) {
      const cx = c.x * w
      const cy = c.y * h
      const base = Math.min(w, h) * 0.08 * c.s
      ctx.globalAlpha = c.a
      ctx.fillStyle = '#e3f2fd'
      ctx.beginPath(); ctx.ellipse(cx, cy, base * 0.9, base * 0.5, 0, 0, Math.PI * 2); ctx.fill()
      ctx.beginPath(); ctx.ellipse(cx + base * 0.5, cy + base * 0.05, base * 0.7, base * 0.4, 0, 0, Math.PI * 2); ctx.fill()
      ctx.beginPath(); ctx.ellipse(cx - base * 0.5, cy + base * 0.05, base * 0.7, base * 0.4, 0, 0, Math.PI * 2); ctx.fill()
      ctx.globalAlpha = 1

      // 漂移
      c.x += c.v / w * 60  // 速度与画布宽度微调
      if (c.x - 0.6 > 1) {
        c.x = -0.2
        c.y = Math.random()
      }
    }

    // 远山
    const drawMountain = (baseY, color, peaks) => {
      ctx.fillStyle = color
      ctx.beginPath()
      ctx.moveTo(0, baseY)
      const step = w / (peaks.length - 1)
      peaks.forEach((p, i) => {
        const x = i * step
        const y = baseY - p * (h * 0.25)
        ctx.lineTo(x, y)
      })
      ctx.lineTo(w, baseY)
      ctx.closePath()
      ctx.fill()
    }
    drawMountain(h * 0.65, '#c5e1a5', [0.1, 0.35, 0.15, 0.4, 0.2, 0.3, 0.1])
    drawMountain(h * 0.7, '#aed581', [0.15, 0.25, 0.1, 0.3, 0.22, 0.2, 0.12])

    // 田野（分层渐变）
    const fieldGrad = ctx.createLinearGradient(0, h * 0.6, 0, h)
    fieldGrad.addColorStop(0, '#dcedc8')
    fieldGrad.addColorStop(1, '#c5e1a5')
    ctx.fillStyle = fieldGrad
    ctx.fillRect(0, h * 0.6, w, h * 0.4)

    // 河流（从左向右弯曲）
    ctx.fillStyle = '#90caf9'
    ctx.beginPath()
    ctx.moveTo(0, h * 0.75)
    ctx.bezierCurveTo(w * 0.25, h * 0.7, w * 0.35, h * 0.85, w * 0.55, h * 0.82)
    ctx.bezierCurveTo(w * 0.75, h * 0.79, w * 0.85, h * 0.9, w, h * 0.88)
    ctx.lineTo(w, h)
    ctx.lineTo(0, h)
    ctx.closePath()
    ctx.fill()

    // 房屋（几何形）
    const drawHouse = (x, y, s) => {
      // 屋体
      ctx.fillStyle = '#fff'
      ctx.strokeStyle = '#90a4ae'
      ctx.lineWidth = 2
      ctx.fillRect(x, y, 40 * s, 28 * s)
      ctx.strokeRect(x, y, 40 * s, 28 * s)
      // 屋顶
      ctx.fillStyle = '#ef5350'
      ctx.beginPath()
      ctx.moveTo(x - 4 * s, y)
      ctx.lineTo(x + 20 * s, y - 16 * s)
      ctx.lineTo(x + 44 * s, y)
      ctx.closePath()
      ctx.fill()
      // 窗
      ctx.fillStyle = '#bbdefb'
      ctx.fillRect(x + 8 * s, y + 8 * s, 10 * s, 8 * s)
      ctx.fillRect(x + 22 * s, y + 8 * s, 10 * s, 8 * s)
      // 门
      ctx.fillStyle = '#8d6e63'
      ctx.fillRect(x + 17 * s, y + 12 * s, 8 * s, 16 * s)
    }
    drawHouse(w * 0.62, h * 0.62, 1)
    drawHouse(w * 0.76, h * 0.64, 0.9)

    // 小孩（简笔画风）
    const drawKid = (x, y, t) => {
      const bounce = Math.sin(t * 3 + x * 0.02) * 2
      const bodyY = y + bounce
      // 头
      ctx.fillStyle = '#ffcc80'
      ctx.beginPath(); ctx.arc(x, bodyY - 10, 5, 0, Math.PI * 2); ctx.fill()
      // 身体
      ctx.strokeStyle = '#424242'; ctx.lineWidth = 2
      ctx.beginPath(); ctx.moveTo(x, bodyY - 5); ctx.lineTo(x, bodyY + 10); ctx.stroke()
      // 手
      ctx.beginPath(); ctx.moveTo(x, bodyY + 0); ctx.lineTo(x - 6, bodyY + 6); ctx.moveTo(x, bodyY + 0); ctx.lineTo(x + 6, bodyY + 6); ctx.stroke()
      // 腿（跑动）
      const leg = Math.sin(t * 6 + x) * 4
      ctx.beginPath(); ctx.moveTo(x, bodyY + 10); ctx.lineTo(x - 5, bodyY + 16 + leg); ctx.moveTo(x, bodyY + 10); ctx.lineTo(x + 5, bodyY + 16 - leg); ctx.stroke()
      // 衣服
      ctx.strokeStyle = '#1e88e5'; ctx.lineWidth = 3
      ctx.beginPath(); ctx.moveTo(x, bodyY - 2); ctx.lineTo(x, bodyY + 6); ctx.stroke()
    }
    angle += 0.01
    drawKid(w * 0.22, h * 0.68, angle)
    drawKid(w * 0.28, h * 0.69, angle + 0.5)
    drawKid(w * 0.34, h * 0.685, angle + 1.0)

    requestAnimationFrame(draw)
  }

  draw()
}

onMounted(() => {
  startLeftCanvas()
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
  max-width: 760px;
  min-height: 560px;
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

.left-art-wrap {
  flex: 1;
  position: relative;
  width: 100%;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  background: #ffffff;
}
 
.left-art {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
}

.welcome-content,
.footer-link {
  position: relative;
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
  position: relative;
  z-index: 2;
  margin-top: 16px;
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
  padding: 28px 24px;
  background: white;
  overflow-y: auto;
}

.register-form-wrapper {
  width: 100%;
  max-width: 360px;
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

/* 已移除社交注册样式 */

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

