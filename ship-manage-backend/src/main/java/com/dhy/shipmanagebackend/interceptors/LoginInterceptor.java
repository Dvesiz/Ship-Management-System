package com.dhy.shipmanagebackend.interceptors;

import com.dhy.shipmanagebackend.utils.JwtUtil;
import com.dhy.shipmanagebackend.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的令牌 (token)
        String token = request.getHeader("Authorization");

        // 2. 校验令牌
        try {
            //从redis中获取相同的token
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String redisToken = ops.get(token);
            if (redisToken == null || !redisToken.equals(token)) {
                throw new RuntimeException("用户未登录");
            }
            // 解析 token 获取业务数据（claims）
            Map<String, Object> claims = JwtUtil.parseToken(token);

            // 3. 把业务数据存到 ThreadLocal 中
            // 这样后续的 Controller 就能直接用 ThreadLocalUtil.get() 拿到当前登录用户是谁了
            ThreadLocalUtil.set(claims);

            // 4. 放行
            return true;
        } catch (Exception e) {
            // 5. 校验失败（Token过期或被篡改），设置响应状态码 401 (未授权)
            response.setStatus(401);
            System.out.println(e.getMessage());
            // 不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // ⚠️ 非常重要：请求结束，清空 ThreadLocal 数据，防止内存泄漏
        ThreadLocalUtil.remove();
    }
}