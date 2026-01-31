package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhy.shipmanagebackend.entity.ShipCategory;
import com.dhy.shipmanagebackend.mapper.ShipCategoryMapper;
import com.dhy.shipmanagebackend.service.ShipCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipCategoryServiceImpl implements ShipCategoryService {
    @Autowired
    private ShipCategoryMapper shipCategoryMapper;
    @Override
    public void add(ShipCategory shipCategory) {
        // 校验名称是否已存在
        checkNameUnique(shipCategory.getName(), null);
        shipCategoryMapper.insert(shipCategory);
    }

    @Override
    public List<ShipCategory> findAll() {
        // 查询所有，并按 ID 倒序排列
        return shipCategoryMapper.selectList(
                new LambdaQueryWrapper<ShipCategory>().orderByDesc(ShipCategory::getId)
        );
    }

    @Override
    public ShipCategory findById(Long id) {
        return shipCategoryMapper.selectById(id);
    }

    @Override
    public void update(ShipCategory shipCategory) {
        // 校验名称是否重复 (排除自己)
        checkNameUnique(shipCategory.getName(), shipCategory.getId());
        shipCategory.setUpdatedAt(java.time.LocalDateTime.now());
        shipCategoryMapper.updateById(shipCategory);
    }

    @Override
    public void delete(Long id) {
        shipCategoryMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return shipCategoryMapper.deleteBatchIds(ids);
    }

    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<ShipCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipCategory::getName, name);
        if (excludeId != null) {
            // 如果是更新操作，要排除掉自己 (ID != excludeId)
            wrapper.ne(ShipCategory::getId, excludeId);
        }
        // 如果查到了记录，说明重名了
        if (shipCategoryMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该船舶类型名称已存在");
        }
    }
}
