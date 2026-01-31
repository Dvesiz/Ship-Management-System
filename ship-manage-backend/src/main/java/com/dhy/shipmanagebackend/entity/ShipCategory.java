package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 船舶类型实体
 * 对应数据库表: ship_categories
 */
@Data
@TableName("ship_categories")
public class ShipCategory {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "类型名称不能为空")
    private String name;  // 例如：散货船

    private String alias; // 例如：BULK_CARRIER

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}