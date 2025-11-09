<template>
  <div class="home-page">
    <!-- 顶部导航栏 -->
    <header class="main-header">
      <div class="header-container">
        <!-- 左侧：Logo和导航 -->
        <div class="header-left">
          <div class="logo" @click="goHome">
            <span class="logo-text">Vortex</span>
          </div>
          <nav class="main-nav">
            <a href="#" class="nav-item">首页</a>
            <a href="#" class="nav-item">番剧</a>
            <a href="#" class="nav-item">直播</a>
            <a href="#" class="nav-item">游戏</a>
            <a href="#" class="nav-item">会员购</a>
            <a href="#" class="nav-item">漫画</a>
          </nav>
        </div>

        <!-- 中间：搜索框 -->
        <div class="header-center">
          <div class="search-box">
            <input 
              type="text" 
              placeholder="搜索视频、番剧、用户" 
              class="search-input"
              v-model="searchKeyword"
            />
            <button class="search-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
            </button>
          </div>
        </div>

        <!-- 右侧：用户功能区 -->
        <div class="header-right">
          <template v-if="isLoggedIn">
            <button class="header-btn icon-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
              </svg>
            </button>
            <button class="header-btn icon-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
            </button>
            <div 
              class="user-avatar-wrapper"
              @mouseenter="showUserMenu = true"
              @mouseleave="handleMouseLeave"
            >
              <div class="user-avatar">
                <img :src="userAvatar" :alt="userInfo?.nickname || '用户'" />
              </div>
              <div v-if="showUserMenu" class="user-profile-popup" @mouseenter="handlePopupEnter" @mouseleave="handlePopupLeave">
                <!-- 用户信息区域 -->
                <div class="popup-header">
                  <div class="popup-avatar">
                    <img :src="userAvatar" :alt="userInfo?.nickname || '用户'" />
                  </div>
                  <div class="popup-user-info">
                    <h3 class="popup-username">{{ userInfo?.nickname || userInfo?.account || '用户' }}</h3>
                    <div class="popup-account">账号：{{ userInfo?.account || '' }}</div>
                  </div>
                </div>
                
                <!-- 社交数据区域 -->
                <div class="popup-stats">
                  <div class="stat-item">
                    <span class="stat-value">{{ userStats.following }}</span>
                    <span class="stat-label">关注</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ userStats.fans }}</span>
                    <span class="stat-label">粉丝</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ userStats.dynamics }}</span>
                    <span class="stat-label">动态</span>
                  </div>
                </div>
                
                <!-- 导航菜单 -->
                <div class="popup-menu">
                  <div class="popup-menu-item" @click="goToProfile">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                      <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span>个人中心</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="arrow-icon">
                      <path d="M5 12h14M12 5l7 7-7 7"></path>
                    </svg>
                  </div>
                  <div class="popup-menu-item" @click="goToSubmission">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                      <line x1="8" y1="21" x2="16" y2="21"></line>
                      <line x1="12" y1="17" x2="12" y2="21"></line>
                    </svg>
                    <span>投稿管理</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="arrow-icon">
                      <path d="M5 12h14M12 5l7 7-7 7"></path>
                    </svg>
                  </div>
                  <div class="popup-menu-item" @click="goToRecommended">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                    </svg>
                    <span>推荐服务</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="arrow-icon">
                      <path d="M5 12h14M12 5l7 7-7 7"></path>
                    </svg>
                  </div>
                  <div class="popup-menu-item logout-item" @click="handleLogout">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                      <polyline points="16 17 21 12 16 7"></polyline>
                      <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                    <span>退出登录</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="arrow-icon">
                      <path d="M5 12h14M12 5l7 7-7 7"></path>
                    </svg>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <button class="login-btn" @click="handleLogin">登录</button>
          </template>
        </div>
      </div>
    </header>

    <!-- 副导航栏 -->
    <div class="sub-nav">
      <div class="sub-nav-container">
        <div class="sub-nav-left">
          <button class="sub-nav-btn active">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"></path>
            </svg>
            <span>动态</span>
          </button>
          <button class="sub-nav-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.56 17.66a8 8 0 0 1-11.32 0L1.3 12.7a1 1 0 0 1 0-1.42l4.95-4.95a8 8 0 0 1 11.32 0l4.95 4.95a1 1 0 0 1 0 1.42l-4.95 4.95zm-9.9-1.42a6 6 0 0 0 8.48 0L20.38 12l-4.24-4.24a6 6 0 0 0-8.48 0L3.4 12l4.25 4.24z"></path>
            </svg>
            <span>热门</span>
          </button>
        </div>
        <div class="sub-nav-center">
          <a href="#" class="category-item">番剧</a>
          <a href="#" class="category-item">国创</a>
          <a href="#" class="category-item">综艺</a>
          <a href="#" class="category-item">动画</a>
          <a href="#" class="category-item">电影</a>
          <a href="#" class="category-item">电视剧</a>
          <a href="#" class="category-item">纪录片</a>
          <a href="#" class="category-item">游戏</a>
          <a href="#" class="category-item">鬼畜</a>
          <a href="#" class="category-item">音乐</a>
          <a href="#" class="category-item">舞蹈</a>
          <a href="#" class="category-item">更多</a>
        </div>
        <div class="sub-nav-right">
          <a href="#" class="sub-nav-link">专栏</a>
          <a href="#" class="sub-nav-link">活动</a>
          <a href="#" class="sub-nav-link">直播</a>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-container">
        <!-- 轮播图区域 -->
        <div class="banner-section">
          <div class="banner-carousel">
            <div class="banner-item active">
              <div class="banner-content">
                <h2 class="banner-title">欢迎来到 Vortex</h2>
                <p class="banner-desc">探索精彩内容，发现无限可能</p>
                <button class="banner-btn">立即体验</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 视频推荐区域 -->
        <div class="video-section">
          <div class="section-header">
            <h3 class="section-title">推荐视频</h3>
            <div class="section-tabs">
              <button class="tab-btn active">综合</button>
              <button class="tab-btn">最新</button>
              <button class="tab-btn">最热</button>
            </div>
          </div>

          <div class="video-grid">
            <div 
              v-for="i in 12" 
              :key="i" 
              class="video-card"
              @click="goToVideo(i)"
            >
              <div class="video-thumbnail">
                <div class="thumbnail-placeholder">
                  <svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2"></rect>
                    <polygon points="9 9 15 12 9 15 9 9"></polygon>
                  </svg>
                </div>
                <div class="video-duration">05:23</div>
                <div class="play-overlay">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="white">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polygon points="10 8 16 12 10 16 10 8"></polygon>
                  </svg>
                </div>
              </div>
              <div class="video-info">
                <h4 class="video-title">这是一个视频标题 {{ i }}</h4>
                <div class="video-meta">
                  <span class="video-author">作者名称</span>
                  <span class="video-stats">
                    <span>1.2万</span>
                    <span>·</span>
                    <span>234</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { tokenManager } from '../api/auth.js'

