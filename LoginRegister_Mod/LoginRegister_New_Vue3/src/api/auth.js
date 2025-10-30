// API基础配置
const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 发送HTTP请求
 */
async function request(url, options = {}) {
  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    })
    
    const data = await response.json()
    return data
  } catch (error) {
    console.error('请求失败:', error)
    throw error
  }
}

/**
 * 用户登录（密码方式）
 */
export async function login(username, password) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

/**
 * 用户登录（验证码方式）
 */
export async function loginWithCode(username, code) {
  return request('/auth/login/code', {
    method: 'POST',
    body: JSON.stringify({ username, code }),
  })
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
  console.log('发送验证码请求 - 接收者:', emailOrPhone)
  const result = await request('/auth/send-code', {
    method: 'POST',
    body: JSON.stringify({ username: emailOrPhone }),
  })
  console.log('发送验证码响应:', result)
  return result
}

/**
 * 检查用户名是否存在
 */
export async function checkUsername(username) {
  return request(`/user/check/username/${username}`)
}

/**
 * 检查邮箱是否存在
 */
export async function checkEmail(email) {
  return request(`/user/check/email/${email}`)
}

