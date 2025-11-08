<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 顶部导航栏 -->
      <header class="profile-header">
        <button class="back-btn" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回
        </button>
        <h1 class="page-title">个人中心</h1>
        <div style="width: 80px;"></div>
      </header>

      <!-- 主要内容 -->
      <div class="profile-content">
        <!-- 用户信息卡片 -->
        <div class="profile-card">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerAvatarUpload">
              <img 
                :src="getAvatarUrl()" 
                :alt="userInfo.nickname || '用户'" 
                class="avatar-img"
              />
              <div class="avatar-overlay">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                  <circle cx="12" cy="13" r="4"/>
                </svg>
                <span>更换头像</span>
              </div>
            </div>
            <input 
              type="file" 
              ref="avatarInput" 
              @change="handleAvatarChange" 
              accept="image/*"
              style="display: none;"
            />
            <h2 class="username">{{ userInfo.nickname || userInfo.account || '用户' }}</h2>
            <p class="user-account">账号：{{ userInfo.account }}</p>
          </div>

          <!-- 用户信息表单 -->
          <div class="info-section">
            <h3 class="section-title">个人信息</h3>
            
            <div class="form-group">
              <label>昵称</label>
              <input 
                type="text" 
                v-model="formData.nickname" 
                placeholder="请输入昵称"
                maxlength="50"
              />
            </div>

            <div class="form-group">
              <label>邮箱</label>
              <input 
                type="email" 
                v-model="formData.email" 
                placeholder="请输入邮箱"
                :disabled="!userInfo.email"
              />
              <span class="form-hint" v-if="!userInfo.email">未绑定邮箱</span>
            </div>

            <div class="form-group">
              <label>手机号</label>
              <input 
                type="tel" 
                v-model="formData.phone" 
                placeholder="请输入手机号"
                :disabled="!userInfo.phone"
              />
              <span class="form-hint" v-if="!userInfo.phone">未绑定手机号</span>
            </div>

            <div class="form-actions">
              <button 
                class="save-btn" 
                @click="handleSave" 
                :disabled="saving"
              >
                {{ saving ? '保存中...' : '保存修改' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片裁剪对话框 -->
    <div v-if="showCropDialog" class="crop-dialog-overlay" @click="closeCropDialog">
      <div class="crop-dialog" @click.stop>
        <div class="crop-dialog-header">
          <h3>裁剪头像</h3>
          <button class="close-btn" @click="closeCropDialog">×</button>
        </div>
        <div class="crop-dialog-content">
          <div class="crop-container">
            <img ref="cropImage" :src="cropImageSrc" alt="裁剪图片" />
          </div>
        </div>
        <div class="crop-dialog-actions">
          <button class="cancel-btn" @click="closeCropDialog">取消</button>
          <button class="confirm-btn" @click="confirmCrop" :disabled="cropping">
            {{ cropping ? '上传中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <MessageBox 
      :show="showMessageBox" 
      :message="messageText" 
      :type="messageType"
      @close="showMessageBox = false"
    />
  </div>
</template>

<script>
import { tokenManager } from '../api/auth.js'
import { getCurrentUser, updateUserProfile, uploadAvatar } from '../api/user.js'
import MessageBox from './MessageBox.vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.min.css'

export default {
  name: 'ProfilePage',
  components: {
    MessageBox
  },
  data() {
    return {
      userInfo: {},
      formData: {
        nickname: '',
        email: '',
        phone: ''
      },
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjNjY3ZWVhIi8+CjxwYXRoIGQ9Ik01MCAzNUM0MCAzNSA0MCA0MCAzNSA0NUMyNS41IDQ1IDIwIDUwLjUgMjAgNjBDMjAgNzAgMjUgNzUgMzAgODBDMzAgODUgNDAgOTAgNTAgOTBDNjAgOTAgNzAgODUgNzAgODBDNzUgNzUgODAgNzAgODAgNjBDODAgNTAuNSA3NC41IDQ1IDY1IDQ1QzYwIDQwIDYwIDM1IDUwIDM1WiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+',
      saving: false,
      uploading: false,
      showMessageBox: false,
      messageText: '',
      messageType: 'info',
      showCropDialog: false,
      cropImageSrc: '',
      cropper: null,
      cropping: false,
      selectedFile: null
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  beforeUnmount() {
    // 销毁cropper实例
    if (this.cropper) {
      this.cropper.destroy()
      this.cropper = null
    }
  },
  methods: {
    getAvatarUrl() {
      if (this.userInfo.avatar && this.userInfo.avatar.trim()) {
        const avatar = this.userInfo.avatar
        // 如果是相对路径，添加API基础URL
        if (avatar.startsWith('/')) {
          return 'http://localhost:8080' + avatar
        } else if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) {
          return avatar
        } else {
          return 'http://localhost:8080/api' + avatar
        }
      }
      return this.defaultAvatar
    },
    async loadUserInfo() {
      try {
        // 先从本地存储获取
        const localUser = tokenManager.getUser()
        if (localUser) {
          this.userInfo = localUser
          this.formData = {
            nickname: localUser.nickname || '',
            email: localUser.email || '',
            phone: localUser.phone || ''
          }
        }

        // 从服务器获取最新信息
        const response = await getCurrentUser()
        if (response.code === 200 && response.data) {
          this.userInfo = response.data
          this.formData = {
            nickname: response.data.nickname || '',
            email: response.data.email || '',
            phone: response.data.phone || ''
          }
          // 更新本地存储
          tokenManager.setUser(response.data)
        }
      } catch (error) {
        this.showMessage('加载用户信息失败', 'error')
      }
    },
    triggerAvatarUpload() {
      this.$refs.avatarInput.click()
    },
    handleAvatarChange(event) {
      const file = event.target.files[0]
      if (!file) return

      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        this.showMessage('请选择图片文件', 'error')
        return
      }

      // 验证文件大小（5MB）
      if (file.size > 5 * 1024 * 1024) {
        this.showMessage('图片大小不能超过5MB', 'error')
        return
      }

      // 保存文件引用
      this.selectedFile = file

      // 读取文件并显示裁剪对话框
      const reader = new FileReader()
      reader.onload = (e) => {
        this.cropImageSrc = e.target.result
        this.showCropDialog = true
        // 等待DOM更新后初始化cropper
        this.$nextTick(() => {
          this.initCropper()
        })
      }
      reader.readAsDataURL(file)

      // 清空input，允许重复选择同一文件
      event.target.value = ''
    },
    initCropper() {
      if (this.cropper) {
        this.cropper.destroy()
      }
      
      const image = this.$refs.cropImage
      if (image) {
        this.cropper = new Cropper(image, {
          aspectRatio: 1, // 1:1比例（圆形头像）
          viewMode: 1, // 限制裁剪框不能超出图片
          dragMode: 'move', // 拖动模式
          autoCropArea: 0.8, // 初始裁剪区域
          restore: false,
          guides: true,
          center: true,
          highlight: false,
          cropBoxMovable: true,
          cropBoxResizable: true,
          toggleDragModeOnDblclick: false,
          responsive: true,
          ready: () => {
            // Cropper初始化完成
          }
        })
      }
    },
    closeCropDialog() {
      this.showCropDialog = false
      if (this.cropper) {
        this.cropper.destroy()
        this.cropper = null
      }
      this.cropImageSrc = ''
      this.selectedFile = null
    },
    async confirmCrop() {
      if (!this.cropper || !this.selectedFile) {
        return
      }

      this.cropping = true
      try {
        // 获取裁剪后的canvas
        const canvas = this.cropper.getCroppedCanvas({
          width: 400,
          height: 400,
          imageSmoothingEnabled: true,
          imageSmoothingQuality: 'high'
        })

        // 将canvas转换为blob
        canvas.toBlob(async (blob) => {
          if (!blob) {
            this.showMessage('图片处理失败', 'error')
            this.cropping = false
            return
          }

          // 创建File对象（前端已经裁剪好了，直接上传）
          const file = new File([blob], this.selectedFile.name, {
            type: 'image/jpeg',
            lastModified: Date.now()
          })

          try {
            // 上传头像（前端已裁剪，不需要传递裁剪参数）
            const response = await uploadAvatar(file)

            if (response.code === 200 && response.data) {
              // 更新头像URL
              const newAvatarUrl = response.data.avatar
              if (newAvatarUrl) {
                this.userInfo = { ...this.userInfo, avatar: newAvatarUrl }
                tokenManager.setUser(this.userInfo)
                this.showMessage('头像上传成功', 'success')
                this.closeCropDialog()
              } else {
                this.showMessage('头像上传失败：未返回头像URL', 'error')
              }
            } else {
              this.showMessage(response.message || '头像上传失败', 'error')
            }
          } catch (error) {
            this.showMessage(error.message || '头像上传失败', 'error')
          } finally {
            this.cropping = false
          }
        }, 'image/jpeg', 0.9) // 质量90%
      } catch (error) {
        this.showMessage('图片处理失败', 'error')
        this.cropping = false
      }
    },
    async handleSave() {
      // 验证昵称
      if (this.formData.nickname && this.formData.nickname.trim().length > 50) {
        this.showMessage('昵称长度不能超过50个字符', 'error')
        return
      }

      this.saving = true
      try {
        const response = await updateUserProfile(this.formData)
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, ...this.formData }
          tokenManager.setUser(this.userInfo)
          this.showMessage('保存成功', 'success')
        } else {
          this.showMessage(response.message || '保存失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '保存失败', 'error')
      } finally {
        this.saving = false
      }
    },
    goBack() {
      // 使用hash路由返回首页
      window.location.hash = '#home'
    },
    showMessage(text, type = 'info') {
      this.messageText = text
      this.messageType = type
      this.showMessageBox = true
    }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin: 0;
}

.profile-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.profile-card {
  padding: 40px;
}

.avatar-section {
  text-align: center;
  padding-bottom: 40px;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 40px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  margin-bottom: 20px;
  border: 4px solid #667eea;
  transition: transform 0.3s;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 14px;
  gap: 8px;
}

.username {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
}

.user-account {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.info-section {
  max-width: 500px;
  margin: 0 auto;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-hint {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-actions {
  margin-top: 32px;
  text-align: center;
}

.save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.save-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page {
    padding: 10px;
  }

  .profile-card {
    padding: 20px;
  }

  .avatar-wrapper {
    width: 120px;
    height: 120px;
  }

  .username {
    font-size: 20px;
  }
}

/* 裁剪对话框样式 */
.crop-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(4px);
}

.crop-dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.crop-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #e5e5e5;
}

.crop-dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: #666;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.3s;
  line-height: 1;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.crop-dialog-content {
  padding: 20px;
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  max-height: 60vh;
}

.crop-container {
  width: 100%;
  max-width: 500px;
  position: relative;
}

.crop-container img {
  max-width: 100%;
  display: block;
}

.crop-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e5e5e5;
}

.crop-dialog-actions .cancel-btn,
.crop-dialog-actions .confirm-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.crop-dialog-actions .cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.crop-dialog-actions .cancel-btn:hover {
  background: #e8e8e8;
}

.crop-dialog-actions .confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.crop-dialog-actions .confirm-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.crop-dialog-actions .confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

