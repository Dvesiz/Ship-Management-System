package com.dhy.shipmanagebackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结构
 * @param <T> 数据泛型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;   // 0: 成功, 其他: 错误码
    private String message; // 提示信息
    private T data;         // 返回数据

    // 快速成功响应（无数据）
    public static <E> Result<E> success() {
        return new Result<>(0, "success", null);
    }

    // 快速成功响应（带数据）
    public static <E> Result<E> success(E data) {
        return new Result<>(0, "success", data);
    }

    // 快速失败响应
    public static <E> Result<E> error(String message) {
        return new Result<>(1, message, null); // 默认错误码1，也可以自定义
    }
}