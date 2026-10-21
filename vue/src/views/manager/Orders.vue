<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.goodsName" style="width: 300px; margin-right: 10px" placeholder="请输入电影名称查询"></el-input>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table
          v-loading="loading"
          :data="data.tableData"
          stripe
          style="width: 100%"
      >
        <el-table-column v-if="data.user.role === 'ADMIN'" label="订单编号" prop="orderNo"></el-table-column>
        <el-table-column label="电影名称" prop="goodsName"></el-table-column>
        <el-table-column label="购买数量" prop="num"></el-table-column>
        <el-table-column label="订单金额" prop="totalPrice">
          <template #default="scope">
            ￥{{ scope.row.totalPrice }}
          </template>
        </el-table-column>

        <el-table-column label="订单状态" prop="status">
          <template #default="scope">
            <el-tag v-if="scope.row.status==='已取消'" type="danger">已取消</el-tag>
            <el-tag v-if="scope.row.status==='待支付'" type="warning">待支付</el-tag>
            <el-tag v-if="scope.row.status==='待取票'" type="primary">待取票</el-tag>
            <el-tag v-if="scope.row.status==='待退票'" type="warning">待退票</el-tag>
            <el-tag v-if="scope.row.status==='已完成'" type="success">已完成</el-tag>
            <el-tag v-if="scope.row.status==='已退票'" type="danger">已退票</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="hallName" label="影厅" width="100"></el-table-column>
        <el-table-column label="放映时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.showTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="seatInfo" label="座位信息" width="150"></el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button v-if="data.user.role==='USER' && scope.row.status==='待支付'" type="primary" @click="changeStatus(scope.row, '待取票')" style="margin-left: 5px;">支付</el-button>
            <el-button v-if="data.user.role==='USER' && scope.row.status === '待支付'" type="danger" @click="changeStatus(scope.row, '已取消')" style="margin-left: 5px;">取消</el-button>
            <el-button v-if="data.user.role==='USER' && scope.row.status === '待取票'" type="success" @click="changeStatus(scope.row, '已完成')" style="margin-left: 5px;">取票</el-button>
            <el-button v-if="data.user.role==='USER' && (scope.row.status === '待取票' || scope.row.status === '已完成')" type="warning" @click="changeStatus(scope.row, '待退票')" style="margin-left: 5px;">退票</el-button>
            <el-button v-if="data.user.role==='USER' && scope.row.status === '已完成'" type="primary" @click="exportInvoice(scope.row)" style="margin-left: 5px;">导出</el-button>
            <el-button v-if="data.user.role==='USER' && scope.row.status === '待退票'" type="info" @click="changeStatus(scope.row, 'CANCEL_REFUND')" style="margin-left: 5px;">取消退票</el-button>
            <el-button v-if="scope.row.status === '待退票' && data.user.role === 'ADMIN'" type="danger" @click="changeStatus(scope.row, '已退票')" style="margin-left: 5px;">确认退票</el-button>
            <el-button v-if="data.user.role==='ADMIN' && scope.row.status==='已完成'" type="info" @click="exportInvoice(scope.row)" style="margin-left: 5px;">导出</el-button>
            <el-button v-if="data.user.role === 'ADMIN'" type="danger" @click="handleDelete(scope.row.id)" style="margin-left: 5px;">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="data.total">
      <el-pagination
          background
          layout="sizes, prev, pager, next, jumper"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="data.pageSize"
          :current-page="data.pageNum"
          :total="data.total"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { reactive, ref } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";

// 添加formatDateTime函数来格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';

  try {
    // 尝试创建日期对象
    const date = new Date(dateString);

    // 检查是否是有效日期
    if (isNaN(date.getTime())) {
      return dateString;
    }

    // 格式化日期为：yyyy-MM-dd HH:mm:ss
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error('日期格式化失败:', error);
    return dateString;
  }
};

const loading = ref(false);
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  orderNo: null,
  goodsName: null,
  // 存储订单原始状态的映射，用于取消退票时恢复
  originalStatusMap: new Map()
});

// 确保用户角色和数据正确
if (!data.user.role) {
  data.user.role = 'USER';
}

