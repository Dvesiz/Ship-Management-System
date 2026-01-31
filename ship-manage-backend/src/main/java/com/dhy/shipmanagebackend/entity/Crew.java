package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 船员实体类
 * 对应数据库表: crew
 */
@Data
@TableName("crew")
public class Crew {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "船员姓名不能为空")
    private String name;

    private String position; // 职位 (如: 船长, 大副)

    private Long shipId;     // 当前所属船舶ID (可为空)

    private String phone;    // 联系电话

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}