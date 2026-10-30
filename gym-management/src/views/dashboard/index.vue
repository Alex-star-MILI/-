<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据概览</h2>
      <p class="sub-desc">健身房核心运营数据一览</p>
    </div>

    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #409eff">
          <div class="stat-label">器材总数</div>
          <div class="stat-value">{{ equipment.totalCount }}</div>
          <div class="stat-sub">正常 {{ equipment.statusStats.normal }} · 维修 {{ equipment.statusStats.repair }} · 报废 {{ equipment.statusStats.scrapped }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #67c23a">
          <div class="stat-label">会员总数</div>
          <div class="stat-value">{{ member.list.length }}</div>
          <div class="stat-sub">有效 {{ member.activeCount }} · 过期 {{ member.expiredCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #e6a23c">
          <div class="stat-label">累计办卡金额</div>
          <div class="stat-value">¥{{ member.totalRevenue.toLocaleString() }}</div>
          <div class="stat-sub">全部会员合计</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #f56c6c">
          <div class="stat-label">近12月能耗</div>
          <div class="stat-value">¥{{ yearlyEnergyCost.toLocaleString() }}</div>
          <div class="stat-sub">水电费用合计</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <!-- 能耗趋势图 -->
      <el-col :span="16">
        <div class="stat-card">
          <div class="card-title">
            <span>月度水电能耗趋势（{{ latestYear }}年）</span>
          </div>
          <div ref="energyChartRef" class="chart-box"></div>
        </div>
      </el-col>

      <!-- 到期会员提醒 -->
      <el-col :span="8">
        <div class="stat-card">
          <div class="card-title">
            <span>到期提醒（7天内）</span>
            <el-button link type="primary" @click="$router.push('/member/expire')">查看全部</el-button>
          </div>
          <el-table :data="expiring7" size="small" empty-text="暂无到期会员">
            <el-table-column prop="name" label="姓名" width="70" />
            <el-table-column prop="phone" label="手机号" width="110" />
            <el-table-column label="到期" width="90">
              <template #default="{ row }">
                <el-tag :type="getExpireTagType(row.expireDate)" size="small">
                  {{ row.expireDate.slice(5) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="剩余" width="60">
              <template #default="{ row }">
                <span :style="{ color: getRemainColor(row.expireDate) }">
                  {{ getRemainDays(row.expireDate) }}天
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <!-- 卡类型分布 -->
      <el-col :span="12">
        <div class="stat-card">
          <div class="card-title"><span>会员卡类型分布</span></div>
          <div ref="cardTypeChartRef" class="chart-box" style="height:260px"></div>
        </div>
      </el-col>
      <!-- 器材状态分布 -->
      <el-col :span="12">
        <div class="stat-card">
          <div class="card-title"><span>器材状态分布</span></div>
          <div ref="eqStatusChartRef" class="chart-box" style="height:260px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useEquipmentStore } from '@/store/equipment'
import { useMemberStore } from '@/store/member'
import { useEnergyStore } from '@/store/energy'

const equipment = useEquipmentStore()
const member = useMemberStore()
const energy = useEnergyStore()

const energyChartRef = ref(null)
const cardTypeChartRef = ref(null)
const eqStatusChartRef = ref(null)

const latestYear = computed(() => {
  const years = [...new Set(energy.list.map(i => i.year))].sort((a, b) => b - a)
  return years[0] || new Date().getFullYear()
})

const yearlyEnergyCost = computed(() =>
  energy.list.reduce((s, i) => s + (i.totalCost || 0), 0).toFixed(2)
)

const expiring7 = computed(() => member.expiringMembers(7).slice(0, 8))

function getRemainDays(dateStr) {
  const now = new Date(); now.setHours(0,0,0,0)
  const exp = new Date(dateStr); exp.setHours(0,0,0,0)
  return Math.ceil((exp - now) / 86400000)
}
function getRemainColor(dateStr) {
  const d = getRemainDays(dateStr)
  if (d < 0) return '#f56c6c'
  if (d <= 3) return '#e6a23c'
  return '#67c23a'
}
function getExpireTagType(dateStr) {
  const d = getRemainDays(dateStr)
  if (d < 0) return 'danger'
  if (d <= 3) return 'warning'
  return 'success'
}

let energyChart, cardTypeChart, eqStatusChart

function initCharts() {
  // 能耗趋势
  const data = energy.latestYearList
  energyChart = echarts.init(energyChartRef.value)
  energyChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['电费(元)', '水费(元)', '合计(元)'], bottom: 0 },
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: data.map(i => i.month + '月') },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      { name: '电费(元)', type: 'bar', stack: 'cost', data: data.map(i => i.elecCost), itemStyle: { color: '#409eff' } },
      { name: '水费(元)', type: 'bar', stack: 'cost', data: data.map(i => i.waterCost), itemStyle: { color: '#67c23a' } },
      { name: '合计(元)', type: 'line', data: data.map(i => i.totalCost), itemStyle: { color: '#f56c6c' } }
    ]
  })

  // 卡类型
  cardTypeChart = echarts.init(cardTypeChartRef.value)
  cardTypeChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '65%'],
      data: [
        { value: member.cardTypeStats.month, name: '月卡', itemStyle: { color: '#409eff' } },
        { value: member.cardTypeStats.quarter, name: '季卡', itemStyle: { color: '#67c23a' } },
        { value: member.cardTypeStats.year, name: '年卡', itemStyle: { color: '#e6a23c' } }
      ],
      label: { formatter: '{b}: {c}人 ({d}%)' }
    }]
  })

  // 器材状态
  eqStatusChart = echarts.init(eqStatusChartRef.value)
  eqStatusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: '60%',
      data: [
        { value: equipment.statusStats.normal, name: '正常', itemStyle: { color: '#67c23a' } },
        { value: equipment.statusStats.repair, name: '维修中', itemStyle: { color: '#e6a23c' } },
        { value: equipment.statusStats.scrapped, name: '报废', itemStyle: { color: '#f56c6c' } }
      ],
      label: { formatter: '{b}: {c}台' }
    }]
  })
}

onMounted(async () => {
  await nextTick()
  initCharts()
  window.addEventListener('resize', () => {
    energyChart?.resize()
    cardTypeChart?.resize()
    eqStatusChart?.resize()
  })
})

watch(() => [equipment.list.length, member.list.length, energy.list.length], () => {
  nextTick(initCharts)
})
</script>

<style scoped>
.sub-desc { color: #909399; margin-top: 4px; font-size: 13px; }
.stat-row { margin-bottom: 0; }
.stat-card {
  background: #fff;
  padding: 20px 24px;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.stat-label { color: #909399; font-size: 13px; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; }
.stat-sub { font-size: 12px; color: #c0c4cc; margin-top: 6px; }
.card-title {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 12px;
}
.chart-box { width: 100%; height: 300px; }
</style>
