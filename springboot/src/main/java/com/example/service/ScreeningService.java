package com.example.service;

import com.example.entity.GoodsStock;
import com.example.entity.Screening;
import com.example.entity.Seat;
import com.example.mapper.ScreeningMapper;
import com.example.mapper.SeatMapper;
import com.example.mapper.GoodsStockMapper;
import com.example.mapper.OrdersMapper;
import com.example.exception.CustomException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 场次管理服务
 */
@Service
public class ScreeningService {
    @Resource private GoodsStockMapper goodsStockMapper;
    @Resource private OrdersMapper ordersMapper;

    private void validate(Screening screening) {
        if (screening.getGoodsId() == null || screening.getStartTime() == null ||
            screening.getHallName() == null || screening.getHallName().trim().isEmpty() ||
            screening.getPrice() == null || screening.getPrice() < 0)
            throw new CustomException("请填写完整的场次信息");
        if (screening.getEndTime() != null && !screening.getEndTime().after(screening.getStartTime()))
            throw new CustomException("结束时间必须晚于开始时间");
    }

    @Resource
    private ScreeningMapper screeningMapper;

    @Resource
    private SeatMapper seatMapper;

    @Resource
    private GoodsStockService goodsStockService;

    /**
     * 新增场次（并自动生成座位）
     */
    @Transactional
    public void add(Screening screening) {
        validate(screening);
        if (goodsStockMapper.selectForUpdate(screening.getGoodsId()) == null)
            throw new CustomException("电影不存在");
        // 1. 定义影厅规格 (默认 6排 * 8列 = 48座)
        int rows = 12;
        int cols = 12;
        int totalSeats = rows * cols;

        // 2. 补全场次信息
        screening.setTotalSeats(totalSeats);
        screening.setAvailableSeats(totalSeats);

        // 3. 插入场次 (获取生成的ID)
        screeningMapper.insert(screening);

        // 4. 自动生成座位数据
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                Seat seat = new Seat();
                seat.setScreeningId(screening.getId());

                // 行号转换: 1 -> "A", 2 -> "B"
                String rowStr = String.valueOf((char) ('A' + (i - 1)));

                // 使用 Seat.java 中定义的正确 Setter 方法
                seat.setSeatRow(rowStr);
                seat.setSeatColumn(j);

                // 拼接座位名，如 "A1"
                seat.setSeatName(rowStr + j);
                seat.setStatus("AVAILABLE"); // 默认可用

                seatMapper.insert(seat);
            }
        }
        goodsStockMapper.syncTickets(screening.getGoodsId());
    }

    /**
     * 修改场次
     */
    @Transactional
    public void updateById(Screening screening) {
        validate(screening);
        goodsStockMapper.selectForUpdate(screening.getGoodsId());
        Screening existing = screeningMapper.selectForUpdate(screening.getId());
        if (existing == null) throw new CustomException("场次不存在");
        if (!existing.getGoodsId().equals(screening.getGoodsId())) throw new CustomException("不能更换场次所属电影");
        if (ordersMapper.countByScreeningId(screening.getId()) > 0)
            throw new CustomException("场次已有订单，不能直接修改排片");
        screening.setTotalSeats(existing.getTotalSeats());
        screening.setAvailableSeats(existing.getAvailableSeats());
        screeningMapper.updateById(screening);
    }

    /**
     * 删除场次
     */
    @Transactional
    public void deleteById(Integer id) {
        Screening existing = screeningMapper.selectById(id);
        if (existing == null) throw new CustomException("场次不存在");
        goodsStockMapper.selectForUpdate(existing.getGoodsId());
        screeningMapper.selectForUpdate(id);
        if (ordersMapper.countByScreeningId(id) > 0) throw new CustomException("场次已有订单，不能删除");
        seatMapper.deleteByScreeningId(id);
        // 这里建议先删座位，再删场次，防止脏数据
        // seatMapper.deleteByScreeningId(id); (如果你有这个接口的话)
        screeningMapper.deleteById(id);
        goodsStockMapper.syncTickets(existing.getGoodsId());
    }

    /**
     * 分页查询（后台列表用）
     */
    public PageInfo<Screening> selectPage(Screening screening, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Screening> list = screeningMapper.selectAll(screening);
        return PageInfo.of(list);
    }

    // ================== 以下是查询相关方法 ==================

    /**
     * 根据ID获取场次信息
     */
    public Screening getById(Integer id) {
        if (id == null || id <= 0) return null;
        return screeningMapper.selectById(id);
    }

    /**
     * 根据电影ID获取场次列表
     */
//    public List<Screening> getByGoodsId(Integer goodsId) {
//        if (goodsId == null || goodsId <= 0) return null;
//        return screeningMapper.selectByGoodsId(goodsId);
//    }

    /**
     * 更新场次可用座位数
     */
    public void updateAvailableSeats(Integer id, Integer availableSeats) {
        screeningMapper.updateAvailableSeats(id, availableSeats);
    }

    /**
     * 检查是否可以购买该场次的票（开场前5分钟内不能购买）
     */
    public boolean canBuyTicket(Integer screeningId) {
        Screening screening = getById(screeningId);
        if (screening == null) return false;

        Date now = new Date();
        Date startTime = screening.getStartTime();
        if (startTime == null) return false;

        long diffMinutes = (startTime.getTime() - now.getTime()) / (1000 * 60);
        return diffMinutes >= 5;
    }

    /**
     * 获取电影的最近场次 (就是这个方法，之前可能被漏掉了)
     */
    public Screening getNearestScreeningByGoodsId(Integer goodsId) {
        return screeningMapper.getNearestScreeningByGoodsId(goodsId);
    }

    /**
     * 计算某电影所有场次的剩余座位总和
     */
    public Integer countTotalTickets(Integer goodsId) {
        List<Screening> list = screeningMapper.selectByGoodsId(goodsId);

        if (list == null || list.isEmpty()) {
            return 0;
        }

        int total = 0;
        Date now = new Date(); // 获取当前时间

        for (Screening s : list) {
            // 只有当场次开始时间晚于当前时间 (s.getStartTime() > now) 时，才累加余票
            if (s.getStartTime() != null && s.getStartTime().after(now)) {
                if (s.getAvailableSeats() != null) {
                    total += s.getAvailableSeats();
                }
            }
        }
        return total;
    }

    /**
     * 根据电影ID获取场次列表
     * 【修改逻辑】：如果场次已过期，强制将返回给前端的 availableSeats 设为 0
     */
    public List<Screening> getByGoodsId(Integer goodsId) {
        if (goodsId == null || goodsId <= 0) return null;

        List<Screening> list = screeningMapper.selectByGoodsId(goodsId);

        Date now = new Date();
        for (Screening s : list) {
            // 如果当前时间已经超过了开场时间
            if (s.getStartTime() != null && s.getStartTime().before(now)) {
                s.setAvailableSeats(0); // 视觉上设为0（不修改数据库，只修改返回数据）
            }
        }

        return list;
    }
}
