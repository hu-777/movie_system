<template>
  <div>
    <div style="height: 60px; background-color: #fff; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <!-- 顶部导航内容保持不变，添加折叠按钮 -->
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/LOGO2.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #7353ba">假娃电影院</div>
          <!-- 移动折叠按钮到这里 -->
          <div 
            style="
              margin-left: 15px;
              cursor: pointer;
              display: flex;
              justify-content: center;
              align-items: center;
              transition: all 0.3s ease;
              user-select: none;
            "
            @click="toggleCollapse"
            title="折叠/展开侧边栏"
          >
            <div 
              :style="{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                backgroundColor: 'transparent',
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                color: '#1967e3',
                transition: 'all 0.2s ease',
                userSelect: 'none'
              }"
              class="toggle-btn"
              unselectable="on"
              onselectstart="return false;"
            >
              <el-icon v-if="data.collapsed"><Expand /></el-icon>
              <el-icon v-else><Fold /></el-icon>
            </div>
          </div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center; cursor: pointer;" @click="goToProfile">
        <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
        <span style="margin-left: 5px">{{ data.user.name }}</span>
      </div>
    </div>

    <div style="display: flex; height: calc(100vh - 60px); overflow: hidden;">
      <!-- 侧边栏动态宽度 -->
      <div 
        :style="{ width: data.collapsed ? '60px' : '200px', borderRight: '1px solid #ddd', transition: 'width 0.3s ease', position: 'relative', height: '100%' }"
      >
        <!-- 菜单容器，移除滚动监听 -->
        <div 
          ref="menuContainer"
          style="position: relative; height: 100%; overflow-y: auto; overflow-x: hidden; padding-bottom: 20px;"
        >
          <!-- 菜单项包装器 -->
          <div class="menu-items-wrapper">
            <el-menu
                router
                style="border: none; height: 100%;"
                :default-active="router.currentRoute.value.path"
                :default-openeds="['1', '2']"
                :collapse="data.collapsed"
                :collapse-transition="true"
                background-color="#fff"
                text-color="#333"
                active-text-color="#1967e3"
            >
              <!-- 菜单内容保持不变 -->
              <el-menu-item index="/home">
                <el-icon><HomeFilled /></el-icon>
                <template #title>系统首页</template>
              </el-menu-item>

              <el-menu-item index="/buy" v-if="data.user.role === 'USER'">
                <el-icon><Film /></el-icon>
                <template #title>电影购买</template>
              </el-menu-item>

              <!-- 修改菜单部分，将订单管理和发票管理合并 -->
              <el-menu-item index="/orders">
                <el-icon><Tickets/></el-icon>
                <template #title>订单管理</template>
              </el-menu-item>
              
              <el-menu-item index="/charts" v-if="data.user.role === 'ADMIN'">
                <el-icon><Histogram /></el-icon>
                <template #title>数据大屏</template>
              </el-menu-item>

              <el-sub-menu index="1" v-if="data.user.role === 'ADMIN'">
                <template #title>
                  <el-icon><Camera /></el-icon>
                  <span>电影管理</span>
                </template>
                <el-menu-item index="/category">
                  <el-icon><VideoCamera /></el-icon>
                  <template #title>电影分类管理</template>
                </el-menu-item>
                <el-menu-item index="/goods">
                  <el-icon><Film /></el-icon>
                  <template #title>电影管理</template>
                </el-menu-item>

                <el-menu-item index="/goodsStock">
                  <el-icon><VideoCameraFilled /></el-icon>
                  <template #title>电影上架管理</template>
                </el-menu-item>
              </el-sub-menu>

              <el-menu-item index="/notice" v-if="data.user.role === 'ADMIN'">
                <el-icon><Bell /></el-icon>
                <template #title>系统公告管理</template>
              </el-menu-item>

              <el-sub-menu index="2" v-if="data.user.role === 'ADMIN'">
                <template #title>
                  <el-icon><Memo /></el-icon>
                  <span>用户管理</span>
                </template>
                <el-menu-item index="/admin">
                  <el-icon><User /></el-icon>
                  <template #title>管理员信息</template>
                </el-menu-item>
                <el-menu-item index="/user">
                  <el-icon><User /></el-icon>
                  <template #title>普通用户信息</template>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 底部菜单项，移除渐变效果类名 -->
              <el-menu-item index="/person">
                <el-icon><User /></el-icon>
                <template #title>个人资料</template>
              </el-menu-item>
              <el-menu-item index="/password">
                <el-icon><Lock /></el-icon>
                <template #title>修改密码</template>
              </el-menu-item>
              <el-menu-item index="login" @click="logout">
                <el-icon><SwitchButton /></el-icon>
                <template #title>退出系统</template>
              </el-menu-item>
            </el-menu>
          </div>
        </div>
      </div>

      <!-- 内容区域保持自适应并优化滚动 -->
      <div style="flex: 1; overflow-y: auto; background-color: #f8f8ff; padding: 10px; position: relative; min-width: 0;">
        <router-view @updateUser="updateUser" />
      </div>
    </div>
  </div>
