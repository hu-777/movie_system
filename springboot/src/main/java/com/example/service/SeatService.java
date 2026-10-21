package com.example.service;

import com.example.entity.Seat;
import com.example.mapper.SeatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    private SeatMapper seatMapper;

    /**
     * 根据场次ID获取该场次的所有座位
     * @param screeningId 场次ID (1-92)
     * @return 座位列表
     */
    public List<Seat> getByScreeningId(Integer screeningId) {
        System.out.println("开始查询座位信息，screeningId: " + screeningId);
        try {
            List<Seat> seats = seatMapper.selectByScreeningId(screeningId);
            if (seats == null || seats.isEmpty()) {
                System.err.println("未查询到场次数据，screeningId: " + screeningId);
            } else {
                System.out.println("查询到的场次数量: " + seats.size());
            }
            return seats;
        } catch (Exception e) {
            System.err.println("座位查询异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据座位ID获取座位信息
     * @param id 座位ID (每个座位的唯一标识符)
     * @return 座位列表(通常只有一个座位)
     */
    public List<Seat> getById(Integer id) {
        System.out.println("开始查询座位信息，座位id: " + id);
        try {
            // 根据座位id查询单个座位信息
            List<Seat> seats = seatMapper.selectById(id);
            if (seats == null || seats.isEmpty()) {
                System.err.println("未查询到座位数据，座位id: " + id);
            } else {
                System.out.println("查询到的座位数量: " + seats.size());
                // 由于id是唯一的，理论上应该只返回一个座位
                if (seats.size() > 1) {
                    System.out.println("警告：查询到多个座位数据，座位id: " + id);
                }
            }
            return seats;
        } catch (Exception e) {
            System.err.println("座位查询异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新单个座位状态
     * @param id 座位ID
     * @param status 座位状态
     * @return 是否更新成功
     */
    public boolean updateSeatStatus(Integer id, String status) {
        return seatMapper.updateSeatStatus(id, status) > 0;
    }

    /**
     * 批量更新座位状态
     * @param seatIds 座位ID列表
     * @param status 座位状态
     * @return 是否更新成功
     */
    public boolean batchUpdateSeatStatus(List<Integer> seatIds, String status) {
        return seatMapper.batchUpdateSeatStatus(seatIds, status) > 0;
    }
}