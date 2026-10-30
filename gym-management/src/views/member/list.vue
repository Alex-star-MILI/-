<template>
  <div class="page-container">
    <div class="page-header">
      <h2>会员列表</h2>
      <p class="sub-desc">管理所有会员信息，支持办卡登记、续费延期、查询导出</p>
    </div>

    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="姓名/手机号/编号" clearable style="width:200px" @keyup.enter="loadList" />
      <el-select v-model="query.cardType" placeholder="卡类型" clearable style="width:140px">
        <el-option label="月卡" value="month" />
        <el-option label="季卡" value="quarter" />
        <el-option label="年卡" value="year" />
      </el-select>
      <el-select v-model="query.status" placeholder="卡状态" clearable style="width:140px">
        <el-option label="有效" value="active" />
        <el-option label="已过期" value="expired" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      <div style="flex:1" />
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增会员</el-button>
      <el-button :icon="Download" @click="handleExport">导出会员数据</el-button>
    </div>

    <div class="table-container">
      <el-table :data="pagedList" border stripe>
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="id" label="会员编号" width="90" />
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="卡类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="cardTag(row.cardType)">{{ cardLabel(row.cardType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="累计金额(元)" width="130" align="right">
          <template #default="{ row }">¥{{ row.amount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="startDate" label="开卡日期" width="110" />
        <el-table-column prop="expireDate" label="到期日期" width="110">
          <template #default="{ row }">
            <span :style="{ color: remainColor(row.expireDate) }">{{ row.expireDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余天数" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="remainDays(row.expireDate) < 0" type="danger" effect="dark">
              过期 {{ -remainDays(row.expireDate) }}天
            </el-tag>
            <span v-else :style="{ color: remainColor(row.expireDate) }">{{ remainDays(row.expireDate) }}天</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleRenew(row)">续费</el-button>
            <el-button link type="warning" @click="handleView(row)">详情</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:12px;display:flex;justify-content:flex-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredList.length"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会员' : '新增会员'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="卡类型" prop="cardType">
          <el-select v-model="form.cardType" style="width:100%">
            <el-option label="月卡 (¥199)" value="month" />
            <el-option label="季卡 (¥599)" value="quarter" />
            <el-option label="年卡 (¥1999)" value="year" />
          </el-select>
        </el-form-item>
        <el-form-item label="开卡日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" @change="calcExpire" />
        </el-form-item>
        <el-form-item label="到期日期" prop="expireDate">
          <el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="办卡金额(元)" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 续费 -->
    <el-dialog v-model="renewVisible" title="会员续费延期" width="480px" :close-on-click-modal="false">
      <el-form :model="renewForm" label-width="110px">
        <el-form-item label="会员">
          <el-input :model-value="`${renewForm.name} (${renewForm.phone})`" disabled />
        </el-form-item>
        <el-form-item label="当前到期日">
          <el-input :model-value="renewForm.currentExpire" disabled />
        </el-form-item>
        <el-form-item label="延长方式">
          <el-radio-group v-model="renewForm.mode">
            <el-radio value="months">按月</el-radio>
            <el-radio value="years">按年</el-radio>
            <el-radio value="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="延长数量" v-if="renewForm.mode === 'months'">
          <el-input-number v-model="renewForm.addMonths" :min="1" :max="36" style="width:100%" /> 个月
        </el-form-item>
        <el-form-item label="延长数量" v-if="renewForm.mode === 'years'">
          <el-input-number v-model="renewForm.addYears" :min="1" :max="10" style="width:100%" /> 年
        </el-form-item>
        <el-form-item label="新到期日期" v-if="renewForm.mode === 'custom'">
          <el-date-picker v-model="renewForm.newExpireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="续费金额(元)">
          <el-input-number v-model="renewForm.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRenew">确认续费</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="会员详情" width="480px">
      <el-descriptions :column="1" border v-if="detailRow">
        <el-descriptions-item label="会员编号">{{ detailRow.id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailRow.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailRow.phone }}</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ cardLabel(detailRow.cardType) }}</el-descriptions-item>
        <el-descriptions-item label="累计办卡金额">¥{{ detailRow.amount?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="开卡日期">{{ detailRow.startDate }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ detailRow.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="剩余">{{ remainDays(detailRow.expireDate) }} 天</el-descriptions-item>
        <el-descriptions-item label="登记时间">{{ detailRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detailRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import { useMemberStore } from '@/store/member'
import { exportExcel, formatDate, daysBetween } from '@/utils/storage'

const store = useMemberStore()

const query = reactive({ keyword: '', cardType: '', status: '' })
const filteredList = ref([])
const page = ref(1)
const pageSize = ref(10)
const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function loadList() { filteredList.value = store.search({ ...query }); page.value = 1 }
function resetQuery() { query.keyword = ''; query.cardType = ''; query.status = ''; loadList() }
function cardTag(t) { return { month: '', quarter: 'success', year: 'warning' }[t] }
function cardLabel(t) { return { month: '月卡', quarter: '季卡', year: '年卡' }[t] }

function remainDays(expireDate) {
  return daysBetween(new Date(), expireDate)
}
function remainColor(expireDate) {
  const d = remainDays(expireDate)
  if (d < 0) return '#f56c6c'
  if (d <= 7) return '#e6a23c'
  return '#303133'
}

// 新增/编辑
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({ id: '', name: '', phone: '', cardType: 'month', startDate: formatDate(new Date()), expireDate: '', amount: 199, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  cardType: [{ required: true, message: '请选择卡类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开卡日期', trigger: 'change' }],
  expireDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}
function calcExpire() {
  if (!form.startDate) return
  const d = new Date(form.startDate)
  if (form.cardType === 'month') d.setMonth(d.getMonth() + 1)
  else if (form.cardType === 'quarter') d.setMonth(d.getMonth() + 3)
  else d.setFullYear(d.getFullYear() + 1)
  form.expireDate = formatDate(d)
}
function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: '', name: '', phone: '', cardType: 'month', startDate: formatDate(new Date()), expireDate: '', amount: 199, remark: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  isEdit.value = true; Object.assign(form, row); dialogVisible.value = true
}
async function submitForm() {
  await formRef.value?.validate()
  if (isEdit.value) { store.update(form.id, { ...form }); ElMessage.success('更新成功') }
  else { store.add({ ...form }); ElMessage.success('新增成功') }
  dialogVisible.value = false; loadList()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除会员 "${row.name}" 吗？`, '警告', { type: 'warning' })
  store.remove(row.id); ElMessage.success('删除成功'); loadList()
}

// 续费
const renewVisible = ref(false)
const renewForm = reactive({ id: '', name: '', phone: '', currentExpire: '', mode: 'months', addMonths: 1, addYears: 1, newExpireDate: '', amount: 0 })
function handleRenew(row) {
  Object.assign(renewForm, { id: row.id, name: row.name, phone: row.phone, currentExpire: row.expireDate, mode: 'months', addMonths: 1, addYears: 1, newExpireDate: '', amount: 0 })
  renewVisible.value = true
}
function submitRenew() {
  const opt = { amount: renewForm.amount }
  if (renewForm.mode === 'months') opt.addMonths = renewForm.addMonths
  else if (renewForm.mode === 'years') opt.addYears = renewForm.addYears
  else opt.newExpireDate = renewForm.newExpireDate
  store.renew(renewForm.id, opt)
  renewVisible.value = false
  ElMessage.success('续费成功'); loadList()
}

// 详情
const detailVisible = ref(false)
const detailRow = ref(null)
function handleView(row) { detailRow.value = row; detailVisible.value = true }

async function handleExport() {
  const data = store.search({ ...query })
  const rows = data.map(m => [m.id, m.name, m.phone, cardLabel(m.cardType), m.amount, m.startDate, m.expireDate, m.remark])
  await exportExcel(rows, '会员数据', ['编号', '姓名', '手机号', '卡类型', '累计金额(元)', '开卡日期', '到期日期', '备注'])
  ElMessage.success('导出成功')
}

onMounted(loadList)
</script>

<style scoped>
.sub-desc { color: #909399; margin-top: 4px; font-size: 13px; }
</style>
