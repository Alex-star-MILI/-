// localStorage 封装工具
const PREFIX = 'gym_'

export function getStorage(key, defaultValue = null) {
  try {
    const raw = localStorage.getItem(PREFIX + key)
    if (raw === null) return defaultValue
    return JSON.parse(raw)
  } catch {
    return defaultValue
  }
}

export function setStorage(key, value) {
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify(value))
  } catch (e) {
    console.error('storage set error:', e)
  }
}

export function removeStorage(key) {
  localStorage.removeItem(PREFIX + key)
}

// 生成唯一 ID
export function generateId(prefix = '') {
  return prefix + Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
}

// 格式化日期
export function formatDate(date, fmt = 'YYYY-MM-DD') {
  if (!date) return ''
  const d = typeof date === 'string' ? new Date(date) : date
  const o = {
    'YYYY': d.getFullYear(),
    'MM': String(d.getMonth() + 1).padStart(2, '0'),
    'DD': String(d.getDate()).padStart(2, '0'),
    'HH': String(d.getHours()).padStart(2, '0'),
    'mm': String(d.getMinutes()).padStart(2, '0'),
    'ss': String(d.getSeconds()).padStart(2, '0')
  }
  return fmt.replace(/YYYY|MM|DD|HH|mm|ss/g, m => o[m])
}

// 计算两个日期相差天数
export function daysBetween(from, to) {
  const a = new Date(from).getTime()
  const b = new Date(to).getTime()
  return Math.ceil((b - a) / (1000 * 60 * 60 * 24))
}

// 导出 Excel 工具函数（基于 SheetJS）
export async function exportExcel(data, filename, headers = null) {
  const XLSX = await import('xlsx')
  const wsData = headers ? [headers, ...data] : data
  const ws = XLSX.utils.aoa_to_sheet(wsData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
  XLSX.writeFile(wb, `${filename}_${formatDate(new Date())}.xlsx`)
}
