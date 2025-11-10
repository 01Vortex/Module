<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 顶部头部区域 -->
      <div class="profile-header">
        <div class="header-content">
          <div class="user-info-section">
            <div class="user-avatar">
              <img :src="getAvatarUrl()" :alt="userInfo.nickname || '用户'" />
            </div>
              <div class="user-details">
              <div class="username-section">
                <span class="username">{{ userInfo.nickname || userInfo.account || '用户' }}</span>
                <span class="level-badge">Lv.{{ userLevel }}</span>
              </div>
              <div class="user-id">账号: {{ userInfo.account || '--' }}</div>
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.fans }}</span>
                  <span class="stat-label">粉丝</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.following }}</span>
                  <span class="stat-label">关注</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.likes }}</span>
                  <span class="stat-label">获赞</span>
                </div>
              </div>
            </div>
          </div>
          <button class="edit-btn" @click="handleEdit">编辑</button>
        </div>
      </div>

      <!-- 主体内容区域 -->
      <div class="profile-body">
        <!-- 左侧导航栏 -->
        <div class="sidebar">
          <h3 class="sidebar-title">个人中心</h3>
          <nav class="sidebar-nav">
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'posts' }"
            @click="switchMenu('posts')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <line x1="8" y1="6" x2="21" y2="6"></line>
              <line x1="8" y1="12" x2="21" y2="12"></line>
              <line x1="8" y1="18" x2="21" y2="18"></line>
              <line x1="3" y1="6" x2="3.01" y2="6"></line>
              <line x1="3" y1="12" x2="3.01" y2="12"></line>
              <line x1="3" y1="18" x2="3.01" y2="18"></line>
            </svg>
            <span>我的发帖</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'comments' }"
            @click="switchMenu('comments')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            <span>我的评论</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'collections' }"
            @click="switchMenu('collections')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
            </svg>
            <span>我的合集</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'favorites' }"
            @click="switchMenu('favorites')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            <span>我的收藏</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'fans' }"
            @click="switchMenu('fans')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <span>我的粉丝</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'following' }"
            @click="switchMenu('following')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
            <span>我的关注</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'level' }"
            @click="switchMenu('level')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
            <span>我的等级</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'coins' }"
            @click="switchMenu('coins')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M12 6v6l4 2"></path>
            </svg>
            <span>我的米游币</span>
          </div>
        </nav>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <h2 class="content-title">{{ currentMenuLabel }}</h2>
        
        <!-- 我的发帖 -->
        <div v-if="activeMenu === 'posts'" class="posts-section">
          <div v-if="posts.length === 0" class="empty-state">
            <p>没有更多数据了</p>
          </div>
          <div v-else>
            <div v-for="post in posts" :key="post.id" class="post-card">
              <div class="post-date">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="date-icon">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                <span>{{ post.date }}</span>
              </div>
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-content">{{ post.content }}</p>
              <div v-if="post.image" class="post-image">
                <img :src="post.image" :alt="post.title" />
              </div>
              <div v-if="post.tag" class="post-tag">{{ post.tag }}</div>
              <div class="post-stats">
                <div class="post-stat-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  <span>{{ post.views }}</span>
                </div>
                <div class="post-stat-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                  </svg>
                  <span>{{ post.comments }}</span>
                </div>
                <div class="post-stat-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 9V5a3 3 0 0 0-6 0v4"></path>
                    <rect x="2" y="9" width="20" height="11" rx="2" ry="2"></rect>
                    <path d="M12 14v3"></path>
                  </svg>
                  <span>{{ post.likes }}</span>
                </div>
              </div>
            </div>
            <div class="empty-state">
              <p>没有更多数据了</p>
            </div>
          </div>
        </div>

        <!-- 其他菜单内容（暂时显示占位内容） -->
        <div v-else class="placeholder-content">
          <p>{{ currentMenuLabel }} 功能开发中...</p>
        </div>
      </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="showEditDialog" class="edit-dialog-overlay" @click="closeEditDialog">
      <div class="edit-dialog" @click.stop>
        <div class="edit-dialog-header">
          <h3>编辑个人信息</h3>
          <button class="close-btn" @click="closeEditDialog">×</button>
        </div>
        <div class="edit-dialog-content">
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
        </div>
        <div class="edit-dialog-actions">
          <button class="cancel-btn" @click="closeEditDialog">取消</button>
          <button class="save-btn" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
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
import { getCurrentUser, updateUserProfile } from '../api/user.js'
import MessageBox from './MessageBox.vue'

