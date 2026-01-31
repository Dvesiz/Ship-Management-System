package com.dhy.shipmanagebackend.exception;

import com.dhy.shipmanagebackend.entity.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理所有 Controller 层抛出的异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数校验异常 (ConstraintViolationException)
     * 比如：@Pattern, @Email, @NotBlank 等注解校验失败时抛出的异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        // 提取错误信息，例如 "register.code: 验证码不能为空" -> "验证码不能为空"
        String message = e.getMessage();
        if (StringUtils.hasLength(message)) {
            // 取冒号后面的部分
            String[] split = message.split(": ");
            if (split.length > 1) {
                message = split[1];
            }
        }
        logger.warn("参数校验失败: {}", message);
        return Result.error(message);
    }

    /**
     * 处理业务异常 (RuntimeException)
     * 比如：UserServiceImpl 中抛出的 "验证码错误"、"用户名已存在"
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        // 记录异常日志，但不暴露堆栈给前端
        logger.error("业务异常: {}", e.getMessage(), e);
        
        // 返回用户友好的错误信息
        String message = e.getMessage();
        if (!StringUtils.hasLength(message) || message.contains("Exception") || message.contains("Error")) {
            message = "操作失败，请稍后重试";
        }
        return Result.error(message);
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        // 记录详细错误日志用于排查问题
        logger.error("系统异常: {}", e.getMessage(), e);
        
        // 返回通用错误信息，不暴露内部细节
        return Result.error("系统繁忙，请稍后重试");
    }
}