// 分页查询
const load = () => {
  console.log('加载订单数据，参数:', {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    goodsName: data.goodsName,
    userId: data.user.id,
    userRole: data.user.role
  });
  loading.value = true;
  request.get('/orders/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      goodsName: data.goodsName,
      userId: data.user.id,
      userRole: data.user.role
    }
  }).then(res => {
    console.log('订单查询响应:', res);
    loading.value = false;
    let list = res.data?.list || [];
    console.log('订单列表数量:', list.length);
    // 排序：将待退票状态的订单置顶
    list.sort((a, b) => {
      if (a.status === '待退票' && b.status !== '待退票') return -1;
      if (a.status !== '待退票' && b.status === '待退票') return 1;
      return 0;
    });

    data.tableData = list;
    data.total = res.data?.total || 0;
  }).catch(err => {
    console.error('订单查询失败:', err);
    loading.value = false;
  });
};

// 编辑保存/状态流转
const changeStatus = (row, status) => {
  // 取消订单确认提示
  if (status === '已取消') {
    ElMessageBox.confirm('确认取消该笔订单吗？', '取消订单确认', {
      type: 'warning'
    }).then(() => {
      updateOrderStatus(row, status);
    }).catch(() => {});
    return;
  }

  // 退票确认提示
  if (status === '待退票') {
    ElMessageBox.confirm('确定要申请退票吗？', '退票确认', {
      type: 'warning'
    }).then(() => {
      // 记录原始状态，用于取消退票时恢复
      data.originalStatusMap.set(row.orderNo, row.status);
      updateOrderStatus(row, status);
    }).catch(() => {});
    return;
  }

  // 取消退票确认提示
  if (status === 'CANCEL_REFUND') {
    ElMessageBox.confirm('确定要取消退票申请吗？', '取消退票确认', {
      type: 'warning'
    }).then(() => {
      // 获取原始状态，如果没有记录则默认为待取票
      const originalStatus = data.originalStatusMap.get(row.orderNo) || '待取票';
      // 清除已恢复的记录
      data.originalStatusMap.delete(row.orderNo);
      updateOrderStatus(row, originalStatus);
    }).catch(() => {});
    return;
  }

  // 管理员确认退票提示
  if (status === '已退票' && data.user.role === 'ADMIN') {
    ElMessageBox.confirm('确定要确认退票吗？此操作将恢复电影票库存。', '确认退票', {
      type: 'warning'
    }).then(() => {
      // 确认退票后清除原始状态记录
      data.originalStatusMap.delete(row.orderNo);
      updateOrderStatus(row, status);
    }).catch(() => {});
    return;
  }

  // 正常状态变更（如支付、取票）
  updateOrderStatus(row, status);
};

// 实际更新订单状态的方法
const updateOrderStatus = (row, status) => {
  // 保存原始状态，用于错误恢复
  const originalStatus = row.status;
  row.status = status;

  // 先更新订单状态，这是核心功能，必须保证成功
  request.put('/orders/update', row).then(res => {
    if (res.code === '200') {
      // 订单状态更新成功后，再尝试更新库存（如果需要）
      if (status === '已取消' || status === '已退票') {
        // 确保库存更新的重要性，不要放在单独的 try-catch 中忽略错误
        updateStock(row, status).then(() => {
          // 重新加载数据并显示成功消息
          load();
          ElMessage.success('操作成功');
        }).catch(err => {
          console.error('库存更新失败:', err);
          ElMessage.warning('订单状态已更新，但库存更新失败，请联系管理员');
          load();
        });
      } else {
        // 其他状态更新直接成功
        load();
        ElMessage.success('操作成功');
      }
    } else {
      // 订单状态更新失败，恢复原状态
      row.status = originalStatus;
      ElMessage.error(res.msg || '操作失败');
      load();
    }
  }).catch(err => {
    // 网络错误，恢复原状态
    row.status = originalStatus;
    ElMessage.error('网络错误，请稍后重试');
    load();
  });
};

