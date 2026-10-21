<template>
  <div class="account-page">
    <div class="card account-card">
      <el-form :model="data.user" label-position="top" class="account-form" :rules="data.rules" ref="formRef">
        <el-form-item label="原密码" prop="password">
          <el-input v-model="data.user.password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="data.user.newPassword" 
            show-password 
            @input="checkPasswordRequirements"
            placeholder="请输入新密码"
          />
          <!-- 密码强度指示器 -->
          <div style="margin-top: 10px;">
            <div style="font-size: 12px; color: #666; margin-bottom: 5px;">为确保账户安全，密码要求：</div>
            <div style="display: flex; flex-direction: column; gap: 3px;">
              <div class="requirement-item">
                <i v-if="data.passwordRequirements.length" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="data.passwordRequirements.length ? 'text-success' : 'text-danger'">至少8个字符</span>
              </div>
              <div class="requirement-item">
                <i v-if="data.passwordRequirements.uppercase" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="data.passwordRequirements.uppercase ? 'text-success' : 'text-danger'">包含大写字母</span>
              </div>
              <div class="requirement-item">
                <i v-if="data.passwordRequirements.lowercase" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="data.passwordRequirements.lowercase ? 'text-success' : 'text-danger'">包含小写字母</span>
              </div>
              <div class="requirement-item">
                <i v-if="data.passwordRequirements.number" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="data.passwordRequirements.number ? 'text-success' : 'text-danger'">包含数字</span>
              </div>
              <div class="requirement-item">
                <i v-if="data.passwordRequirements.specialChar" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="data.passwordRequirements.specialChar ? 'text-success' : 'text-danger'">包含特殊字符</span>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPasword">
          <el-input v-model="data.user.confirmPasword" show-password placeholder="请确认新密码" />
        </el-form-item>
        <div style="text-align: center">
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue"
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import router from "@/router";

const formRef = ref();

// 密码验证函数
const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入确认密码'))
  } else if (value !== data.user.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 密码复杂度验证函数
const validatePasswordStrength = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 8) {
    callback(new Error('密码长度不能少于8位'))
  } else if (!/[A-Z]/.test(value)) {
    callback(new Error('密码必须包含至少一个大写字母'))
  } else if (!/[a-z]/.test(value)) {
    callback(new Error('密码必须包含至少一个小写字母'))
  } else if (!/[0-9]/.test(value)) {
    callback(new Error('密码必须包含至少一个数字'))
  } else if (!/[^A-Za-z0-9]/.test(value)) {
    callback(new Error('密码必须包含至少一个特殊字符'))
  } else {
    callback()
  }
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules: {
    password: [
      { required: true, message: '请输入原密码', trigger: 'blur' },
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { validator: validatePasswordStrength, trigger: 'blur' }
    ],
    confirmPasword: [
      { required: true, message: '请确认新密码', trigger: 'blur' },
      { validator: validatePass, trigger: 'blur' },
    ]
  },
  // 密码要求状态对象
  passwordRequirements: {
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    specialChar: false
  }
})

data.user.password = '';

// 密码输入监听函数
const checkPasswordRequirements = () => {
  const password = data.user.newPassword || ''

  // 检查密码长度
  data.passwordRequirements.length = password.length >= 8

  // 检查是否包含大写字母
  data.passwordRequirements.uppercase = /[A-Z]/.test(password)

  // 检查是否包含小写字母
  data.passwordRequirements.lowercase = /[a-z]/.test(password)

  // 检查是否包含数字
  data.passwordRequirements.number = /[0-9]/.test(password)

  // 检查是否包含特殊字符
  data.passwordRequirements.specialChar = /[^A-Za-z0-9]/.test(password)
}

// 把当前修改的用户信息存储到后台数据库
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.put('/updatePassword', data.user).then(res => {
        if (res.code === '200') {
          ElMessage.success('修改密码成功')
          //清除缓存到登录的界面
          localStorage.removeItem('system-user')
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}
</script>

<style scoped>
/* 密码要求项样式 */
.requirement-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
}

/* 确保颜色更加醒目 */
.text-success {
  color: #67c23a !important;
}

.text-danger {
  color: #f56c6c !important;
}
</style>
