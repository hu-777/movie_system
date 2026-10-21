<template>
  <div class="account-page">
    <div class="card account-card">
      <el-form :model="data.user" label-position="top" class="account-form">
        <div style="margin: 20px 0; text-align: center">
          <el-upload 
            :show-file-list="false" 
            class="avatar-uploader" 
            :action="uploadUrl" 
            :on-success="handleFileUpload" 
            :on-error="handleFileError"
            :before-upload="beforeAvatarUpload"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>
        <el-form-item label="账号">
          <el-input disabled v-model="data.user.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="data.user.name" autocomplete="off" />
        </el-form-item>
        <div v-if="data.user.role === 'USER'">
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="data.user.sex">
              <el-radio label="男"></el-radio><el-radio label="女"></el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="data.user.phone" autocomplete="off" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="data.user.email" autocomplete="off" />
          </el-form-item>
        </div>
        <div style="text-align: center">
          <el-button type="primary" @click="save">保存</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue"
import request from "@/utils/request";
import {ElMessage} from "element-plus";

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const handleFileUpload = (file) => {
  data.user.avatar = file.data
}


const emit = defineEmits(["updateUser"]);

// 更新用户信息的函数
const updateUser = (url) => {
  request.put(url, data.user)
      .then(res => {
        if (res.code === '200') {
          ElMessage.success('更新成功');
          // 更新缓存中的用户信息
          localStorage.setItem('system-user', JSON.stringify(data.user));
          emit('updateUser');
        } else {
          ElMessage.error(res.msg);
        }
      })
      .catch(error => {
        ElMessage.error('网络请求失败，请稍后再试');
        console.error(error);  // 打印详细错误信息，便于调试
      });
};

// 保存用户信息
const save = () => {
  const url = data.user.role === 'ADMIN' ? '/admin/update' : '/user/update';
  updateUser(url);
};


// 添加文件上传前的类型和大小验证
// 增强的文件上传前验证函数
const beforeAvatarUpload = (file) => {
  // 与上面User.vue中完全相同的验证逻辑
  const fileName = file.name;
  const validExtensions = ['.jpg', '.jpeg', '.png', '.gif'];
  const extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
  const hasValidExtension = validExtensions.includes(extension);

  const validMimeTypes = ['image/jpeg', 'image/png', 'image/gif'];
  const hasValidMimeType = validMimeTypes.includes(file.type);

  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!hasValidExtension || !hasValidMimeType) {
    ElMessage.error('上传头像图片只能是 JPG/JPEG/PNG/GIF 格式!');
    return false;
  }

  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
    return false;
  }

  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onloadend = (e) => {
      try {
        const header = new Uint8Array(e.target.result.slice(0, 12));
        
        const isJPEG = header[0] === 0xFF && header[1] === 0xD8;
        const isPNG = header[0] === 0x89 && header[1] === 0x50 && header[2] === 0x4E && header[3] === 0x47;
        const isGIF = header[0] === 0x47 && header[1] === 0x49 && header[2] === 0x46;
        
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
    reader.readAsArrayBuffer(file.slice(0, 12));
  });
}

// 添加错误处理函数
const handleFileError = (err, file, fileList) => {
  ElMessage.error('上传失败，请检查文件类型或网络连接')
  console.error('头像上传错误:', err)
}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>
