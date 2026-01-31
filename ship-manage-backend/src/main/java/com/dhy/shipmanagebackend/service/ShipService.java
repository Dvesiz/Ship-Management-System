package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Ship;

import java.util.List;

public interface ShipService {
    // 添加
    void add(Ship ship);
    
    void update(Ship ship);

    void delete(Long id);

    // 批量删除
    int deleteBatch(List<Long> ids);

    Ship findById(Long id);

    Page<Ship> findPage(int pageNum, int pageSize, Long categoryId, String name, String status);
}
