package com.dhy.shipmanagebackend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptUtil {
    // 实例化一个 encoder 对象
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * @param rawPassword 明文密码
     * @return 加密后的哈希字符串
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码
     * @param rawPassword 明文密码 (用户输入的)
     * @param encodedPassword 密文密码 (数据库存的)
     * @return true=匹配, false=不匹配
     */
    public static boolean match(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}