/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 异步命令执行结果枚举.
 *
 * <p>由业务方在execute/executeSub中显式返回，决定任务的状态走向：
 * SUCCESS → 成功，FAILED → 失败，ASYNC_WAIT → 进入异步等待（等回调）</p>
 *
 * @author zengdegui
 * @since 2026/8/15
 */
@Getter
@RequiredArgsConstructor
public enum AsyncCmdExecuteResultEnum implements BaseEnum<Integer> {

    /**
     * 执行中.
     */
    EXECUTE(10, "执行中"),

    /**
     * 等待中.
     */
    WAITING(20, "等待中"),

    /**
     * 成功.
     */
    SUCCESS(30, "成功"),

    /**
     * 失败.
     */
    FAILED(40, "失败"),

    ;

    /**
     * 值.
     */
    private final Integer value;

    /**
     * 描述.
     */
    private final String desc;
}
