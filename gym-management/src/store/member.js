import { defineStore } from 'pinia'
import * as api from '@/utils/api'

const cardTypeMap = { month: '月卡', quarter: '季卡', year: '年卡' }

export const useMemberStore = defineStore('member', {
  state: () => ({
    list: []
  }),
  getters: {
    activeCount: (s) => s.list.filter(m => new Date(m.expireDate) >= new Date(new Date().toDateString())).length,
    expiredCount: (s) => s.list.filter(m => new Date(m.expireDate) < new Date(new Date().toDateString())).length,
    cardTypeStats: (s) => {
      const stats = { month: 0, quarter: 0, year: 0 }
      s.list.forEach(m => { stats[m.cardType]++ })
      return stats
    },
    totalRevenue: (s) => s.list.reduce((sum, m) => sum + (m.amount || 0), 0)
  },
  actions: {
    async init() {
      await this.loadList()
    },
    async loadList(params = {}) {
      const res = await api.listMember(params)
      this.list = res.data || []
      return this.list
    },
    async search(params = {}) {
      return this.loadList(params)
    },
    async expiringMembers(days = 7) {
      const res = await api.getExpiringMembers(days)
      return res.data || []
    },
    async add(item) {
      const res = await api.addMember(item)
      await this.loadList()
      return res.data
    },
    async update(id, data) {
      await api.updateMember(id, data)
      await this.loadList()
    },
    async renew(id, opt = {}) {
      await api.renewMember(id, opt)
      await this.loadList()
    },
    async remove(id) {
      await api.deleteMember(id)
      await this.loadList()
    },
    cardLabel(type) { return cardTypeMap[type] || type }
  }
})
