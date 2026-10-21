package com.example.controller;

import com.example.common.Result;
import com.example.entity.Screening;
import com.example.service.ScreeningService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/screening")
public class ScreeningController {

    @Autowired
    private ScreeningService screeningService;

    // === 新增：添加场次 ===
    @PostMapping("/add")
    public Result add(@RequestBody Screening screening) {
        screeningService.add(screening);
        return Result.success();
    }

    // === 新增：删除场次 ===
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        screeningService.deleteById(id);
        return Result.success();
    }

    // === 新增：修改场次 ===
    @PutMapping("/update")
    public Result update(@RequestBody Screening screening) {
        screeningService.updateById(screening);
        return Result.success();
    }

    // === 新增：分页查询 ===
    @GetMapping("/page")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             Screening screening) {
        PageInfo<Screening> page = screeningService.selectPage(screening, pageNum, pageSize);
        return Result.success(page);
    }

    // === 原有接口保持不变 ===
    @GetMapping("/getById")
    public Result getById(@RequestParam Integer id) {
        Screening screening = screeningService.getById(id);
        return Result.success(screening);
    }
    @GetMapping("/canBuyTicket")
    public Result canBuyTicket(@RequestParam Integer screeningId) {
        boolean canBuy = screeningService.canBuyTicket(screeningId);
        return Result.success(canBuy);
    }
    @GetMapping("/getByGoodsId")
    public Result getByGoodsId(@RequestParam Integer goodsId) {
        List<Screening> screenings = screeningService.getByGoodsId(goodsId);
        return Result.success(screenings);
    }
}