import { defineStore } from 'pinia'
import * as api from '@/utils/api'

export const useEnergyStore = defineStore('energy', {
  state: () => ({
    list: []
  }),
  getters: {
    latestYearList: (s) => {
      const years = [...new Set(s.list.map(i => i.statYear))].sort((a, b) => b - a)
      if (!years.length) return []
      return s.list
        .filter(i => i.statYear === years[0])
        .sort((a, b) => a.statMonth - b.statMonth)
    }
  },
  actions: {
    async init() {
      await this.loadList()
    },
    async loadList(year) {
      const res = await api.listEnergy(year)
      this.list = res.data || []
      return this.list
    },
    async search({ year = null }) {
      return this.loadList(year)
    },
    async add(item) {
      const res = await api.addEnergy(item)
      await this.loadList()
      return res.data
    },
    async update(id, data) {
      await api.updateEnergy(id, data)
      await this.loadList()
    },
    async remove(id) {
      await api.deleteEnergy(id)
      await this.loadList()
    }
  }
})
