<template>
  <div class="page-container">
    <div class="page-header">
      <h2>维修报废管理</h2>
      <p class="sub-desc">登记器材故障维修记录，支持维修完成或报废归档</p>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 维修中列表 -->
      <el-tab-pane label="维修中" name="repairing">
        <div class="table-container">
          <el-table :data="repairingList" border stripe>
            <el-table-column prop="id" label="单号" width="90" />
            <el-table-column label="器材" min-width="180">
              <template #default="{ row }">
                <div>{{ row.equipmentName }}</div>
                <div style="font-size:12px;color:#909399">{{ row.equipmentId }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="fault" label="故障描述" min-width="180" show-overflow-tooltip />
            <el-table-column prop="repairPerson" label="维修人员" width="100" />
            <el-table-column prop="repairDate" label="登记日期" width="110" />
            <el-table-column label="维修费用(元)" width="120" align="right">
              <template #default="{ row }">¥{{ row.cost?.toLocaleString() || 0 }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="success" @click="finishRepair(row, false)">维修完成</el-button>
                <el-button link type="warning" @click="showScrapDialog(row)">提交报废</el-button>
                <el-button link type="danger" @click="deleteRepair(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 已完成维修 -->
      <el-tab-pane label="已完成维修" name="done">
        <div class="table-container">
          <el-table :data="doneList" border stripe>
            <el-table-column prop="id" label="单号" width="90" />
            <el-table-column prop="equipmentName" label="器材" min-width="150" />
            <el-table-column prop="fault" label="故障描述" min-width="180" show-overflow-tooltip />
            <el-table-column prop="repairPerson" label="维修人员" width="100" />
            <el-table-column prop="repairDate" label="维修日期" width="110" />
            <el-table-column label="维修费用(元)" width="120" align="right">
              <template #default="{ row }">¥{{ row.cost?.toLocaleString() || 0 }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 报废记录 -->
      <el-tab-pane label="报废记录" name="scrapped">
        <div class="table-container">
          <el-table :data="scrappedList" border stripe>
            <el-table-column prop="id" label="单号" width="90" />
            <el-table-column prop="equipmentName" label="器材" min-width="150" />
            <el-table-column prop="fault" label="故障/报废原因" min-width="200" show-overflow-tooltip />
            <el-table-column prop="repairDate" label="报废日期" width="110" />
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 报废确认对话框 -->
    <el-dialog v-model="scrapVisible" title="提交报废申请" width="480px" :close-on-click-modal="false">
      <el-form :model="scrapForm" label-width="100px">
        <el-form-item label="器材">
          <el-input :model-value="scrapForm.equipmentName" disabled />
        </el-form-item>
        <el-form-item label="报废原因" required>
          <el-input v-model="scrapForm.reason" type="textarea" :rows="3" placeholder="请详细说明报废原因，如：配件停产无法维修、安全隐患等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scrapVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmScrap">确认报废</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useEquipmentStore } from '@/store/equipment'

const store = useEquipmentStore()

const activeTab = ref('repairing')

const repairingList = computed(() => store.repairs.filter(r => r.status === 'repairing'))
const doneList = computed(() => store.repairs.filter(r => r.status === 'done'))
const scrappedList = computed(() => store.repairs.filter(r => r.status === 'scrapped'))

function finishRepair(row) {
  ElMessageBox.confirm(`确认器材 "${row.equipmentName}" 维修完成并恢复正常使用？`, '维修完成', { type: 'success' })
    .then(() => {
      store.finishRepair(row.id, false)
      ElMessage.success('已标记为维修完成')
    })
}
function deleteRepair(row) {
  ElMessageBox.confirm('确定删除这条维修记录？', '警告', { type: 'warning' })
    .then(() => { store.removeRepair(row.id); ElMessage.success('删除成功') })
}

// 报废
const scrapVisible = ref(false)
const scrapForm = reactive({ equipmentId: '', equipmentName: '', reason: '' })
function showScrapDialog(row) {
  scrapForm.equipmentId = row.equipmentId
  scrapForm.equipmentName = row.equipmentName
  scrapForm.reason = row.fault
  scrapVisible.value = true
}
function confirmScrap() {
  if (!scrapForm.reason.trim()) return ElMessage.warning('请填写报废原因')
  // 更新 repair 记录为 scrapped
  const rp = store.repairs.find(r => r.equipmentId === scrapForm.equipmentId && r.status === 'repairing')
  if (rp) rp.remark = scrapForm.reason
  store.finishRepair(rp?.id, true)
  scrapVisible.value = false
  ElMessage.success('已提交报废')
}
</script>

<style scoped>
.sub-desc { color: #909399; margin-top: 4px; font-size: 13px; }
</style>
