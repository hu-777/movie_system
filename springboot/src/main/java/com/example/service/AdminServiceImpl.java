package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 管理员业务处理
 **/
@Transactional // 添加注解管理事务
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper,Admin> implements AdminService{

    @Resource
    private AdminMapper adminMapper;

    //  注入 BCryptPasswordEncoder 进行密码加密和验证
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Admin find(String username) {   //  根据用户名查找表中记录的方法，返回用户实例
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);  //  创建相等条件构造器实例
        return adminMapper.selectOne(queryWrapper);
    }

    public void add(Admin admin) {
        Admin dbAdmin = find(admin.getUsername());
        //  如果用户已存在则抛出异常
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException("用户已存在");
        }
        //  如果未提供密码，设为 admin
        com.example.security.IdentityService.passwordPolicy(admin.getPassword());
        // 如果未提供姓名，设置默认姓名为用户名
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        //  密码加密
        String encryptedPassword = passwordEncoder.encode(admin.getPassword());
        admin.setPassword(encryptedPassword);
        //  设置角色身份并插入表中
        admin.setRole("ADMIN");
        adminMapper.insert(admin);
    }

    //  可根据用户名查询记录
    public PageInfo<Admin> selectPage(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);     // 使用 PageHelper 中的 startPage 方法进行分页
        // 创建构造器
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        // 若传入了 name，则根据用户名进行模糊查询
        if (ObjectUtil.isNotEmpty(name)) {
            queryWrapper.like("name", name);
        }
        List<Admin> list = adminMapper.selectList(queryWrapper);    // 如果 name 为空，则显示所有记录
        return PageInfo.of(list);
    }

    public Account login(Account account) {
        Admin dbAdmin = find(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        //  如果密码不正确，则抛出异常
        if (!passwordEncoder.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbAdmin;
    }

    public void updatePassword(Account account) {
        Admin dbAdmin = find(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        if (!passwordEncoder.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException("原密码错误");
        }
        com.example.security.IdentityService.passwordPolicy(account.getNewPassword());
        dbAdmin.setPassword(passwordEncoder.encode(account.getNewPassword()));
        adminMapper.updateById(dbAdmin);
    }
}