<template><div class="management-page">
    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe>
        <el-table-column min-width="120" label="电影名称" prop="name"></el-table-column>
        <el-table-column min-width="120" label="上映时间">
          <template #default="scope">
            <span>{{ formatTime(scope.row.showtime) }}</span>
          </template>
        </el-table-column>
        <el-table-column min-width="120" label="放映位置" prop="position"></el-table-column>
        <el-table-column min-width="120" label="推荐理由" prop="comment"></el-table-column>
        <el-table-column min-width="120" label="上票数量" prop="tickets"></el-table-column>
        <el-table-column min-width="120" label="类型" prop="type"></el-table-column>
        <el-table-column min-width="120" label="票价" prop="price"></el-table-column>
        <el-table-column min-width="120" label="海报" prop="img">
          <template #default="scope">
            <el-image style= "width: 50px; height: 50px; border-radius: 5px" :src="scope.row.img"
                      :preview-src-list="[scope.row.img]" preview-teleported="true"></el-image>
          </template>
        </el-table-column>

        <el-table-column label="操作" header-align="center" width="260"> <template #default="scope">
          <el-button type="warning" @click="handleScreening(scope.row)">安排场次</el-button>

          <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" @current-change="load"/>
    </div>

    <el-dialog title="影片信息" width="600px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-position="top" class="management-form">
        <el-form-item label="电影名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="推荐理由" prop="comment">
          <el-input type="textarea" v-model="data.form.comment" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="海报" prop="img">
          <el-upload :http-request="uploadImage" list-type="picture" :on-success="handleImgSuccess">
            <el-button type="primary">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <!-- 移除余票数手动输入的部分 -->
        <el-form-item label="类型">
          <el-select v-model="data.form.type" placeholder="请选择类型" style="width: 100%">
            <el-option
                v-for="item in data.categoryList"
                :key="item.id"
                :label="item.name"
                :value="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="票价">
          <el-input-number :min="1" v-model="data.form.price" ></el-input-number>
        </el-form-item>
        <el-form-item label="厅号">
          <el-input v-model="data.form.hallNumber" placeholder="请输入厅号（如：1）" />
        </el-form-item>
        <el-form-item label="厅类型">
          <el-select v-model="data.form.hallType" placeholder="请选择厅类型" style="width: 100%">
            <el-option label="普通厅" value="普通厅" />
            <el-option label="杜比厅" value="杜比厅" />
            <el-option label="激光厅" value="激光厅" />
            <el-option label="IMAX厅" value="IMAX厅" />
          </el-select>
        </el-form-item>
        <el-form-item label="上映日期">
          <div class="block">
            <el-date-picker
                v-model="data.form.showtime"
                type="datetime"
                placeholder="请选择上映日期"
                format="YYYY-MM-DD HH:mm"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog title="安排排片" v-model="screeningVisible" width="600px" :close-on-click-modal="false">
      <el-form :model="screeningForm" label-position="top" class="management-form">
        <el-form-item label="电影名称">
          <el-input v-model="screeningForm.goodsName" disabled></el-input>
        </el-form-item>
        <el-form-item label="影厅名称">
          <el-select v-model="screeningForm.hallName" placeholder="请选择影厅" style="width: 100%">
            <el-option label="1号厅 (激光厅)" value="1号厅"></el-option>
            <el-option label="2号厅 (IMAX厅)" value="2号厅"></el-option>
            <el-option label="3号厅 (普通厅)" value="3号厅"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="语言版本">
          <el-select v-model="screeningForm.screenType" placeholder="请选择语言" style="width: 100%">
            <el-option label="国语2D" value="国语2D"></el-option>
            <el-option label="国语3D" value="国语3D"></el-option>
            <el-option label="原版2D" value="原版2D"></el-option>
            <el-option label="原版IMAX" value="原版IMAX"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="screeningForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="screeningForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="票价">
          <el-input-number v-model="screeningForm.price" :min="0" :precision="0"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="screeningVisible = false">取 消</el-button>
      <el-button type="primary" @click="saveScreening">确 定</el-button>
    </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { uploadImage } from '@/utils/request'
