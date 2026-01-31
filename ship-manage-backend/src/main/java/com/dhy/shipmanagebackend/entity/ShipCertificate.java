package com.dhy.shipmanagebackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 船舶证书实体类
 * 管理船舶的各类证书信息
 */
@Data
@TableName("ship_certificates")
public class ShipCertificate {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "必须指定船舶")
    private Long shipId;           // 船舶ID

    @NotBlank(message = "证书名称不能为空")
    private String certificateName; // 证书名称 (如: 船舶国籍证书、船舶检验证书等)

    private String certificateNo;   // 证书编号
    private String issuingAuthority; // 发证机关
    private LocalDate issueDate;    // 发证日期
    private LocalDate expiryDate;   // 到期日期
    
    /**
     * 证书状态:
     * VALID - 有效
     * EXPIRING - 即将到期(30天内)
     * EXPIRED - 已过期
     */
    private String status;
    
    private String attachmentUrl;   // 证书附件URL
    private String remarks;         // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
