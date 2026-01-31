package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.dto.BatchDeleteRequest;
import com.dhy.shipmanagebackend.entity.Crew;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.service.CrewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crew")
public class CrewController {

    @Autowired
    private CrewService crewService;

    // 1. 新增船员
    @PostMapping
    @OperLog(module = "船员管理", operation = "新增船员")
    public Result add(@RequestBody @Validated Crew crew) {
        crewService.add(crew);
        return Result.success();
    }

    // 2. 分页查询
    // 示例: /crew?pageNum=1&pageSize=10&name=张&shipId=1
    @GetMapping
    public Result<Page<Crew>> list(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) String name,
                                   @RequestParam(required = false) Long shipId) {

        Page<Crew> page = crewService.findPage(pageNum, pageSize, name, shipId);
        return Result.success(page);
    }

    // 3. 船员详情
    @GetMapping("/detail")
    public Result<Crew> detail(@RequestParam Long id) {
        Crew crew = crewService.findById(id);
        return Result.success(crew);
    }

    // 4. 更新船员信息
    @PutMapping
    @OperLog(module = "船员管理", operation = "更新船员")
    public Result update(@RequestBody @Validated Crew crew) {
        if (crew.getId() == null) {
            return Result.error("ID不能为空");
        }
        crewService.update(crew);
        return Result.success();
    }

    // 5. 删除船员
    @DeleteMapping
    @OperLog(module = "船员管理", operation = "删除船员")
    public Result delete(@RequestParam Long id) {
        crewService.delete(id);
        return Result.success();
    }

    // 6. 批量删除船员
    @DeleteMapping("/batch")
    @OperLog(module = "船员管理", operation = "批量删除船员")
    public Result deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的船员");
        }
        int deletedCount = crewService.deleteBatch(request.getIds());
        return Result.success("成功删除 " + deletedCount + " 个船员");
    }
}