package com.dhy.shipmanagebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dhy.shipmanagebackend.entity.ShipCertificate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 船舶证书Mapper接口
 */
@Mapper
public interface ShipCertificateMapper extends BaseMapper<ShipCertificate> {
    
    /**
     * 查询即将到期的证书(30天内)
     */
    @Select("SELECT * FROM ship_certificates WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)")
    List<ShipCertificate> findExpiringCertificates();
    
    /**
     * 查询已过期的证书
     */
    @Select("SELECT * FROM ship_certificates WHERE expiry_date < CURDATE()")
    List<ShipCertificate> findExpiredCertificates();
}
