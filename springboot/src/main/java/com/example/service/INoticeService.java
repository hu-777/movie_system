package com.example.service;

import com.example.entity.Notice;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface INoticeService {
    void add(Notice notice);

    void deleteById(Integer id);

    void updateById(Notice notice);

    Notice selectById(Integer id);

    List<Notice> selectAll(Notice notice);

    PageInfo<Notice> selectPage(@Param("notice") Notice notice, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);
}
