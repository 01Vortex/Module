<template>
  <div class="admin-user-management">
    <div class="toolbar">
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索账号、邮箱、昵称..."
          @input="handleSearch"
        />
        <select v-model="statusFilter" @change="loadUsers">
          <option value="">全部状态</option>
          <option value="1">正常</option>
          <option value="0">禁用</option>
        </select>
      </div>
      <button @click="showAddDialog = true" class="add-btn">+ 添加用户</button>
    </div>
    
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="users.length === 0" class="empty-state">
      <p>暂无用户数据</p>
      <p class="hint">可能的原因：</p>
      <ul class="hint-list">
        <li>1. 数据库中确实没有用户数据（请先注册用户或使用"添加用户"功能）</li>
        <li>2. 所有用户都被逻辑删除了</li>
        <li>3. 查询条件过滤掉了所有用户</li>
      </ul>
      <p class="hint" style="margin-top: 20px;">调试信息：</p>
      <ul class="hint-list">
        <li>• 请打开浏览器控制台（F12）查看详细的请求和响应日志</li>
        <li>• 检查后端日志中的"获取用户列表成功"信息</li>
        <li>• 查看网络请求（Network标签）中的响应数据</li>
      </ul>
    </div>
    <div v-else>
      <table class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>账号</th>
            <th>邮箱</th>
            <th>昵称</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>最后登录</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.account }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td>
              <span :class="['status-badge', user.status === 1 ? 'active' : 'disabled']">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>{{ user.lastLoginTime ? formatDate(user.lastLoginTime) : '-' }}</td>
            <td>
              <button @click="handleEdit(user)" class="action-btn edit">编辑</button>
              <button 
                @click="handleToggleStatus(user)" 
                :class="['action-btn', user.status === 1 ? 'disable' : 'enable']"
              >
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
              <button @click="handleDelete(user)" class="action-btn delete">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div class="pagination">
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
        <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
        <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages">下一页</button>
      </div>
    </div>
    
    <!-- 添加/编辑用户对话框 -->
    <div v-if="showAddDialog || showEditDialog" class="dialog-overlay" @click="closeDialog">
      <div class="dialog" @click.stop>
        <h3>{{ showAddDialog ? '添加用户' : '编辑用户' }}</h3>
        <form @submit.prevent="handleSave">
          <div class="form-group">
            <label>账号 *</label>
            <input 
              v-model="formData.account" 
              type="text" 
              required
              :disabled="showEditDialog"
            />
          </div>
          <div class="form-group">
            <label>{{ showAddDialog ? '密码 *' : '新密码（留空不修改）' }}</label>
            <input 
              v-model="formData.password" 
              type="password" 
              :required="showAddDialog"
            />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="formData.email" type="email" />
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input v-model="formData.nickname" type="text" />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="formData.status">
              <option :value="1">正常</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="dialog-actions">
            <button type="button" @click="closeDialog" class="cancel-btn">取消</button>
            <button type="submit" class="save-btn">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus } from '../api/admin.js'
import { tokenManager } from '../api/auth.js'

