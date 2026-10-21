<template>
  <div class="buy-container">
    <div class="search-bar">
      <el-input size="large" v-model="data.name" placeholder="输入电影名称关键字查询" :maxlength="50"></el-input>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="info" @click="reset">重置</el-button>
    </div>

    <div class="goods-container">
      <!-- 添加条件容器来包裹内容 -->
      <div v-if="data.total > 0">
        <el-row :gutter="10">
          <el-col
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              v-for="item in data.goodsStockList"
              :key="item.id"
              class="goods-col"
          >
            <!-- 商品卡片内容保持不变 -->
            <div class="goods-card">
              <div class="image-container">
                <img :src="item.img" alt="" class="goods-image">
              </div>
              <div class="goods-name">{{item.name}}</div>
              <el-tooltip v-if="item.comment && item.comment.length > 40" :content="item.comment" effect="light" placement="top">
                <div class="goods-comment">{{ item.comment }}</div>
              </el-tooltip>
              <div v-else class="goods-comment">{{ item.comment }}</div>
              <div class="goods-showtime">上映时间：{{ formatShowTime(item.showtime) }}</div>
              <div class="goods-type-container">
                <el-tag type="success" size="small">{{ item.type }}</el-tag>
                <el-tag type="info" size="small" v-if="item.position">{{ item.position }}</el-tag>
              </div>
              <div class="goods-details">
                <div class="price-section">
                  <strong class="price">¥{{ item.price }}</strong>
                </div>
                <div class="tickets-section">余票：{{ item.tickets }}</div>
                <div class="quantity-section">
                  <el-input-number
                      @change="handleBuy(item)"
                      v-model="item.num"
                      :min="0"
                      :max="item.tickets || 999"
                      :size="'small'"
                  ></el-input-number>
                </div>
              </div>
              <div class="buy-section" v-if="item.num > 0">
                <div class="total-price">总价：<strong>¥{{ item.total }}</strong></div>
                <el-button type="primary" size="small" @click="showScreenings(item)">查看场次</el-button>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="pagination-container">
          <el-pagination
              background
              layout="prev, pager, next, jumper"
              :page-sizes="[4, 8, 12]"
              :page-size="data.pageSize"
              :total="data.total"
              @current-change="handlePageChange"
              @size-change="handlePageSizeChange"
          />
        </div>
      </div>

      <!-- 现在v-else与v-if是相邻的兄弟元素 -->
      <div v-else class="no-data">暂无数据</div>

      <!-- 场次选择对话框移到条件渲染之外 -->
      <el-dialog
          v-model="screeningDialogVisible"
          title="选择场次"
          width="600px"
          :close-on-click-modal="false"
      >
        <div v-if="currentMovie">
          <div class="movie-header">
            <img :src="currentMovie.img" alt="" class="movie-poster">
            <div class="movie-details">
              <h3>{{ currentMovie.name }}</h3>
              <p>{{ currentMovie.comment }}</p>
              <p>类型：{{ currentMovie.type }}</p>
              <p>上映时间：{{ formatShowTime(currentMovie.showtime) }}</p>
              <p v-if="currentMovie.position">放映位置：{{ currentMovie.position }}</p>
            </div>
          </div>

          <div class="screenings-list" v-if="screenings.length > 0">
            <div
                v-for="screening in screenings"
                :key="screening.id"
                :class="['screening-item', { 'disabled': !canBuyTicket(screening) }]"
                @click="handleScreeningClick(screening)"
            >
              <div class="screening-time">{{ formatDateTime(screening.startTime) }}</div>
              <div class="screening-info">
                <span>{{ screening.hallName }}</span>
                <span>{{ screening.screenType }}</span>
              </div>
              <div class="screening-price">¥{{ screening.price }}</div>

              <div class="screening-seats">剩余：{{ screening.availableSeats }}个</div>

            </div>
          </div>

          <div v-else class="no-screenings">暂无场次安排</div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import dayjs from "dayjs"; // 导入dayjs
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  categoryList: [],
  pageNum: 1,
  pageSize: 4,
  total: 0,
  goodsStockList: [],
  name: '',
})

// 添加场次相关的响应式数据
const screeningDialogVisible = ref(false);
const currentMovie = ref(null);
const screenings = ref([]);
const isSmallScreen = ref(window.innerWidth < 768);
const router = useRouter();

