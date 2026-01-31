package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Maintenance;
import com.dhy.shipmanagebackend.mapper.MaintenanceMapper;
import com.dhy.shipmanagebackend.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    @Autowired
    private MaintenanceMapper maintenanceMapper;

    @Override
    public void add(Maintenance maintenance) {
        // 如果未填维修时间，默认为当前
        if (maintenance.getMaintenanceTime() == null) {
            maintenance.setMaintenanceTime(LocalDateTime.now());
        }
        // 手动处理创建时间 (如果没有配置 MP 自动填充)
        if (maintenance.getCreatedAt() == null) {
            maintenance.setCreatedAt(LocalDateTime.now());
        }
        maintenanceMapper.insert(maintenance);

        // 💡 扩展思路：
        // 这里其实可以联动修改 Ship 表的状态为 "维修中" (MAINTENANCE)
        // 但根据你的文档，这部分逻辑没有强制要求，我们先保持简单，只记账。
    }

    @Override
    public Page<Maintenance> findPage(int pageNum, int pageSize, Long shipId) {
        Page<Maintenance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Maintenance> wrapper = new LambdaQueryWrapper<>();

        // 筛选特定船舶的记录
        wrapper.eq(shipId != null, Maintenance::getShipId, shipId);

        // 按维修时间倒序 (最近的维修排前面)
        wrapper.orderByDesc(Maintenance::getMaintenanceTime);

        return maintenanceMapper.selectPage(page, wrapper);
    }

    @Override
    public Maintenance findById(Long id) {
        return maintenanceMapper.selectById(id);
    }

    @Override
    public void update(Maintenance maintenance) {
        maintenanceMapper.updateById(maintenance);
    }

    @Override
    public void delete(Long id) {
        maintenanceMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return maintenanceMapper.deleteBatchIds(ids);
    }
}