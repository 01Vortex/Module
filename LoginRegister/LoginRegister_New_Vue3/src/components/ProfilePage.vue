<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 顶部头部区域 -->
      <div class="profile-header">
        <div class="header-content">
          <div class="user-info-section">
            <div class="user-avatar">
              <img :src="getAvatarUrl()" :alt="userInfo.nickname || '用户'" />
            </div>
              <div class="user-details">
              <div class="username-section">
                <span class="username">{{ userInfo.nickname || userInfo.account || '用户' }}</span>
                <span class="level-badge">Lv.{{ userLevel }}</span>
              </div>
              <div class="user-id">账号: {{ userInfo.account || '--' }}</div>
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.fans }}</span>
                  <span class="stat-label">粉丝</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.following }}</span>
                  <span class="stat-label">关注</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ userStats.likes }}</span>
                  <span class="stat-label">获赞</span>
                </div>
              </div>
            </div>
          </div>
          <button class="edit-btn" @click="handleEdit">编辑</button>
        </div>
      </div>

      <!-- 主体内容区域 -->
      <div class="profile-body">
        <!-- 左侧导航栏 -->
        <div class="sidebar">
          <h3 class="sidebar-title">个人中心</h3>
          <nav class="sidebar-nav">
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'security' }"
            @click="switchMenu('security')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
          </svg>
            <span>账户安全</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'posts' }"
            @click="switchMenu('posts')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <line x1="8" y1="6" x2="21" y2="6"></line>
              <line x1="8" y1="12" x2="21" y2="12"></line>
              <line x1="8" y1="18" x2="21" y2="18"></line>
              <line x1="3" y1="6" x2="3.01" y2="6"></line>
              <line x1="3" y1="12" x2="3.01" y2="12"></line>
              <line x1="3" y1="18" x2="3.01" y2="18"></line>
            </svg>
            <span>我的发帖</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'comments' }"
            @click="switchMenu('comments')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            <span>我的评论</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'collections' }"
            @click="switchMenu('collections')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
            </svg>
            <span>我的合集</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'favorites' }"
            @click="switchMenu('favorites')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            <span>我的收藏</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'fans' }"
            @click="switchMenu('fans')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <span>我的粉丝</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'following' }"
            @click="switchMenu('following')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
            <span>我的关注</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'level' }"
            @click="switchMenu('level')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
            <span>我的等级</span>
          </div>
          <div 
            class="nav-item"
            :class="{ active: activeMenu === 'coins' }"
            @click="switchMenu('coins')"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="nav-icon">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M12 6v6l4 2"></path>
            </svg>
            <span>我的米游币</span>
          </div>
        </nav>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <h2 class="content-title">{{ currentMenuLabel }}</h2>
        
        <div class="content-wrapper">
          <!-- 账户安全 -->
          <transition name="fade" mode="out-in">
            <div v-if="activeMenu === 'security'" key="security" class="security-section">
          <div class="security-item">
            <div class="security-item-header">
              <div class="security-item-info">
                <h3 class="security-item-title">绑定手机号</h3>
                <p class="security-item-desc">绑定手机号后，可以使用手机号登录和找回密码</p>
              </div>
              <div class="security-item-status">
                <span v-if="userInfo.phone" class="status-badge bound">已绑定</span>
                <span v-else class="status-badge unbound">未绑定</span>
              </div>
            </div>
            <div class="security-item-content">
              <div v-if="userInfo.phone" class="security-item-value">
                <span>{{ maskPhone(userInfo.phone) }}</span>
                <button class="change-btn" @click="openBindPhoneDialog">更换</button>
              </div>
              <div v-else class="security-item-action">
                <button class="bind-btn" @click="openBindPhoneDialog">绑定手机号</button>
              </div>
            </div>
          </div>

          <div class="security-item">
            <div class="security-item-header">
              <div class="security-item-info">
                <h3 class="security-item-title">绑定邮箱</h3>
                <p class="security-item-desc">绑定邮箱后，可以使用邮箱登录和找回密码</p>
              </div>
              <div class="security-item-status">
                <span v-if="userInfo.email" class="status-badge bound">已绑定</span>
                <span v-else class="status-badge unbound">未绑定</span>
              </div>
            </div>
            <div class="security-item-content">
              <div v-if="userInfo.email" class="security-item-value">
                <span>{{ maskEmail(userInfo.email) }}</span>
                <button class="change-btn" @click="openBindEmailDialog">更换</button>
              </div>
              <div v-else class="security-item-action">
                <button class="bind-btn" @click="openBindEmailDialog">绑定邮箱</button>
              </div>
            </div>
          </div>

          <div class="security-item">
            <div class="security-item-header">
              <div class="security-item-info">
                <h3 class="security-item-title">{{ hasPassword ? '重置密码' : '设置密码' }}</h3>
                <p class="security-item-desc">{{ hasPassword ? '定期更改密码可以让账户更安全' : '设置密码后，可以使用账号和密码登录' }}</p>
              </div>
              <div class="security-item-status">
                <span v-if="hasPassword" class="status-badge bound">已设置</span>
                <span v-else class="status-badge unbound danger">未设置</span>
              </div>
            </div>
            <div class="security-item-content">
              <button class="bind-btn" @click="openPasswordDialog">
                {{ hasPassword ? '重置密码' : '设置密码' }}
        </button>
            </div>
          </div>

          <!-- 第三方账号绑定 -->
          <div class="security-item">
            <div class="security-item-header">
              <div class="security-item-info">
                <h3 class="security-item-title">第三方账号</h3>
                <p class="security-item-desc">绑定第三方账号后，可以使用第三方账号快速登录</p>
              </div>
            </div>
            <div class="security-item-content">
              <div class="social-accounts-list">
                <!-- QQ -->
                <div class="social-account-item">
                  <div class="social-account-info">
                    <div class="social-account-icon qq-icon">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                      </svg>
                    </div>
                    <div class="social-account-details">
                      <span class="social-account-name">QQ</span>
                      <span v-if="getSocialAccount('qq')" class="social-account-nickname">{{ getSocialAccount('qq').socialName || '已绑定' }}</span>
                      <span v-else class="social-account-status">未绑定</span>
                    </div>
                  </div>
                  <div class="social-account-action">
                    <button 
                      v-if="getSocialAccount('qq')" 
                      class="unbind-btn" 
                      @click="handleUnbindSocial('qq')"
                      :disabled="unbindingSocial === 'qq'"
                    >
                      {{ unbindingSocial === 'qq' ? '解绑中...' : '解绑' }}
                    </button>
                    <button 
                      v-else 
                      class="bind-btn" 
                      @click="handleBindSocial('qq')"
                    >
                      绑定
                    </button>
                  </div>
                </div>

                <!-- 微信 -->
                <div class="social-account-item">
                  <div class="social-account-info">
                    <div class="social-account-icon wechat-icon">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.598-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 3.405c-1.693-.103-3.686.388-5.22 1.63-1.846 1.51-2.754 3.756-1.898 6.22.078.223.177.435.29.633.458.816 1.113 1.465 1.896 1.872.198.105.41.19.63.253.276.08.558.13.842.15a7.17 7.17 0 0 0 2.006-.278.59.59 0 0 1 .49.033l1.624.949a.295.295 0 0 0 .29 0 .312.312 0 0 0 .135-.213l.38-1.478a.59.59 0 0 1 .24-.656c1.655-1.182 2.733-2.89 2.733-4.784.001-2.278-2.416-4.12-5.337-4.12zm-2.67 2.99c.519 0 .94.427.94.953a.948.948 0 0 1-.94.951.948.948 0 0 1-.94-.951c0-.526.421-.953.94-.953zm4.806 0c.519 0 .94.427.94.953a.948.948 0 0 1-.94.951.948.948 0 0 1-.94-.951c0-.526.421-.953.94-.953z"/>
                      </svg>
                    </div>
                    <div class="social-account-details">
                      <span class="social-account-name">微信</span>
                      <span v-if="getSocialAccount('wechat')" class="social-account-nickname">{{ getSocialAccount('wechat').socialName || '已绑定' }}</span>
                      <span v-else class="social-account-status">未绑定</span>
                    </div>
                  </div>
                  <div class="social-account-action">
                    <button 
                      v-if="getSocialAccount('wechat')" 
                      class="unbind-btn" 
                      @click="handleUnbindSocial('wechat')"
                      :disabled="unbindingSocial === 'wechat'"
                    >
                      {{ unbindingSocial === 'wechat' ? '解绑中...' : '解绑' }}
                    </button>
                    <button 
                      v-else 
                      class="bind-btn" 
                      @click="handleBindSocial('wechat')"
                    >
                      绑定
                    </button>
                  </div>
                </div>

                <!-- Google -->
                <div class="social-account-item">
                  <div class="social-account-info">
                    <div class="social-account-icon google-icon">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                        <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                        <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                        <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                      </svg>
                    </div>
                    <div class="social-account-details">
                      <span class="social-account-name">Google</span>
                      <span v-if="getSocialAccount('google')" class="social-account-nickname">{{ getSocialAccount('google').socialName || '已绑定' }}</span>
                      <span v-else class="social-account-status">未绑定</span>
                    </div>
                  </div>
                  <div class="social-account-action">
                    <button 
                      v-if="getSocialAccount('google')" 
                      class="unbind-btn" 
                      @click="handleUnbindSocial('google')"
                      :disabled="unbindingSocial === 'google'"
                    >
                      {{ unbindingSocial === 'google' ? '解绑中...' : '解绑' }}
                    </button>
                    <button 
                      v-else 
                      class="bind-btn" 
                      @click="handleBindSocial('google')"
                    >
                      绑定
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
            </div>
        
            <!-- 我的发帖 -->
            <div v-else-if="activeMenu === 'posts'" key="posts" class="posts-section">
          <div v-if="posts.length === 0" class="empty-state">
            <p>没有更多数据了</p>
          </div>
          <div v-else>
            <div v-for="post in posts" :key="post.id" class="post-card">
              <div class="post-date">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="date-icon">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                <span>{{ post.date }}</span>
              </div>
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-content">{{ post.content }}</p>
              <div v-if="post.image" class="post-image">
                <img :src="post.image" :alt="post.title" />
            </div>
              <div v-if="post.tag" class="post-tag">{{ post.tag }}</div>
              <div class="post-stats">
                <div class="post-stat-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  <span>{{ post.views }}</span>
                </div>
                <div class="post-stat-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                  </svg>
                  <span>{{ post.comments }}</span>
                </div>
                <div class="post-stat-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 9V5a3 3 0 0 0-6 0v4"></path>
                    <rect x="2" y="9" width="20" height="11" rx="2" ry="2"></rect>
                    <path d="M12 14v3"></path>
                  </svg>
                  <span>{{ post.likes }}</span>
                </div>
              </div>
            </div>
            <div class="empty-state">
              <p>没有更多数据了</p>
            </div>
          </div>
            </div>

            <!-- 其他菜单内容（暂时显示占位内容） -->
            <div v-else :key="activeMenu" class="placeholder-content">
              <p>{{ currentMenuLabel }} 功能开发中...</p>
            </div>
          </transition>
        </div>
      </div>
      </div>
    </div>

    <!-- 绑定手机号对话框 -->
    <div v-if="showBindPhoneDialog" class="dialog-overlay" @click="showBindPhoneDialog = false">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>{{ userInfo.phone ? '更换手机号' : '绑定手机号' }}</h3>
          <button class="close-btn" @click="showBindPhoneDialog = false">×</button>
        </div>
        <div class="dialog-content">
          <div class="form-group">
            <label>手机号</label>
            <input 
              type="tel" 
              v-model="bindPhoneForm.phone" 
              placeholder="请输入手机号"
              maxlength="11"
            />
          </div>
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" @click="showBindPhoneDialog = false">取消</button>
          <button class="confirm-btn" @click="handleBindPhone" :disabled="bindingPhone">
            {{ bindingPhone ? '绑定中...' : '确定' }}
          </button>
        </div>
      </div>
          </div>

    <!-- 绑定邮箱对话框 -->
    <div v-if="showBindEmailDialog" class="dialog-overlay" @click="showBindEmailDialog = false">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>{{ userInfo.email ? '更换邮箱' : '绑定邮箱' }}</h3>
          <button class="close-btn" @click="showBindEmailDialog = false">×</button>
        </div>
        <div class="dialog-content">
          <div class="form-group">
            <label>邮箱</label>
            <input 
              type="email" 
              v-model="bindEmailForm.email" 
              placeholder="请输入邮箱"
            />
          </div>
          <div class="form-group">
            <label>邮箱验证码</label>
            <div class="code-input-group">
              <input 
                type="text" 
                v-model="bindEmailForm.verifyCode" 
                placeholder="请输入验证码"
                maxlength="6"
                class="code-input"
              />
              <button 
                class="send-code-btn" 
                @click="sendEmailCode('bind')"
                :disabled="emailCodeCountdown > 0 || sendingEmailCode || !bindEmailForm.email || !bindEmailForm.email.includes('@')"
              >
                {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}秒` : '发送验证码' }}
              </button>
            </div>
            <span class="form-hint">验证码将发送到您输入的邮箱</span>
          </div>
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" @click="showBindEmailDialog = false">取消</button>
          <button class="confirm-btn" @click="handleBindEmail" :disabled="bindingEmail">
            {{ bindingEmail ? '绑定中...' : '确定' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 设置/重置密码对话框 -->
    <div v-if="showPasswordDialog" class="dialog-overlay" @click="showPasswordDialog = false">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>{{ hasPassword ? '重置密码' : '设置密码' }}</h3>
          <button class="close-btn" @click="showPasswordDialog = false">×</button>
        </div>
            <div class="dialog-content">
              <!-- 设置密码时，直接显示表单 -->
              <template v-if="!hasPassword">
                <!-- 第三方用户第一次设置密码：不需要邮箱验证码 -->
                <template v-if="isSocialUser">
                  <div class="form-group">
                    <label>新密码</label>
                    <input 
                      type="password" 
                      v-model="passwordForm.newPassword" 
                      placeholder="请输入新密码"
                    />
                    <span class="form-hint">密码长度8-20位，包含字母和数字</span>
                  </div>
                  <div class="form-group">
                    <label>确认新密码</label>
                    <input 
                      type="password" 
                      v-model="passwordForm.confirmPassword" 
                      placeholder="请再次输入新密码"
                    />
                  </div>
                </template>
                <!-- 非第三方用户设置密码：需要邮箱验证码 -->
                <template v-else>
                  <div v-if="!userInfo.email" class="form-group">
                    <div class="warning-message">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                        <line x1="12" y1="9" x2="12" y2="13"></line>
                        <line x1="12" y1="17" x2="12.01" y2="17"></line>
                </svg>
                      <span>您尚未绑定邮箱，请先绑定邮箱后再设置密码</span>
              </div>
            </div>
                  <template v-else>
                    <div class="form-group">
                      <label>新密码</label>
            <input 
                        type="password" 
                        v-model="passwordForm.newPassword" 
                        placeholder="请输入新密码"
                      />
                      <span class="form-hint">密码长度8-20位，包含字母和数字</span>
          </div>
                    <div class="form-group">
                      <label>确认新密码</label>
                      <input 
                        type="password" 
                        v-model="passwordForm.confirmPassword" 
                        placeholder="请再次输入新密码"
                      />
                    </div>
                    <div class="form-group">
                      <label>邮箱验证码</label>
                      <div class="code-input-group">
                        <input 
                          type="text" 
                          v-model="passwordForm.verifyCode" 
                          placeholder="请输入验证码"
                          maxlength="6"
                          class="code-input"
                        />
                        <button 
                          class="send-code-btn" 
                          @click="sendEmailCode('password')"
                          :disabled="passwordCodeCountdown > 0 || sendingPasswordCode"
                        >
                          {{ passwordCodeCountdown > 0 ? `${passwordCodeCountdown}秒` : '发送验证码' }}
                        </button>
                      </div>
                      <span class="form-hint">验证码将发送到您的邮箱: {{ maskEmail(userInfo.email) }}</span>
                    </div>
                  </template>
                </template>
              </template>
          
          <!-- 重置密码时，提供两种方式选择 -->
          <template v-else>
            <div class="form-group">
              <label class="reset-method-label">重置方式</label>
              <div class="reset-method-options">
                <label class="radio-option">
                  <input 
                    type="radio" 
                    name="resetMethod" 
                    value="oldPassword" 
                    v-model="passwordForm.resetMethod"
                    @change="onResetMethodChange"
                  />
                  <span>方式一：使用旧密码</span>
                </label>
                <label class="radio-option">
                  <input 
                    type="radio" 
                    name="resetMethod" 
                    value="emailCode" 
                    v-model="passwordForm.resetMethod"
                    @change="onResetMethodChange"
                  />
                  <span>方式二：使用邮箱验证码</span>
                </label>
              </div>
            </div>
            
            <!-- 方式一：使用旧密码 -->
            <template v-if="passwordForm.resetMethod === 'oldPassword'">
              <div class="form-group">
                <label>旧密码</label>
                <input 
                  type="password" 
                  v-model="passwordForm.oldPassword" 
                  placeholder="请输入旧密码"
                />
              </div>
              <div class="form-group">
                <label>新密码</label>
                <input 
                  type="password" 
                  v-model="passwordForm.newPassword" 
                  placeholder="请输入新密码"
                />
                <span class="form-hint">密码长度8-20位，包含字母和数字</span>
              </div>
              <div class="form-group">
                <label>确认新密码</label>
                <input 
                  type="password" 
                  v-model="passwordForm.confirmPassword" 
                  placeholder="请再次输入新密码"
                />
              </div>
            </template>
            
            <!-- 方式二：使用邮箱验证码 -->
            <template v-else-if="passwordForm.resetMethod === 'emailCode'">
              <div v-if="!userInfo.email" class="form-group">
                <div class="warning-message">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                    <line x1="12" y1="9" x2="12" y2="13"></line>
                    <line x1="12" y1="17" x2="12.01" y2="17"></line>
                  </svg>
                  <span>您尚未绑定邮箱，无法使用此方式重置密码</span>
                </div>
              </div>
              <template v-else>
                <div class="form-group">
                  <label>邮箱</label>
                  <input 
                    type="email" 
                    :value="userInfo.email"
                    placeholder="请输入邮箱"
                    readonly
                  />
                  <span class="form-hint">将向此邮箱发送验证码</span>
                </div>
                <div class="form-group">
                  <label>邮箱验证码</label>
                  <div class="code-input-group">
                    <input 
                      type="text" 
                      v-model="passwordForm.verifyCode" 
                      placeholder="请输入验证码"
                      maxlength="6"
                      class="code-input"
                    />
                    <button 
                      class="send-code-btn" 
                      @click="sendEmailCode('password')"
                      :disabled="passwordCodeCountdown > 0 || sendingPasswordCode"
                    >
                      {{ passwordCodeCountdown > 0 ? `${passwordCodeCountdown}秒` : '发送验证码' }}
                    </button>
                  </div>
                  <span class="form-hint">验证码将发送到您的邮箱: {{ maskEmail(userInfo.email) }}</span>
                </div>
                <div class="form-group">
                  <label>新密码</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.newPassword" 
                    placeholder="请输入新密码"
                  />
                  <span class="form-hint">密码长度8-20位，包含字母和数字</span>
                </div>
                <div class="form-group">
                  <label>确认新密码</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.confirmPassword" 
                    placeholder="请再次输入新密码"
                  />
                </div>
              </template>
            </template>
          </template>
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" @click="showPasswordDialog = false">取消</button>
          <button 
            class="confirm-btn" 
            @click="handleSetPassword" 
              :disabled="settingPassword || (!hasPassword && !isSocialUser && !userInfo.email) || (hasPassword && passwordForm.resetMethod === 'emailCode' && !userInfo.email)"
            >
              {{ settingPassword ? (hasPassword ? '重置中...' : '设置中...') : '确定' }}
            </button>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="showEditDialog" class="edit-dialog-overlay" @click="closeEditDialog">
      <div class="edit-dialog" @click.stop>
        <div class="edit-dialog-header">
          <h3>编辑个人信息</h3>
          <button class="close-btn" @click="closeEditDialog">×</button>
        </div>
        <div class="edit-dialog-content">
            <div class="form-group">
              <label>昵称</label>
              <input 
                type="text" 
                v-model="formData.nickname" 
                placeholder="请输入昵称"
                maxlength="50"
              />
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input 
                type="email" 
                v-model="formData.email" 
                placeholder="请输入邮箱"
                :disabled="!userInfo.email"
              />
              <span class="form-hint" v-if="!userInfo.email">未绑定邮箱</span>
            </div>
            <div class="form-group">
              <label>手机号</label>
              <input 
                type="tel" 
                v-model="formData.phone" 
                placeholder="请输入手机号"
                :disabled="!userInfo.phone"
              />
              <span class="form-hint" v-if="!userInfo.phone">未绑定手机号</span>
            </div>
            </div>
        <div class="edit-dialog-actions">
          <button class="cancel-btn" @click="closeEditDialog">取消</button>
          <button class="save-btn" @click="handleSave" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <MessageBox 
      :show="showMessageBox" 
      :message="messageText" 
      :type="messageType"
      @close="showMessageBox = false"
    />
  </div>
</template>

<script>
import { tokenManager, sendVerificationCode, API_BASE_URL } from '../api/auth.js'
import { getCurrentUser, updateUserProfile, setPassword, getSocialAccounts, unbindSocialAccount } from '../api/user.js'
import MessageBox from './MessageBox.vue'

export default {
  name: 'ProfilePage',
  components: {
    MessageBox
  },
  data() {
    return {
      userInfo: {},
      userLevel: 1,
      userStats: {
        fans: 0,
        following: 0,
        likes: 0
      },
      activeMenu: 'security',
      menuItems: [
        { key: 'security', label: '账户安全' },
        { key: 'posts', label: '我的发帖' },
        { key: 'comments', label: '我的评论' },
        { key: 'collections', label: '我的合集' },
        { key: 'favorites', label: '我的收藏' },
        { key: 'fans', label: '我的粉丝' },
        { key: 'following', label: '我的关注' },
        { key: 'level', label: '我的等级' },
        { key: 'coins', label: '我的米游币' }
      ],
      posts: [
        {
          id: 1,
          date: '2021-09-21',
          title: '第一发就抽到了',
          content: '不知是沾了谁欧气_(枫原万叶-偷笑),中秋快乐呀各位_(吃糖葫芦)',
          image: '',
          tag: '祈愿分享',
          views: 46,
          comments: 2,
          likes: 3
        }
      ],
      showEditDialog: false,
      formData: {
        nickname: '',
        email: '',
        phone: ''
      },
      saving: false,
      showMessageBox: false,
      messageText: '',
      messageType: 'info',
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjNjY3ZWVhIi8+CjxwYXRoIGQ9Ik01MCAzNUM0MCAzNSA0MCA0MCAzNSA0NUMyNS41IDQ1IDIwIDUwLjUgMjAgNjBDMjAgNzAgMjUgNzUgMzAgODBDMzAgODUgNDAgOTAgNTAgOTBDNjAgOTAgNzAgODUgNzAgODBDNzUgNzUgODAgNzAgODAgNjBDODAgNTAuNSA3NC41IDQ1IDY1IDQ1QzYwIDQwIDYwIDM1IDUwIDM1WiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+',
      showBindPhoneDialog: false,
      showBindEmailDialog: false,
      showPasswordDialog: false,
      bindingPhone: false,
      bindingEmail: false,
      settingPassword: false,
      bindPhoneForm: {
        phone: ''
      },
      bindEmailForm: {
        email: '',
        verifyCode: ''
      },
      passwordForm: {
        resetMethod: 'oldPassword', // 'oldPassword' 或 'emailCode'
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
        verifyCode: '',
        email: ''
      },
      emailCodeCountdown: 0,
      passwordCodeCountdown: 0,
      sendingEmailCode: false,
      sendingPasswordCode: false,
      socialAccounts: [],
      unbindingSocial: null,
      socialMessageHandler: null
    }
  },
  computed: {
    currentMenuLabel() {
      const menu = this.menuItems.find(item => item.key === this.activeMenu)
      return menu ? menu.label : '个人中心'
    },
    hasPassword() {
      // 根据accountType判断，如果为SOCIAL或password为空，则没有密码
      if (!this.userInfo.accountType) {
        return false
      }
      // 如果accountType为PASSWORD或BOTH，说明有密码
      return this.userInfo.accountType === 'PASSWORD' || this.userInfo.accountType === 'BOTH'
    },
    isSocialUser() {
      if (!this.userInfo) {
        return false
      }
      // 如果accountType为SOCIAL，或者是通过第三方登录注册的用户（有第三方账号绑定但accountType可能为null）
      return this.userInfo.accountType === 'SOCIAL' || 
             (this.userInfo.accountType !== 'PASSWORD' && this.socialAccounts.length > 0)
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadUserStats()
    this.loadSocialAccounts()
    
    // 检查URL参数，如果是从退出登录跳转过来的，自动打开账户安全
    const hash = window.location.hash
    if (hash.includes('security') || sessionStorage.getItem('openSecurity') === 'true') {
      this.activeMenu = 'security'
      sessionStorage.removeItem('openSecurity')
    }
  },
  beforeUnmount() {
    // 清理消息监听器
    if (this.socialMessageHandler) {
      window.removeEventListener('message', this.socialMessageHandler)
      this.socialMessageHandler = null
    }
  },
  methods: {
    getAvatarUrl() {
      if (this.userInfo.avatar && this.userInfo.avatar.trim()) {
        const avatar = this.userInfo.avatar
        if (avatar.startsWith('/')) {
          return 'http://localhost:8080' + avatar
        } else if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) {
          return avatar
        } else {
          return 'http://localhost:8080/api' + avatar
        }
      }
      return this.defaultAvatar
    },
    async loadUserInfo() {
      try {
        const localUser = tokenManager.getUser()
        if (localUser) {
          this.userInfo = localUser
          this.formData = {
            nickname: localUser.nickname || '',
            email: localUser.email || '',
            phone: localUser.phone || ''
          }
        }

        const response = await getCurrentUser()
        if (response.code === 200 && response.data) {
          this.userInfo = response.data
          this.formData = {
            nickname: response.data.nickname || '',
            email: response.data.email || '',
            phone: response.data.phone || ''
          }
          tokenManager.setUser(response.data)
        }
      } catch (error) {
        this.showMessage('加载用户信息失败', 'error')
      }
    },
    loadUserStats() {
      // TODO: 从API获取用户统计数据
      // 暂时使用模拟数据
      this.userStats = {
        fans: 0,
        following: 10,
        likes: 8
      }
      // 根据用户ID计算等级（示例逻辑）
      this.userLevel = 5
    },
    switchMenu(menuKey) {
      this.activeMenu = menuKey
      // TODO: 根据菜单项加载不同的数据
    },
    openBindPhoneDialog() {
      this.bindPhoneForm.phone = this.userInfo.phone || ''
      this.showBindPhoneDialog = true
    },
    openBindEmailDialog() {
      this.bindEmailForm.email = this.userInfo.email || ''
      this.bindEmailForm.verifyCode = ''
      this.showBindEmailDialog = true
    },
    openPasswordDialog() {
      this.passwordForm = {
        resetMethod: 'oldPassword',
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
        verifyCode: '',
        email: this.userInfo.email || ''
      }
      this.showPasswordDialog = true
    },
    onResetMethodChange() {
      // 切换重置方式时，清空表单
      this.passwordForm.oldPassword = ''
      this.passwordForm.verifyCode = ''
      this.passwordForm.newPassword = ''
      this.passwordForm.confirmPassword = ''
    },
    async sendEmailCode(type) {
      let email = ''
      if (type === 'bind') {
        email = this.bindEmailForm.email
        if (!email || !email.includes('@')) {
          this.showMessage('请输入正确的邮箱', 'error')
        return
      }
      } else if (type === 'password') {
        email = this.userInfo.email
        if (!email) {
          this.showMessage('您尚未绑定邮箱', 'error')
        return
        }
      }

      if (type === 'bind') {
        this.sendingEmailCode = true
      } else {
        this.sendingPasswordCode = true
      }

      try {
        const response = await sendVerificationCode(email)
        if (response.code === 200) {
          this.showMessage('验证码已发送，请查收', 'success')
          // 开始倒计时
          if (type === 'bind') {
            this.emailCodeCountdown = 60
            const timer = setInterval(() => {
              this.emailCodeCountdown--
              if (this.emailCodeCountdown <= 0) {
                clearInterval(timer)
              }
            }, 1000)
          } else {
            this.passwordCodeCountdown = 60
            const timer = setInterval(() => {
              this.passwordCodeCountdown--
              if (this.passwordCodeCountdown <= 0) {
                clearInterval(timer)
              }
            }, 1000)
          }
        } else {
          this.showMessage(response.message || '发送验证码失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '发送验证码失败', 'error')
      } finally {
        if (type === 'bind') {
          this.sendingEmailCode = false
        } else {
          this.sendingPasswordCode = false
        }
      }
    },
    maskPhone(phone) {
      if (!phone || phone.length < 11) return phone
      return phone.substring(0, 3) + '****' + phone.substring(7)
    },
    maskEmail(email) {
      if (!email || !email.includes('@')) return email
      const [name, domain] = email.split('@')
      if (name.length <= 2) {
        return name[0] + '***@' + domain
      }
      return name.substring(0, 2) + '***@' + domain
    },
    async handleBindPhone() {
      if (!this.bindPhoneForm.phone || this.bindPhoneForm.phone.trim().length !== 11) {
        this.showMessage('请输入正确的手机号', 'error')
        return
      }

      this.bindingPhone = true
      try {
        const response = await updateUserProfile({ phone: this.bindPhoneForm.phone.trim() })
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, phone: this.bindPhoneForm.phone.trim() }
          tokenManager.setUser(this.userInfo)
          this.showMessage('手机号绑定成功', 'success')
          this.showBindPhoneDialog = false
          this.bindPhoneForm.phone = ''
        } else {
          this.showMessage(response.message || '绑定失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '绑定失败', 'error')
      } finally {
        this.bindingPhone = false
      }
    },
    async handleBindEmail() {
      if (!this.bindEmailForm.email || !this.bindEmailForm.email.includes('@')) {
        this.showMessage('请输入正确的邮箱', 'error')
        return
      }

      if (!this.bindEmailForm.verifyCode || this.bindEmailForm.verifyCode.trim().length !== 6) {
        this.showMessage('请输入6位验证码', 'error')
        return
      }

      this.bindingEmail = true
      try {
        const response = await updateUserProfile({ 
          email: this.bindEmailForm.email.trim(),
          code: this.bindEmailForm.verifyCode.trim()
        })
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, email: this.bindEmailForm.email.trim() }
          tokenManager.setUser(this.userInfo)
          this.showMessage('邮箱绑定成功', 'success')
          this.showBindEmailDialog = false
          this.bindEmailForm.email = ''
          this.bindEmailForm.verifyCode = ''
          await this.loadUserInfo()
        } else {
          this.showMessage(response.message || '绑定失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '绑定失败', 'error')
      } finally {
        this.bindingEmail = false
      }
    },
    async handleSetPassword() {
      // 验证新密码
      if (!this.passwordForm.newPassword) {
        this.showMessage('请输入新密码', 'error')
        return
      }

      if (this.passwordForm.newPassword.length < 8 || this.passwordForm.newPassword.length > 20) {
        this.showMessage('密码长度应为8-20位', 'error')
            return
          }

      if (!/^(?=.*[A-Za-z])(?=.*\d)/.test(this.passwordForm.newPassword)) {
        this.showMessage('密码必须包含字母和数字', 'error')
        return
      }

      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.showMessage('两次输入的密码不一致', 'error')
        return
      }

      // 设置密码（没有旧密码）
      if (!this.hasPassword) {
        // 第三方用户第一次设置密码：不需要验证码
        if (this.isSocialUser) {
          this.settingPassword = true
          try {
            // 设置密码：只需要新密码，不需要验证码
            const response = await setPassword(
              this.passwordForm.newPassword,
              null, // 没有旧密码
              null  // 第三方用户不需要验证码
            )
            if (response.code === 200) {
              this.showMessage('密码设置成功', 'success')
              this.showPasswordDialog = false
              this.passwordForm = {
                resetMethod: 'oldPassword',
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
                verifyCode: '',
                email: ''
              }
              await this.loadUserInfo()
            } else {
              this.showMessage(response.message || '设置失败', 'error')
            }
          } catch (error) {
            this.showMessage(error.message || '设置失败', 'error')
          } finally {
            this.settingPassword = false
          }
          return
        }
        
        // 非第三方用户设置密码：需要邮箱验证码
        if (!this.userInfo.email) {
          this.showMessage('您尚未绑定邮箱，请先绑定邮箱', 'error')
          return
        }

        if (!this.passwordForm.verifyCode || this.passwordForm.verifyCode.trim().length !== 6) {
          this.showMessage('请输入6位邮箱验证码', 'error')
          return
        }

        this.settingPassword = true
        try {
          // 设置密码：只需要新密码和验证码
          const response = await setPassword(
            this.passwordForm.newPassword,
            null, // 没有旧密码
            this.passwordForm.verifyCode.trim()
          )
          if (response.code === 200) {
            this.showMessage('密码设置成功', 'success')
            this.showPasswordDialog = false
            this.passwordForm = {
              resetMethod: 'oldPassword',
              oldPassword: '',
              newPassword: '',
              confirmPassword: '',
              verifyCode: '',
              email: ''
            }
            await this.loadUserInfo()
              } else {
            this.showMessage(response.message || '设置失败', 'error')
          }
        } catch (error) {
          this.showMessage(error.message || '设置失败', 'error')
        } finally {
          this.settingPassword = false
        }
        return
      }

      // 重置密码（有旧密码）
      if (this.passwordForm.resetMethod === 'oldPassword') {
        // 方式一：使用旧密码
        if (!this.passwordForm.oldPassword) {
          this.showMessage('请输入旧密码', 'error')
          return
        }

        this.settingPassword = true
        try {
          // 重置密码：需要旧密码和新密码，不需要验证码
          const response = await setPassword(
            this.passwordForm.newPassword,
            this.passwordForm.oldPassword,
            null // 使用旧密码方式，不需要验证码
          )
          if (response.code === 200) {
            this.showMessage('密码重置成功', 'success')
            this.showPasswordDialog = false
            this.passwordForm = {
              resetMethod: 'oldPassword',
              oldPassword: '',
              newPassword: '',
              confirmPassword: '',
              verifyCode: '',
              email: ''
            }
            await this.loadUserInfo()
            } else {
            this.showMessage(response.message || '重置失败', 'error')
            }
          } catch (error) {
          this.showMessage(error.message || '重置失败', 'error')
          } finally {
          this.settingPassword = false
        }
      } else if (this.passwordForm.resetMethod === 'emailCode') {
        // 方式二：使用邮箱验证码
        if (!this.userInfo.email) {
          this.showMessage('您尚未绑定邮箱，无法使用此方式重置密码', 'error')
          return
        }

        if (!this.passwordForm.verifyCode || this.passwordForm.verifyCode.trim().length !== 6) {
          this.showMessage('请输入6位邮箱验证码', 'error')
          return
        }

        this.settingPassword = true
        try {
          // 重置密码：需要验证码和新密码，不需要旧密码
          const response = await setPassword(
            this.passwordForm.newPassword,
            null, // 使用验证码方式，不需要旧密码
            this.passwordForm.verifyCode.trim()
          )
          if (response.code === 200) {
            this.showMessage('密码重置成功', 'success')
            this.showPasswordDialog = false
            this.passwordForm = {
              resetMethod: 'oldPassword',
              oldPassword: '',
              newPassword: '',
              confirmPassword: '',
              verifyCode: '',
              email: ''
            }
            await this.loadUserInfo()
          } else {
            this.showMessage(response.message || '重置失败', 'error')
          }
      } catch (error) {
          this.showMessage(error.message || '重置失败', 'error')
        } finally {
          this.settingPassword = false
        }
      }
    },
    handleEdit() {
      this.showEditDialog = true
    },
    closeEditDialog() {
      this.showEditDialog = false
    },
    async handleSave() {
      if (this.formData.nickname && this.formData.nickname.trim().length > 50) {
        this.showMessage('昵称长度不能超过50个字符', 'error')
        return
      }

      this.saving = true
      try {
        const response = await updateUserProfile(this.formData)
        if (response.code === 200) {
          this.userInfo = { ...this.userInfo, ...this.formData }
          tokenManager.setUser(this.userInfo)
          this.closeEditDialog()
          // 等待弹窗完成关闭过渡后再显示提示，避免位置抖动
          this.$nextTick(() => {
            setTimeout(() => this.showMessage('保存成功', 'success'), 0)
          })
        } else {
          this.showMessage(response.message || '保存失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '保存失败', 'error')
      } finally {
        this.saving = false
      }
    },
    showMessage(text, type = 'info') {
      this.messageText = text
      this.messageType = type
      this.showMessageBox = true
    },
    async loadSocialAccounts() {
      try {
        const response = await getSocialAccounts()
        if (response.code === 200 && response.data) {
          this.socialAccounts = response.data
        }
      } catch (error) {
        // 静默处理错误，不影响用户体验
      }
    },
    getSocialAccount(provider) {
      return this.socialAccounts.find(account => account.provider === provider)
    },
    handleBindSocial(platform) {
      const w = 520
      const h = 600
      const left = window.screenX + Math.max(0, (window.outerWidth - w) / 2)
      const top = window.screenY + Math.max(0, (window.outerHeight - h) / 3)
      const features = `width=${w},height=${h},left=${left},top=${top},resizable=yes,scrollbars=yes`

      let url = ''
      let messageType = ''
      
      if (platform === 'qq') {
        url = `${API_BASE_URL}/oauth/qq/authorize`
        messageType = 'qq_auth'
      } else if (platform === 'wechat') {
        url = `${API_BASE_URL}/oauth/wechat/authorize`
        messageType = 'wechat_auth'
      } else if (platform === 'google') {
        url = `${API_BASE_URL}/oauth/google/authorize`
        messageType = 'google_auth'
      } else {
        this.showMessage('不支持的第三方平台', 'error')
        return
      }

      const popup = window.open(url, `${platform}_bind`, features)
      if (!popup) {
        this.showMessage(`请允许弹出窗口以完成${platform === 'qq' ? 'QQ' : platform === 'wechat' ? '微信' : 'Google'}绑定`, 'error')
        return
      }

      this.socialMessageHandler = (event) => {
        try {
          const data = event.data || {}
          if (data && data.type === messageType) {
            const payload = data.payload || {}
            if (payload.code === 200) {
              this.showMessage(`${platform === 'qq' ? 'QQ' : platform === 'wechat' ? '微信' : 'Google'}绑定成功！`, 'success')
              // 重新加载第三方账号列表
              this.loadSocialAccounts()
              // 重新加载用户信息
              this.loadUserInfo()
            } else {
              this.showMessage(payload.message || `${platform === 'qq' ? 'QQ' : platform === 'wechat' ? '微信' : 'Google'}绑定失败`, 'error')
            }
            window.removeEventListener('message', this.socialMessageHandler)
            this.socialMessageHandler = null
          }
        } catch (e) {
          // 静默处理错误
        }
      }
      window.addEventListener('message', this.socialMessageHandler)
    },
    async handleUnbindSocial(provider) {
      if (!confirm(`确定要解绑${provider === 'qq' ? 'QQ' : provider === 'wechat' ? '微信' : 'Google'}账号吗？`)) {
        return
      }

      this.unbindingSocial = provider
      try {
        const response = await unbindSocialAccount(provider)
        if (response.code === 200) {
          this.showMessage('解绑成功', 'success')
          // 重新加载第三方账号列表
          await this.loadSocialAccounts()
          // 重新加载用户信息
          await this.loadUserInfo()
        } else {
          this.showMessage(response.message || '解绑失败', 'error')
        }
      } catch (error) {
        this.showMessage(error.message || '解绑失败', 'error')
      } finally {
        this.unbindingSocial = null
      }
    }
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 100px 20px 20px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  box-sizing: border-box;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.profile-container {
  width: 100%;
  max-width: 1200px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  box-sizing: border-box;
}

/* 顶部头部区域 */
.profile-header {
  background: #f5f5f5;
  border-bottom: 1px solid #e5e5e5;
  padding: 24px 0;
}

.header-content {
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e5e5e5;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.username-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.level-badge {
  background: #4a9eff;
  color: white;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}

.user-id {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.user-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.stat-value {
  font-weight: 500;
  color: #666;
  font-size: 12px;
}

.stat-label {
  color: #999;
  font-size: 12px;
}

.edit-btn {
  background: #4a9eff;
  color: white;
  border: none;
  padding: 8px 24px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
  flex-shrink: 0;
}

.edit-btn:hover {
  background: #3a8eef;
}

/* 主体内容区域 */
.profile-body {
  padding: 20px;
  display: flex;
  gap: 20px;
  align-items: flex-start;
  background: #f5f5f5;
  box-sizing: border-box;
  width: 100%;
  overflow: hidden;
}

/* 左侧导航栏 */
.sidebar {
  width: 200px;
  background: white;
  border-radius: 8px;
  padding: 20px 0;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  padding: 0 20px;
  margin-bottom: 16px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #333;
}

.nav-item.active {
  color: #4a9eff;
  background: #f0f7ff;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #4a9eff;
}

.nav-icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 20px;
  min-height: 400px;
  box-sizing: border-box;
  overflow: hidden;
  width: 100%;
}

.content-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

/* 内容包装器，用于稳定高度和过渡 */
.content-wrapper {
  min-height: 500px;
  position: relative;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

/* 过渡动画 - 使用绝对定位避免布局抖动 */
.content-wrapper > * {
  width: 100%;
  box-sizing: border-box;
}

.fade-enter-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
  position: relative;
}

.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  box-sizing: border-box;
  pointer-events: none;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 账户安全 */
.security-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
}

.security-item {
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 20px;
}

.security-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.security-item-info {
  flex: 1;
}

.security-item-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
}

.security-item-desc {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.security-item-status {
  flex-shrink: 0;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.bound {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-badge.unbound {
  background: #f5f5f5;
  color: #666;
}

.status-badge.unbound.danger {
  background: #ffebee;
  color: #c62828;
}

.security-item-content {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.security-item-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.security-item-value span {
  font-size: 14px;
  color: #666;
}

.change-btn {
  background: #f5f5f5;
  color: #666;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.change-btn:hover {
  background: #e8e8e8;
}

.bind-btn {
  background: #4a9eff;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.bind-btn:hover {
  background: #3a8eef;
}

.bind-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 对话框样式 */
.dialog-overlay,
.edit-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.dialog,
.edit-dialog {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog-header,
.edit-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid #e5e5e5;
}

.dialog-header h3,
.edit-dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.dialog-content,
.edit-dialog-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.dialog-actions,
.edit-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #e5e5e5;
}

.cancel-btn,
.confirm-btn,
.save-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #e8e8e8;
}

.confirm-btn,
.save-btn {
  background: #4a9eff;
  color: white;
}

.confirm-btn:hover:not(:disabled),
.save-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.confirm-btn:disabled,
.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 帖子列表 */
.posts-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  box-sizing: border-box;
}

.post-card {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 16px;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.post-card:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.post-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.date-icon {
  width: 14px;
  height: 14px;
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.post-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-image {
  margin: 12px 0;
  border-radius: 4px;
  overflow: hidden;
}

.post-image img {
  width: 100%;
  height: auto;
  display: block;
}

.post-tag {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.post-stats {
  display: flex;
  gap: 20px;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
}

.post-stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.post-stat-item svg {
  width: 14px;
  height: 14px;
  opacity: 0.7;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.placeholder-content {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #666;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.3s;
  line-height: 1;
}

.close-btn:hover {
  background: #f5f5f5;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #4a9eff;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-hint {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.reset-method-label {
  display: block;
  margin-bottom: 12px;
  font-weight: 500;
  color: #333;
}

.reset-method-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 10px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  transition: all 0.2s;
}

.radio-option:hover {
  background: #f5f5f5;
  border-color: #4a9eff;
}

.radio-option input[type="radio"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #4a9eff;
}

.radio-option span {
  font-size: 14px;
  color: #333;
  user-select: none;
}

.radio-option:has(input[type="radio"]:checked) {
  border-color: #4a9eff;
  background: #f0f7ff;
}

.radio-option:has(input[type="radio"]:checked) span {
  color: #4a9eff;
  font-weight: 500;
}

/* 第三方账号列表 */
.social-accounts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.social-account-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.2s;
}

.social-account-item:hover {
  border-color: #d0d0d0;
  background: #f5f5f5;
}

.social-account-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.social-account-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.social-account-icon.qq-icon {
  background: #12b7f5;
  color: white;
}

.social-account-icon.wechat-icon {
  background: #07c160;
  color: white;
}

.social-account-icon.google-icon {
  background: white;
  color: #4285f4;
  border: 1px solid #e5e5e5;
}

.social-account-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.social-account-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.social-account-nickname {
  font-size: 12px;
  color: #666;
}

.social-account-status {
  font-size: 12px;
  color: #999;
}

.social-account-action {
  flex-shrink: 0;
}

.unbind-btn {
  padding: 6px 16px;
  border: 1px solid #ff4d4f;
  background: white;
  color: #ff4d4f;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.unbind-btn:hover:not(:disabled) {
  background: #fff1f0;
  border-color: #ff7875;
}

.unbind-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}


/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page {
    padding: 10px;
  }

  .profile-container {
    border-radius: 4px;
  }

  .profile-body {
    flex-direction: column;
    padding: 15px;
  }

  .sidebar {
    width: 100%;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 15px;
  }

  .edit-btn {
    align-self: flex-end;
  }

  .user-stats {
    flex-wrap: wrap;
    gap: 16px;
  }

  .profile-header {
    padding: 20px 0;
  }
}
</style>