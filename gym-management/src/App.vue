<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="app-aside">
      <div class="logo">
        <el-icon :size="24" color="#409eff"><Dumbbell /></el-icon>
        <span>健身房管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="side-menu"
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-sub-menu index="equipment">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>器材管理</span>
          </template>
          <el-menu-item index="/equipment/list">器材台账</el-menu-item>
          <el-menu-item index="/equipment/repair">维修报废</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="member">
          <template #title>
            <el-icon><User /></el-icon>
            <span>会员管理</span>
          </template>
          <el-menu-item index="/member/list">会员列表</el-menu-item>
          <el-menu-item index="/member/expire">到期提醒</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="energy">
          <template #title>
            <el-icon><Odometer /></el-icon>
            <span>水电能耗</span>
          </template>
          <el-menu-item index="/energy/list">能耗录入</el-menu-item>
          <el-menu-item index="/energy/stats">能耗统计</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
    <el-container>
      <el-header class="app-header">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-right">
          <el-badge :value="expiringCount" :hidden="expiringCount === 0" class="notice-badge">
            <el-button link @click="$router.push('/member/expire')">
              <el-icon :size="20"><Bell /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" style="background:#409eff">管</el-avatar>
              <span class="username">管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleResetData">重置演示数据</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useMemberStore } from './store/member'

const route = useRoute()
const router = useRouter()
const memberStore = useMemberStore()

const titleMap = {
  '/dashboard': '数据概览',
  '/equipment/list': '器材台账',
  '/equipment/repair': '维修报废管理',
  '/member/list': '会员列表',
  '/member/expire': '会员到期提醒',
  '/energy/list': '水电能耗录入',
  '/energy/stats': '能耗统计报表'
}

const currentTitle = computed(() => titleMap[route.path] || '健身房管理系统')

// 到期会员数量（7天内到期）
const expiringCount = computed(() => {
  const list = memberStore.expiringMembers(7)
  return list.length
})

const handleResetData = async () => {
  await ElMessageBox.confirm('确定要重置所有演示数据吗？此操作不可撤销。', '警告', {
    type: 'warning',
    confirmButtonText: '确定重置',
    cancelButtonText: '取消'
  })
  localStorage.clear()
  window.location.reload()
  ElMessage.success('数据已重置')
}
</script>

<style scoped>
.app-container {
  height: 100vh;
  width: 100vw;
}

.app-aside {
  background-color: #001529;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #ffffff1a;
}

.side-menu {
  border-right: none;
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #303133;
}

.app-main {
  padding: 0;
  background: #f5f7fa;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
