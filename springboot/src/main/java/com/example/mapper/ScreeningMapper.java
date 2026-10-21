package com.example.mapper;

import com.example.entity.Screening;
import java.util.List;

public interface ScreeningMapper {
    
    List<Screening> selectByGoodsId(Integer goodsId);
    
    Screening selectById(Integer id);
    
    int updateAvailableSeats(Integer id, Integer availableSeats);
    
    // 添加新方法
    Integer getMaxHallNumber();
    
    void insert(Screening screening);
    
    // 添加查询电影最近场次的方法
    Screening getNearestScreeningByGoodsId(Integer goodsId);
    
    // 添加删除指定电影所有场次的方法
    void deleteByGoodsId(Integer goodsId);

// === 必须补充以下方法 ===

    // 更新
    int updateById(Screening screening);

    // 删除
    int deleteById(Integer id);

    // 查询所有（用于分页）
    List<Screening> selectAll(Screening screening);
}