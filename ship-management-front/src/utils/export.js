/**
 * @file Excel导出工具
 * @description 使用xlsx库导出数据为Excel文件
 */

import * as XLSX from 'xlsx'

/**
 * 导出数据为Excel文件
 * @param {Array} data - 要导出的数据数组
 * @param {String} filename - 文件名（不含扩展名）
 * @param {Array} headers - 表头配置 [{label: '显示名称', key: '字段名'}]
 */
export const exportToExcel = (data, filename, headers) => {
  if (!data || data.length === 0) {
    throw new Error('没有数据可导出')
  }

  // 如果提供了headers，则按照headers的顺序和映射导出
  let exportData = data
  if (headers && headers.length > 0) {
    exportData = data.map(item => {
      const row = {}
      headers.forEach(header => {
        // 兼容 label 和 title 两种字段名
        const headerLabel = header.label || header.title
        row[headerLabel] = formatValue(item[header.key])
      })
      return row
    })
  }

  // 创建工作簿
  const worksheet = XLSX.utils.json_to_sheet(exportData)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1')

  // 设置列宽
  const colWidths = []
  if (headers && headers.length > 0) {
    headers.forEach(header => {
      colWidths.push({ wch: header.width || 15 })
    })
  } else {
    // 自动计算列宽
    const keys = Object.keys(exportData[0])
    keys.forEach(() => {
      colWidths.push({ wch: 15 })
    })
  }
  worksheet['!cols'] = colWidths

  // 导出文件
  XLSX.writeFile(workbook, `${filename}_${formatDate(new Date())}.xlsx`)
}

/**
 * 格式化值
 */
const formatValue = (value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  if (typeof value === 'boolean') {
    return value ? '是' : '否'
  }
  if (value instanceof Date) {
    return formatDateTime(value)
  }
  if (typeof value === 'string' && value.match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/)) {
    return formatDateTime(new Date(value))
  }
  return value
}

/**
 * 格式化日期时间
 */
const formatDateTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

/**
 * 格式化日期（用于文件名）
 */
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}${month}${day}_${hour}${minute}`
}

/**
 * 导出当前页数据
 * @param {Array} data - 当前页数据
 * @param {String} filename - 文件名
 * @param {Array} headers - 表头配置
 */
export const exportCurrentPage = (data, filename, headers) => {
  exportToExcel(data, filename, headers)
}

/**
 * 导出所有数据（需要先获取所有数据）
 * @param {Function} fetchAllDataFn - 获取所有数据的函数
 * @param {String} filename - 文件名
 * @param {Array} headers - 表头配置
 */
export const exportAllData = async (fetchAllDataFn, filename, headers) => {
  try {
    const allData = await fetchAllDataFn()
    exportToExcel(allData, filename, headers)
  } catch (error) {
    throw new Error('获取数据失败：' + error.message)
  }
}
