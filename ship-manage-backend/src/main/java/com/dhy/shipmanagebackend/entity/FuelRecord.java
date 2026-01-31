package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 燃油消耗记录实体类
 * 记录船舶的燃油使用情况
 */
@Data
@TableName("fuel_records")
public class FuelRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "必须指定船舶")
    private Long shipId;           // 船舶ID

    private Long voyageId;         // 关联航次ID (可选)

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;  // 记录日期

    /**
     * 燃油类型:
     * HFO - 重油
     * MDO - 轻柴油
     * MGO - 船用汽油
     * LNG - 液化天然气
     */
    private String fuelType;

    private BigDecimal quantity;   // 消耗量(吨)
    private BigDecimal unitPrice;  // 单价(元/吨)
    private BigDecimal totalCost;  // 总费用
    private String supplier;       // 供应商
    private String port;           // 加油港口
    private String remarks;        // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
