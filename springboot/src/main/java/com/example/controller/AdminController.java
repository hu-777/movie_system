package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.service.AdminServiceImpl;
import com.github.pagehelper.PageInfo;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/admin")   //  定义路径
public class AdminController {

    @Resource
    private AdminServiceImpl adminServiceImpl;

    @PostMapping("/add")    //  添加管理员
    public Result add(@RequestBody Admin admin) {
        adminServiceImpl.add(admin);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")  //  根据ID删除管理员
    public Result deleteById(@PathVariable Integer id) {
        adminServiceImpl.removeById(id);
        return Result.success();
    }

    @PutMapping("/update")  //  更新管理员信息
    public Result updateById(@RequestBody Admin admin) {
        adminServiceImpl.updateById(admin);
        return Result.success();
    }

    @GetMapping("/selectById/{id}") //  根据ID查找管理员
    public Result selectById(@PathVariable Integer id) {
        Admin admin = adminServiceImpl.getById(id);
        return Result.success(admin);
    }

    @GetMapping("/selectAll")   // 获取所有管理员的记录
    public Result selectAll() {
        List<Admin> list = adminServiceImpl.list();
        return Result.success(list);
    }

    @GetMapping("/selectPage")  //  根据名称分页获取管理员记录
    //  当前页码默认为第一页，每页默认显示十个记录，可根据 name 查询记录
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String name) {
        PageInfo<Admin> page = adminServiceImpl.selectPage(pageNum, pageSize, name);
        return Result.success(page);
    }
}