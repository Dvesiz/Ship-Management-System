package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Crew;
import com.dhy.shipmanagebackend.mapper.CrewMapper;
import com.dhy.shipmanagebackend.service.CrewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CrewServiceImpl implements CrewService {

    @Autowired
    private CrewMapper crewMapper;

    @Override
    public void add(Crew crew) {
        // 如果没有配置 MP 自动填充，手动设置创建时间
        if (crew.getCreatedAt() == null) {
            crew.setCreatedAt(java.time.LocalDateTime.now());
            crew.setUpdatedAt(java.time.LocalDateTime.now());
        }
        crewMapper.insert(crew);
    }

    @Override
    public Page<Crew> findPage(int pageNum, int pageSize, String name, Long shipId) {
        Page<Crew> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Crew> wrapper = new LambdaQueryWrapper<>();

        // 按姓名模糊查询
        wrapper.like(StringUtils.hasLength(name), Crew::getName, name);

        // 按船舶筛选 (例如：查询某艘船上的所有船员)
        wrapper.eq(shipId != null, Crew::getShipId, shipId);

        // 按 ID 倒序，新录入的在前面
        wrapper.orderByDesc(Crew::getId);

        return crewMapper.selectPage(page, wrapper);
    }

    @Override
    public Crew findById(Long id) {
        return crewMapper.selectById(id);
    }

    @Override
    public void update(Crew crew) {
        crew.setUpdatedAt(java.time.LocalDateTime.now());
        crewMapper.updateById(crew);
    }

    @Override
    public void delete(Long id) {
        crewMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return crewMapper.deleteBatchIds(ids);
    }
}