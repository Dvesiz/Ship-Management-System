package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Crew;

import java.util.List;

public interface CrewService {
    void add(Crew crew);

    // 分页查询：支持按姓名和所属船舶筛选
    Page<Crew> findPage(int pageNum, int pageSize, String name, Long shipId);

    Crew findById(Long id);

    void update(Crew crew);

    void delete(Long id);

    // 批量删除船员
    int deleteBatch(List<Long> ids);
}