export default {
  name: 'ProfilePage',
  components: {
    MessageBox
  },
  data() {
    return {
      userInfo: {},
      userLevel: 1,
      userStats: {
        fans: 0,
        following: 0,
        likes: 0
      },
      activeMenu: 'posts',
      menuItems: [
        { key: 'posts', label: '我的发帖' },
        { key: 'comments', label: '我的评论' },
        { key: 'collections', label: '我的合集' },
        { key: 'favorites', label: '我的收藏' },
        { key: 'fans', label: '我的粉丝' },
        { key: 'following', label: '我的关注' },
        { key: 'level', label: '我的等级' },
        { key: 'coins', label: '我的米游币' }
      ],
      posts: [
        {
          id: 1,
          date: '2021-09-21',
          title: '第一发就抽到了',
          content: '不知是沾了谁欧气_(枫原万叶-偷笑),中秋快乐呀各位_(吃糖葫芦)',
          image: '',
          tag: '祈愿分享',
          views: 46,
          comments: 2,
          likes: 3
        }
      ],
      showEditDialog: false,
      formData: {
        nickname: '',
        email: '',
        phone: ''
      },
      saving: false,
      showMessageBox: false,
      messageText: '',
      messageType: 'info',
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjNjY3ZWVhIi8+CjxwYXRoIGQ9Ik01MCAzNUM0MCAzNSA0MCA0MCAzNSA0NUMyNS41IDQ1IDIwIDUwLjUgMjAgNjBDMjAgNzAgMjUgNzUgMzAgODBDMzAgODUgNDAgOTAgNTAgOTBDNjAgOTAgNzAgODUgNzAgODBDNzUgNzUgODAgNzAgODAgNjBDODAgNTAuNSA3NC41IDQ1IDY1IDQ1QzYwIDQwIDYwIDM1IDUwIDM1WiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+'
    }
  },
  computed: {
    currentMenuLabel() {
      const menu = this.menuItems.find(item => item.key === this.activeMenu)
      return menu ? menu.label : '个人中心'
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadUserStats()
  },
  methods: {
    getAvatarUrl() {
      if (this.userInfo.avatar && this.userInfo.avatar.trim()) {
        const avatar = this.userInfo.avatar
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
        const localUser = tokenManager.getUser()
        if (localUser) {
          this.userInfo = localUser
          this.formData = {
            nickname: localUser.nickname || '',
            email: localUser.email || '',
            phone: localUser.phone || ''
          }
        }

        const response = await getCurrentUser()
        if (response.code === 200 && response.data) {
          this.userInfo = response.data
          this.formData = {
            nickname: response.data.nickname || '',
            email: response.data.email || '',
            phone: response.data.phone || ''
          }
          tokenManager.setUser(response.data)
        }
      } catch (error) {
        this.showMessage('加载用户信息失败', 'error')
      }
    },
    loadUserStats() {
      // TODO: 从API获取用户统计数据
      // 暂时使用模拟数据
      this.userStats = {
        fans: 0,
        following: 10,
        likes: 8
      }
      // 根据用户ID计算等级（示例逻辑）
      this.userLevel = 5
    },
    switchMenu(menuKey) {
      this.activeMenu = menuKey
      // TODO: 根据菜单项加载不同的数据
    },
    handleEdit() {
      this.showEditDialog = true
    },
    closeEditDialog() {
      this.showEditDialog = false
    },
    async handleSave() {
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
          this.closeEditDialog()
        } else {
          this.showMessage(response.message || '保存失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '保存失败', 'error')
      } finally {
        this.saving = false
      }
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
  background: #f5f5f5;
  padding: 100px 20px 20px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.profile-container {
  width: 100%;
  max-width: 1200px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 顶部头部区域 */
.profile-header {
  background: #f5f5f5;
  border-bottom: 1px solid #e5e5e5;
  padding: 24px 0;
}

.header-content {
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e5e5e5;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.username-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.level-badge {
  background: #4a9eff;
  color: white;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}

.user-id {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.user-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.stat-value {
  font-weight: 500;
  color: #666;
  font-size: 12px;
}

.stat-label {
  color: #999;
  font-size: 12px;
}

.edit-btn {
  background: #4a9eff;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
  flex-shrink: 0;
}

.edit-btn:hover {
  background: #3a8eef;
}

/* 主体内容区域 */
.profile-body {
  padding: 20px;
  display: flex;
  gap: 20px;
  align-items: flex-start;
  background: #f5f5f5;
}

/* 左侧导航栏 */
.sidebar {
  width: 200px;
  background: white;
  border-radius: 8px;
  padding: 20px 0;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  padding: 0 20px;
  margin-bottom: 16px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #333;
}

.nav-item.active {
  color: #4a9eff;
  background: #f0f7ff;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #4a9eff;
}

.nav-icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 20px;
  min-height: 400px;
}

.content-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

/* 帖子列表 */
.posts-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 16px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.post-card:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.post-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.date-icon {
  width: 14px;
  height: 14px;
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.post-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-image {
  margin: 12px 0;
  border-radius: 4px;
  overflow: hidden;
}

.post-image img {
  width: 100%;
  height: auto;
  display: block;
}

.post-tag {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.post-stats {
  display: flex;
  gap: 20px;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
}

.post-stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.post-stat-item svg {
  width: 14px;
  height: 14px;
  opacity: 0.7;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.placeholder-content {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

/* 编辑对话框 */
.edit-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.edit-dialog {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.edit-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #e5e5e5;
}

.edit-dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
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
}

.edit-dialog-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 20px;
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
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #4a9eff;
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

.edit-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e5e5e5;
}

.cancel-btn,
.save-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #e8e8e8;
}

.save-btn {
  background: #4a9eff;
  color: white;
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

  .profile-container {
    border-radius: 4px;
  }

  .profile-body {
    flex-direction: column;
    padding: 15px;
  }

  .sidebar {
    width: 100%;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 15px;
  }

  .edit-btn {
    align-self: flex-end;
  }

  .user-stats {
    flex-wrap: wrap;
    gap: 16px;
  }

  .profile-header {
    padding: 20px 0;
  }
}
</style>