package com.example.entity;

public class GoodsStock {
    private Integer id;
    private String name;        // 对应数据库字段 name
    private String comment;     // 对应数据库字段 comment
    private String showtime;    // 对应数据库字段 showtime
    private Integer tickets;    // 对应数据库字段 tickets
    private Integer goodsId;    // 对应数据库字段 goods_id
    private Integer price;      // 对应数据库字段 price
    private String img;         // 对应数据库字段 img
    private String type;        // 对应数据库字段 type
    private String position;    // 修复拼写错误：放映位置，格式如"1号杜比厅"
    private Integer categoryId; // 非数据库字段，用于联表查询 category.id
    private String goodsName;   // 非数据库字段，用于联表查询 goods.name

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public Integer getTickets() {
        return tickets;
    }

    public void setTickets(Integer tickets) {
        this.tickets = tickets;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // 修复getter方法名
    public String getPosition() {
        return position;
    }

    // 修复setter方法名
    public void setPosition(String position) {
        this.position = position;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }
// Getters and Setters
}