import { ref, reactive, onMounted } from "vue"
import { ElMessage, ElMessageBox } from 'element-plus' // 注意加上 ElMessageBox
import request from '@/utils/request'
import dayjs from 'dayjs'

const data = reactive({
  tableData: [],
  goodsList: [],
  categoryList: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  goodsName: '',
  form: {},
  formVisible: false,
  uploadUrl: '/api/upload'
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 分页查询
const load = () => {
  request.get('/goodsStock/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      goodsName: data.goodsName
    }
  }).then(res => {
    data.tableData = res.data?.list || [];
    data.total = res.data?.total || 0;
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
    // 修复字段名调用
    if (row.position) {
        const match = row.position.match(/(\d+)号(.+厅)/);
        if (match) {
            data.form.hallNumber = match[1];
            data.form.hallType = match[2];
        }
    }
    data.formVisible = true
}
// 新增保存
const add = () => {
    // 组合厅号和厅类型为position字段
    if (data.form.hallNumber && data.form.hallType) {
        data.form.position = `${data.form.hallNumber}号${data.form.hallType}`;
    }
    request.post('/goodsStock/add', data.form).then(res => {
        if (res.code === '200') {
            load()
            ElMessage.success('操作成功')
            data.formVisible = false
        } else {
            ElMessage.error(res.msg)
        }
    })
}

// 编辑保存
const update = () => {
  // 组合厅号和厅类型为position字段
  if (data.form.hallNumber && data.form.hallType) {
    data.form.position = `${data.form.hallNumber}号${data.form.hallType}`;
  }
  // 确保不传递tickets字段
  const { tickets, ...rest } = data.form;
  request.put('/goodsStock/update', rest).then(res => {
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
  if (data.form.id) {
    update()
  } else {
    add()
  }
}

// 删除
const handleDelete = (id) => {
  request.delete(`/goodsStock/delete/${id}`).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 上传图片成功回调
const handleImgSuccess = (response, file, fileList) => {
  data.form.img = file.url
}

// 获取商品列表
const loadGoodsList = () => {
  request.get('/goods/selectAll').then(res => {
    data.goodsList = res.data
  })
}

// 获取分类列表
const loadCategoryList = () => {
  request.get('/category/selectAll').then(res => {
    data.categoryList = res.data || [];
  }).catch(err => {
    console.error('获取分类失败:', err);
  });
};
// === 新增：场次相关变量 ===
const screeningVisible = ref(false)
const screeningForm = reactive({})

// === 新增：点击“安排场次”按钮触发 ===
const handleScreening = (row) => {
  // 清空表单
  Object.keys(screeningForm).forEach(key => delete screeningForm[key])

  // 填入当前选中的电影信息
  screeningForm.goodsId = row.id
  screeningForm.goodsName = row.name
  // 默认票价设为电影原价
  screeningForm.price = row.price

  screeningVisible.value = true
}

// === 新增：保存场次 ===
const saveScreening = () => {
  // 简单的非空校验
  if (!screeningForm.hallName || !screeningForm.startTime || screeningForm.price == null) {
    ElMessage.warning('请填写完整场次信息')
    return
  }

  request.post('/screening/add', screeningForm).then(res => {
    if (res.code === '200') {
      ElMessage.success('场次安排成功，座位已自动生成')
      screeningVisible.value = false
      load() // 重新加载列表（因为我们刚才在后端加了同步更新余票逻辑，所以这里刷新能看到余票变了）
    } else {
      ElMessage.error(res.msg)
    }
  })
}
onMounted(() => {
  loadCategoryList();
  loadGoodsList()
  load()
})
</script>

<style scoped>

</style>
