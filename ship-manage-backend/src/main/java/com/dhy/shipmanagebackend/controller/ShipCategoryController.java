package com.dhy.shipmanagebackend.controller;


import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.dto.BatchDeleteRequest;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.ShipCategory;
import com.dhy.shipmanagebackend.service.ShipCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/ship-categories")
public class ShipCategoryController {
    @Autowired
    private ShipCategoryService shipCategoryService;

    @PostMapping
    @OperLog(module = "船舶分类", operation = "新增船舶分类")
    public Result add(@RequestBody @Validated ShipCategory shipCategory) {
        shipCategoryService.add(shipCategory);
        return Result.success();
    }

    @GetMapping
    public Result<List<ShipCategory>> list() {
        return Result.success(shipCategoryService.findAll());
    }

    @GetMapping("/detail")
    public Result<ShipCategory> detail(@RequestParam Long id) {
        return Result.success(shipCategoryService.findById(id));
    }

    @PutMapping
    @OperLog(module = "船舶分类", operation = "更新船舶分类")
    public Result update(@RequestBody @Validated ShipCategory shipCategory) {
        shipCategoryService.update(shipCategory);
        return Result.success();
    }

    @DeleteMapping
    @OperLog(module = "船舶分类", operation = "删除船舶分类")
    public Result delete(@RequestParam Long id) {
        shipCategoryService.delete(id);
        return Result.success();
    }

    // 批量删除船舶分类
    @DeleteMapping("/batch")
    @OperLog(module = "船舶分类", operation = "批量删除船舶分类")
    public Result deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的船舶分类");
        }
        int deletedCount = shipCategoryService.deleteBatch(request.getIds());
        return Result.success("成功删除 " + deletedCount + " 个船舶分类");
    }
}
