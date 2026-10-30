<template>
  <div class="page-container">
    <div class="page-header">
      <h2>能耗统计报表</h2>
      <p class="sub-desc">自动汇总月度、季度、年度能耗数据，支持趋势分析和报表导出</p>
    </div>

    <div class="search-bar">
      <el-select v-model="queryYear" style="width:140px">
        <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
      </el-select>
      <el-button type="primary" :icon="Download" @click="handleExport">导出年度报表</el-button>
    </div>

    <!-- 年度汇总卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #409eff">
          <div class="stat-label">年度电费</div>
          <div class="stat-value">¥{{ summary.elec.toLocaleString() }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #67c23a">
          <div class="stat-label">年度水费</div>
          <div class="stat-value">¥{{ summary.water.toLocaleString() }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #f56c6c">
          <div class="stat-label">年度水电合计</div>
          <div class="stat-value">¥{{ summary.total.toLocaleString() }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left:4px solid #e6a23c">
          <div class="stat-label">月均能耗</div>
          <div class="stat-value">¥{{ summary.monthlyAvg.toLocaleString() }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="16">
        <div class="stat-card">
          <div class="card-title"><span>{{ queryYear }}年月度能耗趋势</span></div>
          <div ref="trendChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="card-title"><span>季度分布</span></div>
          <div ref="quarterChartRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <div class="stat-card">
          <div class="card-title"><span>{{ queryYear }}年 月度明细表</span></div>
          <el-table :data="monthlyList" border stripe size="small">
            <el-table-column label="月份" width="70" align="center">
              <template #default="{ row }">{{ row.statMonth }}月</template>
            </el-table-column>
            <el-table-column label="电费(元)" align="right" width="110">
              <template #default="{ row }">{{ row.elecCost.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="水费(元)" align="right" width="110">
              <template #default="{ row }">{{ row.waterCost.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="合计(元)" align="right">
              <template #default="{ row }"><b>{{ row.totalCost.toLocaleString() }}</b></template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="stat-card">
          <div class="card-title"><span>季度汇总表</span></div>
          <el-table :data="quarterList" border stripe size="small">
            <el-table-column prop="quarter" label="季度" width="90" align="center" />
            <el-table-column label="电费(元)" align="right" width="120">
              <template #default="{ row }">{{ row.elec.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="水费(元)" align="right" width="120">
              <template #default="{ row }">{{ row.water.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column label="合计(元)" align="right">
              <template #default="{ row }"><b style="color:#f56c6c">{{ row.total.toLocaleString() }}</b></template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Download } from '@element-plus/icons-vue'
import { useEnergyStore } from '@/store/energy'
import { exportExcel } from '@/utils/storage'
import { ElMessage } from 'element-plus'

const store = useEnergyStore()

const yearOptions = computed(() => {
  const set = new Set([new Date().getFullYear(), ...store.list.map(i => i.statYear)])
  return [...set].sort((a, b) => b - a)
})
const queryYear = ref(new Date().getFullYear())

const monthlyList = computed(() =>
  store.list.filter(i => i.statYear === queryYear.value).sort((a, b) => a.statMonth - b.statMonth)
)

const summary = computed(() => {
  const list = monthlyList.value
  const elec = list.reduce((s, i) => s + i.elecCost, 0)
  const water = list.reduce((s, i) => s + i.waterCost, 0)
  const total = elec + water
  return {
    elec: +elec.toFixed(2), water: +water.toFixed(2), total: +total.toFixed(2),
    monthlyAvg: list.length ? +(total / list.length).toFixed(2) : 0
  }
})

const quarterList = computed(() => {
  const list = monthlyList.value
  const groups = [
    { quarter: 'Q1', range: [1, 2, 3], elec: 0, water: 0 },
    { quarter: 'Q2', range: [4, 5, 6], elec: 0, water: 0 },
    { quarter: 'Q3', range: [7, 8, 9], elec: 0, water: 0 },
    { quarter: 'Q4', range: [10, 11, 12], elec: 0, water: 0 }
  ]
  groups.forEach(g => {
    list.filter(i => g.range.includes(i.statMonth)).forEach(i => {
      g.elec += i.elecCost
      g.water += i.waterCost
    })
    g.elec = +g.elec.toFixed(2); g.water = +g.water.toFixed(2); g.total = +(g.elec + g.water).toFixed(2)
  })
  return groups
})

// ECharts
const trendChartRef = ref(null)
const quarterChartRef = ref(null)
let trendChart, quarterChart

function renderCharts() {
  const months = monthlyList.value
  if (!trendChartRef.value || !quarterChartRef.value) return

  trendChart = trendChart || echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['电费', '水费', '合计'], bottom: 0 },
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: months.map(m => m.statMonth + '月') },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      { name: '电费', type: 'bar', stack: 'a', data: months.map(m => m.elecCost), itemStyle: { color: '#409eff' } },
      { name: '水费', type: 'bar', stack: 'a', data: months.map(m => m.waterCost), itemStyle: { color: '#67c23a' } },
      { name: '合计', type: 'line', data: months.map(m => m.totalCost), smooth: true, itemStyle: { color: '#f56c6c' } }
    ]
  })

  quarterChart = quarterChart || echarts.init(quarterChartRef.value)
  quarterChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '65%'],
      data: quarterList.value.map(q => ({ value: q.total, name: q.quarter })),
      label: { formatter: '{b}: ¥{c}' },
      color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c']
    }]
  })
}

onMounted(async () => {
  await store.init()
  await nextTick(); renderCharts()
  window.addEventListener('resize', () => { trendChart?.resize(); quarterChart?.resize() })
})
watch([queryYear, () => store.list.length], async () => {
  await nextTick(); renderCharts()
})

async function handleExport() {
  const rows = monthlyList.value.map(m => [m.statYear, m.statMonth, m.elecUsage, m.elecPrice, m.elecCost, m.waterUsage, m.waterPrice, m.waterCost, m.totalCost])
  await exportExcel(rows, `${queryYear}年能耗报表`, ['年份', '月份', '用电量(kWh)', '电价', '电费(元)', '用水量(吨)', '水价', '水费(元)', '合计(元)'])
  ElMessage.success('导出成功')
}
</script>

<style scoped>
.sub-desc { color: #909399; margin-top: 4px; font-size: 13px; }
.stat-card { background: #fff; padding: 20px 24px; border-radius: 6px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.stat-label { color: #909399; font-size: 13px; margin-bottom: 8px; }
.stat-value { font-size: 26px; font-weight: 700; color: #303133; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 12px; }
.chart-box { width: 100%; height: 300px; }
</style>
