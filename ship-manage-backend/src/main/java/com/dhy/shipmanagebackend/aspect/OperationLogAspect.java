package com.dhy.shipmanagebackend.aspect;

import com.dhy.shipmanagebackend.annotation.OperLog;
import com.dhy.shipmanagebackend.entity.OperationLog;
import com.dhy.shipmanagebackend.service.OperationLogService;
import com.dhy.shipmanagebackend.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 操作日志切面
 * 自动记录带有@OperLog注解的方法的操作日志
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 切点：所有带有@OperLog注解的方法
     */
    @Pointcut("@annotation(com.dhy.shipmanagebackend.annotation.OperLog)")
    public void operLogPointcut() {
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperLog operLog = method.getAnnotation(OperLog.class);

        // 构建日志对象
        OperationLog log = new OperationLog();
        log.setModule(operLog.module());

        // 获取当前用户信息
        String username = "匿名用户";
        try {
            Map<String, Object> userInfo = ThreadLocalUtil.get();
            if (userInfo != null) {
                log.setUserId(((Number) userInfo.get("id")).longValue());
                username = (String) userInfo.get("username");
                log.setUsername(username);
            }
        } catch (Exception e) {
            log.setUserId(0L);
            log.setUsername("anonymous");
        }

        // 设置操作类型
        log.setOperation(operLog.operation());

        // 获取请求信息
        if (request != null) {
            // 设置请求方法 (GET/POST/PUT/DELETE/....)
            log.setMethod(request.getMethod());

            // 设置请求URL
            log.setRequestUrl(request.getRequestURI());

            // 设置User-Agent
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.length() > 500) {
                userAgent = userAgent.substring(0, 500);
            }
            log.setUserAgent(userAgent);

            // 设置IP地址
            log.setIpAddress(getClientIp(request));

            // 获取请求参数
            try {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    String params = objectMapper.writeValueAsString(args);
                    // 限制参数长度，避免存储过大
                    if (params.length() > 2000) {
                        params = params.substring(0, 2000) + "...";
                    }
                    log.setRequestParams(params);
                }
            } catch (Exception e) {
                log.setRequestParams("参数序列化失败");
            }
        }

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            log.setResponseResult("SUCCESS");
        } catch (Throwable e) {
            log.setResponseResult("FAILED");
            log.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            log.setExecutionTime(executionTime);

            // 设置创建时间
            log.setCreatedAt(java.time.LocalDateTime.now());

            // 异步保存日志
            operationLogService.save(log);
        }

        return result;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
