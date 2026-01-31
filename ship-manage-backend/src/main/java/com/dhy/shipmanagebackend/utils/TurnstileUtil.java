package com.dhy.shipmanagebackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TurnstileUtil {

    @Value("${cloudflare.turnstile.secret-key:}")
    private String secretKey;

    @Value("${cloudflare.turnstile.url:}")
    private String verifyUrl;

    @Value("${turnstile.enabled:true}")
    private boolean enabled;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verify(String token) {
        // 本地开发环境可关闭 Turnstile 验证
        if (!enabled) {
            return true;
        }

        if (token == null || token.isEmpty()) {
            return false;
        }

        // 如果未配置密钥，跳过验证（开发环境）
        if (secretKey == null || secretKey.isEmpty()) {
            System.out.println("[Turnstile] 未配置密钥，跳过验证");
            return true;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", token);

        try {
            // 发送请求给 Cloudflare 验证 token
            Map<String, Object> response = restTemplate.postForObject(verifyUrl, body, Map.class);

            // 检查 success 字段
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("[Turnstile] 验证失败: " + e.getMessage());
            return false;
        }
        return false;
    }
}