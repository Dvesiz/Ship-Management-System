package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.entity.FuelRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 燃油记录服务接口
 */
public interface FuelRecordService {
    
    /**
     * 添加燃油记录
     */
    void add(FuelRecord record);
    
    /**
     * 更新燃油记录
     */
    void update(FuelRecord record);
    
    /**
     * 删除燃油记录
     */
    void delete(Long id);

    /**
     * 批量删除燃油记录
     */
    int deleteBatch(List<Long> ids);
    
    /**
     * 根据ID查询
     */
    FuelRecord getById(Long id);
    
    /**
     * 分页查询
     */
    IPage<FuelRecord> page(Integer pageNum, Integer pageSize, Long shipId, Long voyageId, String fuelType);
    
    /**
     * 统计船舶燃油总消耗
     */
    BigDecimal sumQuantityByShipId(Long shipId);
    
    /**
     * 统计船舶燃油总费用
     */
    BigDecimal sumCostByShipId(Long shipId);
    
    /**
     * 按月统计燃油消耗
     */
    List<Map<String, Object>> monthlyStatsByShipId(Long shipId);
    
    /**
     * 获取燃油消耗统计概览
     */
    Map<String, Object> getStatisticsOverview();
}
