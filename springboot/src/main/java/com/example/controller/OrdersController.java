package com.example.controller;

import com.example.common.Result;
import com.example.entity.Orders;
import com.example.service.OrdersService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Resource
    private OrdersService ordersService;

    @PostMapping("/add")
    public Result add(@RequestBody Orders orders) {
        ordersService.add(orders);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        ordersService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Orders orders = ordersService.selectById(id);
        return Result.success(orders);
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam String userRole, @RequestParam Integer userId) {
        List<Orders> list = ordersService.selectAll(userRole, userId);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(Orders orders,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam Integer userId,
                             @RequestParam String userRole,
                             @RequestParam(required = false) String goodsName) {
        // 设置电影名称到Orders对象
        if (goodsName != null && !goodsName.isEmpty()) {
            orders.setGoodsName(goodsName);
        }
        // 添加日志输出，检查参数
        System.out.println("查询订单参数 - userId: " + userId + ", userRole: " + userRole + ", goodsName: " + goodsName);
        PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize, userId, userRole);
        // 输出查询结果数量
        System.out.println("查询到的订单数量: " + (page.getList() != null ? page.getList().size() : 0));
        return Result.success(page);
    }

    // 在OrdersController中添加选座购买的方法
    // 修改选座购买接口，移除seatIds参数
    // 修改addWithSeats方法
    @PostMapping("/addWithSeats")
    public Result addWithSeats(@RequestBody Orders request) {
        ordersService.addOrderWithSeats(request.getGoodsId(), request.getScreeningId(),
                request.getUserId(), null, request.getSeatInfo());
        return Result.success();
    }
    // 修改批量导出发票接口，确保返回完整的订单信息
    @PostMapping("/exportBatchInvoices")
    public Result exportBatchInvoices(@RequestBody Map<String, Object> params) {
        try {
            List<Integer> orderIds = (List<Integer>) params.get("orderIds");
            if (orderIds == null || orderIds.isEmpty()) {
                return Result.error("请选择要导出的订单");
            }
            
            // 批量导出电影票数据
            List<Orders> orderList = new ArrayList<>();
            for (Integer orderId : orderIds) {
                Orders order = ordersService.selectById(orderId);
                if (order != null) {
                    // 确保返回完整的订单信息，包括座位信息、影厅信息等
                    orderList.add(order);
                }
            }
            
            return Result.success(orderList);
        } catch (Exception e) {
            return Result.error("导出失败: " + e.getMessage());
        }
    }
}