package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.entity.Message;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.service.MessageService;
import com.dhy.shipmanagebackend.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取当前用户的消息列表
     */
    @GetMapping
    public Result<IPage<Message>> getUserMessages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        return Result.success(messageService.getUserMessages(userId, pageNum, pageSize, type, status));
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = getCurrentUserId();
        return Result.success(messageService.getUnreadCount(userId));
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/read/{messageId}")
    public Result markAsRead(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        messageService.markAsRead(messageId, userId);
        return Result.success();
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read-all")
    public Result markAllAsRead() {
        Long userId = getCurrentUserId();
        messageService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public Result delete(@PathVariable Long messageId) {
        Long userId = getCurrentUserId();
        messageService.delete(messageId, userId);
        return Result.success();
    }

    /**
     * 发送站内信(管理员功能)
     */
    @PostMapping("/send")
    public Result sendMessage(
            @RequestParam Long receiverId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "PERSONAL") String type) {
        Long senderId = getCurrentUserId();

        // 检查用户角色
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        String role = (String) userInfo.get("role");

        // 只有管理员可以发送非 PERSONAL 类型的消息
        if (!"ADMIN".equals(role) && !"PERSONAL".equals(type)) {
            return Result.error("无权限发送此类消息");
        }

        messageService.sendPersonalMessage(senderId, receiverId, title, content, type);
        return Result.success();
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        return ((Number) userInfo.get("id")).longValue();
    }
}
