// 用户相关API接口
import { API_BASE_URL, tokenManager, request } from './auth.js'

/**
 * 获取当前用户信息
 */
export async function getCurrentUser() {
  return request('/user/profile', {
    method: 'GET'
  })
}

/**
 * 更新用户个人信息
 */
export async function updateUserProfile(userData) {
  return request('/user/profile', {
    method: 'PUT',
    body: JSON.stringify(userData)
  }).then(response => {
    if (response.code === 200 && response.data) {
      // 更新本地存储的用户信息
      const currentUser = tokenManager.getUser()
      if (currentUser) {
        const updatedUser = { ...currentUser, ...response.data }
        tokenManager.setUser(updatedUser)
      }
    }
    return response
  })
}

/**
 * 上传头像
 * @param {File} file - 图片文件
 * @param {number} x - 裁剪区域的x坐标（可选）
 * @param {number} y - 裁剪区域的y坐标（可选）
 * @param {number} width - 裁剪区域的宽度（可选）
 * @param {number} height - 裁剪区域的高度（可选）
 */
export async function uploadAvatar(file, x, y, width, height) {
  const formData = new FormData()
  formData.append('avatar', file)
  
  // 如果提供了裁剪参数，添加到formData
  if (x !== undefined && x !== null) {
    formData.append('x', x.toString())
  }
  if (y !== undefined && y !== null) {
    formData.append('y', y.toString())
  }
  if (width !== undefined && width !== null) {
    formData.append('width', width.toString())
  }
  if (height !== undefined && height !== null) {
    formData.append('height', height.toString())
  }
  
  // 获取访问token
  const accessToken = tokenManager.getAccessToken()
  
  // 构建请求头（不设置Content-Type，让浏览器自动设置multipart/form-data的boundary）
  const headers = {}
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/user/avatar`, {
      method: 'POST',
      headers: headers,
      body: formData
    })
    
    // 处理响应
    const contentType = response.headers.get('content-type')
    let data
    
    if (contentType && contentType.includes('application/json')) {
      data = await response.json()
    } else {
      const text = await response.text()
      try {
        data = JSON.parse(text)
      } catch (e) {
        data = {
          code: response.status,
          message: text || '上传失败'
        }
      }
    }
    
    // 如果返回401，清除token并跳转
    if (response.status === 401) {
      tokenManager.clearTokens()
      window.location.hash = '#login'
      throw new Error('登录已过期，请重新登录')
    }
    
    // 更新本地存储的用户信息
    if (data.code === 200 && data.data) {
      const currentUser = tokenManager.getUser()
      if (currentUser) {
        const updatedUser = { ...currentUser, avatar: data.data.avatar }
        tokenManager.setUser(updatedUser)
      }
    }
    
    return data
  } catch (error) {
    if (error.message) {
      throw error
    }
    throw new Error('上传头像失败，请稍后重试')
  }
}

