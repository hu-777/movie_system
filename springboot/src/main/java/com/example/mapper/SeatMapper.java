package com.example.mapper;

import com.example.entity.Seat;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SeatMapper {
    
    List<Seat> selectByScreeningId(@Param("screeningId") Integer screeningId);

    List<Seat> selectById(@Param("id") Integer id);
    
    int updateSeatStatus(@Param("id") Integer id, @Param("status") String status);

    int batchUpdateSeatStatus(@Param("seatIds") List<Integer> seatIds, @Param("status") String status);
    
    // 添加插入座位方法
    void insert(Seat seat);
}