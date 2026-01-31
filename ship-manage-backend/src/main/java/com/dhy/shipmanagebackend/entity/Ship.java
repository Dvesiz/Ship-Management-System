package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 船舶信息实体
 * 对应数据库表: ships
 */
@Data
@TableName("ships")
public class Ship {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "船舶名称不能为空")
    private String name;

    @NotNull(message = "请选择船舶类型")
    private Long categoryId; // 对应数据库 category_id

    private BigDecimal tonnage; // 吨位

    /**
     * 状态: 在役, 维修中, 停运, 航行中
     * 数据库默认值: '在役'
     */
    private String status;

    private String coverImg; // 对应数据库 cover_img

    // 建议配置自动填充 (需要配置 MyMetaObjectHandler，如果没有配置，需手动 set)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}