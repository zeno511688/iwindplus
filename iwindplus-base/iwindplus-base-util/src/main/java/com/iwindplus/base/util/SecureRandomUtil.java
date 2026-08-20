/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import java.security.SecureRandom;
import java.util.stream.Collectors;

/**
 * 安全随机数工具类.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
public final class SecureRandomUtil {

    private SecureRandomUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 安全随机数生成器实例.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成指定长度的随机字符串（包含大小写字母和数字）.
     * 用于生成密钥、token等安全敏感数据.
     *
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        // 使用Base64编码的随机字节，然后截取指定长度
        byte[] bytes = new byte[length * 3 / 4 + 1];
        SECURE_RANDOM.nextBytes(bytes);
        String encoded = java.util.Base64.getEncoder().withoutPadding().encodeToString(bytes);

        // 移除非字母数字字符，只保留字母和数字
        String filtered = encoded.chars()
            .filter(Character::isLetterOrDigit)
            .mapToObj(c -> String.valueOf((char) c))
            .collect(Collectors.joining());

        // 如果过滤后的长度不够，重新生成
        while (filtered.length() < length) {
            byte[] additionalBytes = new byte[length * 3 / 4 + 1];
            SECURE_RANDOM.nextBytes(additionalBytes);
            String additionalEncoded = java.util.Base64.getEncoder().withoutPadding().encodeToString(additionalBytes);
            String additionalFiltered = additionalEncoded.chars()
                .filter(Character::isLetterOrDigit)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());
            filtered += additionalFiltered;
        }

        return filtered.substring(0, length);
    }

    /**
     * 生成指定长度的随机数字字符串.
     * 用于生成验证码等场景.
     *
     * @param length 数字字符串长度
     * @return 随机数字字符串
     */
    public static String randomNumbers(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成指定长度的随机字节.
     *
     * @param length 字节长度
     * @return 随机字节数组
     */
    public static byte[] randomBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * 生成随机整数（包含边界）.
     *
     * @param bound 上界（不包含）
     * @return 随机整数
     */
    public static int randomInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        return SECURE_RANDOM.nextInt(bound);
    }

    /**
     * 生成随机长整数.
     *
     * @return 随机长整数
     */
    public static long randomLong() {
        return SECURE_RANDOM.nextLong();
    }

    /**
     * 获取SecureRandom实例.
     *
     * @return SecureRandom实例
     */
    public static SecureRandom getSecureRandom() {
        return SECURE_RANDOM;
    }
}