</template>

<script setup>
// 导入Element Plus的图标
import { 
  HomeFilled, 
  Film, 
  Tickets, 
  Histogram, 
  Camera, 
  VideoCamera, 
  VideoCameraFilled, 
  Bell, 
  Memo, 
  User, 
  Lock, 
  SwitchButton,
  Fold,  // 添加折叠图标
  Expand // 添加展开图标
} from '@element-plus/icons-vue';
import { reactive, ref, onMounted } from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  collapsed: false // 添加折叠状态变量
})

// 添加ref引用
const menuContainer = ref(null);

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}

// 简化的切换折叠状态方法
const toggleCollapse = () => {
  data.collapsed = !data.collapsed;
}

// 页面加载时初始化
onMounted(() => {
  // 移除蒙板相关初始化
});

const logout = () => {
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
  router.push('/login')
}

const goToProfile = () => {
  router.push('/person')
}
</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #e0edfd !important;
}
.el-menu-item:hover {
  color: #1967e3;
}
/* 将:deep combinator改为:deep()函数语法 */
:deep(th) {
  color: #333;
}
/* 优化折叠过渡效果 */
.el-menu {
  transition: all 0.3s ease;
}
/* 调整图标间距 */
.el-menu-item .el-icon {
  margin-right: 5px;
}
/* 优化按钮悬停效果 */
.toggle-btn:hover {
  background-color: rgba(25, 103, 227, 0.2) !important;
  transform: scale(1.05);
}
/* 确保按钮本身的样式 */
.toggle-btn {
  transition: all 0.2s ease;
}
/* 确保内容区域不出现横向滚动条 - 修改:deep()语法 */
:deep(.el-scrollbar__wrap) {
  overflow-x: hidden !important;
}
/* 优化内容页面容器 - 修改:deep()语法 */
:deep(.el-main) {
  padding: 10px !important;
  overflow-x: hidden;
}
/* 优化表格样式，避免出现滚动条 - 修改:deep()语法 */
:deep(.el-table) {
  width: 100% !important;
  overflow-x: hidden !important;
}
:deep(.el-table__body-wrapper) {
  overflow-x: hidden !important;
}

/* 剩余样式保持不变 */
/* 禁止文本选择和光标显示 */
.no-select {
  user-select: none !important;
  -webkit-user-select: none !important;
  -moz-user-select: none !important;
  -ms-user-select: none !important;
  outline: none !important;
}

.no-select:focus {
  outline: none !important;
}

/* 防止按钮内部文本被选中 */
.toggle-icon {
  user-select: none !important;
  -webkit-user-select: none !important;
  -moz-user-select: none !important;
  -ms-user-select: none !important;
  outline: none !important;
  cursor: pointer;
}

/* 菜单项包装器 */
.menu-items-wrapper {
  position: relative;
  z-index: 1;
}
</style>

<!-- 全局样式调整，影响所有页面 -->
<style>
/* 去除body的默认边距和溢出 */
body {
  margin: 0;
  padding: 0;
  overflow: hidden;
}
/* 确保根容器占满视口 */
#app {
  height: 100vh;
  overflow: hidden;
}
/* 优化内容区域滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 全局禁止按钮文本选择 */
button, .button-like {
  user-select: none !important;
  -webkit-user-select: none !important;
  -moz-user-select: none !important;
  -ms-user-select: none !important;
  outline: none !important;
}

button:focus, .button-like:focus {
  outline: none !important;
}
</style>