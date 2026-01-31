<template>
  <div class="chart-card">
    <!-- 图表标题和副标题 -->
    <div class="chart-header">
      <h3 class="chart-title">{{ title }}</h3>
      <span class="chart-subtitle">{{ subtitle }}</span>
    </div>
    <!-- 图表渲染容器 -->
    <div class="chart-content">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
/**
 * @file 图表卡片组件
 * @description 封装 ECharts 图表渲染组件，支持响应式、主题切换和加载状态
 * @author Claude Code
 * @date 2025
 */

import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

/**
 * 组件 Props 定义
 */
const props = defineProps({
  title: {
    type: String,
    required: true,
    description: '图表标题'
  },
  subtitle: {
    type: String,
    default: '',
    description: '图表副标题'
  },
  option: {
    type: Object,
    required: true,
    description: 'ECharts 配置对象'
  },
  height: {
    type: String,
    default: '300px',
    description: '图表高度'
  },
  loading: {
    type: Boolean,
    default: false,
    description: '是否显示加载状态'
  }
})

/**
 * 图表容器引用
 * @type {import('vue').Ref<HTMLElement | null>}
 */
const chartRef = ref(null)

/**
 * ECharts 图表实例
 * @type {echarts.ECharts | null}
 */
let chartInstance = null

/**
 * 初始化图表
 * 创建 ECharts 实例并绑定窗口 resize 事件
 */
const initChart = () => {
  if (!chartRef.value) return

  // 销毁旧实例（防止重复初始化）
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建新实例
  chartInstance = echarts.init(chartRef.value)

  // 设置窗口尺寸自适应
  const resizeHandler = () => {
    chartInstance && chartInstance.resize()
  }
  window.addEventListener('resize', resizeHandler)

  // 保存 resize handler 用于组件卸载时清理
  chartRef.value._resizeHandler = resizeHandler
}

/**
 * 更新图表数据
 * 合并基础配置和传入的配置，并应用到图表实例
 */
const updateChart = () => {
  if (!chartInstance || !props.option) return

  // 合并基础配置
  const baseOption = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      textStyle: { color: '#333' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    }
  }

  const finalOption = { ...baseOption, ...props.option }
  chartInstance.setOption(finalOption, true)
}

/**
 * 监听图表配置变化
 * 深度监听 option 对象的变化，变化时更新图表
 */
watch(() => props.option, () => {
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

/**
 * 监听加载状态变化
 * 加载时显示 loading 动画
 */
watch(() => props.loading, (newVal) => {
  if (chartInstance) {
    chartInstance.showLoading({
      text: '加载中...',
      color: '#409EFF',
      maskColor: 'rgba(255, 255, 255, 0.8)'
    })
    if (!newVal) {
      chartInstance.hideLoading()
    }
  }
})

/**
 * 监听主题变化
 * 主题切换时重新初始化图表以应用新主题样式
 */
watch(() => document.documentElement.getAttribute('data-theme'), () => {
  nextTick(() => {
    if (chartInstance) {
      chartInstance.dispose()
      initChart()
      updateChart()
    }
  })
})

/**
 * 组件挂载后初始化图表
 */
onMounted(() => {
  nextTick(() => {
    initChart()
    updateChart()
  })
})

/**
 * 组件卸载前清理资源
 * 销毁图表实例并移除事件监听
 */
onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  if (chartRef.value && chartRef.value._resizeHandler) {
    window.removeEventListener('resize', chartRef.value._resizeHandler)
  }
})

/**
 * 暴露图表实例给父组件
 * 允许父组件直接调用图表方法
 */
defineExpose({
  chartInstance,
  resize: () => chartInstance && chartInstance.resize()
})
</script>

<style scoped>
/* 图表卡片容器 */
.chart-card {
  background: var(--card-bg);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--card-border);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.chart-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--coral-500);
  opacity: 0.2;
  transition: opacity 0.3s;
}

.chart-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
  background: var(--bg-secondary);
}

.chart-card:hover::before {
  opacity: 0.6;
}

/* 图表头部 */
.chart-header {
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border-color);
}

.chart-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.3px;
}

/* 标题前的装饰竖线 */
.chart-title::before {
  content: '';
  width: 4px;
  height: 18px;
  background: var(--coral-500);
  border-radius: 2px;
  box-shadow: var(--shadow-sm);
}

.chart-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  padding-left: 14px;
}

/* 图表内容区 */
.chart-content {
  flex: 1;
  min-height: 200px;
  position: relative;
  overflow: hidden;
  border-radius: var(--border-radius-md);
}

.chart-container {
  width: 100%;
  height: 100%;
  min-height: 200px;
}

/* 响应式：中等屏幕 */
@media (max-width: 768px) {
  .chart-card {
    padding: 18px;
  }

  .chart-header {
    margin-bottom: 14px;
    padding-bottom: 10px;
  }

  .chart-title {
    font-size: 15px;
  }

  .chart-subtitle {
    font-size: 12px;
  }

  .chart-content {
    min-height: 180px;
  }

  .chart-container {
    min-height: 180px;
  }
}

/* 响应式:小屏幕 */
@media (max-width: 480px) {
  .chart-content {
    min-height: 160px;
  }

  .chart-container {
    min-height: 160px;
  }
}
</style>
