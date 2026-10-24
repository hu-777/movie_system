package com.example.controller;

import com.example.common.Result;
import com.example.entity.*;
import com.example.security.*;
import com.example.service.UserServiceImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.annotation.Resource;
import javax.servlet.http.*;
import java.util.*;
import java.io.Serializable;

@RestController
public class AuthController {
    @Resource private IdentityService identities;
    @Resource private UserServiceImpl users;
    @Resource private BCryptPasswordEncoder encoder;
    @Resource private AttemptLimiter limiter;
    public static class LoginInput {public String username,password,role;}
    public static class RegisterInput {public String username,password,name,sex,phone,email,securityQuestion,securityAnswer;}
    public static class PasswordInput {public String password,newPassword;}
    public static class RecoveryInput {public String username,securityAnswer,newPassword,resetToken;}
    private static class ResetGrant implements Serializable {
        final String token,key,stamp; final long expires;
        ResetGrant(String token,String key,String stamp){this.token=token;this.key=key;this.stamp=stamp;this.expires=System.currentTimeMillis()+300000;}
    }
    private String username(String value){
        if(value==null || value.trim().isEmpty() || value.length()>100 || value.matches(".*[\\p{Cntrl}].*")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请输入有效账号（不超过100字）");
        return value;
    }
    private void limit(HttpServletRequest req,String action,String account,int accountLimit){
        limiter.check(action+":ip:"+req.getRemoteAddr(),60,900000);
        if(account!=null) limiter.check(action+":account:"+account.trim().toLowerCase(Locale.ROOT),accountLimit,900000);
    }
    @GetMapping("/auth/csrf") public Result csrf(CsrfToken token){
        Map<String,String> data=new HashMap<>();data.put("token",token.getToken());data.put("headerName",token.getHeaderName());return Result.success(data);
    }
    @PostMapping("/login") public Result login(@RequestBody LoginInput input,HttpServletRequest req,HttpServletResponse res){
        String name=username(input.username);
        if(!Arrays.asList("USER","ADMIN").contains(input.role)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"登录身份无效");
        limit(req,"login",input.role+":"+name,20);
        Account account=identities.find(input.role,name);
        if(account==null || input.password==null || input.password.length()>100 || !encoder.matches(input.password,account.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"账号或密码错误");
        req.getSession();req.changeSessionId();
        req.getSession().removeAttribute("resetGrant");
        SecurityContext context=SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(identities.key(account),identities.stamp(account),AuthorityUtils.createAuthorityList("ROLE_"+input.role)));
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context,req,res);
        new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository().saveToken(null,req,res);
        return Result.success(account);
    }
    @GetMapping("/auth/me") public Result me(){return Result.success(identities.current());}
    @PostMapping("/logout") public Result logout(HttpServletRequest req){
        SecurityContextHolder.clearContext();HttpSession session=req.getSession(false);if(session!=null)session.invalidate();return Result.success();
    }
    @PutMapping("/auth/profile") public Result profile(@RequestBody IdentityService.Profile input){
        Account account=identities.current();identities.updateProfile(account.getRole(),account.getId(),input);return Result.success(identities.byKey(identities.key(account)));
    }
    @PutMapping("/updatePassword") public Result password(@RequestBody PasswordInput input,HttpServletRequest req){
        Account account=identities.current();
        limit(req,"password",identities.key(account),10);
        if(input.password==null || !encoder.matches(input.password,account.getPassword()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"原密码错误");
        identities.changePassword(account,input.newPassword);return logout(req);
    }
    @PostMapping("/register") public Result register(@RequestBody RegisterInput input,HttpServletRequest req){
        limiter.check("register:"+req.getRemoteAddr(),10,3600000);
        User user=new User();user.setUsername(username(input.username));user.setPassword(input.password);user.setName(input.name);
        user.setSex(input.sex);user.setPhone(input.phone);user.setEmail(input.email);
        if(input.securityQuestion==null || input.securityQuestion.trim().isEmpty() || input.securityQuestion.length()>200 || input.securityAnswer==null || input.securityAnswer.trim().isEmpty() || input.securityAnswer.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请填写有效的密保问题与答案");
        user.setSecurityQuestion(input.securityQuestion);user.setSecurityAnswer(input.securityAnswer);users.register(user);return Result.success();
    }
    @GetMapping("/check-username") public Result available(@RequestParam String username,HttpServletRequest req){
        limiter.check("availability:"+req.getRemoteAddr(),60,60000);return Result.success(users.find(username(username))!=null);
    }
    @PostMapping("/getSecurityQuestion") public Result question(@RequestBody RecoveryInput input,HttpServletRequest req){
        String name=username(input.username);limit(req,"question",name,10);User user=users.find(name);
        if(user==null || user.getSecurityQuestion()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"无法使用该账号的密保找回");
        return Result.success(user.getSecurityQuestion());
    }
    @PostMapping("/verifySecurityAnswer") public Result verify(@RequestBody RecoveryInput input,HttpServletRequest req){
        String name=username(input.username);limit(req,"answer",name,5);
        User user=users.find(name);
        if(user==null || input.securityAnswer==null || input.securityAnswer.length()>100 || user.getSecurityAnswer()==null ||
           !encoder.matches(input.securityAnswer,user.getSecurityAnswer()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"密保验证失败");
        String token=UUID.randomUUID().toString()+UUID.randomUUID();
        req.getSession().setAttribute("resetGrant",new ResetGrant(token,identities.key(user),identities.stamp(user)));
        return Result.success(Collections.singletonMap("resetToken",token));
    }
    @PostMapping("/resetPassword") public Result reset(@RequestBody RecoveryInput input,HttpServletRequest req){
        limit(req,"reset",null,0);IdentityService.passwordPolicy(input.newPassword);
        HttpSession session=req.getSession(false);if(session==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请重新验证密保");
        synchronized(session){
            ResetGrant grant=(ResetGrant)session.getAttribute("resetGrant");
            if(grant==null || grant.expires<System.currentTimeMillis() || !grant.token.equals(input.resetToken))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"重置凭据已失效，请重新验证密保");
            session.removeAttribute("resetGrant");Account account=identities.byKey(grant.key);
            if(account==null || !grant.stamp.equals(identities.stamp(account)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"账户已变更，请重新验证密保");
            identities.changePassword(account,input.newPassword);
        }
        return logout(req);
    }
}
