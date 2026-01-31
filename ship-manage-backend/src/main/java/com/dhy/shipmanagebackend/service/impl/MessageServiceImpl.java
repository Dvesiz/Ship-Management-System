package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.Message;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.mapper.MessageMapper;
import com.dhy.shipmanagebackend.mapper.UserMapper;
import com.dhy.shipmanagebackend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void sendSystemMessage(Long receiverId, String title, String content, String type, Long relatedId,
            String relatedType) {
        Message message = new Message();
        message.setSenderId(0L); // 0表示系统消息
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setStatus("UNREAD");
        message.setRelatedId(relatedId);
        message.setRelatedType(relatedType);
        messageMapper.insert(message);
    }

    @Override
    public void sendPersonalMessage(Long senderId, Long receiverId, String title, String content, String type) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type != null ? type : "PERSONAL");
        message.setStatus("UNREAD");
        messageMapper.insert(message);
    }

    @Override
    public void broadcastSystemMessage(String title, String content, String type) {
        // 获取所有用户
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            sendSystemMessage(user.getId(), title, content, type, null, null);
        }
    }

    @Override
    public IPage<Message> getUserMessages(Long userId, Integer pageNum, Integer pageSize, String type, String status) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);

        if (StringUtils.hasLength(type)) {
            wrapper.eq(Message::getType, type);
        }
        if (StringUtils.hasLength(status)) {
            wrapper.eq(Message::getStatus, status);
        }

        wrapper.orderByDesc(Message::getCreatedAt).orderByDesc(Message::getId);
        IPage<Message> messagePage = messageMapper.selectPage(page, wrapper);

        // 填充发送者信息
        messagePage.getRecords().forEach(msg -> {
            if (msg.getSenderId() != null && msg.getSenderId() != 0) {
                User sender = userMapper.selectById(msg.getSenderId());
                if (sender != null) {
                    msg.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
                    msg.setSenderAvatar(sender.getAvatarUrl());
                } else {
                    msg.setSenderName("未知用户");
                }
            } else {
                msg.setSenderName("系统通知");
            }
        });

        return messagePage;
    }

    @Override
    public int getUnreadCount(Long userId) {
        return messageMapper.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getId, messageId)
                .eq(Message::getReceiverId, userId)
                .set(Message::getStatus, "READ")
                .set(Message::getReadAt, LocalDateTime.now());
        messageMapper.update(null, wrapper);
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getReceiverId, userId)
                .eq(Message::getStatus, "UNREAD")
                .set(Message::getStatus, "READ")
                .set(Message::getReadAt, LocalDateTime.now());
        messageMapper.update(null, wrapper);
    }

    @Override
    public void delete(Long messageId, Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getId, messageId)
                .eq(Message::getReceiverId, userId);
        messageMapper.delete(wrapper);
    }
}
