/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Disruptor等待策略类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum DisruptorWaitStrategyEnum implements BaseEnum<String> {

    /**
     * 阻塞等待策略.
     *
     * <p>
     * 基于 Lock + Condition 实现， CPU 占用最低，但线程切换开销较大。 适用于普通业务系统。
     * </p>
     */
    BLOCKING("blocking", "阻塞等待"),

    /**
     * 轻量级阻塞等待策略.
     *
     * <p>
     * BlockingWaitStrategy 的优化版本， 减少锁竞争和线程唤醒开销， 在低 CPU 占用下获得更好的吞吐性能。
     * </p>
     */
    LITE_BLOCKING("liteBlocking", "轻量阻塞等待"),

    /**
     * 带超时的轻量级阻塞等待策略.
     *
     * <p>
     * 在 LiteBlockingWaitStrategy 基础上增加超时机制， 避免消费者线程长期阻塞， 适用于批处理和异步落库场景。
     * </p>
     */
    LITE_TIMEOUT_BLOCKING("liteTimeoutBlocking", "轻量超时阻塞等待"),

    /**
     * 带超时的阻塞等待策略.
     *
     * <p>
     * 在 BlockingWaitStrategy 基础上增加超时控制， 超时后会重新检查序列状态， 防止线程永久挂起。
     * </p>
     */
    TIMEOUT_BLOCKING("timeoutBlocking", "超时阻塞等待"),

    /**
     * 睡眠等待策略.
     *
     * <p>
     * 先进行短暂自旋， 随后进入短时间休眠， 在 CPU 消耗和延迟之间取得平衡。
     * </p>
     */
    SLEEPING("sleeping", "睡眠等待"),

    /**
     * 让出 CPU 等待策略.
     *
     * <p>
     * 当没有可消费事件时调用 Thread.yield()， 延迟较低， 适用于高吞吐、低延迟场景。
     * </p>
     */
    YIELDING("yielding", "让出CPU等待"),

    /**
     * 忙等待策略.
     *
     * <p>
     * 持续自旋等待事件， 不发生线程切换， 延迟最低但 CPU 占用最高。 适用于撮合引擎、风控等超低延迟系统。
     * </p>
     */
    BUSY_SPIN("busySpin", "忙等待");

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