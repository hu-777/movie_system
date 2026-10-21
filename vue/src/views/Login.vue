<template>
  <main class="cinema-login">
    <section class="login-story" aria-label="湖叶影院">
      <CinemaBrand />
      <div class="story-copy"><h1>好故事，<br><span>值得相遇。</span></h1></div>
      <svg class="lake-art" viewBox="0 0 540 310" fill="none" aria-hidden="true">
        <circle cx="300" cy="125" r="91" fill="#d9e3eb" />
        <circle cx="300" cy="125" r="64" stroke="#bccbd7" />
        <path d="M54 213C151 179 266 201 467 177M35 236C185 203 333 224 510 201M74 258C200 237 344 244 475 226M149 279C252 266 324 271 414 253" stroke="#a8bdcd" stroke-width="1.2" />
        <path d="M225 198C225 121 282 75 366 77C366 149 320 203 260 203" fill="#748fa4" />
        <path d="M226 219L329 111" stroke="#edf2f6" stroke-width="2" stroke-linecap="round" />
        <path d="M250 194L251 147M276 167L319 167" stroke="#edf2f6" stroke-width="1.4" stroke-linecap="round" />
        <circle cx="113" cy="116" r="3" fill="#a8bdcd" /><circle cx="436" cy="73" r="2" fill="#a8bdcd" />
      </svg>

    </section>
    <section class="login-form-area">
      <div class="mobile-brand"><CinemaBrand /></div>
      <div class="login-form-wrap">
        <h2>欢迎回到湖叶</h2>
        <div class="role-switch" role="group" aria-label="登录身份"><button type="button" :class="{ selected: form.role === 'USER' }" :aria-pressed="form.role === 'USER'" @click="form.role = 'USER'">观影用户</button><button type="button" :class="{ selected: form.role === 'ADMIN' }" :aria-pressed="form.role === 'ADMIN'" @click="form.role = 'ADMIN'">影院管理</button></div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="login">
          <el-form-item label="账号" prop="username"><el-input v-model="form.username" size="large" placeholder="输入你的账号" autocomplete="username" :prefix-icon="User" /></el-form-item>
          <el-form-item label="密码" prop="password"><el-input v-model="form.password" size="large" placeholder="输入你的密码" autocomplete="current-password" show-password :prefix-icon="Lock" /></el-form-item>
          <div class="login-help"><router-link to="/forgotPassword">忘记密码？</router-link></div>
          <el-button type="primary" size="large" native-type="submit" :loading="submitting" class="login-submit">{{ submitting ? '正在登录' : '登录' }}<el-icon v-if="!submitting"><Right /></el-icon></el-button>
        </el-form>
        <div class="register-link">还没有账户？<router-link to="/register">注册新账户 <span aria-hidden="true">↗</span></router-link></div>
      </div>

    </section>
  </main>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Right } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CinemaBrand from '@/components/CinemaBrand.vue'
import request from '@/utils/request'
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const form = reactive({ username: '', password: '', role: 'USER' })
const rules = { username: [{ required: true, message: '请输入账号', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }
onMounted(() => { document.title = '登录 · 湖叶影院' })
const login = async () => {
  if (submitting.value || !await formRef.value.validate().catch(() => false)) return
  submitting.value = true
  try {
    const res = await request.post('/login', form)
    if (res.code === '200' && res.data?.id) {
      localStorage.setItem('system-user', JSON.stringify(res.data))
      router.push('/home')
    } else ElMessage.error(res.msg || '登录失败，请检查账号和密码')
  } catch { ElMessage.error('暂时无法连接影院，请稍后重试') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.cinema-login { min-height: 100vh; display: grid; grid-template-columns: 1fr 1fr; background: #fff; }
.login-story { margin: 18px; padding: 32px 42px 26px; border-radius: 16px; background: #edf2f6; display: flex; flex-direction: column; min-height: calc(100vh - 36px); position: relative; overflow: hidden; }
.story-copy { margin-top: auto; padding-top: 75px; position: relative; z-index: 1; }
.story-copy .eyebrow { color: #7d93a4; margin-bottom: 23px; }
.story-copy h1 { font-size: clamp(28px, 3.3vw, 48px); line-height: 1.55; font-weight: 500; letter-spacing: -1.8px; margin: 0; color: #334c60; }
.story-copy h1 span { color: #7d93a4; }
.story-copy > p:last-child { font-size: 13px; color: #7b8c9a; margin-top: 24px; letter-spacing: 1px; }
.lake-art { width: 100%; max-height: 320px; flex: 1; min-height: 170px; margin: 16px 0 24px; }
.story-footer { display: flex; align-items: center; justify-content: space-between; gap: 10px; color: #899aa7; font-size: 10px; letter-spacing: 1px; }
.story-footer span:last-child { font-size: 9px; letter-spacing: 2px; }
.login-form-area { display: flex; align-items: center; justify-content: center; padding: 80px 40px; position: relative; }
.login-form-wrap { width: 100%; max-width: 352px; }
.login-form-wrap h2 { font-size: 29px; font-weight: 600; letter-spacing: -1px; margin: 0 0 30px; }
.login-subtitle { color: var(--muted); font-size: 13px; margin: 0 0 30px; }
.role-switch { padding: 4px; background: #f1f4f6; border: 1px solid #e9edf0; border-radius: 8px; display: flex; margin-bottom: 28px; }
.role-switch button { flex: 1; border: 0; background: none; border-radius: 5px; padding: 9px 6px; color: #81909d; font-size: 12px; cursor: pointer; }
.role-switch button.selected { background: #fff; color: #405d74; box-shadow: 0 1px 4px #1d334114; font-weight: 600; }
:deep(.el-form-item__label) { color: #4f5f6d; font-size: 12px; font-weight: 500; padding-bottom: 8px !important; }
:deep(.el-form-item) { margin-bottom: 23px; }
:deep(.el-input__wrapper) { min-height: 45px; }
:deep(.el-input__inner) { font-size: 13px; }
:deep(.el-input__prefix) { margin-right: 6px; color: #9aa6af; }
.login-help { display: flex; align-items: center; justify-content: flex-end; color: #98a1a9; font-size: 11px; margin: 2px 0 25px; }
.login-submit { width: 100%; height: 45px; }
.login-submit .el-icon { margin-left: 13px; }
.register-link { border-top: 1px solid var(--line); padding-top: 23px; margin-top: 28px; text-align: center; font-size: 12px; color: #8a969f; }
.register-link a { margin-left: 8px; font-weight: 500; }
.login-footer { position: absolute; bottom: 35px; color: #a0a8af; font-size: 11px; letter-spacing: 1px; }
.mobile-brand { display: none; }
@media (min-width: 1500px) { .login-story { padding: 45px 65px 32px; } }
@media (max-width: 760px) {
  .cinema-login { display: block; } .login-story { display: none; }
  .login-form-area { min-height: 100vh; padding: 125px 28px 90px; }
  .mobile-brand { display: block; position: absolute; top: 32px; left: 28px; }
  .login-form-wrap h2 { font-size: 27px; }
}
</style>
