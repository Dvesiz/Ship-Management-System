package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 航次实体类
 * 对应数据库表: voyages
 */
@Data
@TableName("voyages")
public class Voyage {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "必须指定船舶")
    private Long shipId;

    private String startPort; // 出发港
    private String endPort;   // 目的港

    /**
     * 状态: 计划中, 执行中, 已完成
     * 默认: 计划中
     */
    private String status;

    private LocalDateTime startTime; // 开航时间
    private LocalDateTime endTime;   // 结束/抵达时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}