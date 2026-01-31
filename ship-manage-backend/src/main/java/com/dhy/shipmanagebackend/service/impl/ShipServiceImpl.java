package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Ship;
import com.dhy.shipmanagebackend.mapper.ShipMapper;
import com.dhy.shipmanagebackend.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {

    @Autowired
    private ShipMapper shipMapper;

    @Override
    public void add(Ship ship) {
        // 修正：默认状态改为中文 "在役"
        if (!StringUtils.hasLength(ship.getStatus())) {
            ship.setStatus("在役");
        }
        // 如果没有上传图片，可以设置一个默认图 (可选)
        // if (!StringUtils.hasLength(ship.getCoverImg())) { ... }

        // 初始创建时间由数据库 DEFAULT CURRENT_TIMESTAMP 处理，或者使用 MP 自动填充
        // 如果没有配置 MP 自动填充，建议手动设置一下
        if (ship.getCreatedAt() == null) {
            ship.setCreatedAt(java.time.LocalDateTime.now());
            ship.setUpdatedAt(java.time.LocalDateTime.now());
        }

        shipMapper.insert(ship);
    }

    @Override
    public Page<Ship> findPage(int pageNum, int pageSize, Long categoryId, String name, String status) {
        Page<Ship> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Ship> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(categoryId != null, Ship::getCategoryId, categoryId);

        // 这里的 status 前端传过来也应该是中文，如 "维修中"
        wrapper.eq(StringUtils.hasLength(status), Ship::getStatus, status);

        wrapper.like(StringUtils.hasLength(name), Ship::getName, name);
        wrapper.orderByDesc(Ship::getCreatedAt);

        return shipMapper.selectPage(page, wrapper);
    }

    @Override
    public Ship findById(Long id) {
        return shipMapper.selectById(id);
    }

    @Override
    public void update(Ship ship) {
        // 更新时间
        ship.setUpdatedAt(java.time.LocalDateTime.now());
        shipMapper.updateById(ship);
    }

    @Override
    public void delete(Long id) {
        shipMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return shipMapper.deleteBatchIds(ids);
    }
}