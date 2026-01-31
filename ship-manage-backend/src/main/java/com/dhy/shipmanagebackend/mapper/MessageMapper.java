package com.dhy.shipmanagebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dhy.shipmanagebackend.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 消息通知Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 统计用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM messages WHERE receiver_id = #{userId} AND status = 'UNREAD'")
    int countUnreadByUserId(Long userId);
}
