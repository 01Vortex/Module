<template>
  <div class="admin-login-container">
    <div class="admin-login-card">
      <div class="admin-login-header">
        <h1>管理员登录</h1>
        <p>Admin Login</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="admin-login-form">
        <div class="form-group">
          <label>账号</label>
          <input 
            v-model="account" 
            type="text" 
            placeholder="请输入管理员账号"
            required
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <input 
            v-model="password" 
            type="password" 
            placeholder="请输入密码"
            required
            :disabled="loading"
          />
        </div>
        
        <div class="forgot-password-link">
          <a href="#" @click.prevent="showForgotPassword = true">忘记密码？</a>
        </div>
        
        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
    
    <!-- 忘记密码对话框 -->
    <div v-if="showForgotPassword" class="dialog-overlay" @click="showForgotPassword = false">
      <div class="dialog" @click.stop>
        <h3>找回密码</h3>
        <p class="dialog-hint">请输入您的邮箱地址，我们将发送验证码到您的邮箱</p>
        
        <div class="form-group">
          <label>邮箱</label>
          <input 
            v-model="forgotEmail" 
            type="email" 
            placeholder="请输入管理员邮箱"
            :disabled="forgotLoading"
          />
        </div>
        
        <div class="dialog-actions">
          <button type="button" @click="showForgotPassword = false" class="cancel-btn">取消</button>
          <button type="button" @click="handleSendResetCode" :disabled="forgotLoading" class="send-btn">
            {{ forgotLoading ? '发送中...' : '发送验证码' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 重置密码对话框 -->
    <div v-if="showResetPassword" class="dialog-overlay" @click="showResetPassword = false">
      <div class="dialog" @click.stop>
        <h3>重置密码</h3>
        
        <div class="form-group">
          <label>验证码</label>
          <div class="code-input-group">
            <input 
              v-model="resetCode" 
              type="text" 
              placeholder="请输入验证码"
              maxlength="6"
              :disabled="resetLoading"
            />
            <button 
              type="button" 
              @click="handleSendResetCode" 
              :disabled="countdown > 0 || resetLoading"
              class="resend-btn"
            >
              {{ countdown > 0 ? `${countdown}秒后重发` : '重新发送' }}
            </button>
          </div>
        </div>
        
        <div class="form-group">
          <label>新密码</label>
          <input 
            v-model="newPassword" 
            type="password" 
            placeholder="请输入新密码（至少8位，包含字母和数字）"
            :disabled="resetLoading"
          />
        </div>
        
        <div class="form-group">
          <label>确认密码</label>
          <input 
            v-model="confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            :disabled="resetLoading"
          />
        </div>
        
        <div class="dialog-actions">
          <button type="button" @click="showResetPassword = false" class="cancel-btn">取消</button>
          <button type="button" @click="handleResetPassword" :disabled="resetLoading" class="reset-btn">
            {{ resetLoading ? '重置中...' : '重置密码' }}
          </button>
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
  </div>
</template>

<script>
import { adminLogin, sendAdminResetPasswordCode, resetAdminPassword } from '../api/admin.js'
import { tokenManager } from '../api/auth.js'
import MessageBox from './MessageBox.vue'

export default {
  name: 'AdminLoginPage',
  components: {
    MessageBox
  },
  data() {
    return {
      account: '',
      password: '',
      loading: false,
      showForgotPassword: false,
      showResetPassword: false,
      forgotEmail: '',
      forgotLoading: false,
      resetCode: '',
      newPassword: '',
      confirmPassword: '',
      resetLoading: false,
      countdown: 0,
      showMessageBox: false,
      messageText: '',
      messageType: 'info'
    }
  },
  methods: {
    async handleLogin() {
      if (!this.account || !this.password) {
        this.showMessage('请输入账号和密码', 'error')
        return
      }
      
      this.loading = true
      
      try {
        const response = await adminLogin(this.account, this.password)
        if (response.code === 200) {
          this.showMessage('登录成功', 'success')
          setTimeout(() => {
            window.location.hash = '#admin/dashboard'
            if (window.$router) {
              window.$router.push('admin/dashboard')
            }
            setTimeout(() => {
              if (window.location.hash !== '#admin/dashboard') {
                window.location.hash = '#admin/dashboard'
                window.location.reload()
              }
            }, 500)
          }, 1000)
        } else {
          this.showMessage(response.message || '登录失败，请检查账号和密码', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '登录失败，请检查账号和密码', 'error')
      } finally {
        this.loading = false
      }
    },
    
    async handleSendResetCode() {
      if (!this.forgotEmail) {
        this.showMessage('请输入邮箱地址', 'error')
        return
      }
      
      // 验证邮箱格式
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(this.forgotEmail)) {
        this.showMessage('邮箱格式不正确', 'error')
        return
      }
      
      this.forgotLoading = true
      
      try {
        const response = await sendAdminResetPasswordCode(this.forgotEmail)
        if (response.code === 200) {
          this.showMessage('验证码已发送到您的邮箱，请查收', 'success')
          this.showForgotPassword = false
          this.showResetPassword = true
          // 开始倒计时
          this.countdown = 60
          const timer = setInterval(() => {
            this.countdown--
            if (this.countdown <= 0) {
              clearInterval(timer)
            }
          }, 1000)
        } else {
          this.showMessage(response.message || '发送验证码失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '发送验证码失败，请稍后重试', 'error')
      } finally {
        this.forgotLoading = false
      }
    },
    
    async handleResetPassword() {
      if (!this.resetCode) {
        this.showMessage('请输入验证码', 'error')
        return
      }
      
      if (!this.newPassword || this.newPassword.length < 8) {
        this.showMessage('密码长度至少8位', 'error')
        return
      }
      
      // 验证密码强度
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&._\-+=()\[\]{}]{8,}$/
      if (!passwordRegex.test(this.newPassword)) {
        this.showMessage('密码必须包含字母和数字，长度至少8位，可包含常见特殊字符', 'error')
        return
      }
      
      if (this.newPassword !== this.confirmPassword) {
        this.showMessage('两次输入的密码不一致', 'error')
        return
      }
      
      this.resetLoading = true
      
      try {
        const response = await resetAdminPassword(this.forgotEmail, this.resetCode, this.newPassword)
        if (response.code === 200) {
          this.showMessage('密码重置成功，请重新登录', 'success')
          setTimeout(() => {
            this.showResetPassword = false
            this.forgotEmail = ''
            this.resetCode = ''
            this.newPassword = ''
            this.confirmPassword = ''
          }, 2000)
        } else {
          this.showMessage(response.message || '重置密码失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '重置密码失败，请稍后重试', 'error')
      } finally {
        this.resetLoading = false
      }
    },
    
    showMessage(text, type = 'info') {
      this.messageText = text
      this.messageType = type
      this.showMessageBox = true
    }
  },
  mounted() {
    // 如果已经登录，直接跳转
    const user = tokenManager.getUser()
    if (user) {
      window.location.hash = '#admin/dashboard'
      window.location.reload()
    }
  }
}
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.admin-login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.admin-login-header {
  text-align: center;
  margin-bottom: 30px;
}

.admin-login-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
}

.admin-login-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.admin-login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  padding: 12px;
  background-color: #fee;
  color: #c33;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}

.login-button {
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.login-button:hover:not(:disabled) {
  opacity: 0.9;
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.forgot-password-link {
  text-align: right;
  margin-top: -10px;
  margin-bottom: 10px;
}

.forgot-password-link a {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: opacity 0.3s;
}

.forgot-password-link a:hover {
  opacity: 0.8;
  text-decoration: underline;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 12px;
  padding: 30px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.dialog h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
}

.dialog-hint {
  color: #666;
  font-size: 14px;
  margin: 0 0 20px 0;
}

.dialog .form-group {
  margin-bottom: 20px;
}

.dialog .form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.dialog .form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.dialog .form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.dialog .form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group input {
  flex: 1;
}

.resend-btn {
  padding: 12px 16px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.resend-btn:hover:not(:disabled) {
  background-color: #e8e8e8;
}

.resend-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn,
.send-btn,
.reset-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.cancel-btn {
  background-color: #e0e0e0;
  color: #333;
}

.send-btn,
.reset-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.cancel-btn:hover,
.send-btn:hover:not(:disabled),
.reset-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-btn:disabled,
.reset-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

