package com.example.controller;

import com.example.common.Result;
import com.example.entity.Seat;
import com.example.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    /**
     * 根据场次ID获取座位信息
     * @param screeningId 场次ID
     * @return 座位列表
     */
    @GetMapping("/selectByScreeningId")
    public Result selectByScreeningId(@RequestParam Integer screeningId) {
        System.out.println("接收到座位查询请求，screeningId: " + screeningId);
        
        if (screeningId == null || screeningId <= 0) {
            System.err.println("无效的screeningId: " + screeningId);
            return Result.error("无效的场次ID");
        }
        
        try {
            List<Seat> seats = seatService.getByScreeningId(screeningId);
            
            if (seats == null) {
                System.err.println("座位查询返回null，可能存在数据库异常");
                return Result.error("座位查询失败");
            }
            
            System.out.println("座位查询成功，返回座位数量: " + seats.size());
            return Result.success(seats);
        } catch (Exception e) {
            System.err.println("座位查询控制器异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("座位查询异常: " + e.getMessage());
        }
    }

    /**
     * 根据座位ID获取座位信息
     * @param id 座位ID
     * @return 座位信息
     */
    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        System.out.println("接收到座位查询请求，座位id: " + id);
        
        if (id == null || id <= 0) {
            System.err.println("无效的座位id: " + id);
            return Result.error("无效的座位ID");
        }
        
        try {
            List<Seat> seats = seatService.getById(id);
            
            if (seats == null || seats.isEmpty()) {
                System.err.println("未查询到座位数据，座位id: " + id);
                return Result.error("未找到指定座位");
            }
            
            // 由于id是唯一的，应该只返回一个座位
            Seat seat = seats.get(0);
            System.out.println("座位查询成功，座位信息: " + seat.getSeatName());
            return Result.success(seat);
        } catch (Exception e) {
            System.err.println("座位查询控制器异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("座位查询异常: " + e.getMessage());
        }
    }

    /**
     * 根据座位ID更新座位状态
     * @param id 座位ID
     * @param status 座位状态
     * @return 操作结果
     */
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestParam Integer id, @RequestParam String status) {
        boolean success = seatService.updateSeatStatus(id, status);
        return success ? Result.success() : Result.error("更新失败");
    }
}