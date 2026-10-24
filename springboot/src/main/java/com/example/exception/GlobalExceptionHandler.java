package com.example.exception;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.example.common.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice(basePackages = "com.example.controller")
public class GlobalExceptionHandler {
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    @ResponseBody
    public org.springframework.http.ResponseEntity<Result> status(org.springframework.web.server.ResponseStatusException e) {
        Result result=Result.error(e.getReason());result.setCode(String.valueOf(e.getRawStatusCode()));
        return org.springframework.http.ResponseEntity.status(e.getRawStatusCode()).body(result);
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseBody
    public org.springframework.http.ResponseEntity<Result> denied(Exception e) {
        Result result=Result.error("无权执行此操作");result.setCode("403");
        return org.springframework.http.ResponseEntity.status(403).body(result);
    }
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    @ResponseBody
    public org.springframework.http.ResponseEntity<Result> unauthenticated(Exception e) {
        Result result=Result.error("请先登录");result.setCode("401");
        return org.springframework.http.ResponseEntity.status(401).body(result);
    }

    private static final Log log = LogFactory.get();


    //统一异常处理@ExceptionHandler,主要用于Exception
    @ExceptionHandler(Exception.class)
    @ResponseBody//返回json串
    public Result error(HttpServletRequest request, Exception e) {
        log.error("异常信息：", e);
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody//返回json串
    public Result customError(HttpServletRequest request, CustomException e) {
        return Result.error(e.getMsg());
    }

}
