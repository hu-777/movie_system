<template>
  <div>

    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="头像">
          <template #default="scope">
            <el-image :src="scope.row.avatar" style="width: 40px; height: 40px; border-radius: 50%"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="role">
          <template #default="scope">
            <span v-if="scope.row.role === 'ADMIN'">管理员</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="160">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination background layout="prev, pager, next, jumper" v-model:page-size="data.pageSize"
                     v-model:current-page="data.pageNum" :total="data.total"
                     @current-change="handlePageChange"/>
      <!-- layout 添加了 jumper 参数，并增加了 current-change 事件，实现页面跳转 -->
    </div>

    <el-dialog title="信息" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="头像" prop="avatar">
          <el-upload 
            :action="uploadUrl" 
            list-type="picture" 
            :on-success="handleImgSuccess" 
            :on-error="handleImgError"
            :before-upload="beforeAvatarUpload"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <el-button type="primary">上传图片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 JPG/JPEG/PNG/GIF 格式的图片，且不超过 2MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="data.form.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

// 文件上传的接口地址
const uploadUrl = import.meta.env.VITE_BASE_URL + '/files/upload'

const data = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null
})

// 分页查询
const load = () => {
  request.get('/admin/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}

// 新增
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/admin/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 新增了切换页面的函数
const handlePageChange = (newPageNum) => {
  data.pageNum = newPageNum;
  load();
};


// 编辑保存
const update = () => {
  request.put('/admin/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  // data.form有id就是更新，没有就是新增
  data.form.id ? update() : add()
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/admin/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.name = null
  load()
}

// 处理文件上传的钩子
const handleImgSuccess = (res) => {
  if (res.code === '200') {
    data.form.avatar = res.data  // res.data就是文件上传返回的文件路径
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.msg || '图片上传失败')
  }
}

// 处理文件上传失败的钩子
const handleImgError = (err, file, fileList) => {
  ElMessage.error('上传失败，请检查文件类型或网络连接')
  console.error('文件上传错误:', err)
}

// 增强的文件上传前验证函数
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

  // 4. 更进一步：通过文件头部特征验证真实文件类型（需要Promise）
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
}

load()
</script>