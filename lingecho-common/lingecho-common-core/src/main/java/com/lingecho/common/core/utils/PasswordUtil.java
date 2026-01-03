package com.lingecho.common.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码工具类
 * 与 Go 版本的 HashPassword 保持一致
 */
public class PasswordUtil {

    /**
     * 对密码进行 SHA256 哈希加密
     * 格式：sha256$<hex>
     *
     * @param password 原始密码
     * @return 加密后的密码字符串
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return "sha256$" + hexString;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * 验证密码
     *
     * @param userPassword 数据库中存储的加密密码
     * @param inputPassword 用户输入的原始密码
     * @return 是否匹配
     */
    public static boolean checkPassword(String userPassword, String inputPassword) {
        if (userPassword == null || userPassword.isEmpty()) {
            return false;
        }
        String hashedInput = hashPassword(inputPassword);
        return userPassword.equals(hashedInput);
    }
}

