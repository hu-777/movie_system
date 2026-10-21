<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #56738b">找回密码</div>
      
      <!-- 步骤指示器 -->
      <div class="steps">
        <div :class="['step', { 'active': step === 1, 'completed': step > 1 }]">1. 输入账号</div>
        <div :class="['step', { 'active': step === 2, 'completed': step > 2 }]">2. 回答密保问题</div>
        <div :class="['step', { 'active': step === 3 }]">3. 设置新密码</div>
      </div>

      <!-- 第一步：输入用户名 -->
      <el-form v-if="step === 1" :model="form" ref="formRef" :rules="rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="getQuestion">下一步</el-button>
        </el-form-item>
      </el-form>

      <!-- 第二步：回答密保问题 -->
      <el-form v-if="step === 2" :model="form" ref="formRef" :rules="questionRules">
        <el-form-item label="密保问题：" style="margin-bottom: 15px">
          <div class="question-text">{{ securityQuestion }}</div>
        </el-form-item>
        <el-form-item prop="securityAnswer">
          <el-input size="large" v-model="form.securityAnswer" placeholder="请输入答案" />
        </el-form-item>
        <!-- 修改第2步的按钮容器 -->
        <el-form-item>
          <div class="button-group">
            <el-button size="large" @click="step = 1">上一步</el-button>
            <el-button size="large" type="primary" @click="verifyAnswer">下一步</el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- 第三步：设置新密码 -->
      <el-form v-if="step === 3" :model="form" ref="formRef" :rules="passwordRules">
        <el-form-item prop="newPassword">
          <el-input
            :prefix-icon="Lock"
            size="large"
            v-model="form.newPassword"
            placeholder="请输入新密码"
            show-password
            @input="checkPasswordRequirements"
          />
          <!-- 密码强度指示器 -->
          <div style="margin-top: 10px;">
            <div style="font-size: 12px; color: #666; margin-bottom: 5px;">密码要求：</div>
            <div style="display: flex; flex-direction: column; gap: 6px;">
              <div class="requirement-item">
                <i v-if="passwordRequirements.length" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="passwordRequirements.length ? 'text-success' : 'text-danger'">至少8个字符</span>
              </div>
              <div class="requirement-item">
                <i v-if="passwordRequirements.uppercase" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="passwordRequirements.uppercase ? 'text-success' : 'text-danger'">包含大写字母</span>
              </div>
              <div class="requirement-item">
                <i v-if="passwordRequirements.lowercase" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="passwordRequirements.lowercase ? 'text-success' : 'text-danger'">包含小写字母</span>
              </div>
              <div class="requirement-item">
                <i v-if="passwordRequirements.number" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="passwordRequirements.number ? 'text-success' : 'text-danger'">包含数字</span>
              </div>
              <div class="requirement-item">
                <i v-if="passwordRequirements.specialChar" class="el-icon-check text-success" style="font-size: 16px;"></i>
                <i v-else class="el-icon-close text-danger" style="font-size: 16px;"></i>
                <span :class="passwordRequirements.specialChar ? 'text-success' : 'text-danger'">包含特殊字符</span>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="form.confirmPassword" placeholder="请确认新密码" show-password />
        </el-form-item>
        <el-form-item>
          <div class="button-group">
            <el-button size="large" @click="step = 2">上一步</el-button>
            <el-button size="large" type="primary" @click="resetPassword">完成</el-button>
          </div>
        </el-form-item>
      </el-form>

      <div style="text-align: center; margin-top: 20px;">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { reactive, ref } from "vue";
  import { User, Lock } from "@element-plus/icons-vue";
  import request from "@/utils/request";
  import { ElMessage } from "element-plus";
  import router from "@/router";

  // 当前步骤
  const step = ref(1);
  // 密保问题
  const securityQuestion = ref('');
  // 表单引用
  const formRef = ref();

  // 表单数据
  const form = reactive({
    username: '',
    securityAnswer: '',
    newPassword: '',
    confirmPassword: ''
  });

  // 密码要求状态对象
  const passwordRequirements = reactive({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    specialChar: false
  });

  // 第一步规则
  const rules = {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
    ],
  };

  // 第二步规则
  const questionRules = {
    securityAnswer: [
      { required: true, message: '请输入答案', trigger: 'blur' },
    ],
  };

  // 第三步规则
  const passwordRules = {
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value.length < 8) {
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
        },
        trigger: 'blur'
      }
    ],
    confirmPassword: [
      { required: true, message: '请确认新密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== form.newPassword) {
            callback(new Error('两次输入密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
  };

  // 检查密码要求
  const checkPasswordRequirements = () => {
    const password = form.newPassword
    if (!password) {
      passwordRequirements.length = false
      passwordRequirements.uppercase = false
      passwordRequirements.lowercase = false
      passwordRequirements.number = false
      passwordRequirements.specialChar = false
      return
    }
    
    // 检查密码长度
    passwordRequirements.length = password.length >= 8
    
    // 检查是否包含大写字母
    passwordRequirements.uppercase = /[A-Z]/.test(password)
    
    // 检查是否包含小写字母
    passwordRequirements.lowercase = /[a-z]/.test(password)
    
    // 检查是否包含数字
    passwordRequirements.number = /[0-9]/.test(password)
    
    // 检查是否包含特殊字符
    passwordRequirements.specialChar = /[^A-Za-z0-9]/.test(password)
  };

  // 获取密保问题
  const getQuestion = () => {
    formRef.value.validate((valid) => {
      if (valid) {
        request.post('/getSecurityQuestion', { username: form.username }).then(res => {
          if (res.code === '200') {
            securityQuestion.value = res.data
            step.value = 2
          } else {
            ElMessage.error(res.msg || '获取密保问题失败')
          }
        }).catch(error => {
          ElMessage.error('网络错误，请稍后重试')
          console.error('获取密保问题失败:', error)
        })
      }
    })
  };

  // 修改验证密保答案的方法
  const verifyAnswer = () => {
    formRef.value.validate((valid) => {
      if (valid) {
        // 添加向后端验证答案的逻辑
        request.post('/verifySecurityAnswer', {
          username: form.username,
          securityAnswer: form.securityAnswer
        }).then(res => {
          if (res.code === '200') {
            // 答案正确，进入下一步
            step.value = 3
          } else {
            // 答案错误，显示提示
            ElMessage.error(res.msg || '密保答案错误，请重新输入')
          }
        }).catch(error => {
          // 优化错误提示，不再简单显示网络错误
          ElMessage.error('密保答案错误，请重新输入')
          console.error('验证密保答案失败:', error)
        })
      }
    })
  };

  // 重置密码
  const resetPassword = () => {
    formRef.value.validate((valid) => {
      if (valid) {
        request.post('/resetPassword', {
          username: form.username,
          securityAnswer: form.securityAnswer,
          newPassword: form.newPassword
        }).then(res => {
          if (res.code === '200') {
            ElMessage.success('密码重置成功，请使用新密码登录')
            router.push('/login')
          } else {
            ElMessage.error(res.msg || '密码重置失败')
          }
        }).catch(error => {
          ElMessage.error('网络错误，请稍后重试')
          console.error('重置密码失败:', error)
        })
      }
    })
  };
</script>

<style scoped>
.login-container {
  min-height: 100dvh;
  padding: 32px 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(ellipse at top left, #e5edf3, #f5f7f9 65%);
  background-size: cover;
}
.login-box {
  width: min(100%, 460px);
  padding: 40px 30px;
  border-radius: 16px;
  border: 1px solid #e5e9ed; box-shadow: 0 16px 50px rgba(35,55,75,.05);
  background-color: rgba(255, 255, 255, .9);
}
.steps {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
}
.step {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  color: #999;
  font-size: 14px;
}
.step.active {
  color: #56738b;
  font-weight: bold;
}
.step.completed {
  color: #67c23a;
}
.question-text {
  font-size: 16px;
  color: #333;
  font-weight: 500;
  margin-bottom: 5px;
}

/* 添加新的按钮组样式 */
.button-group {
  display: flex;
  width: 100%;
  gap: 10px;
}

.button-group .el-button {
  flex: 1;
  height: 44px;
  font-size: 16px;
}

.requirement-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
}
.text-success {
  color: #67c23a !important;
}
.text-danger {
  color: #f56c6c !important;
}
</style>