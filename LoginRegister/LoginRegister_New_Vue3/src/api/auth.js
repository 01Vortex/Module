// API基础配置
export const API_BASE_URL = 'http://localhost:8080/api'

// Token存储键名
const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_KEY = 'user'

/**
 * Token管理
 */
export const tokenManager = {
  // 保存token
  setTokens(accessToken, refreshToken) {
    // 使用sessionStorage存储token（更安全，关闭标签页后自动清除）
    // 如果需要在多个标签页共享，可以使用localStorage
    // 注意：生产环境建议使用httpOnly Cookie存储token，更安全
    try {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
    if (refreshToken) {
      sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
      }
    } catch (e) {
      // 如果sessionStorage不可用（如隐私模式），使用内存存储
      console.warn('sessionStorage不可用，使用内存存储token')
      tokenManager._memoryTokens = { accessToken, refreshToken }
    }
  },
  
  // 内存存储（备用方案）
  _memoryTokens: null,
  
  // 获取访问token
  getAccessToken() {
    try {
      return sessionStorage.getItem(ACCESS_TOKEN_KEY) || (tokenManager._memoryTokens?.accessToken)
    } catch (e) {
      return tokenManager._memoryTokens?.accessToken
    }
  },
  
  // 获取刷新token
  getRefreshToken() {
    try {
      return sessionStorage.getItem(REFRESH_TOKEN_KEY) || (tokenManager._memoryTokens?.refreshToken)
    } catch (e) {
      return tokenManager._memoryTokens?.refreshToken
    }
  },
  
  // 清除token
  clearTokens() {
    try {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY)
    sessionStorage.removeItem(REFRESH_TOKEN_KEY)
    sessionStorage.removeItem(USER_KEY)
    } catch (e) {
      // 忽略错误
    }
    tokenManager._memoryTokens = null
  },
  
  // 保存用户信息
  setUser(user) {
    sessionStorage.setItem(USER_KEY, JSON.stringify(user))
  },
  
  // 获取用户信息
  getUser() {
    const userStr = sessionStorage.getItem(USER_KEY)
    return userStr ? JSON.parse(userStr) : null
  }
}

/**
 * 刷新访问令牌
 */
async function refreshAccessToken() {
  const refreshToken = tokenManager.getRefreshToken()
  if (!refreshToken) {
    throw new Error('没有刷新令牌')
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    })
    
    const data = await response.json()
    if (data.code === 200 && data.accessToken) {
      tokenManager.setTokens(data.accessToken, refreshToken)
      return data.accessToken
    } else {
      tokenManager.clearTokens()
      throw new Error('刷新令牌失败')
    }
  } catch (error) {
    tokenManager.clearTokens()
    throw error
  }
}

/**
 * 发送HTTP请求（带自动token刷新）
 */