// 单独的库存更新函数
const updateStock = (row, status) => {
  return new Promise((resolve, reject) => {
    // 检查订单对象中是否有必要的信息
    if (!row || !row.num) {
      console.error('订单信息不完整，无法更新库存');
      reject(new Error('订单信息不完整'));
      return;
    }

    // 根据后端实际接口设计选择合适的API调用方式
    request.post('/orders/updateStock', {
      orderId: row.id,
      status: status,
      refund: true
    }).then(stockRes => {
      if (stockRes.code === '200') {
        console.log('库存更新成功');
        resolve();
      } else {
        console.warn('库存更新失败:', stockRes.msg || '未知错误');
        reject(new Error(stockRes.msg || '库存更新失败'));
      }
    }).catch(stockErr => {
      console.warn('库存更新请求失败，可能是接口不存在:', stockErr);
      reject(stockErr);

      // 尝试其他方式更新库存
      if (row.goodsId) {
        request.post('/goods/stock', {
          id: row.goodsId,
          quantity: row.num,
          type: 'refund'
        }).catch(err2 => {
          console.warn('库存更新方式2也失败:', err2);

          // 尝试方式3：使用更通用的接口格式
          request.post('/common/updateStock', {
            referenceId: row.id,
            referenceType: 'order',
            quantity: row.num,
            operation: 'increase'
          }).catch(err3 => {
            console.warn('库存更新方式3也失败:', err3);
          });
        });
      }
    });
  });
};

