package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 座位实体
 */
@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("screening_id")
    private Integer screeningId; // 场次ID
    
    @TableField("seat_row")
    private String seatRow; // 座位行（如A、B、C）
    
    @TableField("seat_column")
    private Integer seatColumn; // 座位列（如1、2、3）
    
    @TableField("status")
    private String status; // 座位状态：AVAILABLE（可用）、SELECTED（已选）、SOLD（已售）
    
    @TableField("seat_name")
    private String seatName; // 座位名称（如A1、B5）

    // getter and setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Integer screeningId) {
        this.screeningId = screeningId;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public Integer getSeatColumn() {
        return seatColumn;
    }

    public void setSeatColumn(Integer seatColumn) {
        this.seatColumn = seatColumn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }
}