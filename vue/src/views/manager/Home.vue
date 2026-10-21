<template>
  <div class="home-container">
    <div class="recommended-section">
      <div class="section-title">热映电影</div>
      <div class="scroll-container">
        <div class="scroll-content">
          <div v-for="item in data.filmList" :key="item.id" class="film-item">
            <!-- 移除排序数字 -->
            <img :src="item.img" alt="" class="film-image">
            <div class="film-info">
              <div class="film-name">{{item.name}}</div>
              <div class="film-comment line3">{{item.comment}}</div>
              <div class="film-type line3">{{item.type}}</div>
              <!-- 新增位置显示 -->
              <div class="film-position" v-if="item.position">放映位置：{{item.position}}</div>
              <div class="film-tickets">余票: {{item.tickets}}</div>
            </div>
          </div>
          <!-- 复制一份用于无缝滚动 -->
          <div v-for="item in data.filmList" :key="`copy-${item.id}`" class="film-item">
            <img :src="item.img" alt="" class="film-image">
            <div class="film-info">
              <div class="film-name">{{item.name}}</div>
              <div class="film-comment line3">{{item.comment}}</div>
              <div class="film-type line3">{{item.type}}</div>
              <div class="film-tickets">余票: {{item.tickets}}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="notice-section">
      <div class="section-title">系统公告</div>
      <div class="scroll-container notice-scroll">
        <div class="scroll-content notice-content">
          <el-timeline>
            <el-timeline-item :timestamp="item.time" placement="top" v-for="item in data.noticeList" :key="item.id">
              <div class="notice-card">
                <h4>{{ item.title }}</h4>
                <p>{{ item.content }}</p>
              </div>
            </el-timeline-item>
            <!-- 复制一份用于无缝滚动 -->
            <el-timeline-item :timestamp="item.time" placement="top" v-for="item in data.noticeList" :key="`copy-${item.id}`">
              <div class="notice-card">
                <h4>{{ item.title }}</h4>
                <p>{{ item.content }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  padding: 10px;
  height: calc(100vh - 20px);
}

.recommended-section {
  flex: 1;
  min-width: 300px;
  max-width: 50%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notice-section {
  flex: 1;
  min-width: 300px;
  max-width: 50%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.section-title {
  padding-left: 20px;
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 20px;
}

/* 滚动容器样式 */
.scroll-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 推荐观看滚动内容 */
.scroll-content {
  animation: scrollUp 60s linear infinite;
}

/* 公告滚动内容 - 速度稍慢 */
.notice-content {
  animation: scrollUp 50s linear infinite;
}

/* 滚动动画 */
@keyframes scrollUp {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-50%);
  }
}

/* 鼠标悬停时暂停滚动 */
.scroll-container:hover .scroll-content {
  animation-play-state: paused;
}

.film-item {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* 改进的鼠标悬停效果 - 更突出显示 */
.film-item:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #ffffff 0%, #f0f4f8 100%);
}

/* 添加悬停时的边框高亮效果 */
.film-item:hover::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #4CAF50, #2196F3);
}

.film-image {
  width: 100px;
  height: auto;
  max-height: 150px;
  object-fit: cover;
  border-radius: 6px;
  transition: transform 0.3s ease;
}

.film-item:hover .film-image {
  transform: scale(1.05);
}

.film-info {
  flex: 1;
  min-width: 0; /* 防止flex子元素溢出 */
}

.film-name {
  font-size: 1.2rem;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
  transition: color 0.3s ease;
}

.film-item:hover .film-name {
  color: #2196F3;
}

.film-comment,
.film-type {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 修改位置显示样式，使用浅蓝色背景和深蓝色文字 */
/* 修改放映位置和余票数样式，使其风格一致 */
/* 修改放映位置样式，设置为并排显示 */
.film-position {
  color: #1565C0; /* 深蓝色文字 */
  font-weight: 600;
  font-size: 0.9rem;
  margin-right: 15px; /* 右侧添加水平间隙 */
  padding: 6px 12px;
  background-color: #E3F2FD; /* 浅蓝色背景 */
  border-radius: 4px;
  display: inline-block; /* 确保并排显示 */
  min-height: 30px;
  box-sizing: border-box;
  vertical-align: middle; /* 垂直对齐 */
}

/* 修改余票显示样式，确保并排显示 */
.film-tickets {
  color: #4CAF50;
  font-weight: 600;
  font-size: 0.9rem;
  padding: 6px 12px;
  background: rgba(76, 175, 80, 0.1);
  border-radius: 4px;
  display: inline-block; /* 确保并排显示 */
  min-height: 30px;
  box-sizing: border-box;
  vertical-align: middle; /* 垂直对齐 */
}

.notice-card {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  background: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin: 5px 0;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* 改进的公告悬停效果 */
.notice-card:hover {
  transform: scale(1.01);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #ffffff 0%, #f0f4f8 100%);
}

.notice-card:hover::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #FF9800, #E91E63);
}

/* 响应式断点 */
@media (max-width: 768px) {
  .home-container {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
  }
  
  .recommended-section,
  .notice-section {
    max-width: 100%;
    height: 500px;
    margin-bottom: 20px;
  }
  
  .film-image {
    width: 80px;
  }
  
  .section-title {
    font-size: 1.2rem;
  }
}

@media (max-width: 480px) {
  .film-name {
    font-size: 1rem;
  }
  
  .home-container {
    gap: 10px;
    padding: 5px;
  }
  
  .film-item {
    padding: 10px;
  }
}
</style>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  noticeList: [],
  filmList: []
})

//请求后台的公告数据
request.get('/notice/selectAll').then(res => {
  data.noticeList = res.data
})

// 修改为获取所有可观看电影，并按余票数排序
// 假设后端有一个接口可以获取所有电影，如果没有，可以根据实际情况调整
onMounted(() => {
  // 尝试获取所有电影数据
  request.get('/goodsStock/selectAll').then(res => {
    // 按余票数从高到低排序
    data.filmList = res.data.sort((a, b) => b.tickets - a.tickets);
  }).catch(error => {
    // 如果获取所有电影失败，回退到原来的排序接口
    console.log('获取所有电影失败，使用排序接口');
    request.get('/goodsStock/selectRanking').then(res => {
      data.filmList = res.data;
    });
  });
})
</script>