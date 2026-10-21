package com.example.mapper;

import com.example.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrdersMapper {
    @Select("select * from orders where id = #{id} for update")
    Orders selectForUpdate(Integer id);
    @Select("select count(*) from orders where screening_id = #{id}")
    int countByScreeningId(Integer id);
    int insert(Orders orders);

    @Delete("delete from orders where id = #{id}")
    int deleteById(Integer id);

    int updateById(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders selectById(Integer id);

    List<Orders> selectAll(@Param("userRole") String userRole, @Param("userId") Integer userId);
    List<Orders> selectAllOrders();

    List<Orders> selectPage(@Param("orders") Orders orders, @Param("userId") Integer userId, @Param("userRole") String userRole);
}
