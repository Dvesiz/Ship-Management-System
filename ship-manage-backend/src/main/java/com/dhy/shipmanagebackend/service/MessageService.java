package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.entity.Message;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface MessageService {

    /**
     * 发送系统消息
     */
    void sendSystemMessage(Long receiverId, String title, String content, String type, Long relatedId,
            String relatedType);

    /**
     * 发送站内信
     */
    void sendPersonalMessage(Long senderId, Long receiverId, String title, String content, String type);

    /**
     * 批量发送系统消息给所有用户
     */
    void broadcastSystemMessage(String title, String content, String type);

    /**
     * 获取用户消息列表(分页)
     */
    IPage<Message> getUserMessages(Long userId, Integer pageNum, Integer pageSize, String type, String status);

    /**
     * 获取用户未读消息数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 批量标记为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除消息
     */
    void delete(Long messageId, Long userId);
}
