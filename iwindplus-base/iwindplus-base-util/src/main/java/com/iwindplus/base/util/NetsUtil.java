/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.net.NetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 网络工具类.
 *
 * @author zengdegui
 * @since 2021/1/11
 */
@Slf4j
public class NetsUtil extends NetUtil {

    /**
     * 下一个最大重试间隔.
     *
     * @param period    初始间隔时间（单位：毫秒）
     * @param maxPeriod 最大重试间隔时间（单位：毫秒）
     * @param attempt   第几次
     * @return long
     */
    public static long nextMaxInterval(long period, long maxPeriod, int attempt) {
        int ii = attempt - 1;
        long interval = (long) (period * Math.pow(1.5d, ii));
        return Math.min(interval, maxPeriod);
    }
}
