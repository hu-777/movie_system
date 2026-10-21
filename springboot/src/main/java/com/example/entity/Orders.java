package com.example.entity;

public class Orders {
    private Integer id;
    private String orderNo;
    private Integer goodsId;
    private Integer num;
    private Integer userId;
    private String status;
    private String time;
    private String goodsName;
    private String userName;
    private String userRole;
    private Integer totalPrice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    // 修改Orders实体类，添加以下字段
    private Integer screeningId; // 场次ID
    private String seatInfo; // 座位信息（如"A1,A2,B3"）
    private String hallName; // 影厅名称
    private String showTime; // 放映时间
    
    // 添加相应的getter和setter方法
    public Integer getScreeningId() {
        return screeningId;
    }
    
    public void setScreeningId(Integer screeningId) {
        this.screeningId = screeningId;
    }
    
    public String getSeatInfo() {
        return seatInfo;
    }
    
    public void setSeatInfo(String seatInfo) {
        this.seatInfo = seatInfo;
    }
    
    public String getHallName() {
        return hallName;
    }
    
    public void setHallName(String hallName) {
        this.hallName = hallName;
    }
    
    public String getShowTime() {
        return showTime;
    }
    
    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }
}