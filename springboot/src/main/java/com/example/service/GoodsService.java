package com.example.service;

import com.example.entity.Goods;
import com.example.entity.Orders;
import com.example.exception.CustomException;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrdersMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.example.mapper.CategoryMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统公告业务处理
 **/
@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private GoodsService goodsService;

    /**
     * 新增
     */
    public void add(Goods goods) {
        goodsMapper.insert(goods);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Goods goods) {

        goodsMapper.updateById(goods);
    }

    /**
     * 根据ID查询
     */
    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    /**
     * 分页查询
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }

}