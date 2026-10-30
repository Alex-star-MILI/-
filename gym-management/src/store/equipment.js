import { defineStore } from 'pinia'
import * as api from '@/utils/api'

export const useEquipmentStore = defineStore('equipment', {
  state: () => ({
    list: [],
    repairs: []
  }),
  getters: {
    normalList: (s) => s.list.filter(e => e.status === 'normal'),
    repairList: (s) => s.list.filter(e => e.status === 'repair'),
    scrappedList: (s) => s.list.filter(e => e.status === 'scrapped'),
    totalCount: (s) => s.list.length,
    statusStats: (s) => {
      const stats = { normal: 0, repair: 0, scrapped: 0 }
      s.list.forEach(e => { stats[e.status]++ })
      return stats
    }
  },
  actions: {
    async init() {
      await this.loadList()
      await this.loadRepairs()
    },
    async loadList(params = {}) {
      const res = await api.listEquipment(params)
      this.list = res.data || []
      return this.list
    },
    async loadRepairs(status) {
      const res = await api.listRepair(status)
      this.repairs = res.data || []
      return this.repairs
    },
    async search(params = {}) {
      return this.loadList(params)
    },
    async add(item) {
      const res = await api.addEquipment(item)
      await this.loadList()
      return res.data
    },
    async update(id, data) {
      await api.updateEquipment(id, data)
      await this.loadList()
    },
    async remove(id) {
      await api.deleteEquipment(id)
      await this.loadList()
    },
    async addRepair(record) {
      await api.addRepair(record)
      await this.loadRepairs()
      await this.loadList()
    },
    async finishRepair(id, isScrapped = false) {
      await api.finishRepair(id, isScrapped)
      await this.loadRepairs()
      await this.loadList()
    },
    async removeRepair(id) {
      await api.deleteRepair(id)
      await this.loadRepairs()
    }
  }
})
