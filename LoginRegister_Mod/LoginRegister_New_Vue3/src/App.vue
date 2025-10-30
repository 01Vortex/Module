<script setup>
import { ref } from 'vue'
import LoginPage from './components/LoginPage.vue'
import RegisterPage from './components/RegisterPage.vue'
import ForgotPasswordPage from './components/ForgotPasswordPage.vue'

const currentPage = ref('login') // 'login', 'register' 或 'forgot-password'
const prefilledData = ref(null)

const switchToRegister = () => {
  currentPage.value = 'register'
}

const switchToLogin = (data) => {
  currentPage.value = 'login'
  if (data) {
    // 注册成功后自动填充用户名和密码
    prefilledData.value = data
  }
}

const switchToForgotPassword = () => {
  currentPage.value = 'forgot-password'
}

const handleRegisterSuccess = (data) => {
  prefilledData.value = data
}
</script>

<template>
  <LoginPage 
    v-if="currentPage === 'login'" 
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
