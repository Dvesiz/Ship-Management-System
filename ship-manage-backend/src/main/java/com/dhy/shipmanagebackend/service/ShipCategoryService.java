package com.dhy.shipmanagebackend.service;

import com.dhy.shipmanagebackend.entity.ShipCategory;

import java.util.List;

public interface ShipCategoryService {
    // 添加分类
    void add(ShipCategory shipCategory);
    // 列出所有分类
    List<ShipCategory> findAll();
    // 根据 ID 查询
    ShipCategory findById(Long id);
    // 更新
    void update(ShipCategory shipCategory);
    // 删除
    void delete(Long id);

    // 批量删除
    int deleteBatch(List<Long> ids);
}
