// Axios 封装 - API 地址通过环境变量控制
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 生产部署到 Railway 时设置 VITE_API_BASE_URL=https://gym-backend.up.railway.app
// 本地开发走 Vite 代理 /api
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL,
  timeout: 15000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    if (error.response?.status === 404) {
      ElMessage.error('接口不存在，请检查后端服务是否启动')
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('无法连接后端服务')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
