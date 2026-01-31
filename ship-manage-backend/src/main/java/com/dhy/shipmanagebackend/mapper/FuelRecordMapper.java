package com.dhy.shipmanagebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dhy.shipmanagebackend.entity.FuelRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 燃油记录Mapper接口
 */
@Mapper
public interface FuelRecordMapper extends BaseMapper<FuelRecord> {
    
    /**
     * 统计船舶燃油总消耗
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM fuel_records WHERE ship_id = #{shipId}")
    BigDecimal sumQuantityByShipId(Long shipId);
    
    /**
     * 统计船舶燃油总费用
     */
    @Select("SELECT COALESCE(SUM(total_cost), 0) FROM fuel_records WHERE ship_id = #{shipId}")
    BigDecimal sumCostByShipId(Long shipId);
    
    /**
     * 按月统计燃油消耗
     */
    @Select("SELECT DATE_FORMAT(record_date, '%Y-%m') as month, SUM(quantity) as total_quantity, SUM(total_cost) as total_cost " +
            "FROM fuel_records WHERE ship_id = #{shipId} GROUP BY DATE_FORMAT(record_date, '%Y-%m') ORDER BY month DESC LIMIT 12")
    List<Map<String, Object>> monthlyStatsByShipId(Long shipId);
}
