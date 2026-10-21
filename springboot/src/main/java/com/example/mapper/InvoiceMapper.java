package com.example.mapper;

import com.example.entity.Invoice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InvoiceMapper {
    int insert(Invoice invoice);

    @Delete("delete from invoice where id = #{id}")
    int deleteById(Integer id);

    int updateById(Invoice invoice);

    @Select("select * from invoice where id = #{id}")
    Invoice selectById(Integer id);

    List<Invoice> selectAll(@Param("userRole") String userRole, @Param("userId") Integer userId);

    List<Invoice> selectPage(@Param("invoice") Invoice invoice, @Param("userId") Integer userId, @Param("userRole") String userRole);

    // 新增接口方法
    @Select("select * from invoice where order_no = #{orderNo}")
    Invoice selectByOrderNo(String orderNo);

}