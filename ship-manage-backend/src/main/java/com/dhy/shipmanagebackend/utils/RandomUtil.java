package com.dhy.shipmanagebackend.utils;

import java.util.Random;

public class RandomUtil {
    public static String getSixBitRandom() {
        Random random = new Random();
        // 生成 100000 - 999999 之间的随机数
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }
}