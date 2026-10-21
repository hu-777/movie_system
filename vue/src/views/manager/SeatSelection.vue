<template>
  <div class="seat-selection-container">
    <div class="movie-info">
      <h2>{{ movieName }}</h2>
      <div class="info-row">
        <span>影厅：{{ hallName }}</span>
<span>时间：{{ formatTime(startTime) }}</span>
        <span>票价：¥{{ price }}</span>
      </div>
    </div>

    <div class="screen-area">
      <div class="screen">屏幕</div>

      <div class="seats-container">
        <div v-for="row in rows" :key="row" class="seat-row">
          <div class="row-label">{{ row }}</div>
          <div
              v-for="col in cols"
              :key="`${row}-${col}`"
              class="seat"
              :class="getSeatClass(row, col)"
              @click="selectSeat(row, col)"
          >
            {{ col }}
          </div>
        </div>
      </div>

      <div class="seat-legend">
        <div class="legend-item">
          <div class="legend-seat available"></div>
          <span>可选</span>
        </div>
        <div class="legend-item">
          <div class="legend-seat selected"></div>
          <span>已选</span>
        </div>
        <div class="legend-item">
          <div class="legend-seat sold"></div>
          <span>已售</span>
        </div>
      </div>
    </div>

<div class="selected-seats">
      <h3>已选座位：</h3>
      <div class="seats-list">
        <span v-for="seat in selectedSeats" :key="seat" class="seat-tag">
          {{ seat }}
          <span class="close" @click="removeSeat(seat)">×</span>
        </span>
      </div>
      <div class="total-info">
        <span>总价：¥{{ totalPrice }}</span>
        <el-button
            type="primary"
            :disabled="selectedSeats.length === 0 || isTooLateToBuy"
            @click="confirmOrder"
        >
          确认订单
        </el-button>
      </div>
    </div>
</div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';

const route = useRoute();
const router = useRouter();
const user = JSON.parse(localStorage.getItem('system-user') || '{}');

// 页面参数 - 修改为从路径参数获取screeningId
const screeningId = ref(route.params.screeningId || route.params.id);  // 优先使用screeningId参数，如果没有则使用id参数
const goodsId = ref('');

// 电影信息
const movieName = ref('');
const hallName = ref('');
const startTime = ref('');
const price = ref(0);

// 座位相关
const rows = ref(['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L']);
const cols = ref([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]);
const seats = ref([]);
const selectedSeats = ref([]);
const totalPrice = ref(0);
const isTooLateToBuy = ref(false); // 开场前5分钟检查变量，只声明一次

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  return dayjs(time).format('YYYY-MM-DD HH:mm');
};

// 获取座位样式类
const getSeatClass = (row, col) => {
  const seatName = `${row}${col}`;
  // 检查是否为已售座位
  const soldSeat = seats.value.find(s => s.seatName === seatName && s.status === 'SOLD');
  if (soldSeat) {
    return 'sold';
  }
  // 检查是否为已选座位
  if (selectedSeats.value.includes(seatName)) {
    return 'selected';
  }
  // 如果开场前5分钟内，将可选座位也显示为禁用
  return isTooLateToBuy.value ? 'available disabled' : 'available';
};

// 选择座位
const selectSeat = (row, col) => {
  // 添加开场前5分钟检查
  const now = new Date();
  const startTimeDate = new Date(startTime.value);
  const minutesDiff = (startTimeDate.getTime() - now.getTime()) / (1000 * 60);
  
  // 如果开场前5分钟内，不允许选座
  if (minutesDiff <= 5) {
    isTooLateToBuy.value = true;
    ElMessage.warning('开场前5分钟内不允许选座');
    return;
  }
  
  const seatName = `${row}${col}`;
  const seat = seats.value.find(s => s.seatName === seatName);

  // 如果座位已售出，不能选择
  if (seat && seat.status === 'SOLD') {
    return;
  }

  // 切换选中状态
  const index = selectedSeats.value.indexOf(seatName);
  if (index > -1) {
    selectedSeats.value.splice(index, 1);
  } else {
    selectedSeats.value.push(seatName);
  }

  // 更新总价
  updateTotalPrice();
};

// 移除已选座位
const removeSeat = (seatName) => {
  const index = selectedSeats.value.indexOf(seatName);
  if (index > -1) {
    selectedSeats.value.splice(index, 1);
    updateTotalPrice();
  }
};

// 更新总价
const updateTotalPrice = () => {
  // 确保 price.value 是数字
  const unitPrice = Number(price.value) || 0;
  totalPrice.value = selectedSeats.value.length * unitPrice;
};

