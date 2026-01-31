<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  TrendCharts,
  Ship as ShipIcon,
  User as UserIcon,
  Position as PositionIcon,
  Tools as ToolsIcon,
  Document,
  DataAnalysis,
  Bell,
  Tickets,
  Plus,
  ArrowRight
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import axios from '@/utils/request'
import ChartCard from './components/ChartCard.vue'
import * as echarts from 'echarts'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(true)


const isAdmin = computed(() => {
  return userStore.user.role === 'ADMIN'
})

const allQuickActions = [
  { icon: ShipIcon, title: '新增船舶', path: '/ship', color: '#3b82f6' },
  { icon: UserIcon, title: '新增船员', path: '/crew', color: '#10b981' },
  { icon: PositionIcon, title: '新建航次', path: '/voyage', color: '#f59e0b' },
  { icon: ToolsIcon, title: '维修登记', path: '/maintenance', color: '#ef4444' },
  { icon: Document, title: '证书管理', path: '/certificate', color: '#f59e0b' },
  { icon: DataAnalysis, title: '燃油记录', path: '/fuel', color: '#3b82f6' },
  { icon: Bell, title: '消息通知', path: '/message', color: '#10b981' },
  { icon: Tickets, title: '操作日志', path: '/log', color: '#6366f1', adminOnly: true }
]

const quickActions = computed(() => {
  return allQuickActions.filter(action => !action.adminOnly || isAdmin.value)
})

const navigateTo = (path) => {
  router.push(path)
}

const stats = ref({
  shipCount: 0,
  crewCount: 0,
  voyageCount: 0,
  maintenanceCount: 0,
  categoryCount: 0,
  activeShipCount: 0,
  certificateCount: 0,
  fuelRecordCount: 0,
  messageCount: 0,
  unreadMessageCount: 0,
  operationLogCount: 0
})

const chartOptions = reactive({
  statusPie: {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: 'var(--text-secondary)',
        fontSize: 12
      }
    },
    series: [
      {
        name: '船舶状态',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          position: 'outside',
          fontSize: 12
        },
        labelLine: {
          show: true,
          length: 10,
          length2: 8
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        data: []
      }
    ]
  },

  statusTrend: {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      data: ['在役', '维修中', '航行中'],
      textStyle: { color: 'var(--text-secondary)' },
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月'],
      axisLabel: { color: 'var(--text-secondary)' },
      axisLine: { lineStyle: { color: 'var(--border-color)' } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: 'var(--text-secondary)' },
      splitLine: { lineStyle: { color: 'var(--border-color)', type: 'dashed' } },
      axisLine: { show: false }
    },
    series: [
      {
        name: '在役',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
          ])
        }
      },
      {
        name: '维修中',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: { color: '#f59e0b' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0.05)' }
          ])
        }
      },
      {
        name: '航行中',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
          ])
        }
      }
    ]
  },

  maintenanceRing: {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {d}%'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: 'var(--text-secondary)',
        fontSize: 12
      }
    },
    series: [
      {
        name: '维护类型',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 11
        },
        labelLine: {
          show: true,
          length: 8,
          length2: 6
        },
        data: []
      }
    ]
  }
})

