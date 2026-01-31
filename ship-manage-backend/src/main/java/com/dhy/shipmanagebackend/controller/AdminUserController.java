package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.User;
import com.dhy.shipmanagebackend.entity.UserVO;
import com.dhy.shipmanagebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器（管理员功能）
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<IPage<UserVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        
        checkAdmin();
        
        IPage<User> userPage = userService.getUserPage(pageNum, pageSize, username, role);
        
        // 转换为VO，隐藏密码
        IPage<UserVO> voPage = userPage.convert(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });
        
        return Result.success(voPage);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        checkAdmin();
        
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return Result.success(vo);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/role")
    @OperLog(module = "用户管理", operation = "修改用户角色")
    public Result updateRole(@RequestParam Long userId, @RequestParam String role) {
        checkAdmin();
        
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            return Result.error("无效的角色类型");
        }
        
        userService.updateUserRole(userId, role);
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/reset-password")
    @OperLog(module = "用户管理", operation = "重置用户密码")
    public Result resetPassword(@RequestParam Long userId, @RequestParam String newPassword) {
        checkAdmin();
        
        userService.resetUserPassword(userId, newPassword);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @OperLog(module = "用户管理", operation = "删除用户")
    public Result delete(@PathVariable Long id) {
        checkAdmin();
        
        // 不能删除自己
        Long currentUserId = getCurrentUserId();
        if (currentUserId.equals(id)) {
            return Result.error("不能删除自己的账号");
        }
        
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @OperLog(module = "用户管理", operation = "批量删除用户")
    public Result batchDelete(@RequestBody List<Long> ids) {
        checkAdmin();
        
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的用户");
        }
        
        Long currentUserId = getCurrentUserId();
        int deletedCount = userService.deleteUsers(ids, currentUserId);
        
        if (deletedCount == 0) {
            return Result.error("不能删除自己的账号，请重新选择");
        }
        
        return Result.success("成功删除 " + deletedCount + " 个用户");
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        checkAdmin();
        
        Map<String, Object> stats = userService.getUserStats();
        return Result.success(stats);
    }

    /**
     * 检查当前用户是否为管理员
     */
    private void checkAdmin() {
        Map<String, Object> userInfo = com.dhy.shipmanagebackend.utils.ThreadLocalUtil.get();
        String role = (String) userInfo.get("role");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权限访问");
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Map<String, Object> userInfo = com.dhy.shipmanagebackend.utils.ThreadLocalUtil.get();
        return ((Number) userInfo.get("id")).longValue();
    }
}
