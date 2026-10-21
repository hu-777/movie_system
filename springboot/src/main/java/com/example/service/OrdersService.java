package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdersService {
    @Resource private OrdersMapper ordersMapper;
    @Resource private GoodsStockMapper goodsStockMapper;
    @Resource private SeatMapper seatMapper;
    @Resource private ScreeningMapper screeningMapper;

    // Legacy non-seat orders retain their existing behavior with validated quantities.
    @Transactional
    public void add(Orders order) {
        if (order.getNum() == null || order.getNum() <= 0 || order.getUserId() == null || order.getUserId() <= 0)
            throw new CustomException("订单参数无效");
        if (order.getScreeningId() != null) throw new CustomException("场次购票请使用选座入口");
        GoodsStock movie = goodsStockMapper.selectForUpdate(order.getGoodsId());
        if (movie == null) throw new CustomException("电影不存在");
        if (movie.getTickets() < order.getNum()) throw new CustomException("电影票库存不足");
        order.setOrderNo(IdUtil.fastSimpleUUID());
        order.setTime(DateUtil.now());
        order.setStatus("待支付");
        order.setTotalPrice(Math.multiplyExact(movie.getPrice(), order.getNum()));
        movie.setTickets(movie.getTickets() - order.getNum());
        goodsStockMapper.updateById(movie);
        if (ordersMapper.insert(order) != 1) throw new CustomException("订单创建失败");
    }

    @Transactional
    public void deleteById(Integer id) {
        Orders order = ordersMapper.selectForUpdate(id);
        if (order == null) throw new CustomException("订单不存在");
        if (!Arrays.asList("已取消", "已退票").contains(order.getStatus()))
            throw new CustomException("请先取消或完成退票，再删除订单");
        ordersMapper.deleteById(id);
    }

    @Transactional
    public void updateById(Orders incoming) {
        Orders order = ordersMapper.selectForUpdate(incoming.getId());
        if (order == null) throw new CustomException("订单不存在");
        String previous = order.getStatus(), next = incoming.getStatus();
        if (Objects.equals(previous, next)) return; // Idempotent retries must not restore stock twice.
        boolean allowed = ("待支付".equals(previous) && Arrays.asList("待取票", "已取消").contains(next))
            || ("待取票".equals(previous) && Arrays.asList("已完成", "待退票").contains(next))
            || ("已完成".equals(previous) && "待退票".equals(next))
            || ("待退票".equals(previous) && Arrays.asList("已退票", "待取票", "已完成").contains(next));
        if (!allowed) throw new CustomException("订单状态已变化，请刷新后重试");
        if ("已取消".equals(next) || "已退票".equals(next)) {
            GoodsStock movie = goodsStockMapper.selectForUpdate(order.getGoodsId());
            if (order.getScreeningId() != null) {
                Screening screening = screeningMapper.selectForUpdate(order.getScreeningId());
                if (screening == null) throw new CustomException("场次不存在，无法恢复座位");
                List<Seat> seats = resolveSeats(screening.getId(), order.getSeatInfo());
                if (seats.size() != order.getNum()) throw new CustomException("订单座位信息不一致");
                for (Seat seat : seats) {
                    if (seatMapper.transition(seat.getId(), screening.getId(), "SOLD", "AVAILABLE") != 1)
                        throw new CustomException("座位状态不一致，请刷新后重试");
                }
                screeningMapper.updateAvailableSeats(screening.getId(), screening.getAvailableSeats() + seats.size());
                if (movie != null) goodsStockMapper.syncTickets(movie.getId());
            } else if (movie != null) {
                movie.setTickets(movie.getTickets() + order.getNum());
                goodsStockMapper.updateById(movie);
            }
        }
        // Only status may change: prices, ownership and seat details remain server-owned.
        order.setStatus(next);
        if (ordersMapper.updateById(order) != 1) throw new CustomException("订单更新失败");
    }

    private List<Seat> resolveSeats(Integer screeningId, String seatInfo) {
        if (seatInfo == null || seatInfo.trim().isEmpty() || seatInfo.length() > 500)
            throw new CustomException("请选择有效座位");
        String[] names = seatInfo.split(",", -1);
        Map<String, Seat> available = seatMapper.selectByScreeningId(screeningId).stream()
            .collect(Collectors.toMap(Seat::getSeatName, seat -> seat));
        Set<String> unique = new HashSet<>();
        List<Seat> selected = new ArrayList<>();
        for (String raw : names) {
            String name = raw.trim();
            if (!unique.add(name)) throw new CustomException("座位不能重复");
            Seat seat = available.get(name);
            if (seat == null) throw new CustomException("找不到座位：" + name);
            selected.add(seat);
        }
        selected.sort(Comparator.comparing(Seat::getId));
        return selected;
    }

    @Transactional
    public boolean addOrderWithSeats(Integer goodsId, Integer screeningId, Integer userId, List<Integer> ignoredSeatIds, String seatInfo) {
        if (screeningId == null || screeningId <= 0 || userId == null || userId <= 0)
            throw new CustomException("场次或用户信息无效");
        Screening snapshot = screeningMapper.selectById(screeningId);
        if (snapshot == null) throw new CustomException("场次不存在");
        if (!Objects.equals(snapshot.getGoodsId(), goodsId)) throw new CustomException("电影与场次不匹配");
        // Consistent lock order: movie, then screening, then seats. Shared movie stock
        // remains consistent even when different screenings are purchased concurrently.
        GoodsStock movie = goodsStockMapper.selectForUpdate(snapshot.getGoodsId());
        if (movie == null) throw new CustomException("电影不存在");
        Screening screening = screeningMapper.selectForUpdate(screeningId);
        if (screening == null || !Objects.equals(screening.getGoodsId(), goodsId)) throw new CustomException("场次已变更，请重试");
        if (screening.getStartTime() == null || screening.getStartTime().getTime() - System.currentTimeMillis() <= 300000)
            throw new CustomException("开场前5分钟停止售票");
        if (screening.getPrice() == null || screening.getPrice() < 0) throw new CustomException("场次价格无效");
        List<Seat> seats = resolveSeats(screeningId, seatInfo);
        if (screening.getAvailableSeats() == null || screening.getAvailableSeats() < seats.size()) throw new CustomException("场次余票不足");
        for (Seat seat : seats) {
            if (seatMapper.transition(seat.getId(), screeningId, "AVAILABLE", "SOLD") != 1)
                throw new CustomException("座位已被占用：" + seat.getSeatName());
        }
        screeningMapper.updateAvailableSeats(screeningId, screening.getAvailableSeats() - seats.size());
        goodsStockMapper.syncTickets(goodsId);
        Orders order = new Orders();
        order.setGoodsId(screening.getGoodsId());
        order.setScreeningId(screeningId);
        order.setUserId(userId);
        order.setNum(seats.size());
        order.setTotalPrice(Math.multiplyExact(screening.getPrice(), seats.size()));
        order.setOrderNo(IdUtil.fastSimpleUUID());
        order.setStatus("待支付");
        order.setTime(DateUtil.now());
        order.setSeatInfo(seats.stream().map(Seat::getSeatName).collect(Collectors.joining(",")));
        order.setHallName(screening.getHallName());
        order.setShowTime(DateUtil.format(screening.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
        if (ordersMapper.insert(order) != 1) throw new CustomException("订单创建失败");
        // Let exceptions propagate so Spring rolls back seats, counts and order together.
        return true;
    }

    public Orders selectById(Integer id) { return ordersMapper.selectById(id); }
    public List<Orders> selectAll(String role, Integer userId) { return ordersMapper.selectAll(role, userId); }
    public List<Orders> selectAllOrders() { return ordersMapper.selectAllOrders(); }
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize, Integer userId, String role) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(ordersMapper.selectPage(orders, userId, role));
    }
}
