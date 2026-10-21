<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #1450aa">用户注册</div>
      
      <!-- 步骤指示器 -->
      <div class="steps">
        <div :class="['step', { 'active': step === 1, 'completed': step > 1 }]">1. 输入账号</div>
        <div :class="['step', { 'active': step === 2, 'completed': step > 2 }]">2. 回答密保问题</div>
        <div :class="['step', { 'active': step === 3 }]">3. 设置个人信息</div>
      </div>

      <!-- 第一步：输入账号，密码，确认密码 -->
      <el-form v-if="step === 1" :model="form" ref="formRef" :rules="step1Rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            :prefix-icon="Lock"
            size="large"
            v-model="form.password"
            placeholder="请输入密码"
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
          <el-input :prefix-icon="Lock" size="large" v-model="form.confirmPassword" placeholder="请确认密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="nextStep">下一步</el-button>
        </el-form-item>
      </el-form>

      <!-- 第二步：输入密保问题及答案 -->
      <el-form v-if="step === 2" :model="form" ref="formRef" :rules="step2Rules">
        <el-form-item label="密保问题：" style="margin-bottom: 15px">
          <div class="question-text">请设置一个密保问题，用于找回密码</div>
        </el-form-item>
        <el-form-item prop="securityQuestion">
          <el-input :prefix-icon="HelpFilled" size="large" v-model="form.securityQuestion" placeholder="请输入您的密保问题" />
        </el-form-item>
        <el-form-item prop="securityAnswer">
          <el-input :prefix-icon="Document" size="large" v-model="form.securityAnswer" placeholder="请输入密保问题的答案" />
        </el-form-item>
        <!-- 修改第2步的按钮容器 -->
        <el-form-item>
          <div class="button-group">
            <el-button size="large" @click="prevStep">上一步</el-button>
            <el-button size="large" type="primary" @click="nextStep">下一步</el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- 第三步：完善账号细节 -->
      <el-form v-if="step === 3" :model="form" ref="formRef" :rules="step3Rules">
        <div class="avatar-uploader">
          <el-upload
            v-model:file-list="fileList"
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :on-error="handleAvatarError"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="avatar-tip" style="text-align: center; font-size: 12px; color: #909399; margin-top: 5px;">
            请上传头像，支持 JPG、JPEG、PNG、GIF 格式，最大2MB
          </div>
        </div>
        
        <el-form-item prop="name">
          <el-input :prefix-icon="User" size="large" v-model="form.name" placeholder="请输入您的名称（选填）" />
        </el-form-item>
        
        <el-form-item>
          <div style="margin-bottom: 10px;">性别：</div>
          <el-radio-group v-model="form.sex">
            <el-radio :label="'男'">男</el-radio>
            <el-radio :label="'女'">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input :prefix-icon="Phone" size="large" v-model="form.phone" placeholder="请输入您的手机号（选填）" />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input :prefix-icon="Message" size="large" v-model="form.email" placeholder="请输入您的邮箱（选填）" />
        </el-form-item>
        
        <el-form-item>
          <div class="button-group">
            <el-button size="large" @click="prevStep">上一步</el-button>
            <el-button size="large" type="primary" @click="register">完成</el-button>
          </div>
        </el-form-item>
      </el-form>

      <div style="text-align: center; margin-top: 20px;">
        <a href="/login">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { reactive, ref } from "vue";
  import { User, Lock, HelpFilled, Document, Phone, Message, Plus } from "@element-plus/icons-vue";
  import request from "@/utils/request";
  import { ElMessage } from "element-plus";
  import router from "@/router";

  // 添加uploadUrl的定义，使用环境变量构建正确的上传路径
  const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload';

  // 当前步骤
  const step = ref(1);
  // 表单引用
  const formRef = ref();
  // 文件列表
  const fileList = ref([]);

  // 表单数据
  const form = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    securityQuestion: '',
    securityAnswer: '',
    role: 'USER',
    name: '',
    sex: '',
    phone: '',
    email: '',
    avatar: ''
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
  // 修改username验证规则，使其更加灵活
  const step1Rules = {
    username: [
      { required: true, message: '请输入账号', trigger: ['blur', 'change'] },
      {
        validator: async (rule, value, callback) => {
          if (!value) {
            return callback(new Error('请输入账号'));
          }
          try {
            // 尝试调用后端API检查账号是否已存在
            const res = await request.get('/check-username', {
              params: { username: value }
            });
            
            // 添加详细的调试日志
            console.log('账号检查完整响应:', JSON.stringify(res));
            console.log('响应状态码:', res.code);
            console.log('响应数据:', res.data);
            console.log('响应数据类型:', typeof res.data);
            
            // 简化并强化判断逻辑
            // 假设后端遵循以下逻辑：
            // 1. 如果账号可用，返回code='200'，data=false或其他非真值
            // 2. 如果账号已存在，返回code='200'，data=true或其他真值
            // 3. 如果发生错误，返回非'200'的code或抛出异常
            
            if (res.code === '200' || res.code === 200) {
              // 检查多种可能表示账号已存在的情况
              // 1. 简单值判断：如果data是布尔值true，或字符串'exists'
              // 2. 对象判断：如果data对象中有exists=true或available=false
              // 3. 反向思维：如果后端在账号存在时返回任何真值数据
              const isExists = res.data === true || 
                              res.data === 'exists' || 
                              res.data === 'true' ||
                              res.data?.exists === true || 
                              res.data?.available === false ||
                              (typeof res.data === 'object' && Object.keys(res.data).length > 0);
                              
              console.log('判断账号是否存在:', isExists);
              
              if (isExists) {
                callback(new Error('该账号已被注册，请使用其他账号'));
              } else {
                callback();
              }
            } else {
              // 后端返回了错误状态码
              console.error('后端返回错误码:', res.code, res.msg || '无错误消息');
              callback(new Error('检测账号可用性失败，请稍后重试'));
            }
          } catch (error) {
            console.error('检查账号失败（异常）:', error);
            // 尝试从error中提取更多信息
            if (error.response) {
              console.error('响应错误数据:', error.response.data);
              console.error('响应状态:', error.response.status);
            }
            callback(new Error('检测账号可用性失败，请稍后重试'));
          }
        },
        trigger: 'blur'
      }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
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
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== form.password) {
            callback(new Error('两次输入密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  };

  // 第二步规则
  const step2Rules = {
    securityQuestion: [
      { required: true, message: '请输入密保问题', trigger: 'blur' },
    ],
    securityAnswer: [
      { required: true, message: '请输入密保答案', trigger: 'blur' },
    ]
  };

  // 第三步规则（所有字段都是选填）
  const step3Rules = {
    name: [],
    sex: [],
    phone: [
      {
        pattern: /^1[3-9]\d{9}$/,
        message: '请输入正确的手机号码格式',
        trigger: 'blur',
        required: false
      }
    ],
    email: [
      {
        pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        message: '请输入正确的邮箱格式',
        trigger: 'blur',
        required: false
      }
    ]
  };

  // 检查密码要求
  const checkPasswordRequirements = () => {
    const password = form.password || ''
    
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

  // 下一步
  const nextStep = () => {
    formRef.value.validate((valid) => {
      if (valid) {
        step.value++;
      }
    });
  };

  // 上一步
  const prevStep = () => {
    if (step.value > 1) {
      step.value--;
    }
  };

  // 头像上传前验证
  const beforeAvatarUpload = (file) => {
    // 1. 检查文件扩展名
    const fileName = file.name;
    const validExtensions = ['.jpg', '.jpeg', '.png', '.gif'];
    const extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    const hasValidExtension = validExtensions.includes(extension);

    // 2. 检查MIME类型
    const validMimeTypes = ['image/jpeg', 'image/png', 'image/gif'];
    const hasValidMimeType = validMimeTypes.includes(file.type);

    // 3. 检查文件大小
    const isLt2M = file.size / 1024 / 1024 < 2;

    // 如果扩展名或MIME类型不匹配，直接拒绝
    if (!hasValidExtension || !hasValidMimeType) {
      ElMessage.error('上传头像图片只能是 JPG/JPEG/PNG/GIF 格式!');
      return false;
    }

    if (!isLt2M) {
      ElMessage.error('上传头像图片大小不能超过 2MB!');
      return false;
    }

    // 4. 更进一步：通过文件头部特征验证真实文件类型
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onloadend = (e) => {
        try {
          // 读取文件头部前几个字节
          const header = new Uint8Array(e.target.result.slice(0, 12));
          
          // 检查常见图片格式的文件头特征
          const isJPEG = header[0] === 0xFF && header[1] === 0xD8; // JPEG文件头
          const isPNG = header[0] === 0x89 && header[1] === 0x50 && header[2] === 0x4E && header[3] === 0x47; // PNG文件头
          const isGIF = header[0] === 0x47 && header[1] === 0x49 && header[2] === 0x46; // GIF文件头
          
          if (!isJPEG && !isPNG && !isGIF) {
            ElMessage.error('检测到无效的图片文件，请上传正确格式的图片!');
            resolve(false);
            return;
          }
          
          resolve(true);
        } catch (error) {
          ElMessage.error('文件验证失败，请重试!');
          resolve(false);
        }
      };
      // 只读文件的前12个字节来进行验证
      reader.readAsArrayBuffer(file.slice(0, 12));
    });
  };

  // 头像上传成功处理
  const handleAvatarSuccess = (res) => {
    if (res.code === '200') {
      form.avatar = res.data;
      ElMessage.success('头像上传成功');
    } else {
      ElMessage.error(res.msg || '头像上传失败');
    }
  };

  // 头像上传失败处理
  const handleAvatarError = (err) => {
    ElMessage.error('上传失败，请检查文件类型或网络连接');
    console.error('头像上传错误:', err);
  };

  // 注册方法
  const register = () => {
    formRef.value.validate((valid) => {
      if (valid) {
        // 如果用户没有上传头像，设置默认头像路径
        if (!form.avatar) {
          // 修改为前端资源路径
          form.avatar = '/src/assets/imgs/defaultAvatar.png';
        }
        
        // 调用后台的注册接口
        request.post('/register', form).then(res => {
          if (res.code === '200') {
            ElMessage.success('注册成功');
            router.push('/login');
          } else {
            ElMessage.error(res.msg);
          }
        }).catch(error => {
          console.error('注册失败:', error);
          ElMessage.error('网络错误，请稍后重试');
        });
      }
    });
  };
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow:hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("@/assets/imgs/fengmian.png");
  background-size: cover;
}
.login-box {
  width: 400px;
  padding: 40px 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0,.1);
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
  color: #1450aa;
  font-weight: bold;
}
.step.completed {
  color: #67c23a;
}
.question-text {
  font-size: 14px; /* 改为更小的字体，与标题大小一致 */
  color: #909399; /* 改为灰色 */
  margin-bottom: 5px;
  font-weight: normal; /* 移除加粗效果 */
  white-space: nowrap; /* 确保文字在一行显示 */
  overflow: hidden; /* 防止溢出 */
  text-overflow: ellipsis; /* 超出部分显示省略号 */
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

/* 头像上传样式 - 调整为与Person.vue一致 */
.avatar-uploader {
  text-align: center;
  margin-bottom: 20px;
}
.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}

/* 非scoped样式，用于更好地控制Element Plus组件样式 */
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%; /* 改为圆形 */
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  margin: 0 auto;
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
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
</style>