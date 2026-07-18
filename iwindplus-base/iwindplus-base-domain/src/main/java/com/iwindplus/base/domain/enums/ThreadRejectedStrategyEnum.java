/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 多线程拒绝策略枚举定义.
 *
 * @author zengdegui
 * @since 2024/11/17 11:16
 */

@Getter
@RequiredArgsConstructor
public enum ThreadRejectedStrategyEnum implements BaseEnum<String> {
    /**
     * 中止策略.
     */
    ABORT_POLICY("abortPolicy", "中止策略"),

    /**
     * 调用者运行策略.
     */
    CALLER_RUNS_POLICY("callerRunsPolicy", "调用者运行策略"),

    /**
     * 丢弃策略.
     */
    DISCARD_POLICY("discardPolicy", "丢弃策略"),

    /**
     * 丢弃最旧的策略.
     */
    DISCARD_OLDEST_POLICY("discardOldestPolicy", "丢弃最旧的策略"),

    ;

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}
