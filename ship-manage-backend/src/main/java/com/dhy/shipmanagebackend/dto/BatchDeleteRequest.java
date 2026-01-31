package com.dhy.shipmanagebackend.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量删除请求 DTO
 */
@Data
public class BatchDeleteRequest {
    private List<Long> ids;
}
