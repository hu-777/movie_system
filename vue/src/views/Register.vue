<template>
  <main class="register-page">
    <section class="register-card">
      <router-link to="/login" class="register-brand" aria-label="返回湖叶影院登录页"><CinemaBrand /></router-link>
      <h1>创建账户</h1>
      <ol class="register-steps" aria-label="注册进度">
        <li v-for="(label, index) in ['账号设置', '密保问题', '个人资料']" :key="label" :class="{ active: step === index + 1, completed: step > index + 1 }" :aria-current="step === index + 1 ? 'step' : undefined"><span>{{ index + 1 }}</span>{{ label }}</li>
      </ol>
      <el-form v-if="step === 1" ref="formRef" :model="form" :rules="step1Rules" label-position="top" @submit.prevent="nextStep">
        <el-form-item label="账号" prop="username"><el-input v-model="form.username" :prefix-icon="User" size="large" placeholder="设置账号" autocomplete="username" /></el-form-item>
        <el-form-item label="密码" prop="password" class="password-item"><el-input v-model="form.password" :prefix-icon="Lock" size="large" placeholder="设置密码" autocomplete="new-password" show-password aria-describedby="register-password-hint" /></el-form-item>
        <p id="register-password-hint" class="password-hint">至少8位，含大小写字母、数字及特殊字符</p>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" :prefix-icon="Lock" size="large" placeholder="再次输入密码" autocomplete="new-password" show-password /></el-form-item>
        <el-button class="register-next" size="large" type="primary" native-type="submit">下一步</el-button>
      </el-form>
      <el-form v-if="step === 2" ref="formRef" :model="form" :rules="step2Rules" label-position="top" @submit.prevent="nextStep">
        <el-form-item label="密保问题" prop="securityQuestion"><el-input v-model="form.securityQuestion" :prefix-icon="HelpFilled" size="large" placeholder="设置一个用于找回密码的问题" /></el-form-item>
        <el-form-item label="密保答案" prop="securityAnswer"><el-input v-model="form.securityAnswer" :prefix-icon="Document" size="large" placeholder="输入答案" /></el-form-item>
        <div class="register-actions"><el-button size="large" @click="prevStep">上一步</el-button><el-button size="large" type="primary" native-type="submit">下一步</el-button></div>
      </el-form>
      <el-form v-if="step === 3" ref="formRef" :model="form" :rules="step3Rules" label-position="top" @submit.prevent="register">
        <div class="register-avatar"><el-upload v-model:file-list="fileList" :action="uploadUrl" :show-file-list="false" :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload" :on-error="handleAvatarError" accept=".jpg,.jpeg,.png,.gif" aria-label="上传头像"><img v-if="form.avatar" :src="form.avatar" alt="头像预览" /><el-icon v-else :size="24"><Plus /></el-icon></el-upload><p>头像选填 · JPG / PNG / GIF，最大 2MB</p></div>
        <el-form-item label="昵称" prop="name"><el-input v-model="form.name" :prefix-icon="User" size="large" placeholder="选填" /></el-form-item>
        <el-form-item label="性别"><el-radio-group v-model="form.sex"><el-radio label="男">男</el-radio><el-radio label="女">女</el-radio></el-radio-group></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" :prefix-icon="Phone" size="large" placeholder="选填" autocomplete="tel" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" :prefix-icon="Message" size="large" placeholder="选填" autocomplete="email" /></el-form-item>
        <div class="register-actions"><el-button size="large" @click="prevStep">上一步</el-button><el-button size="large" type="primary" native-type="submit">完成注册</el-button></div>
      </el-form>
      <router-link to="/login" class="back-to-login">返回登录</router-link>
    </section>
  </main>
</template>

<script setup>
  import { reactive, ref, onMounted } from "vue";
  import { User, Lock, HelpFilled, Document, Phone, Message, Plus } from "@element-plus/icons-vue";
  import request from "@/utils/request";
  import { ElMessage } from "element-plus";
  import router from "@/router";
  import CinemaBrand from "@/components/CinemaBrand.vue";
  import defaultAvatar from "@/assets/imgs/defaultAvatar.png";
  onMounted(() => { document.title = "注册 · 湖叶影院" });

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
          form.avatar = defaultAvatar;
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
.register-page { min-height: 100vh; padding: 48px 20px; display: flex; align-items: center; justify-content: center; background: #edf2f6; }
.register-card { width: 100%; max-width: 480px; padding: 36px 44px 28px; background: #fff; border: 1px solid #e2e8ed; border-radius: 14px; box-shadow: 0 8px 35px #304a6110; }
.register-brand { display: flex; justify-content: center; margin-bottom: 28px; }
.register-card h1 { margin: 0 0 25px; text-align: center; font-size: 25px; font-weight: 600; letter-spacing: -.7px; }
.register-steps { display: flex; list-style: none; padding: 0 0 23px; margin: 0 0 25px; border-bottom: 1px solid var(--line); gap: 12px; justify-content: space-between; }
.register-steps li { display: flex; align-items: center; gap: 6px; color: #8b959e; font-size: 11px; white-space: nowrap; }
.register-steps li > span { width: 21px; height: 21px; display: grid; place-items: center; border-radius: 50%; background: #f1f4f6; font-size: 10px; }
.register-steps .active { color: #4b6a82; font-weight: 600; }
.register-steps .active > span { color: #fff; background: var(--accent); }
.register-steps .completed > span { color: var(--accent); background: #e8eff4; }
:deep(.el-form-item) { margin-bottom: 23px; }
:deep(.el-form-item__label) { padding-bottom: 7px !important; font-size: 12px; color: #566470; }
:deep(.el-input__wrapper) { min-height: 44px; border-radius: 7px; }
:deep(.el-input__inner) { font-size: 13px; }
:deep(.el-input__prefix) { color: #94a1ab; }
.password-item { margin-bottom: 0; }
.password-item :deep(.el-form-item__error) { position: static; padding-top: 7px; }
.password-hint { display: grid; gap: 2px; margin: 8px 0 20px; color: #858d95; font-size: 11px; font-weight: 400; line-height: 1.7; }
.register-next { width: 100%; }
.register-actions { display: flex; gap: 12px; }.register-actions .el-button { flex: 1; margin: 0; }
.back-to-login { display: block; padding-top: 23px; margin-top: 25px; border-top: 1px solid var(--line); text-align: center; font-size: 12px; color: #718496; }
.register-avatar { text-align: center; margin-bottom: 20px; }
.register-avatar :deep(.el-upload) { height: 72px; width: 72px; border: 1px dashed #c3d0da; border-radius: 50%; overflow: hidden; color: #8196a6; background: #f5f8fa; }
.register-avatar img { width: 100%; height: 100%; object-fit: cover; }
.register-avatar p { font-size: 10px; color: #8b969e; margin-top: 9px; }
@media (max-width: 520px) { .register-page { padding: 22px 14px; align-items: flex-start; }.register-card { padding: 28px 23px 24px; }.register-steps { gap: 6px; }.register-steps li { font-size: 10px; gap: 5px; } }
</style>
