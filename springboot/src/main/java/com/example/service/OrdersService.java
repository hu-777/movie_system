package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.entity.GoodsStock;
import com.example.entity.Orders;
import com.example.entity.Seat;
import com.example.entity.Screening;
import com.example.exception.CustomException;
import com.example.mapper.OrdersMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class OrdersService {
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private GoodsStockService goodsStockService;
    // 注入SeatService以查询实际座位信息
    @Resource
    private SeatService seatService;
    @Resource
    private ScreeningService screeningService;
    
    // 辅助方法：解析座位信息为座位ID列表
    private List<Integer> parseSeatIds(String seatInfo) {
        List<Integer> seatIds = new ArrayList<>();
        if (seatInfo != null && !seatInfo.isEmpty()) {
            String[] seats = seatInfo.split(",");
            for (String seat : seats) {
                // 这里简化处理，实际应用中应该根据座位名称查询对应的ID
                // 这里仅作为示例，假设座位ID生成规则为：行号对应的ASCII值*100+列号
                try {
                    char row = seat.charAt(0);
                    int col = Integer.parseInt(seat.substring(1));
                    int seatId = row * 100 + col;
                    seatIds.add(seatId);
                } catch (Exception e) {
                    // 忽略格式不正确的座位信息
                }
            }
        }
        return seatIds;
    }
    
    /**
     * 根据座位名称和场次ID查找座位ID
     * @param seatName 座位名称，如"A1"
     * @param screeningId 场次ID
     * @return 座位ID，如果找不到返回null
     */
    private Integer getSeatIdByName(String seatName, Integer screeningId) {
        try {
            // 获取该场次的所有座位
            List<Seat> allSeats = seatService.getByScreeningId(screeningId);
            if (allSeats != null && !allSeats.isEmpty()) {
                // 根据座位名称查找对应的座位ID
                for (Seat seat : allSeats) {
                    if (seatName.trim().equals(seat.getSeatName())) {
                        return seat.getId();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("查找座位ID时发生异常: " + e.getMessage());
        }
        return null;
    }
    
    @Transactional
    public void add(Orders orders) {
        String orderNo = IdUtil.fastSimpleUUID();
        orders.setOrderNo(orderNo);
        orders.setTime(DateUtil.now());
        orders.setStatus("待支付");

        GoodsStock goodsStock = goodsStockService.selectById(orders.getGoodsId());
        if (goodsStock == null) {
            throw new CustomException("电影不存在");
        }

        int store = goodsStock.getTickets() - orders.getNum();
        if (store < 0) {
            throw new CustomException("电影票库存不足");
        }

        Integer unitPrice = goodsStockService.getPriceById(orders.getGoodsId());
        orders.setTotalPrice(unitPrice * orders.getNum());

        goodsStock.setTickets(store);
        goodsStockService.updateById(goodsStock);
        ordersMapper.insert(orders);
    }

    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
    }

    // 合并后的updateById方法，包含退票和支付逻辑
    @Transactional
    public void updateById(Orders orders) {
        // 原始订单状态
        Orders originalOrder = ordersMapper.selectById(orders.getId());
    
        // 处理退票逻辑
        if ("待退票".equals(orders.getStatus()) && 
            ("待取票".equals(originalOrder.getStatus()) || "已完成".equals(originalOrder.getStatus()))) {
            // 订单标记为待退票，但库存暂不恢复，等待管理员确认
        }
        // 管理员确认退票
        else if ("已退票".equals(orders.getStatus()) && "待退票".equals(originalOrder.getStatus())) {
            // 恢复电影票库存
            GoodsStock goodsStock = goodsStockService.selectById(originalOrder.getGoodsId());
            if (goodsStock != null) {
                goodsStock.setTickets(goodsStock.getTickets() + originalOrder.getNum());
                goodsStockService.updateById(goodsStock);
            }
            
            // 恢复座位状态
            if (originalOrder.getScreeningId() != null && originalOrder.getSeatInfo() != null) {
                List<Integer> seatIds = parseSeatIds(originalOrder.getSeatInfo());
                if (seatIds != null && !seatIds.isEmpty()) {
                    seatService.batchUpdateSeatStatus(seatIds, "AVAILABLE");
                    
                    // 更新场次可用座位数
                    Screening screening = screeningService.getById(originalOrder.getScreeningId());
                    if (screening != null) {
                        int newAvailableSeats = screening.getAvailableSeats() + seatIds.size();
                        screeningService.updateAvailableSeats(originalOrder.getScreeningId(), newAvailableSeats);
                    }
                }
            }
        }
        // 处理支付逻辑，支付后直接变为待取票
        else if ("待取票".equals(orders.getStatus()) && "待支付".equals(originalOrder.getStatus())) {
            // 这里不需要再次扣除库存，因为在创建订单时已经扣除了座位
            // 但需要确保场次的可用座位数已经正确更新
            System.out.println("订单支付成功: " + orders.getOrderNo());
        }
        
        // 更新订单
        ordersMapper.updateById(orders);
    }

    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    public List<Orders> selectAll(String userRole, Integer userId) {
        return ordersMapper.selectAll(userRole, userId);
    }

    public List<Orders> selectAllOrders() {
        return ordersMapper.selectAllOrders(); // 直接获取全部数据
    }

    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize, Integer userId, String userRole) {
        PageHelper.startPage(pageNum, pageSize);
        // 确保参数命名与Mapper接口一致
        List<Orders> list = ordersMapper.selectPage(orders, userId, userRole);
        return new PageInfo<>(list);
    }
    
    // 新增选座购买方法
    // 修改 addOrderWithSeats 方法，添加开场前5分钟检查
    @Transactional
    public boolean addOrderWithSeats(Integer goodsId, Integer screeningId, Integer userId, List<Integer> seatIds, String seatInfo) {
        try {
            // 参数有效性检查
            System.out.println("开始创建订单: goodsId=" + goodsId + ", screeningId=" + screeningId + 
                              ", userId=" + userId + ", seatInfo=" + seatInfo);
            
            if (screeningId == null || screeningId <= 0) {
                throw new CustomException("场次ID不能为空或无效");
            }
            
            // 先检查场次是否存在
            Screening screening = screeningService.getById(screeningId);
            if (screening == null) {
                throw new CustomException("场次不存在，请刷新页面后重试");
            }
            
            // 检查是否开场前5分钟内
            boolean canBuy = screeningService.canBuyTicket(screeningId);
            if (!canBuy) {
                throw new CustomException("该场次将在5分钟内开始，已停止售票");
            }
            
            // 根据seatInfo解析座位名称，并查询对应的座位ID
            if (seatIds == null || seatIds.isEmpty()) {
                seatIds = new ArrayList<>();
                if (seatInfo != null && !seatInfo.isEmpty()) {
                    String[] seatNames = seatInfo.split(",");
                    // 直接根据座位名称和场次ID查找座位ID
                    for (String seatName : seatNames) {
                        Integer seatId = getSeatIdByName(seatName, screeningId);
                        if (seatId != null) {
                            seatIds.add(seatId);
                        } else {
                            throw new CustomException("找不到座位: " + seatName);
                        }
                    }
                }
            }
            
            // 如果仍然没有找到座位ID，抛出异常
            if (seatIds.isEmpty()) {
                throw new CustomException("无法找到对应的座位信息");
            }
            
            // 检查座位状态 - 修改为对每个座位ID单独检查
            if (seatIds != null && !seatIds.isEmpty()) {
                boolean hasUnavailableSeat = false;
                String unavailableSeatName = "";
                
                // 对每个座位ID单独调用getById检查状态，而不是获取整个场次的座位
                for (Integer seatId : seatIds) {
                    List<Seat> seatList = seatService.getById(seatId);
                    if (seatList != null && !seatList.isEmpty()) {
                        Seat seat = seatList.get(0);
                        // 确保座位属于当前场次且状态为可用
                        if (!seat.getScreeningId().equals(screeningId) || !"AVAILABLE".equals(seat.getStatus())) {
                            hasUnavailableSeat = true;
                            unavailableSeatName = seat.getSeatName();
                            break;
                        }
                    }
                }
                
                if (hasUnavailableSeat) {
                    throw new CustomException("座位已被占用: " + unavailableSeatName);
                }
            } else {
                System.out.println("提示: 无法获取座位状态信息，跳过状态检查");
            }
            
            // 更新座位状态，确保成功执行
            boolean seatUpdateSuccess = seatService.batchUpdateSeatStatus(seatIds, "SOLD");
            if (!seatUpdateSuccess) {
                System.err.println("错误: 更新座位状态失败，订单创建将被回滚");
                throw new CustomException("座位状态更新失败，请稍后重试");
            }
            
            // 更新场次可用座位数
            int newAvailableSeats = screening.getAvailableSeats() - seatIds.size();
            if (newAvailableSeats < 0) {
                newAvailableSeats = 0; // 确保不会出现负数
            }
            screeningService.updateAvailableSeats(screeningId, newAvailableSeats);
            
            // 创建订单
            Orders order = new Orders();
            order.setGoodsId(goodsId);
            order.setUserId(userId);
            order.setNum(seatIds.size());
            order.setTotalPrice(screening.getPrice() * seatIds.size());
            order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
            order.setStatus("待支付"); // 修改这里，从"已完成"改为"待支付"
            order.setTime(DateUtil.now());
            order.setScreeningId(screeningId);
            order.setSeatInfo(seatInfo);
            order.setHallName(screening.getHallName());
            order.setShowTime(screening.getStartTime().toString());
            
            int insertResult = ordersMapper.insert(order);
            if (insertResult <= 0) {
                throw new CustomException("订单创建失败");
            }
            return true;
        } catch (CustomException e) {
            // 记录自定义异常信息
            System.err.println("订单创建失败: " + e.getMsg());
            e.printStackTrace();
            // 事务会自动回滚
            return false;
        } catch (Exception e) {
            // 记录系统异常信息
            System.err.println("订单创建时发生系统错误: " + e.getMessage());
            e.printStackTrace();
            // 事务会自动回滚
            return false;
        }
    }

}