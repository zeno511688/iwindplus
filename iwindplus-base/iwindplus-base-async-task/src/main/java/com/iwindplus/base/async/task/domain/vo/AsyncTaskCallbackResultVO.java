/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.domain.vo;

import com.iwindplus.base.async.task.domain.enums.AsyncTaskCallbackResultEnum;
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
public class AsyncTaskCallbackResultVO implements Serializable {

    /**
     * 状态决策枚举.
     */
    @Schema(description = "状态决策枚举")
    private final AsyncTaskCallbackResultEnum status;

    private AsyncTaskCallbackResultVO(
        AsyncTaskCallbackResultEnum status) {
        this.status = status;
    }

    /**
     * 设置状态.
     *
     * @param status 状态
     * @return 当前执行结果
     */
    public static AsyncTaskCallbackResultVO setStatus(AsyncTaskCallbackResultEnum status) {
        return new AsyncTaskCallbackResultVO(
            status
        );
    }

    /**
     * 成功.
     *
     * @return 执行结果
     */
    public static AsyncTaskCallbackResultVO success() {
        return new AsyncTaskCallbackResultVO(
            AsyncTaskCallbackResultEnum.SUCCESS
        );
    }

    /**
     * 失败.
     *
     * @return 执行结果
     */
    public static AsyncTaskCallbackResultVO failed() {
        return new AsyncTaskCallbackResultVO(
            AsyncTaskCallbackResultEnum.FAILED
        );
    }

    /**
     * 异步等待.
     *
     * @return 执行结果
     */
    public static AsyncTaskCallbackResultVO waiting() {
        return new AsyncTaskCallbackResultVO(
            AsyncTaskCallbackResultEnum.WAITING
        );
    }
}