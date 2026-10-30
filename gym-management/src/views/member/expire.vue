<template>
  <div class="page-container">
    <div class="page-header">
      <h2>会员到期提醒</h2>
      <p class="sub-desc">系统自动识别即将到期与已过期会员，便于前台主动跟进续卡</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #f56c6c">
          <div class="stat-label">已过期</div>
          <div class="stat-value">{{ expiredCount }} 人</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #e6a23c">
          <div class="stat-label">3天内到期</div>
          <div class="stat-value">{{ within3Count }} 人</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #409eff">
          <div class="stat-label">7天内到期</div>
          <div class="stat-value">{{ within7Count }} 人</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #67c23a">
          <div class="stat-label">30天内到期</div>
          <div class="stat-value">{{ within30Count }} 人</div>
        </div>
      </el-col>
    </el-row>

    <div class="search-bar">
      <el-select v-model="filterDays" style="width:160px">
        <el-option label="已过期" value="expired" />
        <el-option label="3天内到期" :value="3" />
        <el-option label="7天内到期" :value="7" />
        <el-option label="30天内到期" :value="30" />
        <el-option label="全部即将到期" :value="9999" />
      </el-select>
      <el-input v-model="keyword" placeholder="姓名/手机号搜索" clearable style="width:200px" />
      <div style="flex:1" />
      <el-button type="primary" :icon="Download" @click="handleExport">导出提醒名单</el-button>
    </div>

    <div class="table-container">
      <el-table :data="filteredList" border stripe>
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="copyPhone(row.phone)">{{ row.phone }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="卡类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag>{{ cardLabel(row.cardType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期日期" width="110" />
        <el-table-column label="剩余天数" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="remain(row.expireDate) < 0" type="danger" effect="dark">
              过期 {{ -remain(row.expireDate) }} 天
            </el-tag>
            <el-tag v-else-if="remain(row.expireDate) <= 3" type="warning" effect="dark">
              {{ remain(row.expireDate) }} 天
            </el-tag>
            <el-tag v-else-if="remain(row.expireDate) <= 7" type="warning">
              {{ remain(row.expireDate) }} 天
            </el-tag>
            <el-tag v-else type="info">{{ remain(row.expireDate) }} 天</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="quickRenew(row)">快速续费</el-button>
            <el-button link type="primary" @click="copyPhone(row.phone)">复制手机</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredList.length === 0" description="暂无即将到期会员" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { useMemberStore } from '@/store/member'
import { exportExcel, daysBetween } from '@/utils/storage'

const store = useMemberStore()
const router = useRouter()

const filterDays = ref(7)
const keyword = ref('')

const expiredCount = computed(() => store.expiringMembers(0).filter(m => remain(m.expireDate) < 0).length)
const within3Count = computed(() => store.expiringMembers(3).filter(m => remain(m.expireDate) >= 0).length)
const within7Count = computed(() => store.expiringMembers(7).filter(m => remain(m.expireDate) >= 0).length)
const within30Count = computed(() => store.expiringMembers(30).filter(m => remain(m.expireDate) >= 0).length)

const filteredList = computed(() => {
  let list
  if (filterDays.value === 'expired') {
    list = store.expiringMembers(0).filter(m => remain(m.expireDate) < 0)
  } else {
    list = store.expiringMembers(filterDays.value).filter(m => filterDays.value === 9999 || remain(m.expireDate) >= 0)
  }
  if (keyword.value) {
    const k = keyword.value
    list = list.filter(m => m.name.includes(k) || m.phone.includes(k))
  }
  return list
})

function cardLabel(t) { return { month: '月卡', quarter: '季卡', year: '年卡' }[t] }
function remain(expireDate) { return daysBetween(new Date(), expireDate) }

function copyPhone(phone) {
  navigator.clipboard?.writeText(phone)
  ElMessage.success(`手机号 ${phone} 已复制`)
}

function quickRenew(row) {
  router.push({ path: '/member/list', query: `renew=${row.id}` })
}

async function handleExport() {
  const rows = filteredList.value.map(m => [m.id, m.name, m.phone, cardLabel(m.cardType), m.expireDate, remain(m.expireDate)])
  await exportExcel(rows, '到期提醒名单', ['编号', '姓名', '手机号', '卡类型', '到期日期', '剩余(天)'])
  ElMessage.success('导出成功')
}
</script>

<style scoped>
.sub-desc { color: #909399; margin-top: 4px; font-size: 13px; }
.stat-card { background: #fff; padding: 20px 24px; border-radius: 6px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.stat-label { color: #909399; font-size: 13px; margin-bottom: 8px; }
.stat-value { font-size: 26px; font-weight: 700; color: #303133; }
</style>
