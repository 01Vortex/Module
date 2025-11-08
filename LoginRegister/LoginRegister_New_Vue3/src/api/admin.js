// 管理员API接口
import { API_BASE_URL, tokenManager, request } from './auth.js'

/**
 * 管理员登录
 */
export function adminLogin(account, password) {
  return request('/auth/admin/login', {
    method: 'POST',
    body: JSON.stringify({ account, password })
  }).then(response => {
    if (response.code === 200) {
      // 保存token和用户信息
      tokenManager.setTokens(response.accessToken, response.refreshToken)
      tokenManager.setUser(response.data)
      return response
    }
    throw new Error(response.message || '登录失败')
  })
}

/**
 * 获取用户统计信息
 */
export function getStatistics() {
  return request('/admin/statistics', {
    method: 'GET'
  })
}

/**
 * 获取用户列表
 */
export function getUserList(params) {
  // 构建查询字符串
  const queryString = new URLSearchParams(params).toString()
  const url = queryString ? `/admin/users?${queryString}` : '/admin/users'
  
  return request(url, {
    method: 'GET'
  })
}

/**
 * 获取用户详情
 */
export function getUserById(id) {
  return request(`/admin/users/${id}`, {
    method: 'GET'
  })
}

/**
 * 创建用户
 */
export function createUser(userData) {
  return request('/admin/users', {
    method: 'POST',
    body: JSON.stringify(userData)
  })
}

/**
 * 更新用户
 */
export function updateUser(id, userData) {
  return request(`/admin/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(userData)
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request(`/admin/users/${id}`, {
    method: 'DELETE'
  })
}

/**
 * 更新用户状态（启用/禁用）
 */
export function updateUserStatus(id, status) {
  return request(`/admin/users/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })
}

/**
 * 发送管理员重置密码验证码（仅支持邮箱）
 */
export function sendAdminResetPasswordCode(email) {
  return request('/auth/admin/forgot-password/send-code', {
    method: 'POST',
    body: JSON.stringify({ email })
  })
}

/**
 * 管理员重置密码（仅支持邮箱）
 */
export function resetAdminPassword(email, code, newPassword) {
  return request('/auth/admin/forgot-password/reset', {
    method: 'POST',
    body: JSON.stringify({ email, code, newPassword })
  })
}

