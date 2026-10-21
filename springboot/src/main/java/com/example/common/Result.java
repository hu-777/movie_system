package com.example.common;

public class Result {
    private String code;
    private String msg;
    private Object data;

    private Result(Object data) {
        this.data = data;
    }

    public Result() {
    }

    // 成功请求（无数据）
    public static Result success() {
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }

    // 成功请求（带数据）
    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }

    // 通用错误（无自定义消息）
    public static Result error() {
        Result result = new Result();
        result.setCode("500");
        result.setMsg("请求失败");
        return result;
    }

    // 通用错误（带自定义消息）
    public static Result error(String msg) {
        Result result = new Result();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }

    // 专用失败方法（可以带失败消息）
    public static Result fail(String msg) {
        Result result = new Result();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }

    // Getter 和 Setter 方法
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
