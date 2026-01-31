package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.entity.UserVO;
import org.hibernate.validator.constraints.URL;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface UserService {
    User findByUsername(String username);

    // 修改：增加 code 参数
    void register(String username, String password, String email, String code);

    // 新增：发送验证码
    void sendCode(String email);

    // 更新用户
    void update(User user);

    // 邮箱登录
    String loginByEmail(String email, String code);

    // 修改头像
    void updateAvatar(String username, String avatarUrl);

    // 邮箱验证码重置密码
    void resetPassword(String email, String code, String newPassword);

    // ========== Admin Methods ==========

    // 分页查询用户
    IPage<User> getUserPage(Integer pageNum, Integer pageSize, String username, String role);

    // 根据ID获取用户
    User getUserById(Long id);

    // 更新用户角色
    void updateUserRole(Long userId, String role);

    // 重置用户密码
    void resetUserPassword(Long userId, String newPassword);

    // 删除用户
    void deleteUser(Long id);

    // 批量删除用户
    int deleteUsers(List<Long> ids, Long currentUserId);

    // 获取用户统计
    Map<String, Object> getUserStats();
}
