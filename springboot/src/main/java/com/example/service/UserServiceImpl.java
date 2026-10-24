package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional // 添加注解管理事务
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User find(String username) {    // 将private改为public
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(); 
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    // 根据用户名获取密保问题
    public String getSecurityQuestion(String username) {
        User user = find(username);
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("用户不存在");
        }
        return user.getSecurityQuestion();
    }

    // 验证密保答案并重置密码
    public void resetPassword(String username, String securityAnswer, String newPassword) {
        User user = find(username);
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("用户不存在");
        }
        if (ObjectUtil.isEmpty(user.getSecurityQuestion()) || ObjectUtil.isEmpty(user.getSecurityAnswer())) {
            throw new CustomException("该用户未设置密保问题");
        }
        if (securityAnswer == null || !passwordEncoder.matches(securityAnswer, user.getSecurityAnswer())) {
            throw new CustomException("密保答案错误");
        }
        // 验证新密码是否符合正则要求
        if (!validatePassword(newPassword)) {
            throw new CustomException("新密码必须包含至少8个字符，且包含大小写字母、数字和特殊字符");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // 密码验证方法
    private boolean validatePassword(String password) {
        if (password == null || password.length() < 8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        return password.matches(".*[^A-Za-z0-9].*");
    }

    public void add(User user) {
        User dbUser = find(user.getUsername());
        if (ObjectUtil.isNotNull(dbUser)) {
            throw new CustomException("用户已存在");
        }
        com.example.security.IdentityService.passwordPolicy(user.getPassword());
        if (ObjectUtil.isEmpty(user.getName())) {
            user.setName(user.getUsername());
        }
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        if (user.getSecurityAnswer() != null) user.setSecurityAnswer(passwordEncoder.encode(user.getSecurityAnswer()));
        user.setRole("USER");
        userMapper.insert(user);
    }

    public Account login(Account account) {
        User dbUser = find(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!passwordEncoder.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbUser;
    }

    public void updatePassword(Account account) {
        User dbUser = find(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!passwordEncoder.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        // 添加密码验证
        if (!validatePassword(account.getNewPassword())) {
            throw new CustomException("新密码必须包含至少8个字符，且包含大小写字母、数字和特殊字符");
        }
        dbUser.setPassword(passwordEncoder.encode(account.getNewPassword()));
        userMapper.updateById(dbUser);
    }

    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(name)) {
            queryWrapper.like("name", name);
        }
        List<User> list = userMapper.selectList(queryWrapper);
        return PageInfo.of(list);
    }

    public void register(User user) {
        this.add(user);
    }

    // 验证密保答案
    @Override
    public boolean verifySecurityAnswer(String username, String securityAnswer) {
        User user = find(username);
        if (ObjectUtil.isNull(user)) {
            return false;
        }
        if (ObjectUtil.isEmpty(user.getSecurityQuestion()) || ObjectUtil.isEmpty(user.getSecurityAnswer())) {
            return false;
        }
        return securityAnswer != null && passwordEncoder.matches(securityAnswer, user.getSecurityAnswer());
    }
}