package com.dhy.shipmanagebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhy.shipmanagebackend.entity.ShipCertificate;

import java.util.List;

/**
 * 船舶证书服务接口
 */
public interface ShipCertificateService {
    
    /**
     * 添加证书
     */
    void add(ShipCertificate certificate);
    
    /**
     * 更新证书
     */
    void update(ShipCertificate certificate);
    
    /**
     * 删除证书
     */
    void delete(Long id);

    /**
     * 批量删除证书
     */
    int deleteBatch(List<Long> ids);
    
    /**
     * 根据ID查询
     */
    ShipCertificate getById(Long id);
    
    /**
     * 分页查询
     */
    IPage<ShipCertificate> page(Integer pageNum, Integer pageSize, Long shipId, String status);
    
    /**
     * 查询船舶的所有证书
     */
    List<ShipCertificate> listByShipId(Long shipId);
    
    /**
     * 查询即将到期的证书
     */
    List<ShipCertificate> findExpiringCertificates();
    
    /**
     * 查询已过期的证书
     */
    List<ShipCertificate> findExpiredCertificates();
    
    /**
     * 更新证书状态(定时任务调用)
     */
    void updateCertificateStatus();
}
