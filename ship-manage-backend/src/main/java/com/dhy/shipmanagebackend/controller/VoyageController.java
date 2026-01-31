package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.dto.BatchDeleteRequest;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.Voyage;
import com.dhy.shipmanagebackend.service.VoyageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/voyages")
public class VoyageController {

    @Autowired
    private VoyageService voyageService;

    // 1. 开航 (新增航次)
    @PostMapping
    @OperLog(module = "航次管理", operation = "新增航次")
    public Result add(@RequestBody @Validated Voyage voyage) {
        voyageService.add(voyage);
        return Result.success();
    }

    // 2. 完工 (结束航次)
    // PATCH /voyages/finish?id=1
    @PatchMapping("/finish")
    @OperLog(module = "航次管理", operation = "完成航次")
    public Result finish(@RequestParam Long id) {
        voyageService.finish(id);
        return Result.success();
    }

    // 3. 分页查询
    @GetMapping
    public Result<Page<Voyage>> list(@RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long shipId) {
        Page<Voyage> page = voyageService.findPage(pageNum, pageSize, shipId);
        return Result.success(page);
    }

    // 4. 详情
    @GetMapping("/detail")
    public Result<Voyage> detail(@RequestParam Long id) {
        return Result.success(voyageService.findById(id));
    }

    // 5. 删除航次
    @DeleteMapping("/{id}")
    @OperLog(module = "航次管理", operation = "删除")
    public Result delete(@PathVariable Long id) {
        voyageService.delete(id);
        return Result.success();
    }

    // 6. 批量删除航次
    @DeleteMapping("/batch")
    @OperLog(module = "航次管理", operation = "批量删除航次")
    public Result deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的航次");
        }
        int deletedCount = voyageService.deleteBatch(request.getIds());
        return Result.success("成功删除 " + deletedCount + " 个航次");
    }
}