// 修改loadData函数，优化错误处理和API调用
const loadData = async () => {
  try {
    console.log('开始加载数据，screeningId:', screeningId.value);
    
    // 从sessionStorage获取场次信息
    let currentScreeningId = null;
    const screeningInfo = sessionStorage.getItem('screeningInfo');
    if (screeningInfo) {
      try {
        const screening = JSON.parse(screeningInfo);
        movieName.value = screening.movieName || '电影名称';
        hallName.value = screening.hallName || '1号厅';
        startTime.value = screening.startTime || new Date().toISOString();
        price.value = screening.price || 30;
        currentScreeningId = screening.screeningId;  // 从sessionStorage中获取screeningId
        console.log('从sessionStorage获取到场次信息，screeningId:', currentScreeningId);
      } catch (e) {
        console.error('解析sessionStorage中的场次信息失败:', e);
        sessionStorage.removeItem('screeningInfo'); // 清除损坏的数据
      }
    }
    
    // 优先使用路径参数中的screeningId（场次ID）
    if (screeningId.value) {
      currentScreeningId = screeningId.value;  // 从路由参数中获取screeningId
      console.log('使用路径参数中的screeningId:', currentScreeningId);
      
      try {
        // 调用后端API获取场次信息
        console.log('调用场次信息API:', `/screening/getById?id=${currentScreeningId}`);
        const res = await request.get(`/screening/getById?id=${currentScreeningId}`);
        console.log('场次信息API返回:', res);
        
        if (res && res.code === '200' && res.data) {
          const screening = res.data;
          movieName.value = screening.goodsName || movieName.value;
          hallName.value = screening.hallName || hallName.value;
          startTime.value = screening.startTime || startTime.value;
          price.value = screening.price || price.value;
          console.log('成功从后端获取场次信息');
        } else {
          console.warn('场次信息API返回异常:', res);
          ElMessage.warning('获取场次信息不完整，但将继续加载');
        }
      } catch (apiError) {
        console.error('调用场次信息API失败:', apiError);
        // 不抛出错误，继续执行，避免整个加载过程中断
        ElMessage.warning('获取场次信息失败，但将继续加载座位信息');
      }
    }

    // 添加开场前5分钟检查逻辑
    const now = new Date();
    const startTimeDate = new Date(startTime.value);
    const minutesDiff = (startTimeDate.getTime() - now.getTime()) / (1000 * 60);
    isTooLateToBuy.value = minutesDiff <= 5;

    // 初始化座位，使用正确的screeningId（场次ID）参数
    await initSeats(currentScreeningId);
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败，请刷新页面重试');
    // 出错时仍尝试初始化座位
    await initSeats();
  }
};

// 修改initSeats函数，接受screeningId参数，优化错误处理
const initSeats = async (providedScreeningId = null) => {
  try {
    let targetScreeningId = providedScreeningId;
    
    // 如果没有提供screeningId，尝试从sessionStorage获取
    if (!targetScreeningId) {
      const screeningInfo = sessionStorage.getItem('screeningInfo');
      if (screeningInfo) {
        const screening = JSON.parse(screeningInfo);
        targetScreeningId = screening.screeningId;  // 从sessionStorage中获取screeningId
        console.log('从sessionStorage获取screeningId:', targetScreeningId);
      }
    }
    
    // 如果还是没有，使用路径参数
    if (!targetScreeningId && screeningId.value) {
      targetScreeningId = screeningId.value;  // 从路由参数中获取screeningId
      console.log('从路径参数获取screeningId:', targetScreeningId);
    }
    
    // 确保screeningId是数字类型
    if (targetScreeningId) {
      targetScreeningId = parseInt(targetScreeningId);
      console.log('转换后的screeningId:', targetScreeningId, typeof targetScreeningId);
    }
    
    if (targetScreeningId && !isNaN(targetScreeningId)) {
      try {
        // 调用后端API获取座位信息，使用screeningId（场次ID）参数
        console.log('调用座位信息API:', `/seat/selectByScreeningId?screeningId=${targetScreeningId}`);
        const res = await request.get(`/seat/selectByScreeningId?screeningId=${targetScreeningId}`);
        console.log('座位信息API返回完整数据:', JSON.stringify(res));
        
        if (res) {
          if (res.code === '200') {
            if (res.data) {
              if (Array.isArray(res.data)) {
                if (res.data.length > 0) {
                  // 使用后端返回的座位数据
                  seats.value = res.data;
                  console.log('成功获取座位信息，共', seats.value.length, '个座位');
                  console.log('前5个座位数据示例:', seats.value.slice(0, 5));
                  return;
                } else {
                  console.warn('座位信息API返回空数组，可能该场次没有座位数据');
                  ElMessage.warning('该场次暂无座位信息');
                }
              } else {
                console.error('座位信息API返回的数据不是数组:', res.data);
              }
            } else {
              console.warn('座位信息API返回data为空');
            }
          } else {
            console.error('座位信息API返回错误状态码:', res.code, '错误信息:', res.msg || res.message);
          }
        } else {
          console.error('座位信息API返回null或undefined');
        }
      } catch (apiError) {
        console.error('调用座位信息API失败:', apiError);
        console.error('错误详情:', apiError.message, '响应:', apiError.response || '无响应');
        ElMessage.warning('获取座位信息失败，将使用默认座位');
      }
    } else {
      console.warn('无效的screeningId:', targetScreeningId);
    }
    
    // 如果没有获取到有效数据，使用默认座位数据
    console.log('使用默认座位数据');
    const initSeatsList = [];
    rows.value.forEach(row => {
      cols.value.forEach(col => {
        initSeatsList.push({
          seatRow: row,
          seatColumn: col,
          seatName: `${row}${col}`,
          status: 'AVAILABLE' // 默认全部可用
        });
      });
    });
    seats.value = initSeatsList;
  } catch (error) {
    console.error('初始化座位失败:', error);
    ElMessage.error('初始化座位失败，使用默认座位');
    
    // 出错时使用默认座位数据
    const initSeatsList = [];
    rows.value.forEach(row => {
      cols.value.forEach(col => {
        initSeatsList.push({
          seatRow: row,
          seatColumn: col,
          seatName: `${row}${col}`,
          status: 'AVAILABLE'
        });
      });
    });
    seats.value = initSeatsList;
  }
};

