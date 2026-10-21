package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.User;

public interface UserService extends IService<User> {
    // 验证密保答案
    boolean verifySecurityAnswer(String username, String securityAnswer);
}