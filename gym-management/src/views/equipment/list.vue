<template>
  <div class="page-container">
    <div class="page-header">
      <h2>器材台账</h2>
      <p class="sub-desc">全生命周期管理器材信息，支持录入、查询、导出</p>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="名称/型号/编号搜索" clearable style="width:200px" @keyup.enter="loadList" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
        <el-option label="正常" value="normal" />
        <el-option label="维修中" value="repair" />
        <el-option label="已报废" value="scrapped" />
      </el-select>
      <el-input v-model="query.location" placeholder="存放位置" clearable style="width:160px" />
      <el-button type="primary" :icon="Search" @click="loadList">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      <div style="flex:1" />
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增器材</el-button>
      <el-button :icon="Upload" @click="handleBatchAdd">批量录入</el-button>
      <el-button :icon="Download" @click="handleExport">导出台账</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="pagedList" border stripe>
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="name" label="器材名称" width="120" />
        <el-table-column prop="model" label="型号" width="160" />
        <el-table-column prop="purchaseDate" label="采购日期" width="110" />
        <el-table-column prop="price" label="采购价(元)" width="110" align="right">
          <template #default="{ row }">¥{{ row.price?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="location" label="存放位置" width="110" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" effect="dark">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" v-if="row.status === 'repair' || row.status === 'normal'" @click="handleAddRepair(row)">报修</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑器材' : '新增器材'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="器材名称" prop="name">
          <el-input v-model="form.name" placeholder="如：跑步机" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model" placeholder="如：Precor TRM 885" />
        </el-form-item>
        <el-form-item label="采购日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="采购价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="存放位置" prop="location">
          <el-input v-model="form.location" placeholder="如：有氧区A" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="正常" value="normal" />
            <el-option label="维修中" value="repair" />
            <el-option label="已报废" value="scrapped" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 报修对话框 -->
    <el-dialog v-model="repairVisible" title="登记报修" width="480px" :close-on-click-modal="false">
      <el-form :model="repairForm" label-width="100px">
        <el-form-item label="器材">
          <el-input :model-value="repairForm.equipmentName" disabled />
        </el-form-item>
        <el-form-item label="故障描述" required>
          <el-input v-model="repairForm.fault" type="textarea" :rows="3" placeholder="请详细描述故障现象" />
        </el-form-item>
        <el-form-item label="维修人员">
          <el-input v-model="repairForm.repairPerson" placeholder="如：张师傅" />
        </el-form-item>
        <el-form-item label="维修费用(元)">
          <el-input-number v-model="repairForm.cost" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRepair">提交报修</el-button>
      </template>
    </el-dialog>

    <!-- 批量录入对话框 -->
    <el-dialog v-model="batchVisible" title="批量录入器材" width="600px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px">
        每行一条，按顺序：名称,型号,采购日期,价格,位置
      </el-alert>
      <el-input v-model="batchText" type="textarea" :rows="10" placeholder="跑步机,Test,2026-10-01,10000,有氧区A" />
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatch">确认录入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Upload, Download } from '@element-plus/icons-vue'
import { useEquipmentStore } from '@/store/equipment'
import { exportExcel, formatDate } from '@/utils/storage'

const store = useEquipmentStore()

const query = reactive({ keyword: '', status: '', location: '' })
const filteredList = ref([])
const page = ref(1)
const pageSize = ref(10)

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function loadList() {
  filteredList.value = store.search({ ...query })
  page.value = 1
}
function resetQuery() {
  query.keyword = ''; query.status = ''; query.location = ''
  loadList()
}
function statusTag(s) { return { normal: 'success', repair: 'warning', scrapped: 'danger' }[s] }
function statusLabel(s) { return { normal: '正常', repair: '维修中', scrapped: '已报废' }[s] }

// 新增/编辑
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({ id: '', name: '', model: '', purchaseDate: formatDate(new Date()), price: 0, location: '', status: 'normal', remark: '' })
const rules = {
  name: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
  purchaseDate: [{ required: true, message: '请选择采购日期', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  location: [{ required: true, message: '请输入存放位置', trigger: 'blur' }]
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: '', name: '', model: '', purchaseDate: formatDate(new Date()), price: 0, location: '', status: 'normal', remark: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}
async function submitForm() {
  await formRef.value?.validate()
  if (isEdit.value) {
    store.update(form.id, { ...form })
    ElMessage.success('更新成功')
  } else {
    store.add({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadList()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除器材 "${row.name}" 吗？`, '警告', { type: 'warning' })
  store.remove(row.id)
  ElMessage.success('删除成功')
  loadList()
}

// 报修
const repairVisible = ref(false)
const repairForm = reactive({ equipmentId: '', equipmentName: '', fault: '', repairPerson: '', cost: 0 })

function handleAddRepair(row) {
  Object.assign(repairForm, { equipmentId: row.id, equipmentName: `${row.name}(${row.model})`, fault: '', repairPerson: '', cost: 0 })
  repairVisible.value = true
}
function submitRepair() {
  if (!repairForm.fault.trim()) return ElMessage.warning('请填写故障描述')
  store.addRepair({
    equipmentId: repairForm.equipmentId,
    fault: repairForm.fault,
    repairDate: formatDate(new Date()),
    cost: repairForm.cost,
    repairPerson: repairForm.repairPerson,
    status: 'repairing',
    remark: ''
  })
  repairVisible.value = false
  ElMessage.success('报修登记成功')
  loadList()
}

// 批量录入
const batchVisible = ref(false)
const batchText = ref('')
function handleBatchAdd() {
  batchText.value = ''
  batchVisible.value = true
}
function submitBatch() {
  const lines = batchText.value.trim().split('\n').filter(Boolean)
  if (!lines.length) return ElMessage.warning('请输入至少一行')
  let ok = 0, fail = 0
  lines.forEach(line => {
    const parts = line.split(',').map(s => s.trim())
    if (parts.length >= 5) {
      store.add({
        name: parts[0], model: parts[1], purchaseDate: parts[2],
        price: Number(parts[3]) || 0, location: parts[4],
        status: 'normal', remark: parts[5] || ''
      })
      ok++
    } else { fail++ }
  })
  batchVisible.value = false
  ElMessage.success(`成功录入 ${ok} 条${fail ? `，${fail} 条格式错误已跳过` : ''}`)
  loadList()
}

// 导出
async function handleExport() {
  const data = store.search({ ...query })
  const rows = data.map(e => [e.id, e.name, e.model, e.purchaseDate, e.price, e.location, statusLabel(e.status), e.remark])
  await exportExcel(rows, '器材台账', ['编号', '名称', '型号', '采购日期', '价格(元)', '存放位置', '状态', '备注'])
  ElMessage.success('导出成功')
}

onMounted(loadList)
</script>

<style scoped>
.sub-desc { color: #909399; margin-top: 4px; font-size: 13px; }
</style>
