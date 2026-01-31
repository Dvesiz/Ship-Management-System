package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.dto.BatchDeleteRequest;
import com.dhy.shipmanagebackend.entity.FuelRecord;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.service.FuelRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 燃油记录控制器
 */
@RestController
@RequestMapping("/fuel")
public class FuelRecordController {

    @Autowired
    private FuelRecordService fuelRecordService;

    /**
     * 添加燃油记录
     */
    @PostMapping
    @OperLog(module = "燃油管理", operation = "新增燃油记录")
    public Result add(@RequestBody @Valid FuelRecord record) {
        fuelRecordService.add(record);
        return Result.success();
    }

    /**
     * 更新燃油记录
     */
    @PutMapping
    @OperLog(module = "燃油管理", operation = "更新燃油记录")
    public Result update(@RequestBody @Valid FuelRecord record) {
        fuelRecordService.update(record);
        return Result.success();
    }

    /**
     * 删除燃油记录
     */
    @DeleteMapping("/{id}")
    @OperLog(module = "燃油管理", operation = "删除燃油记录")
    public Result delete(@PathVariable Long id) {
        fuelRecordService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除燃油记录
     */
    @DeleteMapping("/batch")
    @OperLog(module = "燃油管理", operation = "批量删除燃油记录")
    public Result deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的燃油记录");
        }
        int deletedCount = fuelRecordService.deleteBatch(request.getIds());
        return Result.success("成功删除 " + deletedCount + " 条燃油记录");
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public Result<FuelRecord> getById(@PathVariable Long id) {
        return Result.success(fuelRecordService.getById(id));
    }

    /**
     * 分页查询
     */
    @GetMapping
    public Result<IPage<FuelRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long shipId,
            @RequestParam(required = false) Long voyageId,
            @RequestParam(required = false) String fuelType) {
        return Result.success(fuelRecordService.page(pageNum, pageSize, shipId, voyageId, fuelType));
    }

    /**
     * 统计船舶燃油总消耗
     */
    @GetMapping("/stats/quantity/{shipId}")
    public Result<BigDecimal> sumQuantityByShipId(@PathVariable Long shipId) {
        return Result.success(fuelRecordService.sumQuantityByShipId(shipId));
    }

    /**
     * 统计船舶燃油总费用
     */
    @GetMapping("/stats/cost/{shipId}")
    public Result<BigDecimal> sumCostByShipId(@PathVariable Long shipId) {
        return Result.success(fuelRecordService.sumCostByShipId(shipId));
    }

    /**
     * 按月统计燃油消耗
     */
    @GetMapping("/stats/monthly/{shipId}")
    public Result<List<Map<String, Object>>> monthlyStatsByShipId(@PathVariable Long shipId) {
        return Result.success(fuelRecordService.monthlyStatsByShipId(shipId));
    }

    /**
     * 获取燃油消耗统计概览
     */
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getStatisticsOverview() {
        return Result.success(fuelRecordService.getStatisticsOverview());
    }
}
