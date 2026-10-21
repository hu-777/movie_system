<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="12">
        <el-card>
          <div style="width: 100%; height: 400px" id="line"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div style="width: 100%; height: 400px" id="bar"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" style="margin: 10px 0">
      <el-col :span="12">
        <el-card>
          <div style="width: 100%; height: 400px" id="pie"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div style="width: 100%; height: 400px" id="scatter"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { defineComponent } from 'vue'
import request from '@/utils/request'

// Charts.vue 修改echarts配置
const option = {
  title: {
    text: '订单统计趋势图',
    subtext: '数据粒度：天->具体日期',
    left: 'center',
    textStyle: {
      fontSize: 20
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['订单金额', '订单数量'],
    bottom: 10
  },
  grid: {
    top: 80,
    bottom: 80
  },
  xAxis: {
    type: 'category',
    data: [],
  },
  yAxis: [
    {
      type: 'value',
      name: '金额（元）',
      position: 'left',
      axisLine: {
        lineStyle: {
          color: '#5470C6'
        }
      }
    },
    {
      type: 'value',
      name: '订单量（笔）',
      position: 'right',
      axisLine: {
        lineStyle: {
          color: '#EE6666'
        }
      }
    }
  ],
  series: [
    {
      name: '订单金额',
      type: 'line',
      data: [],
      yAxisIndex: 0,
      smooth: true,
      itemStyle: {
        color: '#5470C6'
      }
    },
    {
      name: '订单数量',
      type: 'line',
      data: [],
      yAxisIndex: 1,
      smooth: true,
      itemStyle: {
        color: '#EE6666'
      }
    }
  ]
};

const option1 = {
  title: {
    text: '商品种类统计',
    subtext: '统计依据：电影类型',
    left: 'center',
    textStyle: {
      fontSize: 20
    }
  },
  legend: {
    data: ['订单金额', '订单数量'],
    bottom: 10,
    itemGap: 20
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    top: 80,
    bottom: 80
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: [ // 改为数组形式
    {
      type: 'value',
      name: '金额（元）',
      position: 'left',
      axisLine: {
        lineStyle: {
          color: '#00c9a7'
        }
      },
      axisLabel: {
        formatter: '{value}' // 金额格式化
      }
    },
    {
      type: 'value',
      name: '订单量（笔）',
      position: 'right',
      axisLine: {
        lineStyle: {
          color: '#FC8452'
        }
      },
      splitLine: { show: false } // 去除右侧网格线
    }
  ],
  series: [
    {
      name: '订单金额',
      type: 'bar',
      data: [],
      yAxisIndex: 0, // 指定使用第一个Y轴
      itemStyle: {
        color: '#00c9a7'
      }
    },
    {
      name: '订单数量',
      type: 'bar',
      data: [],
      yAxisIndex: 1, // 指定使用第二个Y轴
      itemStyle: {
        color: '#FC8452'
      }
    }
  ]
};

// 修改图表标题部分
const option2 = {
  title: {
    text: '交易金额占比前5用户分析 (百分制)',
    subtext: '内圈：交易数量占比  |  外圈：交易金额占比',
    left: 'center',
    textStyle: {
      fontSize: 18,
      color: '#333'
    }
  },
  tooltip: {},
  legend: {
    type: 'scroll',
    orient: 'horizontal',
    bottom: 10,
    itemGap: 15,
    textStyle: {
      color: '#666'
    }
  },
  color: [ // 添加自定义颜色数组
    '#5470C6', '#91CC75', '#FAC858', '#EE6666',
    '#73C0DE', '#3BA272', '#FC8452', '#9A60B4',
    '#EA7CCC'
  ],
  series: [
    {
      name: '交易数量占比',
      type: 'pie',
      selectedMode: 'single',
      radius: [0, '30%'],
      labelLine: {
        lineStyle: {
          type: 'dashed', // 虚线引导线
          color: 'rgba(100,100,100,0.5)'
        },
        showAbove: false // 引导线显示在图形上层
      },
      data: []
    },
    {
      name: '交易金额占比',
      type: 'pie',
      radius: ['45%', '60%'],
      labelLine: {
        lineStyle: {
          type: 'dashed', // 虚线引导线
          color: 'rgba(100,100,100,0.5)'
        },
        showAbove: false // 引导线显示在图形上层
      },
      data: []
    }
  ]
};

const option3 = {
  title: {
    text: '用户购买时间密度分析',
    subtext: '数据粒度：小时级 | 颜色深度表示订单密度',
    left: 'center',
    textStyle: {
      fontSize: 20
    }
  },
  tooltip: {
    trigger: 'item',
    formatter: function(params) {
      const endHour = params.value[0] === 23 ? '00' : params.value[0] + 1;
      return `日期：${params.value[1]}<br/>
              时段：${params.value[0]}:00-${endHour}:00<br/>
              订单量：${params.value[2]}`;
    }
  },
  grid: {
    top: 70,
    left: 70,
    right: 30,
    bottom: 120
  },
  xAxis: {
    type: 'category',
    data: Array.from({length: 24}, (_, i) => i),
    axisLabel: {
      formatter: value => `${value}`,
      fontSize: 12
    },
    axisTick: {
      alignWithLabel: true
    },
    splitLine: {
      show: true,
      lineStyle: {
        color: '#f0f0f0',
        type: 'dashed'
      }
    }
  },

  yAxis: {
    type: 'category',
    inverse: true,
    axisLine: {
      show: true,
      lineStyle: {
        color: '#666',
        width: 1
      }
    },
    axisLabel: {
      rotate: 0,
      fontSize: 12,
      color: '#666',
      interval: 0,  // 强制显示所有标签，这是关键修改
      formatter: value => {
        const parts = value.split('-');
        return parts.length === 3 ? `${parts[1]}-${parts[2]}` : value;
      }
    },
    splitLine: {
      show: true,
      lineStyle: {
        color: '#f0f0f0',
        type: 'dashed'
      }
    }
  },
  visualMap: {
    min: 0,
    max: 50,
    calculable: true,
    orient: 'horizontal',
    left: 'center',
    bottom: 35,  // 微调位置
    textStyle: {
      color: '#666',
      // 标签位置微调
      padding: [0, 5], // 右标签左移5px，左标签右移5px
      align: 'center'
    },
    inRange: {
      color: ['#ebedf0', '#9be9a8', '#40c463', '#30a14e', '#216e39']
    }
  },
  series: [{
    type: 'heatmap',
    data: [],
    itemStyle: {
      borderRadius: 2,  // GitHub风格的圆角
      borderWidth: 1,
      borderColor: 'rgba(255,255,255,0.8)'
    },
    // 修改单元格大小，确保格子为正方形
    cellSize: [25, 25],  // 改为相同的宽度和高度，使格子成为正方形
    // 确保热力图能够填充整个容器
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    },
    // 移除markLine配置
  }],
  graphic: [{
    type: 'text',
    left: 'center',
    bottom: 20,
    style: {
      text: '颜色深度说明：浅色 → 低密度 → 高密度 → 深色',
      font: '12px "Microsoft YaHei"',
      fill: '#666'
    }
  }]
};

// 修改数据处理逻辑，确保按天计数并显示所有格子
request.get('/scatterCharts').then(scatterRes => {
  const { data, references } = scatterRes.data;

  // 转换数据格式
  const scatterData = data.map(item => [item.x, item.y, item.value]);

  // 提取并排序日期，确保所有日期都显示
  const sortedDates = [...new Set(data.map(item => item.y))].sort((a, b) => {
    const [ma, da] = a.split('-');
    const [mb, db] = b.split('-');
    return new Date(`2024-${ma}-${da}`) - new Date(`2024-${mb}-${db}`);
  });

  // 为每一天的每个小时创建完整的数据网格，确保格子画完
  const completeData = [];
  sortedDates.forEach(date => {
    // 为每个小时(0-23)创建数据点，即使该小时没有数据也创建默认值为0的点
    for (let hour = 0; hour < 24; hour++) {
      // 查找现有数据
      const existingPoint = scatterData.find(point => point[0] === hour && point[1] === date);
      if (existingPoint) {
        completeData.push(existingPoint);
      } else {
        // 没有数据时添加默认值为0的点
        completeData.push([hour, date, 0]);
      }
    }
  });

  // 更新配置
  option3.yAxis.data = sortedDates;
  option3.series[0].data = completeData;
  option3.visualMap.max = Math.max(...data.map(item => item.value)) || 50;

  // 添加参考线
  option3.series[0].markLine.data = references.map(ref => ({
    name: ref.name,
    xAxis: ref.x,
    label: { show: true, position: 'end' }
  }));

  scatterChart.setOption(option3);
});



export default defineComponent({
  name: "Charts",
  data(){
    return{}
  },
  // 等待页面元素全部加载完成后再初始化
  mounted(){
    let lineDom = document.getElementById('line');
    let lineChart = echarts.init(lineDom);

    let barDom = document.getElementById('bar');
    let barChart = echarts.init(barDom);

    let pieDom = document.getElementById('pie');
    let pieChart = echarts.init(pieDom);

    let scatterDom = document.getElementById('scatter');
    let scatterChart = echarts.init(scatterDom);

    request.get('/lineCharts').then(res=>{
      option.xAxis.data = res.data?.map(v=>v.date) || []
      option.series[0].data = res.data?.map(v=>v.amount) || []
      option.series[1].data = res.data?.map(v=>v.count) || []
      lineChart.setOption(option)
    })

    request.get('/barCharts').then(barRes => {
      const barData = barRes.data || [];
      option1.xAxis.data = barData.map(v => v.type);
      option1.series[0].data = barData.map(v => v.amount);
      option1.series[1].data = barData.map(v => v.count);
      barChart.setOption(option1);
    })

    // 修改pieCharts请求的回调函数
    request.get('/pieCharts').then(pieRes => {
    const pieData = pieRes.data?.data || [];
    
    // 按交易金额占比排序，取前5个用户数据
    const top5Users = [...pieData]
    .sort((a, b) => b.amountRatio - a.amountRatio) // 按交易金额占比降序排序
    .slice(0, 5); // 取前5个用户
    
    // 生成图例数据
    const legendData = top5Users.map(v => v.name);
    
    // 处理内环数据（数量占比）
    const innerData = top5Users.map(v => ({
    name: v.name,
    value: (v.countRatio * 100).toFixed(2)
    }));
    
    // 处理外环数据（金额占比）
    const outerData = top5Users.map(v => ({
    name: v.name,
    value: (v.amountRatio * 100).toFixed(2)
    }));
    
    // 更新配置
    option2.legend.data = legendData;
    option2.series[0].data = innerData;
    option2.series[1].data = outerData;
    
    // 确保调用setOption方法应用配置
    pieChart.setOption(option2);
    });

    request.get('/scatterCharts').then(scatterRes => {
      // 添加数据校验
      const responseData = scatterRes.data || {};

      // 使用安全数据访问方式
      const rawData = Array.isArray(responseData.data) ? responseData.data : [];
      const references = Array.isArray(responseData.references) ? responseData.references : [];

      // 转换数据结构为ECharts所需格式
      const scatterData = rawData.map(item => [
        item.x || 0,          // 小时数
        item.y || '1970-01-01', // 日期
        item.value || 0       // 订单量
      ]);

      // 提取所有日期并排序
      const dateSet = new Set(rawData.map(item => item.y));
      const sortedDates = Array.from(dateSet).sort((a, b) =>
          new Date(a) - new Date(b)
      );

      // 安全更新配置
      if(option3.series && option3.series[0]){
        option3.series[0].data = scatterData;


      }

      // 更新可视化配置
      if(option3.visualMap){
        const maxValue = Math.max(...rawData.map(item => item.value || 0));
        option3.visualMap.max = maxValue > 0 ? maxValue : 50;
      }

      // 设置更新后的配置
      scatterChart.setOption(option3);

      // 优化resize处理
      const handleResize = () => {
        scatterChart.resize();
        lineChart.resize();
        barChart.resize();
        pieChart.resize();
      };

      window.addEventListener('resize', handleResize);

      // 添加卸载处理
      this.$once('hook:beforeUnmount', () => {
        window.removeEventListener('resize', handleResize);
      });
    }).catch(error => {
      console.error('散点图数据加载失败:', error);
      // 可在此添加错误状态提示
    });
  },
  methods:{}
})

</script>

<style scoped>
/* 热力图容器优化 */
#scatter {
  position: relative;
  padding: 20px 0;
}

/* GitHub风格提示框 */
.echarts-tooltip {
  background: rgba(255,255,255,0.95) !important;
  border: 1px solid #e1e4e8 !important;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12) !important;
  border-radius: 6px !important;
  padding: 12px !important;
  font-family: -apple-system,BlinkMacSystemFont,"Segoe UI",Helvetica,Arial,sans-serif;
}

/* 图例样式 */
.echarts-visual-map-piece {
  border-radius: 2px !important;
  margin: 0 2px !important;
}
</style>