export async function request(url, options = {}) {
  // 获取访问token
  let accessToken = tokenManager.getAccessToken()
  
  // 构建请求头
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  }
  
  // 添加Authorization头（公开端点除外）
  const publicEndpoints = ['/auth/login', '/auth/admin/login', '/auth/admin/forgot-password', '/auth/register', '/auth/send-code', '/auth/login/code', '/auth/forgot-password', '/auth/refresh']
  const isPublicEndpoint = publicEndpoints.some(endpoint => url.includes(endpoint))
  
  if (!isPublicEndpoint) {
    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`
    }
  }
  
  try {
    let response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers,
    })
    
    // 如果返回401，尝试刷新token
    if (response.status === 401 && !isPublicEndpoint && tokenManager.getRefreshToken()) {
      try {
        accessToken = await refreshAccessToken()
        // 重试请求
        headers['Authorization'] = `Bearer ${accessToken}`
        response = await fetch(`${API_BASE_URL}${url}`, {
          ...options,
          headers,
        })
      } catch (refreshError) {
        // 刷新失败，清除token并跳转到登录页
        tokenManager.clearTokens()
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        throw new Error('登录已过期，请重新登录')
      }
    }
    
    // 检查响应内容类型并读取响应文本
    const contentType = response.headers.get('content-type')
    let responseText = ''
    
    try {
      // 先读取响应文本（只能读取一次）
      responseText = await response.text()
    } catch (readError) {
      responseText = ''
    }
    
    let data
    
    // 如果响应是JSON格式，尝试解析
    if (contentType && contentType.includes('application/json')) {
      try {
        if (responseText && responseText.trim()) {
          data = JSON.parse(responseText)
        } else {
          // 响应体为空，创建默认响应
          data = {
            code: response.status,
            message: response.status === 403 ? '访问被拒绝' : 
                     response.status === 401 ? '未授权，请重新登录' : '请求失败'
          }
        }
      } catch (parseError) {
        data = {
          code: response.status,
          message: response.status === 403 ? '访问被拒绝' : 
                   response.status === 401 ? '未授权，请重新登录' : '请求失败'
        }
      }
    } else {
      // 非JSON响应，创建默认响应
      data = {
        code: response.status,
        message: response.status === 403 ? '访问被拒绝' : 
                 response.status === 401 ? '未授权，请重新登录' : 
                 response.status === 404 ? '资源不存在' : '请求失败'
      }
    }
    
    // 如果返回401，说明token过期或无效，清除token并跳转
    if (response.status === 401) {
      tokenManager.clearTokens()
      // 如果是管理员页面，跳转到管理员登录页
      if (url.includes('/admin/')) {
        window.location.hash = '#admin/login'
      } else {
        window.location.hash = '#login'
      }
    }
    // 403错误不自动跳转，让组件自己处理（可能是权限问题，但不一定是未登录）
    
    return data
  } catch (error) {
    // 如果是网络错误或JSON解析错误，返回友好的错误信息
    if (error.name === 'TypeError' && error.message.includes('json')) {
      throw new Error('服务器响应格式错误，请稍后重试')
    }
    throw error
  }
}

/**
 * 用户登录（密码方式）
 */
export async function login(account, password) {
  const result = await request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ account, password }),
  })
  
  // 登录成功，保存token和用户信息
  if (result.code === 200) {
    if (result.accessToken && result.refreshToken) {
      tokenManager.setTokens(result.accessToken, result.refreshToken)
    }
    if (result.data) {
      tokenManager.setUser(result.data)
    }
  }
  
  return result
}

/**
 * 用户登录（验证码方式）
 */
export async function loginWithCode(account, code) {
  const result = await request('/auth/login/code', {
    method: 'POST',
    body: JSON.stringify({ account, code }),
  })
  
  // 登录成功，保存token和用户信息
  if (result.code === 200) {
    if (result.accessToken && result.refreshToken) {
      tokenManager.setTokens(result.accessToken, result.refreshToken)
    }
    if (result.data) {
      tokenManager.setUser(result.data)
    }
  }
  
  return result
}

/**
 * 用户登出
 */
export function logout() {
  tokenManager.clearTokens()
}

/**
 * 用户注册
 */
export async function register(userData) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(userData),
  })
}

/**
 * 发送验证码
 */
export async function sendVerificationCode(emailOrPhone) {
  return request('/auth/send-code', {
    method: 'POST',
    body: JSON.stringify({ account: emailOrPhone }),
  })
}

/**
 * 检查账号是否存在
 */
export async function checkAccount(account) {
  return request(`/user/check/account/${account}`)
}

/**
 * 检查邮箱是否存在
 */
export async function checkEmail(email) {
  return request(`/user/check/email/${email}`)
}

/**
 * 发送重置密码验证码
 */
export async function sendResetPasswordCode(emailOrPhone) {
  return request('/auth/forgot-password/send-code', {
    method: 'POST',
    body: JSON.stringify({ emailOrPhone }),
  })
}

/**
 * 重置密码
 */
export async function resetPassword(emailOrPhone, code, newPassword) {
  return request('/auth/forgot-password/reset', {
    method: 'POST',
    body: JSON.stringify({ emailOrPhone, code, newPassword }),
  })
}