// 修改confirmOrder函数，确保使用正确的screeningId
const confirmOrder = () => {
  if (selectedSeats.value.length === 0) {
    ElMessage.warning('请选择座位');
    return;
  }
  
  if (!user.id) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  // 从sessionStorage获取场次信息
  const screeningInfo = sessionStorage.getItem('screeningInfo');
  if (!screeningInfo) {
    ElMessage.error('场次信息丢失，请返回重新选择场次');
    router.push('/buy');
    return;
  }
  
  try {
    const screening = JSON.parse(screeningInfo);
    
    // 构建订单数据，确保使用正确的screeningId（场次ID）
    const orderData = {
      goodsId: screening.goodsId,
      screeningId: screening.screeningId, // 这是正确的场次ID
      userId: user.id,
      num: selectedSeats.value.length,
      seatInfo: selectedSeats.value.join(',')
    };
    
    console.log('提交订单数据:', orderData);
    
    // 调用正确的后端接口处理选座购买
    request.post('/orders/addWithSeats', orderData).then(res => {
      console.log('订单创建API返回:', res);
      if (res && res.code === '200') {
        ElMessage.success(`已选择 ${selectedSeats.value.length} 个座位，总价：¥${totalPrice.value}`);
        router.push('/orders');
      } else {
        ElMessage.error(res?.msg || '订单创建失败');
      }
    }).catch(err => {
      console.error('订单创建失败:', err);
      ElMessage.error('网络错误，请稍后重试');
    });
  } catch (e) {
    console.error('解析场次信息失败:', e);
    ElMessage.error('场次信息格式错误，请重新选择场次');
  }
};

// 页面加载时执行
onMounted(async () => {
  console.log('SeatSelection组件挂载');
  await loadData();
});
</script>

<style scoped>
.seat-selection-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.movie-info {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
}

.movie-info h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.info-row {
  display: flex;
  gap: 20px;
  color: #666;
}

.screen-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.screen {
  width: 60%;
  height: 10px;
  background: #e0e0e0;
  border-radius: 5px 5px 0 0;
  margin-bottom: 30px;
  text-align: center;
  font-size: 12px;
  color: #999;
  padding-top: 40px;
  position: relative;
}

.screen::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 40px;
  background: linear-gradient(to top, #e0e0e0, #f0f0f0);
  border-radius: 5px 5px 0 0;
}

.seats-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 30px;
}

.seat-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.row-label {
  width: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.seat {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.seat.available {
  background: #409eff;
  color: white;
}

.seat.available:hover {
  background: #66b1ff;
  transform: scale(1.1);
}

.seat.available.disabled {
  background: #ccc;
  cursor: not-allowed;
}

.seat.available.disabled:hover {
  background: #ccc;
  transform: none;
}

.seat.selected {
  background: #67c23a;
  color: white;
}

.seat.sold {
  background: #909399;
  color: white;
  cursor: not-allowed;
}

.seat-legend {
  display: flex;
  gap: 30px;
  margin-top: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.legend-seat {
  width: 20px;
  height: 20px;
  border-radius: 3px;
}

.legend-seat.available {
  background: #409eff;
}

.legend-seat.selected {
  background: #67c23a;
}

.legend-seat.sold {
  background: #909399;
}

.selected-seats {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
}

.selected-seats h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.seats-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.seat-tag {
  background: #67c23a;
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.seat-tag .close {
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

.total-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.total-info span {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .seat-selection-container {
    padding: 10px;
  }

  .info-row {
    flex-direction: column;
    gap: 8px;
  }

  .screen {
    width: 80%;
  }

  .seat {
    width: 25px;
    height: 25px;
    font-size: 10px;
  }

  .seat-legend {
    flex-direction: column;
  }

  .total-info {
    flex-direction: column;
    gap: 15px;
  }
}
</style>