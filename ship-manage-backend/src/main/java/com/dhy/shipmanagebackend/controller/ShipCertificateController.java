package com.dhy.shipmanagebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.dto.BatchDeleteRequest;
import com.dhy.shipmanagebackend.entity.Result;
import com.dhy.shipmanagebackend.entity.ShipCertificate;
import com.dhy.shipmanagebackend.service.ShipCertificateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 船舶证书控制器
 */
@RestController
@RequestMapping("/certificate")
public class ShipCertificateController {

    @Autowired
    private ShipCertificateService certificateService;

    /**
     * 添加证书
     */
    @PostMapping
    @OperLog(module = "证书管理", operation = "新增证书")
    public Result add(@RequestBody @Valid ShipCertificate certificate) {
        certificateService.add(certificate);
        return Result.success();
    }

    /**
     * 更新证书
     */
    @PutMapping
    @OperLog(module = "证书管理", operation = "更新证书")
    public Result update(@RequestBody @Valid ShipCertificate certificate) {
        certificateService.update(certificate);
        return Result.success();
    }

    /**
     * 删除证书
     */
    @DeleteMapping("/{id}")
    @OperLog(module = "证书管理", operation = "删除证书")
    public Result delete(@PathVariable Long id) {
        certificateService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除证书
     */
    @DeleteMapping("/batch")
    @OperLog(module = "证书管理", operation = "批量删除证书")
    public Result deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            return Result.error("请选择要删除的证书");
        }
        int deletedCount = certificateService.deleteBatch(request.getIds());
        return Result.success("成功删除 " + deletedCount + " 个证书");
    }

    /**
     * 根据ID查询证书
     */
    @GetMapping("/{id}")
    public Result<ShipCertificate> getById(@PathVariable Long id) {
        return Result.success(certificateService.getById(id));
    }

    /**
     * 分页查询证书
     */
    @GetMapping
    public Result<IPage<ShipCertificate>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long shipId,
            @RequestParam(required = false) String status) {
        return Result.success(certificateService.page(pageNum, pageSize, shipId, status));
    }

    /**
     * 查询船舶的所有证书
     */
    @GetMapping("/ship/{shipId}")
    public Result<List<ShipCertificate>> listByShipId(@PathVariable Long shipId) {
        return Result.success(certificateService.listByShipId(shipId));
    }

    /**
     * 查询即将到期的证书
     */
    @GetMapping("/expiring")
    public Result<List<ShipCertificate>> findExpiringCertificates() {
        return Result.success(certificateService.findExpiringCertificates());
    }

    /**
     * 查询已过期的证书
     */
    @GetMapping("/expired")
    public Result<List<ShipCertificate>> findExpiredCertificates() {
        return Result.success(certificateService.findExpiredCertificates());
    }
}
