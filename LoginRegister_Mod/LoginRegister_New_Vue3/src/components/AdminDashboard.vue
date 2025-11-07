<template>
  <div class="admin-dashboard">
    <div class="admin-header">
      <h1>管理员控制台</h1>
      <div class="header-actions">
        <span class="admin-name">{{ adminInfo?.account || '管理员' }}</span>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
    </div>
    
    <div class="admin-nav">
      <button 
        :class="['nav-btn', { active: currentView === 'dashboard' }]"
        @click="currentView = 'dashboard'"
      >
        数据统计
      </button>
      <button 
        :class="['nav-btn', { active: currentView === 'users' }]"
        @click="currentView = 'users'"
      >
        用户管理
      </button>
    </div>
    
    <div class="admin-content">
      <AdminStatistics v-if="currentView === 'dashboard'" />
      <AdminUserManagement v-if="currentView === 'users'" />
    </div>
  </div>
</template>

<script>
import { tokenManager } from '../api/auth.js'
import AdminStatistics from './AdminStatistics.vue'
import AdminUserManagement from './AdminUserManagement.vue'

export default {
  name: 'AdminDashboard',
  components: {
    AdminStatistics,
    AdminUserManagement
  },
  data() {
    return {
      currentView: 'dashboard',
      adminInfo: null
    }
  },
  mounted() {
    this.adminInfo = tokenManager.getUser()
    if (!this.adminInfo) {
      console.warn('未找到管理员信息，跳转到登录页')
      window.location.hash = '#admin/login'
      // 触发hashchange事件
      window.dispatchEvent(new HashChangeEvent('hashchange'))
    } else {
      console.log('管理员信息:', this.adminInfo)
    }
  },
  methods: {
    handleLogout() {
      tokenManager.clearTokens()
      window.location.hash = '#admin/login'
      window.location.reload()
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.admin-header {
  background: white;
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.admin-header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-name {
  color: #666;
  font-size: 14px;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #d32f2f;
}

.admin-nav {
  background: white;
  padding: 0 30px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.nav-btn {
  padding: 15px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.nav-btn:hover {
  color: #667eea;
}

.nav-btn.active {
  color: #667eea;
  border-bottom-color: #667eea;
}

.admin-content {
  padding: 30px;
}
</style>

