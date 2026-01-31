package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 记录用户的所有操作行为，用于审计追踪
 */
@Data
@TableName("operation_logs")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 操作用户ID
    private String username; // 操作用户名
    private String module; // 操作模块 (用户管理/船舶管理/船员管理等)
    private String operation; // 操作类型 (新增/修改/删除/查询/登录/登出)
    private String operationDesc; // 操作详细描述 (如：删除了「极地之星」船舶)
    private String method; // 请求方法 (GET/POST/PUT/DELETE)
    private String requestUrl; // 请求URL
    private String requestParams; // 请求参数 (JSON格式)
    private String responseResult; // 响应结果 (成功/失败)
    private String ipAddress; // 操作IP地址
    private String userAgent; // 浏览器信息
    private Long executionTime; // 执行耗时(毫秒)
    private String errorMsg; // 错误信息(如果失败)

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
