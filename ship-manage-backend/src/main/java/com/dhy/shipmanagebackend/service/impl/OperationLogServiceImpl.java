package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.OperationLog;
import com.dhy.shipmanagebackend.mapper.OperationLogMapper;
import com.dhy.shipmanagebackend.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    @Async
    public void save(OperationLog log) {
        // 异步保存日志，不影响主业务性能
        operationLogMapper.insert(log);
    }

    @Override
    public IPage<OperationLog> page(Integer pageNum, Integer pageSize, String username, String module, String operation) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasLength(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (StringUtils.hasLength(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (StringUtils.hasLength(operation)) {
            wrapper.eq(OperationLog::getOperation, operation);
        }
        
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return operationLogMapper.selectPage(page, wrapper);
    }

    @Override
    public void cleanOldLogs(int days) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreatedAt, LocalDateTime.now().minusDays(days));
        operationLogMapper.delete(wrapper);
    }
}