// 监听窗口大小变化
const handleResize = () => {
  isSmallScreen.value = window.innerWidth < 768;
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
  loadCategoryList();
  load();
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

// 获取分类列表
const loadCategoryList = () => {
  request.get('/category/selectAll').then(res => {
    data.categoryList = res.data || [];
  }).catch(err => {
    console.error('获取分类失败:', err);
  });
};

// 购买订单
const buy = (goods) => {
  if (!goods.num || goods.num <= 0) {
    ElMessage.warning('请选择购买数量');
    return;
  }

  let orderData = {
    goodsId: goods.id,
    num: goods.num,
    userId: data.user.id
  };

  request.post('/orders/add', orderData).then(res => {
    if(res.code === '200'){
      ElMessage.success('购买成功');
      load();
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  }).catch(err => {
    ElMessage.error(err.response?.data?.msg || '网络连接异常');
  });
};

const handleBuy = (goodsStock) => {
  goodsStock.total = (goodsStock.price * goodsStock.num).toFixed(2);
};

// 分页查询
const load = () => {
  request.get('/goodsStock/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
    }
  }).then(res => {
    data.goodsStockList = res.data?.list || [];
    data.total = res.data?.total || 0;
    data.goodsStockList.forEach(item => {
      item.num = 0;
      item.total = '0.00';
    });
  }).catch(err => {
    console.error('加载商品失败:', err);
    ElMessage.error('加载失败，请稍后重试');
  });
};

// 分页查询辅助search函数
const search = () => {
  data.pageNum = 1;
  load();
};

// 重置
const reset = () => {
  data.name = '';
  data.pageNum = 1;
  load();
};

// 处理页码变化
const handlePageChange = (pageNum) => {
  data.pageNum = pageNum;
  load();
};

// 处理每页显示条数变化
const handlePageSizeChange = (pageSize) => {
  data.pageSize = pageSize;
  data.pageNum = 1;
  load();
};

// 格式化上映时间函数
const formatShowTime = (time) => {
  if (!time) return '';
  return dayjs(time).format('YYYY-MM-DD HH:mm');
};

