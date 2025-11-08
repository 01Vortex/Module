<script setup>
import { ref, computed, onMounted } from 'vue'
import HomePage from './components/HomePage.vue'
import LoginPage from './components/LoginPage.vue'
import RegisterPage from './components/RegisterPage.vue'
import ForgotPasswordPage from './components/ForgotPasswordPage.vue'
import ProfilePage from './components/ProfilePage.vue'
import AdminLoginPage from './components/AdminLoginPage.vue'
import AdminDashboard from './components/AdminDashboard.vue'
import { tokenManager } from './api/auth.js'

const currentPage = ref('home') // 'home', 'login', 'register', 'forgot-password', 'profile', 'admin/login', 'admin/dashboard'
const prefilledData = ref(null)

// 简单的路由系统
const route = computed(() => {
  const hash = window.location.hash.slice(1) || 'home'
  return hash
})

const updateRoute = (path) => {
  window.location.hash = path
  currentPage.value = path
}

onMounted(() => {
  // 监听hash变化
  window.addEventListener('hashchange', () => {
    const newRoute = route.value
    currentPage.value = newRoute
    
    // 如果访问管理员页面，检查是否已登录
    if (newRoute === 'admin/dashboard') {
      const user = tokenManager.getUser()
      if (!user) {
        updateRoute('admin/login')
      }
    }
    
    // 如果访问个人中心，检查是否已登录
    if (newRoute === 'profile') {
      const user = tokenManager.getUser()
      if (!user) {
        updateRoute('login')
      }
    }
  })
  
  // 初始化路由
  const initialRoute = route.value
  currentPage.value = initialRoute
  
  // 如果访问管理员页面，检查是否已登录
  if (initialRoute === 'admin/dashboard') {
    const user = tokenManager.getUser()
    if (!user) {
      updateRoute('admin/login')
    }
  }
  
  // 如果访问个人中心，检查是否已登录
  if (initialRoute === 'profile') {
    const user = tokenManager.getUser()
    if (!user) {
      updateRoute('login')
    }
  }
  
  // 监听路由变化，确保 currentPage 与 hash 同步
  const syncRoute = () => {
    const hash = window.location.hash.slice(1) || 'home'
    if (currentPage.value !== hash) {
      currentPage.value = hash
    }
  }
  
  // 定期检查路由同步（用于处理直接修改 hash 的情况）
  setInterval(syncRoute, 100)
})

const switchToRegister = () => {
  updateRoute('register')
}

const switchToLogin = (data) => {
  updateRoute('login')
  if (data) {
    // 注册成功后自动填充用户名和密码
    prefilledData.value = data
  }
}

const switchToForgotPassword = () => {
  updateRoute('forgot-password')
}

const handleRegisterSuccess = (data) => {
  prefilledData.value = data
}

// 导出路由方法供子组件使用
window.$router = {
  push: (path) => {
    updateRoute(path)
  }
}
</script>

<template>
  <!-- 主页 -->
  <HomePage v-if="currentPage === 'home'" />
  
  <!-- 普通用户页面 -->
  <LoginPage 
    v-else-if="currentPage === 'login'" 
    :prefilled-data="prefilledData"
    @switch-to-register="switchToRegister"
    @switch-to-forgot-password="switchToForgotPassword"
  />
  <RegisterPage 
    v-else-if="currentPage === 'register'"
    @switch-to-login="switchToLogin"
    @register-success="handleRegisterSuccess"
  />
  <ForgotPasswordPage 
    v-else-if="currentPage === 'forgot-password'"
    @switch-to-login="switchToLogin"
  />
  <ProfilePage 
    v-else-if="currentPage === 'profile'"
  />
  
  <!-- 管理员页面 -->
  <AdminLoginPage v-else-if="currentPage === 'admin/login'" />
  <AdminDashboard v-else-if="currentPage === 'admin/dashboard'" />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
