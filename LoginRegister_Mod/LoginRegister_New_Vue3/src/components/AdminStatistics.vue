<template>
  <div class="admin-statistics">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #667eea;">
            <span>👥</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon" style="background: #48bb78;">
            <span>✓</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.activeUsers }}</div>
            <div class="stat-label">活跃用户（30天）</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon" style="background: #ed8936;">
            <span>➕</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.todayNewUsers }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon" style="background: #f56565;">
            <span>🚫</span>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.disabledUsers }}</div>
            <div class="stat-label">禁用用户</div>
          </div>
        </div>
      </div>
      
      <!-- 图表区域 -->
      <div class="charts-section">
        <div class="chart-card">
          <h3>最近7天新增用户趋势</h3>
          <div class="chart-container">
            <canvas ref="lineChartCanvas"></canvas>
          </div>
        </div>
        
        <div class="chart-card">
          <h3>用户状态分布</h3>
          <div class="chart-container">
            <canvas ref="pieChartCanvas"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getStatistics } from '../api/admin.js'

export default {
  name: 'AdminStatistics',
  data() {
    return {
      loading: true,
      error: null,
      statistics: {
        totalUsers: 0,
        activeUsers: 0,
        todayNewUsers: 0,
        disabledUsers: 0,
        dailyNewUsers: {},
        statusDistribution: {}
      }
    }
  },
  mounted() {
    this.loadStatistics()
  },
  methods: {
    async loadStatistics() {
      this.loading = true
      this.error = null
      
      try {
        const response = await getStatistics()
        
        if (response && response.code === 200) {
          const data = response.data || {}
          this.statistics = {
            totalUsers: data.totalUsers || 0,
            activeUsers: data.activeUsers || 0,
            todayNewUsers: data.todayNewUsers || 0,
            disabledUsers: data.disabledUsers || 0,
            dailyNewUsers: data.dailyNewUsers || {},
            statusDistribution: data.statusDistribution || {}
          }
          
          // 确保在 DOM 更新后渲染图表
          this.$nextTick(() => {
            setTimeout(() => {
              this.renderCharts()
            }, 100)
          })
        } else {
          this.error = response?.message || '获取统计信息失败'
        }
      } catch (error) {
        this.error = error.message || '获取统计信息失败'
      } finally {
        this.loading = false
      }
    },
    
    renderCharts() {
      this.renderLineChart()
      this.renderPieChart()
    },
    
    renderLineChart() {
      const canvas = this.$refs.lineChartCanvas
      if (!canvas) {
        return
      }
      
      const ctx = canvas.getContext('2d')
      const dailyData = this.statistics.dailyNewUsers || {}
      const dates = Object.keys(dailyData).sort()
      const values = dates.map(date => {
        const val = dailyData[date]
        return typeof val === 'number' ? val : parseInt(val) || 0
      })
      
      // 设置画布大小
      const container = canvas.parentElement
      if (container) {
        canvas.width = container.offsetWidth || 400
      } else {
        canvas.width = 400
      }
      canvas.height = 300
      
      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      
      if (dates.length === 0) {
        ctx.fillStyle = '#999'
        ctx.font = '16px Arial'
        ctx.textAlign = 'center'
        ctx.fillText('暂无数据', canvas.width / 2, canvas.height / 2)
        return
      }
      
      const padding = 40
      const chartWidth = canvas.width - padding * 2
      const chartHeight = canvas.height - padding * 2
      const maxValue = Math.max(...values, 1)
      
      // 绘制坐标轴
      ctx.strokeStyle = '#ddd'
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(padding, padding)
      ctx.lineTo(padding, canvas.height - padding)
      ctx.lineTo(canvas.width - padding, canvas.height - padding)
      ctx.stroke()
      
      // 绘制折线
      ctx.strokeStyle = '#667eea'
      ctx.lineWidth = 2
      ctx.beginPath()
      
      dates.forEach((date, index) => {
        const x = padding + (index / (dates.length - 1 || 1)) * chartWidth
        const y = canvas.height - padding - (values[index] / maxValue) * chartHeight
        
        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
        
        // 绘制数据点
        ctx.fillStyle = '#667eea'
        ctx.beginPath()
        ctx.arc(x, y, 4, 0, Math.PI * 2)
        ctx.fill()
        
        // 绘制日期标签
        ctx.fillStyle = '#666'
        ctx.font = '12px Arial'
        ctx.textAlign = 'center'
        ctx.fillText(date.substring(5), x, canvas.height - padding + 20)
      })
      
      ctx.stroke()
    },
    
    renderPieChart() {
      const canvas = this.$refs.pieChartCanvas
      if (!canvas) {
        return
      }
      
      const ctx = canvas.getContext('2d')
      const distribution = this.statistics.statusDistribution || {}
      const labels = Object.keys(distribution)
      const values = Object.values(distribution).map(val => {
        return typeof val === 'number' ? val : parseInt(val) || 0
      })
      const total = values.reduce((sum, val) => sum + val, 0)
      
      // 设置画布大小
      const container = canvas.parentElement
      if (container) {
        canvas.width = container.offsetWidth || 400
      } else {
        canvas.width = 400
      }
      canvas.height = 300
      
      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      
      if (total === 0) {
        ctx.fillStyle = '#999'
        ctx.font = '16px Arial'
        ctx.textAlign = 'center'
        ctx.fillText('暂无数据', canvas.width / 2, canvas.height / 2)
        return
      }
      
      const centerX = canvas.width / 2
      const centerY = canvas.height / 2
      const radius = Math.min(canvas.width, canvas.height) / 2 - 40
      const colors = ['#48bb78', '#f56565']
      
      let currentAngle = -Math.PI / 2
      
      values.forEach((value, index) => {
        const sliceAngle = (value / total) * Math.PI * 2
        
        // 绘制扇形
        ctx.beginPath()
        ctx.moveTo(centerX, centerY)
        ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle)
        ctx.closePath()
        ctx.fillStyle = colors[index] || '#999'
        ctx.fill()
        
        // 绘制标签
        const labelAngle = currentAngle + sliceAngle / 2
        const labelX = centerX + Math.cos(labelAngle) * (radius * 0.7)
        const labelY = centerY + Math.sin(labelAngle) * (radius * 0.7)
        
        ctx.fillStyle = '#fff'
        ctx.font = 'bold 14px Arial'
        ctx.textAlign = 'center'
        ctx.textBaseline = 'middle'
        ctx.fillText(value, labelX, labelY)
        
        // 绘制图例
        const legendX = centerX + radius + 20
        const legendY = centerY - radius + index * 30
        
        ctx.fillStyle = colors[index] || '#999'
        ctx.fillRect(legendX, legendY - 10, 20, 20)
        
        ctx.fillStyle = '#333'
        ctx.font = '14px Arial'
        ctx.textAlign = 'left'
        ctx.fillText(`${labels[index]}: ${value}`, legendX + 30, legendY + 5)
        
        currentAngle += sliceAngle
      })
    }
  }
}
</script>

<style scoped>
.admin-statistics {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.loading, .error {
  text-align: center;
  padding: 40px;
  color: #666;
}

.error {
  color: #f56565;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  background: rgba(255, 255, 255, 0.2);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
}

.chart-card h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.chart-container canvas {
  width: 100%;
  height: 100%;
}
</style>

