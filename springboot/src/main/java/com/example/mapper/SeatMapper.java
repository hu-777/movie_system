package com.example.mapper;

import com.example.entity.Seat;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SeatMapper {
    @org.apache.ibatis.annotations.Delete("delete from seat where screening_id = #{id}")
    int deleteByScreeningId(Integer id);
    @org.apache.ibatis.annotations.Update("update seat set status = #{next} where id = #{id} and screening_id = #{screeningId} and status = #{previous}")
    int transition(@Param("id") Integer id, @Param("screeningId") Integer screeningId,
                   @Param("previous") String previous, @Param("next") String next);
    
    List<Seat> selectByScreeningId(@Param("screeningId") Integer screeningId);

    List<Seat> selectById(@Param("id") Integer id);
    
    int updateSeatStatus(@Param("id") Integer id, @Param("status") String status);

    int batchUpdateSeatStatus(@Param("seatIds") List<Integer> seatIds, @Param("status") String status);
    
    // 添加插入座位方法
    void insert(Seat seat);
}
