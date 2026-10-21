package com.example.mapper;

import com.example.entity.GoodsStock;
import java.util.List;

/**
 * GoodsStockMapper 接口
 */
public interface GoodsStockMapper {

    // 插入
    int insert(GoodsStock goodsStock);

    // 删除（注解方式删除）
    // 删除注解，避免重复定义
    int deleteById(Integer id);

    // 修改
    int updateById(GoodsStock goodsStock);

    // 根据 ID 查询
    GoodsStock selectById(Integer id);

    // 查询所有
    List<GoodsStock> selectAll(GoodsStock goodsStock);

    List<GoodsStock> selectAllUpcoming();
}
