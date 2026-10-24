import axios from 'axios'
import { ElMessage } from 'element-plus'
const request = axios.create({ baseURL:'/api', timeout:30000, withCredentials:true })
let csrfPromise, csrfToken, reporting = false
export function clearCsrf() { csrfToken = null; csrfPromise = null }
async function csrf() {
  if (csrfToken) return csrfToken
  if (!csrfPromise) csrfPromise = axios.get('/api/auth/csrf', {withCredentials:true,timeout:30000}).then(res => {
    csrfToken = res.data.data.token; return csrfToken
  }).finally(() => { csrfPromise = null })
  return csrfPromise
}
request.interceptors.request.use(async config => {
  if (!['get','head','options'].includes((config.method || 'get').toLowerCase())) config.headers['X-CSRF-TOKEN'] = await csrf()
  return config
})
request.interceptors.response.use(response => {
  if (['/login','/logout','/updatePassword','/resetPassword'].includes(response.config.url)) clearCsrf()
  return response.data
}, async error => {
  const status = error.response?.status
  error.message = error.response?.data?.msg || error.message
  if (status === 401 && !error.config?.quietAuth && error.config?.url !== '/login') {
    clearCsrf(); window.dispatchEvent(new Event('auth-expired'))
    if (!reporting) { reporting = true; ElMessage.warning('登录已失效，请重新登录'); setTimeout(() => reporting = false, 1500) }
  }
  if (status === 403) {
    clearCsrf()
    // An expired session can fail CSRF before the authentication check.
    // Check identity without replaying the original purchase or mutation.
    try { await axios.get('/api/auth/me', {withCredentials:true, timeout:10000}) }
    catch (probe) {
      if (probe.response?.status === 401) window.dispatchEvent(new Event('auth-expired'))
    }
  }
  // Preserve the existing Result contract for validated business errors.
  if ([400, 429].includes(status) && error.response?.data?.code) return error.response.data
  if (status === 403 && !reporting) {
    reporting = true; ElMessage.error(error.message || '无权执行此操作')
    setTimeout(() => reporting = false, 1500)
  }
  return Promise.reject(error)
})
export async function uploadImage(options) {
  const body = new FormData(); body.append('file',options.file)
  const result = await request.post('/files/upload',body)
  if (result.code !== '200') throw new Error(result.msg || '上传失败')
  return result
}
export default request
