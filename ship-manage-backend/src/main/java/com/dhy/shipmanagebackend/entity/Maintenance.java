package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修记录实体
 * 对应数据库表: maintenance
 */
@Data
@TableName("maintenance")
public class Maintenance {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "必须指定船舶ID")
    private Long shipId;

    private String description; // 维修描述/内容

    private BigDecimal cost;    // 维修费用

    private LocalDateTime maintenanceTime; // 维修发生时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}