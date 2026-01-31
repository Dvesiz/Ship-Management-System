package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Voyage;

import java.util.List;

public interface VoyageService {
    /**
     * 开航 (新增航次)
     * 事务操作：1. 校验船舶状态 2. 创建航次 3. 修改船舶状态为"航行中"
     */
    void add(Voyage voyage);

    /**
     * 完工 (结束航次)
     * 事务操作：1. 更新航次结束时间 2. 修改船舶状态为"在役"
     */
    void finish(Long id);

    /**
     * 分页查询
     */
    Page<Voyage> findPage(int pageNum, int pageSize, Long shipId);

    Voyage findById(Long id);

    /**
     * 删除航次
     */
    void delete(Long id);

    /**
     * 批量删除航次
     */
    int deleteBatch(List<Long> ids);
}