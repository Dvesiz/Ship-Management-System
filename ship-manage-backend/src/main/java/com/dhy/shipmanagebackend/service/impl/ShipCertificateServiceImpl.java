package com.dhy.shipmanagebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhy.shipmanagebackend.entity.ShipCertificate;
import com.dhy.shipmanagebackend.mapper.ShipCertificateMapper;
import com.dhy.shipmanagebackend.service.ShipCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 船舶证书服务实现类
 */
@Service
public class ShipCertificateServiceImpl implements ShipCertificateService {

    @Autowired
    private ShipCertificateMapper certificateMapper;

    @Override
    public void add(ShipCertificate certificate) {
        // 自动设置证书状态
        updateStatus(certificate);
        certificateMapper.insert(certificate);
    }

    @Override
    public void update(ShipCertificate certificate) {
        // 自动更新证书状态
        updateStatus(certificate);
        certificateMapper.updateById(certificate);
    }

    @Override
    public void delete(Long id) {
        certificateMapper.deleteById(id);
    }

    @Override
    public ShipCertificate getById(Long id) {
        return certificateMapper.selectById(id);
    }

    @Override
    public IPage<ShipCertificate> page(Integer pageNum, Integer pageSize, Long shipId, String status) {
        Page<ShipCertificate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShipCertificate> wrapper = new LambdaQueryWrapper<>();
        
        if (shipId != null) {
            wrapper.eq(ShipCertificate::getShipId, shipId);
        }
        if (StringUtils.hasLength(status)) {
            wrapper.eq(ShipCertificate::getStatus, status);
        }
        
        wrapper.orderByAsc(ShipCertificate::getExpiryDate);
        return certificateMapper.selectPage(page, wrapper);
    }

    @Override
    public List<ShipCertificate> listByShipId(Long shipId) {
        LambdaQueryWrapper<ShipCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShipCertificate::getShipId, shipId)
               .orderByAsc(ShipCertificate::getExpiryDate);
        return certificateMapper.selectList(wrapper);
    }

    @Override
    public List<ShipCertificate> findExpiringCertificates() {
        return certificateMapper.findExpiringCertificates();
    }

    @Override
    public List<ShipCertificate> findExpiredCertificates() {
        return certificateMapper.findExpiredCertificates();
    }

    @Override
    public void updateCertificateStatus() {
        // 更新所有证书状态
        List<ShipCertificate> allCertificates = certificateMapper.selectList(null);
        for (ShipCertificate cert : allCertificates) {
            String oldStatus = cert.getStatus();
            updateStatus(cert);
            if (!cert.getStatus().equals(oldStatus)) {
                certificateMapper.updateById(cert);
            }
        }
    }
    
    /**
     * 根据到期日期更新证书状态
     */
    private void updateStatus(ShipCertificate certificate) {
        if (certificate.getExpiryDate() == null) {
            certificate.setStatus("VALID");
            return;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = certificate.getExpiryDate();
        
        if (expiryDate.isBefore(today)) {
            certificate.setStatus("EXPIRED");
        } else if (expiryDate.isBefore(today.plusDays(30))) {
            certificate.setStatus("EXPIRING");
        } else {
            certificate.setStatus("VALID");
        }
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return certificateMapper.deleteBatchIds(ids);
    }
}
