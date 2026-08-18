/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.ToString;

/**
 * 异步回调结果视图对象.
 *
 * @author zengdegui
 * @since 2026/8/16
 */
@Schema(description = "异步回调结果视图对象")
@ToString
@Getter
public class AsyncCmdCallbackResultVO implements Serializable {

    /**
     * 状态决策枚举.
     */
    @Schema(description = "状态决策枚举")
    private final AsyncCmdCallbackResultEnum status;

    private AsyncCmdCallbackResultVO(
        AsyncCmdCallbackResultEnum status) {
        this.status = status;
    }

    /**
     * 设置状态.
     *
     * @param status 状态
     * @return 当前执行结果
     */
    public static AsyncCmdCallbackResultVO setStatus(AsyncCmdCallbackResultEnum status) {
        return new AsyncCmdCallbackResultVO(
            status
        );
    }

    /**
     * 成功.
     *
     * @return 执行结果
     */
    public static AsyncCmdCallbackResultVO success() {
        return new AsyncCmdCallbackResultVO(
            AsyncCmdCallbackResultEnum.SUCCESS
        );
    }

    /**
     * 失败.
     *
     * @return 执行结果
     */
    public static AsyncCmdCallbackResultVO failed() {
        return new AsyncCmdCallbackResultVO(
            AsyncCmdCallbackResultEnum.FAILED
        );
    }

    /**
     * 异步等待.
     *
     * @return 执行结果
     */
    public static AsyncCmdCallbackResultVO waiting() {
        return new AsyncCmdCallbackResultVO(
            AsyncCmdCallbackResultEnum.WAITING
        );
    }
}