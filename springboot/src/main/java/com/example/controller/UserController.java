package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserServiceImpl;
import javax.annotation.Resource;

import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserServiceImpl userServiceImpl;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userServiceImpl.add(user);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        userServiceImpl.removeById(id); // 更改为 mybatis-plus 中方法
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody User user) {
        userServiceImpl.updateById(user);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userServiceImpl.getById(id);
        return Result.success(user);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<User> list = userServiceImpl.list(); // 更改为 mybatis-plus 中方法
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String name) {
        PageInfo<User> page = userServiceImpl.selectPage(pageNum, pageSize, name);
        return Result.success(page);
    }


}