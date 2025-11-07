/**
 * 前端常量定义
 */

// API响应状态码
export const ResponseCode = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  TOO_MANY_REQUESTS: 429,
  LOCKED: 423,
  INTERNAL_SERVER_ERROR: 500
}

// Token类型
export const TokenType = {
  ACCESS: 'access',
  REFRESH: 'refresh'
}

// 用户状态
export const UserStatus = {
  DISABLED: 0,
  ENABLED: 1
}

// 路由路径
export const RoutePath = {
  HOME: 'home',
  LOGIN: 'login',
  REGISTER: 'register',
  ADMIN_LOGIN: 'admin/login',
  ADMIN_DASHBOARD: 'admin/dashboard'
}

// 本地存储Key
export const StorageKey = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
  USER_INFO: 'userInfo',
  ADMIN_INFO: 'adminInfo'
}

// 消息提示类型
export const MessageType = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
}
