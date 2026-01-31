<script setup>
/**
 * @file 应用根组件
 * @description 应用程序根组件，配置 Element Plus 国际化（中文），并提供路由视图容器
 * @author Claude Code
 * @date 2024
 */

import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import './styles/button-fix.css'
import './styles/layout-fix.css'
</script>

<template>
  <!-- Element Plus 配置提供者：设置中文语言环境 -->
  <el-config-provider :locale="zhCn">
    <!-- 路由视图渲染区 -->
    <router-view />
  </el-config-provider>
</template>

<style>
/* 全局样式重置和布局 */
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
