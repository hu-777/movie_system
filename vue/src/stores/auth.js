import { ref } from 'vue'
import request from '@/utils/request'
export const currentUser = ref(null)
let pending
localStorage.removeItem('system-user')
export const setUser = user => { currentUser.value = user }
export const getUser = () => currentUser.value || {}
export async function refreshUser() {
  if (!pending) pending = request.get('/auth/me', { quietAuth: true }).then(res => {
    currentUser.value = res.code === '200' ? res.data : null
    return currentUser.value
  }).catch(error => {
    currentUser.value = null
    if (error.response?.status !== 401) throw error
    return null
  }).finally(() => { pending = null })
  return pending
}
window.addEventListener('auth-expired', () => { currentUser.value = null })
