/**
 * 安全工具函数
 * 用于XSS防护和输入验证
 */

/**
 * 清理HTML，防止XSS攻击
 */
export function sanitizeHtml(html) {
  if (!html) return ''
  
  const div = document.createElement('div')
  div.textContent = html
  return div.innerHTML
}

/**
 * 验证输入是否包含危险字符
 */
export function containsDangerousChars(input) {
  if (!input) return false
  
  const dangerousPatterns = [
    /<script/i,
    /javascript:/i,
    /on\w+\s*=/i, // onclick, onerror等
    /<iframe/i,
    /<object/i,
    /<embed/i,
  ]
  
  return dangerousPatterns.some(pattern => pattern.test(input))
}

/**
 * 清理用户输入
 */
export function sanitizeInput(input) {
  if (typeof input !== 'string') return input
  
  // 移除HTML标签
  let cleaned = input.replace(/<[^>]*>/g, '')
  
  // 移除JavaScript事件处理器
  cleaned = cleaned.replace(/on\w+\s*=\s*["'][^"']*["']/gi, '')
  
  // 移除特殊字符
  cleaned = cleaned.replace(/[<>\"']/g, '')
  
  return cleaned.trim()
}

/**
 * 验证邮箱格式
 */
export function isValidEmail(email) {
  if (!email) return false
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证密码强度（与后端规则保持一致）
 */
export function validatePasswordStrength(password) {
  if (!password) {
    return { valid: false, message: '密码不能为空' }
  }
  
  if (password.length < 8) {
    return { valid: false, message: '密码长度至少为8位' }
  }
  
  if (!/[A-Za-z]/.test(password)) {
    return { valid: false, message: '密码必须包含字母' }
  }
  
  if (!/\d/.test(password)) {
    return { valid: false, message: '密码必须包含数字' }
  }
  
  // 检查是否包含不允许的字符（只允许字母、数字和常见特殊字符）
  const allowedPattern = /^[A-Za-z\d@$!%*#?&._\-+=()\[\]{}]+$/
  if (!allowedPattern.test(password)) {
    return { valid: false, message: '密码包含不允许的字符，只能使用字母、数字和常见特殊字符（@$!%*#?&._-+=()[]{}）' }
  }
  
  return { valid: true, message: '密码强度符合要求' }
}

/**
 * 验证手机号格式（中国大陆）
 */
export function isValidPhone(phone) {
  if (!phone) return false
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 转义HTML特殊字符
 */
export function escapeHtml(text) {
  if (!text) return ''
  
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  }
  
  return text.replace(/[&<>"']/g, m => map[m])
}

