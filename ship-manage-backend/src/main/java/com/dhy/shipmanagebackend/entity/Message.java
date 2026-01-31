package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 * 用于系统消息和站内信功能
 */
@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId; // 发送者ID (0表示系统消息)
    private Long receiverId; // 接收者ID
    private String title; // 消息标题
    private String content; // 消息内容

    /**
     * 消息类型:
     * SYSTEM - 系统通知
     * MAINTENANCE - 维修提醒
     * VOYAGE - 航次通知
     * CERTIFICATE - 证书到期提醒
     * PERSONAL - 站内信
     */
    private String type;

    /**
     * 消息状态:
     * UNREAD - 未读
     * READ - 已读
     */
    private String status;

    private Long relatedId; // 关联业务ID (如船舶ID、航次ID等)
    private String relatedType; // 关联业务类型

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime readAt; // 阅读时间

    @TableField(exist = false)
    private String senderName; // 发送者名称

    @TableField(exist = false)
    private String senderAvatar; // 发送者头像
}
