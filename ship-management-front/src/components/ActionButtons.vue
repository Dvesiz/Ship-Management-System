<template>
  <div class="action-buttons">
    <slot />
  </div>
</template>

<script setup>
// 统一的操作按钮容器组件
</script>

<style scoped>
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-buttons :deep(.el-button) {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.action-buttons :deep(.el-button--primary) {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-color: #3b82f6;
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
}

.action-buttons :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border-color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.3);
}

.action-buttons :deep(.el-button--success) {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-color: #10b981;
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.action-buttons :deep(.el-button--success:hover) {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  border-color: #059669;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(16, 185, 129, 0.3);
}

.action-buttons :deep(.el-button--warning) {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border-color: #f59e0b;
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(245, 158, 11, 0.2);
}

.action-buttons :deep(.el-button--warning:hover) {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
  border-color: #d97706;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(245, 158, 11, 0.3);
}

.action-buttons :deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border-color: #ef4444;
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.2);
}

.action-buttons :deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  border-color: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(239, 68, 68, 0.3);
}

.action-buttons :deep(.el-button--default) {
  background: #ffffff;
  border-color: #d1d5db;
  color: #374151;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.action-buttons :deep(.el-button--default:hover) {
  background: #f9fafb;
  border-color: #9ca3af;
  color: #111827;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-buttons :deep(.el-button.is-link) {
  height: auto;
  padding: 4px 8px;
  background: transparent;
  border: none;
  box-shadow: none;
  font-weight: 500;
}

.action-buttons :deep(.el-button--primary.is-link) {
  color: #3b82f6;
}

.action-buttons :deep(.el-button--primary.is-link:hover) {
  color: #2563eb;
  background: rgba(59, 130, 246, 0.1);
  transform: none;
}

.action-buttons :deep(.el-button--danger.is-link) {
  color: #ef4444;
}

.action-buttons :deep(.el-button--danger.is-link:hover) {
  color: #dc2626;
  background: rgba(239, 68, 68, 0.1);
  transform: none;
}

.action-buttons :deep(.el-button--warning.is-link) {
  color: #f59e0b;
}

.action-buttons :deep(.el-button--warning.is-link:hover) {
  color: #d97706;
  background: rgba(245, 158, 11, 0.1);
  transform: none;
}
</style>