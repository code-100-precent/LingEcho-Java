package com.lingecho.common.core.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 随机字符串工具类
 */
public class RandomUtil {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String ALPHANUMERIC = LETTERS + NUMBERS;

    private static final Random RANDOM = new SecureRandom();

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return randomString(length, ALPHANUMERIC);
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @param chars  字符集
     * @return 随机字符串
     */
    public static String randomString(int length, String chars) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机数字字符串
     *
     * @param length 长度
     * @return 随机数字字符串
     */
    public static String randomNumberString(int length) {
        return randomString(length, NUMBERS);
    }

    /**
     * 生成随机验证码（6位数字）
     *
     * @return 6位数字验证码
     */
    public static String randomVerificationCode() {
        return randomNumberString(6);
    }
}

