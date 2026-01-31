package com.dhy.shipmanagebackend.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class UserVO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String nickname;
    private String email;
    private String avatarUrl;    // 对应数据库 avatar_url
    private String role;         // USER 或 ADMIN

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
