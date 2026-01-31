package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.dto.BatchDeleteRequest;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.Ship;
import com.dhy.shipmanagebackend.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/ship")
public class ShipController {
    @Autowired
    private ShipService shipService;

    @PostMapping
    @OperLog(module = "船舶管理", operation = "新增船舶")
    public Result add(@RequestBody @Validated Ship ship) {
        shipService.add(ship);
        return Result.success();
    }

    // 2. 分页查询
    // URL 示例: /ships?pageNum=1&pageSize=10&name=远洋&status=IN_SERVICE
    @GetMapping
    public Result<Page<Ship>> list(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String status) {

        Page<Ship> page = shipService.findPage(pageNum, pageSize, categoryId, name, status);
        return Result.success(page);
    }

    // 3. 详情
    @GetMapping("/detail")
    public Result<Ship> detail(@RequestParam Long id) {
        return Result.success(shipService.findById(id));
    }

    // 4. 更新
    @PutMapping
    @OperLog(module = "船舶管理", operation = "更新船舶")
    public Result update(@RequestBody @Validated Ship ship) {
        if (ship.getId() == null) {
            return Result.error("ID不能为空");
        }
        shipService.update(ship);
        return Result.success();
    }

    // 5. 删除
    @DeleteMapping
    @OperLog(module = "船舶管理", operation = "删除船舶")
    public Result delete(@RequestParam Long id) {
        shipService.delete(id);
        return Result.success();
    }

    // 6. 批量删除船舶
    @DeleteMapping("/batch")
    @OperLog(module = "船舶管理", operation = "批量删除船舶")
    public Result deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的船舶");
        }
        int deletedCount = shipService.deleteBatch(request.getIds());
        return Result.success("成功删除 " + deletedCount + " 个船舶");
    }
}
