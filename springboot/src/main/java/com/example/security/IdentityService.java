package com.example.security;

import com.example.entity.*;
import com.example.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class IdentityService {
    @Resource private UserMapper users;
    @Resource private AdminMapper admins;
    @Resource private BCryptPasswordEncoder encoder;
    public Account find(String role, String username) {
        if ("ADMIN".equals(role)) return admins.selectOne(new QueryWrapper<Admin>().eq("username",username));
        if ("USER".equals(role)) return users.selectOne(new QueryWrapper<User>().eq("username",username));
        return null;
    }
    public Account byKey(String key) {
        try {
            String[] parts=key.split(":");int id=Integer.parseInt(parts[1]);
            return "ADMIN".equals(parts[0]) ? admins.selectById(id) : "USER".equals(parts[0]) ? users.selectById(id) : null;
        } catch (RuntimeException invalid) { return null; }
    }
    public String key(Account account) { return account.getRole()+":"+account.getId(); }
    public String stamp(Account account) { return cn.hutool.crypto.SecureUtil.sha256(account.getPassword()); }
    public Account current() {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        Account account=authentication==null ? null : byKey(authentication.getName());
        if (account==null) throw new AuthenticationCredentialsNotFoundException("请先登录");
        return account;
    }
    public void assertOwner(Orders order) {
        Account account=current();
        if (order==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"订单不存在");
        if (!"ADMIN".equals(account.getRole()) && !Objects.equals(order.getUserId(),account.getId()))
            throw new AccessDeniedException("不能访问他人的订单");
    }
    public static void passwordPolicy(String password) {
        if(password==null || password.length()<8 || password.getBytes(StandardCharsets.UTF_8).length>72 ||
            !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") ||
            !password.matches(".*[0-9].*") || !password.matches(".*[^A-Za-z0-9].*"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"密码须至少8位，包含大小写字母、数字及特殊字符，且不超过72字节");
    }
    public void changePassword(Account account, String password) {
        passwordPolicy(password);
        // Update only credentials; never accept a client supplied account entity.
        if ("ADMIN".equals(account.getRole())) { Admin patch=new Admin();patch.setId(account.getId());patch.setPassword(encoder.encode(password));admins.updateById(patch); }
        else { User patch=new User();patch.setId(account.getId());patch.setPassword(encoder.encode(password));users.updateById(patch); }
    }
    public static class Profile {
        public Integer id; public String name; public String avatar; public String sex; public String phone; public String email;
    }
    public void updateProfile(String role, Integer id, Profile input) {
        if(input.name!=null && input.name.length()>100 || input.avatar!=null && input.avatar.length()>500 ||
            input.phone!=null && input.phone.length()>30 || input.email!=null && input.email.length()>100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"资料字段过长");
        if(input.avatar!=null && !input.avatar.isEmpty() && !input.avatar.startsWith("/api/files/download/") &&
            !input.avatar.matches("https?://(localhost|127\\.0\\.0\\.1):9090/files/download/[^/]+") && !input.avatar.startsWith("/assets/"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请使用本站上传的头像");
        if("ADMIN".equals(role)){ Admin patch=new Admin();patch.setId(id);patch.setName(input.name);patch.setAvatar(input.avatar);admins.updateById(patch); }
        else { User patch=new User();patch.setId(id);patch.setName(input.name);patch.setAvatar(input.avatar);patch.setSex(input.sex);patch.setPhone(input.phone);patch.setEmail(input.email);users.updateById(patch); }
    }
}