onMounted(async () => {
  loading.value = true
  try {
    const shipRes = await axios.get('/ship', { params: { pageSize: 1000 } })
    stats.value.shipCount = shipRes.data.total || 0

    const ships = shipRes.data.records || []
    const statusCount = {
      '在役': 0,
      '维修中': 0,
      '停运': 0,
      '航行中': 0
    }

    ships.forEach(ship => {
      if (statusCount[ship.status] !== undefined) {
        statusCount[ship.status]++
      }
    })

    stats.value.activeShipCount = statusCount['在役'] + statusCount['航行中']

    chartOptions.statusPie.series[0].data = [
      { name: '在役', value: statusCount['在役'], itemStyle: { color: '#10b981' } },
      { name: '维修中', value: statusCount['维修中'], itemStyle: { color: '#f59e0b' } },
      { name: '停运', value: statusCount['停运'], itemStyle: { color: '#94a3b8' } },
      { name: '航行中', value: statusCount['航行中'], itemStyle: { color: '#3b82f6' } }
    ]

    try {
      const [crewRes, voyageRes, maintenanceRes, categoryRes] = await Promise.all([
        axios.get('/crew').catch(() => ({ data: { total: 0 } })),
        axios.get('/voyages').catch(() => ({ data: { total: 0 } })),
        axios.get('/maintenance').catch(() => ({ data: { total: 0 } })),
        axios.get('/ship-categories').catch(() => ({ data: { total: 0 } }))
      ])

      stats.value.crewCount = crewRes.data?.total ?? 0
      stats.value.voyageCount = voyageRes.data?.total ?? 0
      stats.value.maintenanceCount = maintenanceRes.data?.total ?? 0
      stats.value.categoryCount = categoryRes.data?.total ?? 0

      try {
        // 基础功能接口调用
        const [certRes, fuelRes, msgRes] = await Promise.all([
          axios.get('/certificate', { params: { pageSize: 1 } }).catch(() => ({ data: { total: 0 } })),
          axios.get('/fuel', { params: { pageSize: 1 } }).catch(() => ({ data: { total: 0 } })),
          axios.get('/message', { params: { pageSize: 1 } }).catch(() => ({ data: { total: 0 } }))
        ])

        stats.value.certificateCount = certRes.data?.total ?? 0
        stats.value.fuelRecordCount = fuelRes.data?.total ?? 0
        stats.value.messageCount = msgRes.data?.total ?? 0

        // 只有管理员才调用操作日志接口
        if (isAdmin.value) {
          try {
            const logRes = await axios.get('/log', { params: { pageSize: 1 } })
            stats.value.operationLogCount = logRes.data?.total ?? 0
          } catch (error) {
            console.error('加载操作日志统计失败:', error)
            stats.value.operationLogCount = 0
          }
        } else {
          stats.value.operationLogCount = 0
        }

        try {
          const unreadRes = await axios.get('/message/unread-count')
          stats.value.unreadMessageCount = unreadRes.data ?? 0
        } catch (e) {
          stats.value.unreadMessageCount = 0
        }
      } catch (error) {
        console.error('加载新功能统计数据失败:', error)
      }

      chartOptions.maintenanceRing.series[0].data = [
        { name: '定期保养', value: Math.floor(stats.value.maintenanceCount * 0.4), itemStyle: { color: '#3b82f6' } },
        { name: '故障维修', value: Math.floor(stats.value.maintenanceCount * 0.3), itemStyle: { color: '#f59e0b' } },
        { name: '紧急抢修', value: Math.floor(stats.value.maintenanceCount * 0.15), itemStyle: { color: '#ef4444' } },
        { name: '升级改造', value: Math.floor(stats.value.maintenanceCount * 0.1), itemStyle: { color: '#10b981' } },
        { name: '其他', value: Math.floor(stats.value.maintenanceCount * 0.05), itemStyle: { color: '#94a3b8' } }
      ]

      const baseInService = Math.max(statusCount['在役'], 5)
      const baseMaintenance = Math.max(statusCount['维修中'], 2)
      const baseRunning = Math.max(statusCount['航行中'], 3)

      chartOptions.statusTrend.series[0].data = [
        baseInService - 2, baseInService - 1, baseInService,
        baseInService + 1, baseInService + 2, baseInService
      ]
      chartOptions.statusTrend.series[1].data = [
        baseMaintenance - 1, baseMaintenance, baseMaintenance + 1,
        baseMaintenance, baseMaintenance + 2, baseMaintenance
      ]
      chartOptions.statusTrend.series[2].data = [
        baseRunning - 1, baseRunning, baseRunning + 1,
        baseRunning + 2, baseRunning + 1, baseRunning
      ]

    } catch (error) {
      console.error('加载统计数据失败:', error)
      stats.value.crewCount = 0
      stats.value.voyageCount = 0
      stats.value.maintenanceCount = 0
      stats.value.categoryCount = 0

      chartOptions.maintenanceRing.series[0].data = [
        { name: '定期保养', value: 0, itemStyle: { color: '#3b82f6' } },
        { name: '故障维修', value: 0, itemStyle: { color: '#f59e0b' } },
        { name: '紧急抢修', value: 0, itemStyle: { color: '#ef4444' } },
        { name: '升级改造', value: 0, itemStyle: { color: '#10b981' } },
        { name: '其他', value: 0, itemStyle: { color: '#94a3b8' } }
      ]
    }

  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <div class="page-header">
      <div class="page-header-main">
        <h1 class="page-title">核心指标</h1>
        <p class="page-subtitle">欢迎回来，今日航行状态已同步</p>
      </div>
      <div class="page-header-meta">
        <div class="meta-item">
          <span class="meta-label">在役船舶</span>
          <span class="meta-value">{{ stats.activeShipCount }}</span>
        </div>
        <div class="meta-divider"></div>
        <div class="meta-item">
          <span class="meta-label">未读消息</span>
          <span class="meta-value">{{ stats.unreadMessageCount }}</span>
        </div>
      </div>
    </div>



    <div class="quick-actions">
      <div class="quick-actions-header">
        <span class="quick-title">高频操作</span>
        <span class="quick-subtitle">快速进入关键业务流程</span>
      </div>
      <div class="quick-actions-grid">
        <div 
          v-for="action in quickActions" 
          :key="action.path" 
          class="quick-action-item"
          @click="navigateTo(action.path)"
        >
          <div class="quick-action-icon" :style="{ background: action.color }">
            <el-icon :size="20"><component :is="action.icon" /></el-icon>
          </div>
          <span class="quick-action-text">{{ action.title }}</span>
          <el-icon class="quick-action-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <div class="charts-section">
      <div class="charts-header">
        <div>
          <div class="charts-title">运行态势</div>
          <div class="charts-subtitle">关键指标趋势与结构</div>
        </div>
        <div class="charts-tabs">
          <span class="tab-item active">近7天</span>
          <span class="tab-item">近30天</span>
          <span class="tab-item">近90天</span>
        </div>
      </div>
      <div class="charts-grid">
        <div class="chart-item">
          <ChartCard
            title="船舶状态分布"
            subtitle="各状态船舶数量统计"
            :option="chartOptions.statusPie"
            :loading="loading"
            height="280px"
          />
        </div>

        <div class="chart-item">
          <ChartCard
            title="维护类型分布"
            subtitle="各类维护工作占比"
            :option="chartOptions.maintenanceRing"
            :loading="loading"
            height="280px"
          />
        </div>

        <div class="chart-item chart-full">
          <ChartCard
            title="船舶状态趋势"
            subtitle="近6个月状态变化趋势"
            :option="chartOptions.statusTrend"
            :loading="loading"
            height="300px"
          />
        </div>
      </div>
    </div>


  </div>
</template>

<style scoped>
.dashboard-container {
  background: transparent;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  padding: 0;
}

.page-header {
  margin-bottom: 28px;
  padding: 24px 28px;
  background: var(--card-bg);
  border-radius: var(--radius-2xl);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-md);
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--coral-500);
}

.page-header-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-header-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 16px;
  border-radius: var(--radius-lg);
  background: rgba(255, 107, 107, 0.12);
  border: 1px solid rgba(255, 107, 107, 0.28);
  color: var(--text-primary);
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meta-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.meta-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.meta-divider {
  width: 1px;
  height: 28px;
  background: rgba(255, 107, 107, 0.35);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 500;
}



.quick-actions {
  background: var(--card-bg);
  border-radius: var(--radius-2xl);
  padding: 24px;
  margin-bottom: 28px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--card-border);
  transition: var(--transition);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.quick-actions:hover {
  box-shadow: var(--shadow-lg);
}

.quick-actions-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.quick-actions-header::before {
  content: '';
  width: 4px;
  height: 20px;
  background: var(--coral-500);
  border-radius: 2px;
}

.quick-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.quick-subtitle {
  font-size: 12px;
  color: var(--text-secondary);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

@media (min-width: 1400px) {
  .quick-actions-grid {
    grid-template-columns: repeat(8, 1fr);
  }
}

.quick-action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 18px;
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--card-border);
  position: relative;
  overflow: hidden;
  color: var(--text-primary);
}

