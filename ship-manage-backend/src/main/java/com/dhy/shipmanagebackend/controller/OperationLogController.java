package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.entity.OperationLog;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.service.OperationLogService;
import com.dhy.shipmanagebackend.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 操作日志控制器
 * 仅管理员可访问
 */
@RestController
@RequestMapping("/log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public Result<IPage<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation) {
        
        // 检查是否为管理员
        checkAdmin();
        
        return Result.success(operationLogService.page(pageNum, pageSize, username, module, operation));
    }

    /**
     * 清理指定天数前的日志
     */
    @DeleteMapping("/clean/{days}")
    public Result cleanOldLogs(@PathVariable int days) {
        // 检查是否为管理员
        checkAdmin();
        
        operationLogService.cleanOldLogs(days);
        return Result.success();
    }
    
    /**
     * 检查当前用户是否为管理员
     */
    private void checkAdmin() {
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        String role = (String) userInfo.get("role");
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权限访问");
        }
    }
}
