package com.dhy.shipmanagebackend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * 使用 HMAC256 算法生成和验证 Token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:your-256-bit-secret-key-here-must-be-at-least-32-characters}")
    private String secretKey;

    @Value("${jwt.expiration:12}")
    private int expirationHours;

    // 静态实例用于支持静态方法调用
    private static JwtUtil instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 生成 JWT Token
     * @param claims 业务数据
     * @return Token 字符串
     */
    public static String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * instance.expirationHours))
                .sign(Algorithm.HMAC256(instance.secretKey));
    }

    /**
     * 验证并解析 JWT Token
     * @param token Token 字符串
     * @return 业务数据
     */
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(instance.secretKey))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
}
