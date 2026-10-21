package com.example.controller;

import com.example.common.Result;
import com.example.entity.GoodsStock;
import com.example.service.GoodsStockService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 系统公告前端操作接口
 **/
@RestController
@RequestMapping("/goodsStock")
public class GoodsStockController {

    @Resource
    private GoodsStockService goodsStockService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody GoodsStock goodsStock) {
        goodsStockService.add(goodsStock);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        goodsStockService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody GoodsStock goodsStock) {
        goodsStockService.updateById(goodsStock);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        GoodsStock goodsStock = goodsStockService.selectById(id);
        return Result.success(goodsStock);
    }

//    /**
//     * 查询所有
//     */
//    @GetMapping("/selectAll")
//    public Result selectAll(GoodsStock goodsStock) {
//        List<GoodsStock> list = goodsStockService.selectAll(goodsStock);
//        return Result.success(list);
//    }
//
//    /**
//     * 分页查询
//     */
//    @GetMapping("/selectPage")
//    public Result selectPage(GoodsStock goodsStock,
//                             @RequestParam(defaultValue = "1") Integer pageNum,
//                             @RequestParam(defaultValue = "10") Integer pageSize) {
//        PageInfo<GoodsStock> page = goodsStockService.selectPage(goodsStock, pageNum, pageSize);
//        return Result.success(page);
//    }


    @GetMapping("/selectAll")
    public Result selectAll(GoodsStock goodsStock) {
        try {
            List<GoodsStock> list = goodsStockService.selectAll(goodsStock);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整的错误日志
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/selectPage")
    public Result selectPage(GoodsStock goodsStock,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageInfo<GoodsStock> page = goodsStockService.selectPage(goodsStock, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整的错误日志
            return Result.fail("分页查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/selectRanking")
    public Result selectRanking(GoodsStock goodsStock) {
        List<GoodsStock> list = goodsStockService.selectRanking(goodsStock);
        return Result.success(list);
    }

}