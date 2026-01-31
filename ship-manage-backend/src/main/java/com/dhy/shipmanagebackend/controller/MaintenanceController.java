package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.dto.BatchDeleteRequest;
import com.dhy.shipmanagebackend.entity.Maintenance;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    // 1. 新增维修记录
    @PostMapping
    @OperLog(module = "维修管理", operation = "新增维修记录")
    public Result add(@RequestBody @Validated Maintenance maintenance) {
        maintenanceService.add(maintenance);
        return Result.success();
    }

    // 2. 分页查询
    // URL: /maintenance?pageNum=1&pageSize=10&shipId=1
    @GetMapping
    public Result<Page<Maintenance>> list(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) Long shipId) {

        Page<Maintenance> page = maintenanceService.findPage(pageNum, pageSize, shipId);
        return Result.success(page);
    }

    // 3. 详情
    @GetMapping("/detail")
    public Result<Maintenance> detail(@RequestParam Long id) {
        return Result.success(maintenanceService.findById(id));
    }

    // 4. 删除 (补充功能)
    @DeleteMapping
    @OperLog(module = "维修管理", operation = "删除维修记录")
    public Result delete(@RequestParam Long id) {
        maintenanceService.delete(id);
        return Result.success();
    }

    // 5. 批量删除维修记录
    @DeleteMapping("/batch")
    @OperLog(module = "维修管理", operation = "批量删除维修记录")
    public Result deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的维修记录");
        }
        int deletedCount = maintenanceService.deleteBatch(request.getIds());
        return Result.success("成功删除 " + deletedCount + " 条维修记录");
    }
}