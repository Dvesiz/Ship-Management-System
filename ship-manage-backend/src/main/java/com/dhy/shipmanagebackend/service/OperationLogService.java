package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {
    
    /**
     * 保存操作日志
     */
    void save(OperationLog log);
    
    /**
     * 分页查询操作日志
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param username 用户名(可选)
     * @param module 模块(可选)
     * @param operation 操作类型(可选)
     */
    IPage<OperationLog> page(Integer pageNum, Integer pageSize, String username, String module, String operation);
    
    /**
     * 清理指定天数前的日志
     */
    void cleanOldLogs(int days);
}
