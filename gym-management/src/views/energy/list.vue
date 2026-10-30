<template>
  <div class="page-container">
    <div class="page-header">
      <h2>水电能耗录入</h2>
      <p class="sub-desc">按月录入场馆用电量、用水量、单价及费用，系统自动汇总统计</p>
    </div>

    <div class="search-bar">
      <el-select v-model="queryYear" placeholder="年份" style="width:120px">
        <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
      </el-select>
      <div style="flex:1" />
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增月度数据</el-button>
    </div>

    <div class="table-container">
      <el-table :data="filteredList" border stripe>
        <el-table-column prop="statYear" label="年份" width="80" align="center" />
        <el-table-column label="月份" width="70" align="center">
          <template #default="{ row }">{{ row.statMonth }} 月</template>
        </el-table-column>
        <el-table-column label="用电量(kWh)" width="130" align="right">
          <template #default="{ row }">{{ row.elecUsage?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="电价(元)" width="110" align="right">
          <template #default="{ row }">{{ row.elecPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="电费(元)" width="130" align="right">
          <template #default="{ row }">¥{{ row.elecCost?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="用水量(吨)" width="120" align="right">
          <template #default="{ row }">{{ row.waterUsage?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="水价(元)" width="100" align="right">
          <template #default="{ row }">{{ row.waterPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="水费(元)" width="120" align="right">
          <template #default="{ row }">¥{{ row.waterCost?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="合计费用(元)" width="130" align="right">
          <template #default="{ row }">
            <span style="font-weight:600;color:#f56c6c">¥{{ row.totalCost?.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredList.length" style="margin-top:12px;display:flex;justify-content:flex-end;gap:24px;color:#606266">
        <span>年度电费合计：<b style="color:#409eff">¥{{ yearSummary.elec.toLocaleString() }}</b></span>
        <span>年度水费合计：<b style="color:#67c23a">¥{{ yearSummary.water.toLocaleString() }}</b></span>
        <span>年度水电合计：<b style="color:#f56c6c">¥{{ yearSummary.total.toLocaleString() }}</b></span>
      </div>
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑能耗数据' : '新增月度能耗'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="年份" prop="statYear">
              <el-select v-model="form.statYear" style="width:100%">
                <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="月份" prop="statMonth">
              <el-select v-model="form.statMonth" style="width:100%">
                <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">用电</el-divider>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用电量(kWh)" prop="elecUsage">
              <el-input-number v-model="form.elecUsage" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电价(元)" prop="elecPrice">
              <el-input-number v-model="form.elecPrice" :min="0" :precision="4" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">用水</el-divider>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用水量(吨)" prop="waterUsage">
              <el-input-number v-model="form.waterUsage" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="水价(元)" prop="waterPrice">
              <el-input-number v-model="form.waterPrice" :min="0" :precision="4" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px">
          系统自动计算：电费 ¥{{ calc.elecCost }} · 水费 ¥{{ calc.waterCost }} · 合计 <b>¥{{ calc.total }}</b>
        </el-alert>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useEnergyStore } from '@/store/energy'

const store = useEnergyStore()

const yearOptions = computed(() => {
  const set = new Set([new Date().getFullYear(), ...store.list.map(i => i.statYear)])
  return [...set].sort((a, b) => b - a)
})
const queryYear = ref(new Date().getFullYear())

const filteredList = computed(() =>
  store.list.filter(i => i.statYear === queryYear.value).sort((a, b) => a.statMonth - b.statMonth)
)

const yearSummary = computed(() => {
  const list = filteredList.value
  return {
    elec: list.reduce((s, i) => s + (i.elecCost || 0), 0).toFixed(2),
    water: list.reduce((s, i) => s + (i.waterCost || 0), 0).toFixed(2),
    total: list.reduce((s, i) => s + (i.totalCost || 0), 0).toFixed(2)
  }
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: '', statYear: new Date().getFullYear(), statMonth: new Date().getMonth() + 1,
  elecUsage: 0, elecPrice: 0.85, waterUsage: 0, waterPrice: 5.6, remark: ''
})
const rules = {
  statYear: [{ required: true, message: '请选择年份' }],
  statMonth: [{ required: true, message: '请选择月份' }],
  elecUsage: [{ required: true, message: '请输入用电量' }],
  elecPrice: [{ required: true, message: '请输入电价' }],
  waterUsage: [{ required: true, message: '请输入用水量' }],
  waterPrice: [{ required: true, message: '请输入水价' }]
}
const calc = computed(() => ({
  elecCost: (+(form.elecUsage * form.elecPrice).toFixed(2)).toLocaleString(),
  waterCost: (+(form.waterUsage * form.waterPrice).toFixed(2)).toLocaleString(),
  total: (+(form.elecUsage * form.elecPrice + form.waterUsage * form.waterPrice).toFixed(2)).toLocaleString()
}))

function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: '', statYear: new Date().getFullYear(), statMonth: new Date().getMonth() + 1,
    elecUsage: 0, elecPrice: 0.85, waterUsage: 0, waterPrice: 5.6, remark: ''
  })
  dialogVisible.value = true
}
function handleEdit(row) {
  isEdit.value = true; Object.assign(form, row); dialogVisible.value = true
}
async function submitForm() {
  await formRef.value?.validate()
  if (isEdit.value) { await store.update(form.id, { ...form }); ElMessage.success('更新成功') }
  else { await store.add({ ...form }); ElMessage.success('新增成功') }
  dialogVisible.value = false
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除 ${row.statYear}年${row.statMonth}月 的数据？`, '警告', { type: 'warning' })
  await store.remove(row.id); ElMessage.success('删除成功')
}

onMounted(async () => { await store.init() })
</script>

<style scoped>
.sub-desc { color: #909399; margin-top: 4px; font-size: 13px; }
</style>