// 导出电影票功能 - 使用Canvas生成电影票样式图片
const exportInvoice = (row) => {
  try {
    // 模拟生成电影票数据，优先使用真实数据
    const mockInvoiceData = {
      invoiceNo: 'FP' + Date.now(),
      orderNo: row.orderNo,
      goodsName: row.goodsName,
      num: row.num,
      price: row.totalPrice,
      userName: row.userName,
      time: row.time,
      hallName: row.hallName,
      seatInfo: row.seatInfo
    };
    // 生成电影票图片
    generateInvoiceImage(mockInvoiceData);
  } catch (error) {
    console.error('导出电影票失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
};

// 生成电影票图片函数
const generateInvoiceImage = (row) => {
  try {
    // 创建临时DOM元素来渲染电影票样式
    const tempElement = document.createElement('div');
    tempElement.style.cssText = `
      position: absolute;
      left: -9999px;
      width: 800px;
      background: #ffffff;
      padding: 30px;
      font-family: 'SimSun', serif;
    `;

    // 设置电影票HTML内容
    tempElement.innerHTML = `
      <div style="
        border: 3px double #c00;
        padding: 25px 40px;
position: relative;
background:
          repeating-linear-gradient(
            transparent,
            transparent 24px,
            #eee 25px,
#eee 26px
          ),
          linear-gradient(
            to right,
            transparent 98%,
            #c00 98%
          );
      ">
        <div style="
          text-align: center;
          border-bottom: 2px dashed #c00;
          padding-bottom: 15px;
          margin-bottom: 25px;
          position: relative;
        ">
          <h1 style="
            color: #c00;
            font-size: 32px;
            margin: 0;
            letter-spacing: 4px;
            text-shadow: 1px 1px 1px rgba(0,0,0,0.1);
          ">🎬 假娃电影院票据</h1>
          <div style="
            position: absolute;
            right: -20px;
            top: -15px;
            background: #c00;
            color: white;
            padding: 5px 15px;
            border-radius: 15px;
            font-size: 14px;
          ">电子电影票</div>
          <p style="margin:10px 0;color:#666;font-size:16px">电影票代码：${row.invoiceNo || 'N/A'}</p>
        </div>

        <table style="width:100%;border-collapse:collapse;margin:15px 0;font-size:18px">
          ${[
      ['订单编号', row.orderNo],
      ['电影名称', row.goodsName],
      ['放映厅', row.hallName || '未知影厅'],
      // 在电影票模板中使用formatDateTime
      ['购票时间', formatDateTime(row.time) || new Date().toLocaleString()],
      ['座位号', row.seatInfo || '暂无座位信息'],
      ['数量', `${row.num}张`],
      ['单价', `￥${((row.price || row.totalPrice) / row.num).toFixed(2)}`],
      ['合计', `￥${(row.price || row.totalPrice).toFixed(2)}`],
      ['购票人', row.userName]
    ].map(item => `
              <tr>
                <td style="text-align:left;padding:10px 0;border-bottom:1px solid #eee;font-weight:bold;width:30%">${item[0]}:</td>
                <td style="text-align:left;padding:10px 0;border-bottom:1px solid #eee">${item[1]}</td>
              </tr>
            `).join('')}
        </table>

        <div style="
          margin-top: 30px;
          padding-top: 20px;
          border-top: 2px dashed #c00;
          text-align: center;
          color: #666;
          line-height: 1.8;
        ">
          <p style="font-size:18px;color:#c00;margin:15px 0">
            ✨ 假娃电影院期待您的到来 ٩(๑>◡<๑)۶ ✨
          </p>
          <div style="font-size:14px">
            <p>服务热线：400-888-1314</p>
            <p>影院地址：四川省成都市郫都区电影大道2006号</p>
            <p>开票时间：${new Date().toLocaleString()}</p>
          </div>
        </div>
      </div>
    `;

    // 将临时元素添加到文档中
    document.body.appendChild(tempElement);

    // 使用html2canvas库将DOM元素转换为图片
    import('html2canvas').then(html2canvas => {
      html2canvas.default(tempElement, {
        backgroundColor: '#ffffff',
        scale: 2, // 提高图片质量
        useCORS: true,
        logging: false
      }).then(canvas => {
        // 创建下载链接
        const link = document.createElement('a');
        // 替换文件名中的特殊字符
        const fileName = `${row.goodsName.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_')}_${row.orderNo}_电影票.png`;
        link.download = fileName;
        link.href = canvas.toDataURL('image/png');

        // 模拟点击下载
        document.body.appendChild(link);
        link.click();

        // 清理
        document.body.removeChild(link);
        document.body.removeChild(tempElement);

        ElMessage.success('导出成功');
      }).catch(error => {
        console.error('Canvas生成失败:', error);
        document.body.removeChild(tempElement);
        ElMessage.error('导出失败，请稍后重试');
      });
    }).catch(error => {
      console.error('html2canvas加载失败:', error);
      document.body.removeChild(tempElement);

      // 降级方案：如果html2canvas不可用，提供简单的Canvas绘制
      fallbackToSimpleCanvas(row);
    });
  } catch (error) {
    console.error('导出过程发生错误:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
};

// 降级方案：简单Canvas绘制（如果html2canvas不可用）
const fallbackToSimpleCanvas = (row) => {
  try {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    // 设置画布尺寸
    canvas.width = 800;
    canvas.height = 1200;

    // 绘制背景
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    // 模拟边框和样式
    ctx.strokeStyle = '#c00';
    ctx.lineWidth = 3;
    ctx.setLineDash([]);
    ctx.strokeRect(30, 30, canvas.width - 60, canvas.height - 60);
    // 绘制标题
    ctx.fillStyle = '#c00';
    ctx.font = 'bold 32px SimSun';
    ctx.textAlign = 'center';
    ctx.fillText('🎬 假娃电影院票据', canvas.width / 2, 80);

    // 绘制电影票信息
    ctx.fillStyle = '#333333';
    ctx.font = '18px SimSun';
    ctx.textAlign = 'left';

    const infoItems = [
      ['电影票代码:', row.invoiceNo],
      ['订单编号:', row.orderNo],
      ['电影名称:', row.goodsName],
      ['购票人:', row.userName],
      ['金额:', `￥${(row.price || row.totalPrice).toFixed(2)}`]
    ];
    let yPos = 150;
    infoItems.forEach(item => {
      ctx.fillText(item[0], 100, yPos);
      ctx.fillText(item[1], 300, yPos);
      yPos += 40;
    });
    // 创建下载链接
    const link = document.createElement('a');
    const fileName = `${row.goodsName.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '_')}_${row.orderNo}_电影票.png`;
    link.download = fileName;
    link.href = canvas.toDataURL('image/png');
    // 模拟点击下载
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    ElMessage.success('导出成功（降级模式）');
  } catch (error) {
    console.error('降级方案也失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
};

// 查询
const search = () => {
  data.pageNum = 1;
  load();
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(() => {
    request.delete('/orders/delete/' + id).then(res => {
      if (res.code === '200') {
        load();
        ElMessage.success('操作成功');
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(() => {});
};

// 重置
const reset = () => {
  data.goodsName = null;
  data.pageNum = 1;
  load();
};

// 分页页码变化
const handlePageChange = (page) => {
  data.pageNum = page;
  load();
};

// 每页条数变化
const handlePageSizeChange = (size) => {
  data.pageSize = size;
  data.pageNum = 1;
  load();
};

// 初始加载数据
load();
</script>