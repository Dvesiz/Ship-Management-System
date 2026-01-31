package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.FuelRecord;
import com.dhy.shipmanagebackend.mapper.FuelRecordMapper;
import com.dhy.shipmanagebackend.service.FuelRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 燃油记录服务实现类
 */
@Service
public class FuelRecordServiceImpl implements FuelRecordService {

    @Autowired
    private FuelRecordMapper fuelRecordMapper;

    @Override
    public void add(FuelRecord record) {
        // 自动计算总费用
        if (record.getQuantity() != null && record.getUnitPrice() != null) {
            record.setTotalCost(record.getQuantity().multiply(record.getUnitPrice()));
        }
        fuelRecordMapper.insert(record);
    }

    @Override
    public void update(FuelRecord record) {
        // 自动计算总费用
        if (record.getQuantity() != null && record.getUnitPrice() != null) {
            record.setTotalCost(record.getQuantity().multiply(record.getUnitPrice()));
        }
        fuelRecordMapper.updateById(record);
    }

    @Override
    public void delete(Long id) {
        fuelRecordMapper.deleteById(id);
    }

    @Override
    public FuelRecord getById(Long id) {
        return fuelRecordMapper.selectById(id);
    }

    @Override
    public IPage<FuelRecord> page(Integer pageNum, Integer pageSize, Long shipId, Long voyageId, String fuelType) {
        Page<FuelRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FuelRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (shipId != null) {
            wrapper.eq(FuelRecord::getShipId, shipId);
        }
        if (voyageId != null) {
            wrapper.eq(FuelRecord::getVoyageId, voyageId);
        }
        if (StringUtils.hasLength(fuelType)) {
            wrapper.eq(FuelRecord::getFuelType, fuelType);
        }
        
        wrapper.orderByDesc(FuelRecord::getRecordDate);
        return fuelRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public BigDecimal sumQuantityByShipId(Long shipId) {
        return fuelRecordMapper.sumQuantityByShipId(shipId);
    }

    @Override
    public BigDecimal sumCostByShipId(Long shipId) {
        return fuelRecordMapper.sumCostByShipId(shipId);
    }

    @Override
    public List<Map<String, Object>> monthlyStatsByShipId(Long shipId) {
        return fuelRecordMapper.monthlyStatsByShipId(shipId);
    }

    @Override
    public Map<String, Object> getStatisticsOverview() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总消耗量
        LambdaQueryWrapper<FuelRecord> wrapper = new LambdaQueryWrapper<>();
        List<FuelRecord> allRecords = fuelRecordMapper.selectList(wrapper);
        
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (FuelRecord record : allRecords) {
            if (record.getQuantity() != null) {
                totalQuantity = totalQuantity.add(record.getQuantity());
            }
            if (record.getTotalCost() != null) {
                totalCost = totalCost.add(record.getTotalCost());
            }
        }
        
        stats.put("totalQuantity", totalQuantity);
        stats.put("totalCost", totalCost);
        stats.put("recordCount", allRecords.size());
        
        return stats;
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return fuelRecordMapper.deleteBatchIds(ids);
    }
}
