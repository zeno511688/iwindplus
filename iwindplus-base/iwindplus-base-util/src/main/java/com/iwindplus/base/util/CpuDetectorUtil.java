/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;

/**
 * CPU检测工具.
 *
 * @author zengdegui
 * @since 2026/03/01 02:46
 */
@Slf4j
public class CpuDetectorUtil {

    private CpuDetectorUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 获取可用CPU核心数.
     *
     * @return int
     */
    public static int getEffectiveCpu() {
        int available = Runtime.getRuntime().availableProcessors();

        String parallelism =
            System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");

        if (parallelism != null) {
            try {
                return Math.min(available, Integer.parseInt(parallelism));
            } catch (Exception ignored) {
                log.error("Failed to parse cpu count", ignored);
            }
        }

        return available;
    }

    /**
     * 推荐CPU核心数.
     *
     * @return int
     */
    public static int recommendCore() {
        return Math.min(getEffectiveCpu() * 2, 64);
    }

    /**
     * 推荐最大CPU核心数.
     *
     * @return int
     */
    public static int recommendMax() {
        return Math.min(getEffectiveCpu() * 8, 256);
    }
}
