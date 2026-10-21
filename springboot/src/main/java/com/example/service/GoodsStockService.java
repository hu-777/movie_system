package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Goods;
import com.example.entity.GoodsStock;
import com.example.entity.Screening; 
import com.example.exception.CustomException;
import com.example.mapper.GoodsStockMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统公告业务处理
 **/
@Service
public class GoodsStockService {

    @Resource
    private GoodsStockMapper goodsStockMapper;
    
    @Resource
    private ScreeningService screeningService;

    /**
     * 新增
     */
    @Transactional
    public void add(GoodsStock goodsStock) {
        // 1. 插入电影基本信息
        // 注意：这里不需要手动setTickets，因为下面安排场次时会自动计算并更新回来
        goodsStockMapper.insert(goodsStock);

        // 2. 【核心逻辑】自动创建一个默认场次
        Screening defaultScreening = new Screening();

        // 关联刚插入的电影ID
        defaultScreening.setGoodsId(goodsStock.getId());
        defaultScreening.setGoodsName(goodsStock.getName());

        // 设置默认值 (你可以根据需求修改)
        defaultScreening.setHallName("1号厅"); // 默认在1号厅
        defaultScreening.setScreenType("国语2D"); // 默认类型
        defaultScreening.setPrice(goodsStock.getPrice()); // 票价跟随电影定价

        // 设置开始时间：默认为电影的上映时间
        // 如果上映时间为空，就默认设为当前时间
        Date startTime;
        if (goodsStock.getShowtime() != null && !goodsStock.getShowtime().isEmpty()) {
            // 将 String 转为 Date
            startTime = DateUtil.parse(goodsStock.getShowtime());
        } else {
            // 默认为当前时间
            startTime = new Date();
        }

        defaultScreening.setStartTime(startTime);        defaultScreening.setStartTime(startTime);

        // 设置结束时间：默认开始时间往后推2小时 (7200秒 * 1000)
        Date endTime = new Date(startTime.getTime() + 2 * 60 * 60 * 1000);
        defaultScreening.setEndTime(endTime);

        // 3. 调用场次服务 (这会自动生成144个座位，并自动更新GoodsStock的余票数)
        screeningService.add(defaultScreening);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        goodsStockMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(GoodsStock goodsStock) {
        // 确保不允许手动修改余票数
        GoodsStock existing = selectById(goodsStock.getId());
        if (existing != null) {
            goodsStock.setTickets(existing.getTickets());
        }
        goodsStockMapper.updateById(goodsStock);
    }

    /**
     * 根据ID查询
     */
    public GoodsStock selectById(Integer id) {
        GoodsStock goodsStock = goodsStockMapper.selectById(id);
        // 动态更新余票数
        updateTicketsFromNearestScreening(goodsStock);
        return goodsStock;
    }

    /**
     * 查询所有
     */
    public List<GoodsStock> selectAll(GoodsStock goodsStock) {
        List<GoodsStock> list = goodsStockMapper.selectAll(goodsStock);
        // 动态更新每个商品的余票数
        list.forEach(this::updateTicketsFromNearestScreening);
        return list;
    }

    /**
     * 分页查询
     */
    public PageInfo<GoodsStock> selectPage(GoodsStock goodsStock, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<GoodsStock> list = goodsStockMapper.selectAll(goodsStock);
        // 动态更新每个商品的余票数
        list.forEach(this::updateTicketsFromNearestScreening);
        return PageInfo.of(list);
    }

    public Integer getPriceById(Integer goodsId) {
        GoodsStock goodsStock = goodsStockMapper.selectById(goodsId);
        if (goodsStock == null) {
            throw new CustomException("商品不存在");
        }
        return goodsStock.getPrice();
    }

    public List<GoodsStock> selectRanking(GoodsStock goodsStock) {
        List<GoodsStock> goodsList = goodsStockMapper.selectAll(goodsStock);
        // 动态更新每个商品的余票数
        goodsList.forEach(this::updateTicketsFromNearestScreening);
        return goodsList.stream().sorted((g1,g2)->g2.getTickets().compareTo(g1.getTickets())).limit(5).collect(Collectors.toList());
    }

    public List<GoodsStock> selectAllUpcoming() {
        List<GoodsStock> list = goodsStockMapper.selectAllUpcoming();
        // 动态更新每个商品的余票数
        list.forEach(this::updateTicketsFromNearestScreening);
        return list;
    }
    
    /**
     * 从最近场次更新余票数
     */
    private void updateTicketsFromNearestScreening(GoodsStock goodsStock) {
        try {
            // 调用刚才在 ScreeningService 写的新方法
            Integer totalTickets = screeningService.countTotalTickets(goodsStock.getId());

            // 设置给商品对象
            goodsStock.setTickets(totalTickets);

            System.out.println("电影ID: " + goodsStock.getId() + " 总余票更新为: " + totalTickets);

        } catch (Exception e) {
            System.err.println("统计总余票时出错: " + e.getMessage());
            e.printStackTrace();
            goodsStock.setTickets(0);
        }
    }
}