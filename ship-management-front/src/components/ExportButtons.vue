<script setup>
/**
 * @file 导出按钮组件
 * @description 提供导出当前页和导出全部数据的按钮
 */

import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { exportToExcel } from '@/utils/export'

const props = defineProps({
  // 当前页数据
  currentData: {
    type: Array,
    required: true
  },
  // 获取所有数据的函数
  fetchAllDataFn: {
    type: Function,
    required: true
  },
  // 文件名
  filename: {
    type: String,
    required: true
  },
  // 表头配置
  headers: {
    type: Array,
    required: true
  },
  // 按钮大小
  size: {
    type: String,
    default: 'default'
  }
})

// 导出当前页
const handleExportCurrent = () => {
  try {
    if (!props.currentData || props.currentData.length === 0) {
      ElMessage.warning('当前页没有数据可导出')
      return
    }
    exportToExcel(props.currentData, props.filename, props.headers)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}

// 导出全部数据
const handleExportAll = async () => {
  try {
    const allData = await props.fetchAllDataFn()
    if (!allData || allData.length === 0) {
      ElMessage.warning('没有数据可导出')
      return
    }
    exportToExcel(allData, props.filename + '_全部', props.headers)
    ElMessage.success(`成功导出 ${allData.length} 条数据`)
  } catch (error) {
    ElMessage.error('导出失败：' + error.message)
  }
}
</script>

<template>
  <el-button-group>
    <el-button 
      type="success" 
      :icon="Download" 
      :size="size"
      @click="handleExportCurrent"
    >
      导出当前页
    </el-button>
    <el-button 
      type="warning" 
      :icon="Download" 
      :size="size"
      @click="handleExportAll"
    >
      导出全部
    </el-button>
  </el-button-group>
</template>
