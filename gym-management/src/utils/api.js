import request from '@/utils/request'

// Dashboard
export const getDashboardSummary = () => request.get('/dashboard/summary')

// Equipment
export const listEquipment = (params) => request.get('/equipment', { params })
export const getEquipmentStats = () => request.get('/equipment/stats')
export const addEquipment = (data) => request.post('/equipment', data)
export const updateEquipment = (id, data) => request.put(`/equipment/${id}`, data)
export const deleteEquipment = (id) => request.delete(`/equipment/${id}`)

// Repair
export const listRepair = (status) => request.get('/repair', { params: { status } })
export const addRepair = (data) => request.post('/repair', data)
export const updateRepair = (id, data) => request.put(`/repair/${id}`, data)
export const finishRepair = (id, scrapped = false) =>
  request.post(`/repair/${id}/finish`, null, { params: { scrapped } })
export const deleteRepair = (id) => request.delete(`/repair/${id}`)

// Member
export const listMember = (params) => request.get('/member', { params })
export const getMemberStats = () => request.get('/member/stats')
export const getExpiringMembers = (days = 7) => request.get('/member/expiring', { params: { days } })
export const addMember = (data) => request.post('/member', data)
export const updateMember = (id, data) => request.put(`/member/${id}`, data)
export const renewMember = (id, data) => request.post(`/member/${id}/renew`, data)
export const deleteMember = (id) => request.delete(`/member/${id}`)

// Energy
export const listEnergy = (year) => request.get('/energy', { params: { year } })
export const getEnergyStats = () => request.get('/energy/stats')
export const addEnergy = (data) => request.post('/energy', data)
export const updateEnergy = (id, data) => request.put(`/energy/${id}`, data)
export const deleteEnergy = (id) => request.delete(`/energy/${id}`)
