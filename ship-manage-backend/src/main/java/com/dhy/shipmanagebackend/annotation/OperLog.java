package com.dhy.shipmanagebackend.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operation() default "";

    /**
     * 操作描述（可选，用于记录详细的操作内容）
     * 支持SpEL表达式，如: "删除了船舶#{#name}"
     */
    String description() default "";
}