export default {
  name: 'AdminUserManagement',
  data() {
    return {
      loading: false,
      error: null,
      users: [],
      currentPage: 1,
      pageSize: 10,
      totalPages: 1,
      searchKeyword: '',
      statusFilter: '',
      showAddDialog: false,
      showEditDialog: false,
      formData: {
        account: '',
        password: '',
        email: '',
        nickname: '',
        status: 1
      },
      editingUser: null
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      this.loading = true
      this.error = null
      
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword
        }
        if (this.statusFilter) {
          params.status = parseInt(this.statusFilter)
        }
        
        const response = await getUserList(params)
        
        if (response && response.code === 200) {
          // 处理MyBatis-Plus分页返回的数据结构
          if (response.data) {
            // MyBatis-Plus的Page对象包含records和total等字段
            if (response.data.records !== undefined) {
              this.users = response.data.records || []
              this.totalPages = response.data.pages || 1
            } else if (Array.isArray(response.data)) {
              // 如果直接返回数组
              this.users = response.data
              this.totalPages = 1
            } else {
              this.users = []
              this.totalPages = 1
            }
          } else {
            this.users = []
            this.totalPages = 1
          }
        } else {
          this.error = response?.message || '获取用户列表失败'
        }
      } catch (error) {
        this.error = error.message || '获取用户列表失败'
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      this.currentPage = 1
      this.loadUsers()
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        this.loadUsers()
      }
    },
    
    handleEdit(user) {
      this.editingUser = user
      this.formData = {
        account: user.account,
        password: '',
        email: user.email || '',
        nickname: user.nickname || '',
        status: user.status
      }
      this.showEditDialog = true
    },
    
    async handleSave() {
      try {
        if (this.showAddDialog) {
          const response = await createUser(this.formData)
          if (response.code === 200) {
            alert('添加用户成功')
            this.closeDialog()
            this.loadUsers()
          } else {
            alert(response.message || '添加用户失败')
          }
        } else {
          const response = await updateUser(this.editingUser.id, this.formData)
          if (response.code === 200) {
            alert('更新用户成功')
            this.closeDialog()
            this.loadUsers()
          } else {
            alert(response.message || '更新用户失败')
          }
        }
      } catch (error) {
        alert(error.message || '操作失败')
      }
    },
    
    async handleToggleStatus(user) {
      if (!confirm(`确定要${user.status === 1 ? '禁用' : '启用'}用户 ${user.account} 吗？`)) {
        return
      }
      
      try {
        const newStatus = user.status === 1 ? 0 : 1
        const response = await updateUserStatus(user.id, newStatus)
        if (response.code === 200) {
          alert(response.message || '操作成功')
          this.loadUsers()
        } else {
          alert(response.message || '操作失败')
        }
      } catch (error) {
        alert(error.message || '操作失败')
      }
    },
    
    async handleDelete(user) {
      if (!confirm(`确定要删除用户 ${user.account} 吗？此操作不可恢复！`)) {
        return
      }
      
      try {
        const response = await deleteUser(user.id)
        if (response.code === 200) {
          alert('删除用户成功')
          this.loadUsers()
        } else {
          alert(response.message || '删除用户失败')
        }
      } catch (error) {
        alert(error.message || '删除用户失败')
      }
    },
    
    closeDialog() {
      this.showAddDialog = false
      this.showEditDialog = false
      this.editingUser = null
      this.formData = {
        account: '',
        password: '',
        email: '',
        nickname: '',
        status: 1
      }
    },
    
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.admin-user-management {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  gap: 10px;
}

.search-box input,
.search-box select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-box input {
  width: 300px;
}

.add-btn {
  padding: 8px 16px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn:hover {
  background-color: #5568d3;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.user-table th {
  background-color: #f5f5f5;
  font-weight: 500;
  color: #333;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-badge.active {
  background-color: #c6f6d5;
  color: #22543d;
}

.status-badge.disabled {
  background-color: #fed7d7;
  color: #742a2a;
}

.action-btn {
  padding: 4px 8px;
  margin-right: 5px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.action-btn.edit {
  background-color: #4299e1;
  color: white;
}

.action-btn.disable {
  background-color: #ed8936;
  color: white;
}

.action-btn.enable {
  background-color: #48bb78;
  color: white;
}

.action-btn.delete {
  background-color: #f56565;
  color: white;
}

.action-btn:hover {
  opacity: 0.8;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 8px;
  padding: 30px;
  width: 90%;
  max-width: 500px;
}

.dialog h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.dialog .form-group {
  margin-bottom: 20px;
}

.dialog label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-size: 14px;
}

.dialog input,
.dialog select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn,
.save-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-state p {
  margin: 10px 0;
  font-size: 16px;
}

.empty-state .hint {
  margin-top: 20px;
  font-size: 14px;
  color: #999;
}

.empty-state .hint-list {
  text-align: left;
  display: inline-block;
  margin-top: 10px;
  padding-left: 20px;
}

.empty-state .hint-list li {
  margin: 5px 0;
  font-size: 13px;
  color: #888;
}

.cancel-btn {
  background-color: #e0e0e0;
  color: #333;
}

.save-btn {
  background-color: #667eea;
  color: white;
}
</style>

