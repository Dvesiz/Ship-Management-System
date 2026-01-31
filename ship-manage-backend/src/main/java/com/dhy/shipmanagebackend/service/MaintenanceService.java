package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Maintenance;

import java.util.List;

public interface MaintenanceService {
    void add(Maintenance maintenance);

    // 分页查询: 支持按船舶ID筛选
    Page<Maintenance> findPage(int pageNum, int pageSize, Long shipId);

    Maintenance findById(Long id);

    // 雖然接口文档没明确写更新和删除，但通常为了容错建议加上
    void update(Maintenance maintenance);
    void delete(Long id);

    // 批量删除
    int deleteBatch(List<Long> ids);
}