// 格式化日期时间函数
const formatDateTime = (time) => {
  if (!time) return '';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 判断是否可以购买该场次的票
const canBuyTicket = (screening) => {
  if (!screening.startTime) return false;

  // 检查余票数是否为0
  if (screening.availableSeats === 0) {
    return false;
  }

  const now = dayjs();
  const startTime = dayjs(screening.startTime);

  // 如果开场时间已过或即将在5分钟内开始，则不能购买
  const diffMinutes = startTime.diff(now, 'minute');
  return diffMinutes >= 5;
};

// 处理场次点击事件
const handleScreeningClick = (screening) => {
  if (!canBuyTicket(screening)) {
    // 如果是因为票已售空，显示温馨提示
    if (screening.availableSeats === 0) {
      ElMessage({ message: '🎫 当前场次票已售空，可以考虑其他场次或其他电影呀', type: 'warning' });
    }
    return;
  }
  selectScreening(screening);
};

// 显示场次对话框
const showScreenings = (movie) => {
  currentMovie.value = movie;
  screeningDialogVisible.value = true;
  
  // 添加详细的调试信息
  console.log('准备获取电影场次，电影ID:', movie.id);
  console.log('API调用路径:', `/screening/getByGoodsId?goodsId=${movie.id}`);
  
  // 从后端获取真实的场次数据
  request.get(`/screening/getByGoodsId?goodsId=${movie.id}`).then(res => {
    console.log('获取场次成功，响应数据:', res);
    if (res.code === '200' && res.data && res.data.length > 0) {
      screenings.value = res.data;
    } else {
      // 如果后端没有数据，不再使用模拟数据，而是显示暂无场次
      screenings.value = [];
      // 可以添加提示信息
      ElMessage({
        message: '当前电影暂无场次安排，请稍后再试',
        type: 'info'
      });
    }
  }).catch(err => {
    console.error('获取场次失败:', err);
    console.error('错误详情:', {
      status: err.response?.status,
      statusText: err.response?.statusText,
      url: err.config?.url,
      baseURL: err.config?.baseURL,
      fullPath: err.config?.baseURL + err.config?.url
    });
    // 错误情况下也不使用模拟数据
    screenings.value = [];
    ElMessage({
      message: `获取场次信息失败: ${err.response?.status || '网络错误'}，请检查服务是否正常运行`,
      type: 'error'
    });
  });
};

// 选择场次 - 修复传参问题，使用screening.id作为场次ID
const selectScreening = (screening) => {
  // 存储必要信息到sessionStorage
  sessionStorage.setItem('screeningInfo', JSON.stringify({
    goodsId: currentMovie.value.id,
    screeningId: screening.id,  // 使用screening.id作为场次ID
    movieName: currentMovie.value.name,
    hallName: screening.hallName,
    startTime: screening.startTime,
    price: screening.price
  }));
  // 使用params传递screeningId（场次ID），避免暴露其他信息
  router.push({
    path: `/seatSelection/${screening.id}`,  // 使用screening.id作为路径参数
  });
  screeningDialogVisible.value = false;
};
</script>

<style scoped>
.buy-container {
  padding: 10px;
  box-sizing: border-box;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-bar .el-input {
  min-width: 200px;
  flex: 1;
  max-width: 300px;
}

.goods-container {
  width: 100%;
}

.goods-col {
  margin-bottom: 10px;
}

.goods-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  background: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.image-container {
  width: 100%;
  padding-top: 150%; /* 固定宽高比 */
  position: relative;
  overflow: hidden;
}

.goods-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.goods-name {
  font-size: 1.1rem;
  margin: 8px 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-comment {
  margin: 5px 0;
  color: #666;
  font-size: 0.9rem;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goods-showtime {
  margin: 5px 0;
  color: #666;
  font-size: 0.9rem;
}

.goods-type-container {
  margin: 10px 0;
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.goods-details {
  display: flex;
  align-items: center;
  margin: 10px 0;
  flex-wrap: wrap;
  gap: 5px;
}

.price-section {
  flex: 1;
}

.price {
  font-size: 1.2rem;
  color: #ff4d4f;
}

.tickets-section {
  margin: 0 5px;
  color: #666;
}

.quantity-section {
  min-width: 100px;
}

.buy-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 10px;
}

.total-price {
  font-size: 1rem;
  color: #ff4d4f;
}

.total-price strong {
  font-size: 1.1rem;
}

.no-data {
  font-size: 1.5rem;
  padding: 50px 0;
  text-align: center;
  color: #888;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式断点 */
@media (max-width: 768px) {
  .search-bar {
    justify-content: center;
  }

  .search-bar .el-input {
    max-width: none;
  }

  .goods-details {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .price-section,
  .tickets-section,
  .quantity-section {
    width: 100%;
    text-align: center;
  }

  .buy-section {
    flex-direction: column;
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .buy-container {
    padding: 5px;
  }

  .goods-name {
    font-size: 1rem;
  }

  .price {
    font-size: 1.1rem;
  }

  .no-data {
    font-size: 1.2rem;
    padding: 30px 0;
  }
}

/* 场次对话框样式 */
.movie-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.movie-poster {
  width: 120px;
  height: 180px;
  object-fit: cover;
  border-radius: 4px;
}

.movie-details {
  flex: 1;
}

.movie-details h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.movie-details p {
  margin: 5px 0;
  color: #666;
}

.screenings-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.screening-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.screening-item:hover:not(.disabled) {
  border-color: #1890ff;
  background-color: #f0f7ff;
}

/* 禁用状态样式 */
.screening-item.disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
  border-color: #d9d9d9;
}

.screening-item.disabled .screening-price {
  color: #999;
}

.disabled-hint {
  position: absolute;
  right: 15px;
  bottom: 5px;
  font-size: 0.8rem;
  color: #ff4d4f;
}

.screening-time {
  font-size: 1.1rem;
  font-weight: 500;
  color: #333;
}

.screening-info {
  display: flex;
  gap: 20px;
  color: #666;
}

.screening-price {
  font-size: 1.2rem;
  font-weight: 500;
  color: #ff4d4f;
}

.screening-seats {
  color: #666;
}

.no-screenings {
  text-align: center;
  padding: 40px 0;
  color: #999;
}
</style>