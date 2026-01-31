package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Ship;
import com.dhy.shipmanagebackend.entity.Voyage;
import com.dhy.shipmanagebackend.mapper.ShipMapper;
import com.dhy.shipmanagebackend.mapper.VoyageMapper;
import com.dhy.shipmanagebackend.service.VoyageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoyageServiceImpl implements VoyageService {

    @Autowired
    private VoyageMapper voyageMapper;

    @Autowired
    private ShipMapper shipMapper; // 需要操作船舶表

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void add(Voyage voyage) {
        // 1. 检查船舶是否存在
        Ship ship = shipMapper.selectById(voyage.getShipId());
        if (ship == null) {
            throw new RuntimeException("船舶不存在");
        }

        // 2. 判断用户想要创建什么状态的航次
        // 如果前端没传 status，默认视为 "执行中" (保持和文档一致的开航逻辑)
        // 或者用户明确传了 "执行中"
        if (!StringUtils.hasLength(voyage.getStatus()) || "执行中".equals(voyage.getStatus())) {

            // === 走开航逻辑 ===

            // A. 只有"在役"或"停运"的船才能开航
            if (!"在役".equals(ship.getStatus()) && !"停运".equals(ship.getStatus())) {
                throw new RuntimeException("该船舶当前状态不可开航：" + ship.getStatus());
            }

            voyage.setStatus("执行中");
            // 更新船舶状态
            ship.setStatus("航行中");
            ship.setUpdatedAt(LocalDateTime.now());
            shipMapper.updateById(ship);

        } else if ("计划中".equals(voyage.getStatus())) {

            // === 走计划逻辑 ===

            // 计划中的航次，通常不需要校验船舶当前是否空闲（因为是未来的事）
            // 也不需要修改船舶状态，船还在港口里趴着呢
            voyage.setStatus("计划中");

        } else {
            throw new RuntimeException("不支持的航次状态，只能是 '计划中' 或 '执行中'");
        }

        // 3. 通用字段处理
        if (voyage.getStartTime() == null) {
            voyage.setStartTime(LocalDateTime.now());
        }
        if (voyage.getCreatedAt() == null) {
            voyage.setCreatedAt(LocalDateTime.now());
            voyage.setUpdatedAt(LocalDateTime.now());
        }

        // 4. 保存航次
        voyageMapper.insert(voyage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void finish(Long id) {
        // 1. 获取航次
        Voyage voyage = voyageMapper.selectById(id);
        if (voyage == null) {
            throw new RuntimeException("航次不存在");
        }
        if ("已完成".equals(voyage.getStatus())) {
            throw new RuntimeException("该航次已经结束，请勿重复操作");
        }

        // 2. 更新航次状态
        voyage.setStatus("已完成");
        voyage.setEndTime(LocalDateTime.now());
        voyage.setUpdatedAt(LocalDateTime.now());
        voyageMapper.updateById(voyage);

        // 3. 释放船舶状态 (变回 "在役")
        Ship ship = shipMapper.selectById(voyage.getShipId());
        if (ship != null) {
            ship.setStatus("在役");
            ship.setUpdatedAt(LocalDateTime.now());
            shipMapper.updateById(ship);
        }
    }

    @Override
    public Page<Voyage> findPage(int pageNum, int pageSize, Long shipId) {
        Page<Voyage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Voyage> wrapper = new LambdaQueryWrapper<>();

        // 筛选某艘船的航次记录
        wrapper.eq(shipId != null, Voyage::getShipId, shipId);

        // 按开航时间倒序
        wrapper.orderByDesc(Voyage::getStartTime);

        return voyageMapper.selectPage(page, wrapper);
    }

    @Override
    public Voyage findById(Long id) {
        return voyageMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 获取航次信息
        Voyage voyage = voyageMapper.selectById(id);
        if (voyage == null) {
            throw new RuntimeException("航次不存在");
        }

        // 2. 如果航次是"执行中"状态，需要恢复船舶状态
        if ("执行中".equals(voyage.getStatus())) {
            Ship ship = shipMapper.selectById(voyage.getShipId());
            if (ship != null && "航行中".equals(ship.getStatus())) {
                ship.setStatus("在役");
                ship.setUpdatedAt(LocalDateTime.now());
                shipMapper.updateById(ship);
            }
        }

        // 3. 删除航次记录
        voyageMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return voyageMapper.deleteBatchIds(ids);
    }
}