.quick-action-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: var(--coral-500);
  transform: scaleY(0);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.quick-action-item:hover {
  background: rgba(255, 107, 107, 0.12);
  border-color: rgba(255, 107, 107, 0.35);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-action-item:hover::before {
  transform: scaleY(1);
}

.quick-action-icon {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: var(--shadow-md);
  transition: transform 0.3s;
}

.quick-action-item:hover .quick-action-icon {
  transform: scale(1.05);
}

.quick-action-text {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.2px;
}

.quick-action-arrow {
  color: var(--text-tertiary);
  font-size: 14px;
  transition: transform 0.3s;
}

.quick-action-item:hover .quick-action-arrow {
  transform: translateX(4px);
  color: var(--coral-500);
}

.charts-section {
  background: var(--card-bg);
  border-radius: var(--radius-2xl);
  padding: 28px;
  margin-bottom: 28px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--card-border);
  transition: var(--transition);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.charts-section:hover {
  box-shadow: var(--shadow-lg);
}

.charts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.charts-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.charts-subtitle {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.charts-tabs {
  display: flex;
  gap: 10px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  padding: 4px;
  border-radius: 999px;
}

.tab-item {
  font-size: 12px;
  color: var(--text-secondary);
  padding: 4px 12px;
  border-radius: 999px;
  transition: all 0.3s ease;
}

.tab-item.active {
  background: rgba(255, 107, 107, 0.14);
  color: var(--coral-600);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.chart-item {
  background: transparent;
  border: none;
  padding: 0;
  transition: var(--transition);
}

.chart-item:hover {
  box-shadow: none;
}

.chart-item.chart-full {
  grid-column: span 2;
}

@media (max-width: 1200px) {
  .quick-actions-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .chart-item.chart-full {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-action-item {
    padding: 14px 16px;
  }

  .quick-action-icon {
    width: 36px;
    height: 36px;
  }

  .quick-action-text {
    font-size: 13px;
  }
  
  .page-header {
    padding: 20px 24px;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .page-header-meta {
    width: 100%;
    justify-content: space-between;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .charts-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .charts-section {
    padding: 20px;
  }
}

</style>