export default {
  name: 'HomePage',
  data() {
    return {
      searchKeyword: '',
      isLoggedIn: false,
      userInfo: null,
      userAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCAxMDAgMTAwIiBmaWxsPSJub25lIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgo8cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iIzY2N2VlYSIvPgo8cGF0aCBkPSJNNTAgMzVDNDAgMzUgNDAgNDAgMzUgNDVDMjUuNSA0NSAyMCA1MC41IDIwIDYwQzIwIDcwIDI1IDc1IDMwIDgwQzMwIDg1IDQwIDkwIDUwIDkwQzYwIDkwIDcwIDg1IDcwIDgwQzc1IDc1IDgwIDcwIDgwIDYwQzgwIDUwLjUgNzQuNSA0NSA2NSA0NUM2MCA0MCA2MCAzNSA1MCAzNVoiIGZpbGw9IndoaXRlIi8+Cjwvc3ZnPg==',
      showUserMenu: false,
      menuTimer: null,
      userStats: {
        following: 0,
        fans: 0,
        dynamics: 0
      }
    }
  },
  mounted() {
    this.checkLoginStatus()
    this.loadUserStats()
  },
  beforeUnmount() {
    // 清除定时器，避免内存泄漏
    if (this.menuTimer) {
      clearTimeout(this.menuTimer)
      this.menuTimer = null
    }
  },
  methods: {
    checkLoginStatus() {
      const user = tokenManager.getUser()
      if (user) {
        this.isLoggedIn = true
        this.userInfo = user
        // 如果有头像URL，使用实际头像
        if (user.avatar && user.avatar.trim()) {
          // 如果是相对路径，添加API基础URL
          if (user.avatar.startsWith('/')) {
            this.userAvatar = 'http://localhost:8080' + user.avatar
          } else if (user.avatar.startsWith('http://') || user.avatar.startsWith('https://') || user.avatar.startsWith('data:')) {
            this.userAvatar = user.avatar
          } else {
            this.userAvatar = 'http://localhost:8080/api' + user.avatar
          }
        }
      }
    },
    loadUserStats() {
      // 加载用户统计数据（关注、粉丝、动态）
      // 这里可以使用API获取，暂时使用模拟数据
      // TODO: 如果后端有相关API，可以调用获取真实数据
      this.userStats = {
        following: 0,
        fans: 0,
        dynamics: 0
      }
    },
    handleMouseLeave() {
      // 延迟隐藏，以便用户可以从头像移动到弹窗
      if (this.menuTimer) {
        clearTimeout(this.menuTimer)
      }
      this.menuTimer = setTimeout(() => {
        this.showUserMenu = false
      }, 300)
    },
    handlePopupEnter() {
      // 鼠标进入弹窗时，清除隐藏定时器
      if (this.menuTimer) {
        clearTimeout(this.menuTimer)
        this.menuTimer = null
      }
    },
    handlePopupLeave() {
      // 鼠标离开弹窗时，隐藏菜单
      this.showUserMenu = false
    },
    handleLogin() {
      window.location.hash = '#login'
    },
    handleLogout() {
      tokenManager.clearTokens()
      this.isLoggedIn = false
      this.userInfo = null
      this.showUserMenu = false
    },
    goHome() {
      window.location.hash = '#home'
    },
    goToVideo(id) {
      // 跳转到视频详情页
      console.log('跳转到视频:', id)
    },
    goToProfile() {
      this.showUserMenu = false
      // 跳转到个人中心
      window.location.hash = '#profile'
    },
    goToSubmission() {
      this.showUserMenu = false
      // 跳转到投稿管理
      console.log('跳转到投稿管理')
      // TODO: 实现投稿管理页面路由
    },
    goToRecommended() {
      this.showUserMenu = false
      // 跳转到推荐服务
      console.log('跳转到推荐服务')
      // TODO: 实现推荐服务页面路由
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f4f4f4;
}

/* 主头部 */
.main-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
  flex: 0 0 auto;
}

.logo {
  cursor: pointer;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: white;
  letter-spacing: 2px;
}

.main-nav {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
  white-space: nowrap;
}

.nav-item:hover {
  color: white;
}

.header-center {
  flex: 1;
  max-width: 500px;
  margin: 0 40px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 8px 12px;
  backdrop-filter: blur(10px);
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  color: white;
  font-size: 14px;
  outline: none;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.search-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 0 0 auto;
}

.header-btn {
  background: none;
  border: none;
  color: white;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 6px;
  transition: background 0.3s;
}

.header-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.icon-btn {
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn {
  background: #ff6699;
  border: none;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 24px;
  border-radius: 6px;
  transition: background 0.3s;
}

.login-btn:hover {
  background: #ff4d8a;
}

.user-avatar-wrapper {
  position: relative;
  display: inline-block;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: border-color 0.3s;
}

.user-avatar-wrapper:hover .user-avatar {
  border-color: white;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-profile-popup {
  position: absolute;
  top: 48px;
  left: 50%;
  transform: translateX(-50%);
  width: 320px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  overflow: hidden;
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.popup-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.popup-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid #f0f0f0;
}

.popup-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.popup-user-info {
  flex: 1;
  min-width: 0;
}

.popup-username {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.popup-account {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.popup-stats {
  padding: 16px 20px;
  display: flex;
  justify-content: space-around;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.popup-menu {
  padding: 8px 0;
}

.popup-menu-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: background 0.2s;
  position: relative;
}

.popup-menu-item:hover {
  background: #f5f5f5;
}

.popup-menu-item svg:first-child {
  flex-shrink: 0;
  color: #666;
}

.popup-menu-item span {
  flex: 1;
}

.popup-menu-item .arrow-icon {
  flex-shrink: 0;
  opacity: 0.3;
}

.popup-menu-item.logout-item {
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
}

.popup-menu-item.logout-item:hover {
  background: #fff5f5;
}

.popup-menu-item.logout-item svg:first-child {
  color: #ff6699;
}

/* 副导航栏 */
.sub-nav {
  background: white;
  border-bottom: 1px solid #e5e5e5;
  position: sticky;
  top: 64px;
  z-index: 999;
}

.sub-nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sub-nav-left {
  display: flex;
  gap: 8px;
}

.sub-nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: none;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
}

.sub-nav-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.sub-nav-btn.active {
  background: #ff6699;
  color: white;
}

.sub-nav-center {
  display: flex;
  gap: 24px;
  flex: 1;
  justify-content: center;
  overflow-x: auto;
  scrollbar-width: none;
}

.sub-nav-center::-webkit-scrollbar {
  display: none;
}

.category-item {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  white-space: nowrap;
  transition: color 0.3s;
  padding: 8px 0;
}

.category-item:hover {
  color: #ff6699;
}

.sub-nav-right {
  display: flex;
  gap: 20px;
}

.sub-nav-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.sub-nav-link:hover {
  color: #ff6699;
}

/* 主内容区 */
.main-content {
  padding: 20px 0;
}

.content-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 轮播图区域 */
.banner-section {
  margin-bottom: 30px;
}

.banner-carousel {
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  height: 300px;
  position: relative;
}

.banner-item {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.banner-content {
  text-align: center;
}

.banner-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 12px;
}

.banner-desc {
  font-size: 18px;
  margin-bottom: 24px;
  opacity: 0.9;
}

.banner-btn {
  background: white;
  color: #667eea;
  border: none;
  padding: 12px 32px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.3s;
}

.banner-btn:hover {
  transform: translateY(-2px);
}

/* 视频区域 */
.video-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.section-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 6px 16px;
  border: 1px solid #e5e5e5;
  background: white;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-btn:hover {
  border-color: #ff6699;
  color: #ff6699;
}

.tab-btn.active {
  background: #ff6699;
  border-color: #ff6699;
  color: white;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.video-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.video-thumbnail {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 aspect ratio */
  background: #f0f0f0;
  overflow: hidden;
}

.thumbnail-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s;
}

.video-card:hover .play-overlay {
  opacity: 1;
}

.video-info {
  padding: 12px;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.video-author {
  color: #666;
}

.video-stats {
  display: flex;
  gap: 4px;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-center {
    max-width: 400px;
  }
  
  .sub-nav-center {
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .main-nav {
    display: none;
  }
  
  .header-center {
    margin: 0 20px;
  }
  
  .sub-nav-center {
    display: none;
  }
  
  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
}
</style>
