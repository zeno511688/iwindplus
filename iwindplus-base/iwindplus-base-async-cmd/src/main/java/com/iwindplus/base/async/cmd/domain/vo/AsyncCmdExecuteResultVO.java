/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.util.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

/**
 * 异步命令执行结果视图对象.
 *
 * @author zengdegui
 * @since 2026/8/16
 */
@Schema(description = "异步命令执行结果视图对象")
@ToString
@Getter
public class AsyncCmdExecuteResultVO implements Serializable {

    /**
     * 状态决策枚举.
     */
    @Schema(description = "状态决策枚举")
    private final AsyncCmdExecuteResultEnum status;

    /**
     * 业务返回值（可选），最终合并到 result 字段中持久化.
     */
    @Schema(description = "结果")
    private Map<String, Object> result;

    private AsyncCmdExecuteResultVO(
        AsyncCmdExecuteResultEnum status,
        Map<String, Object> result) {
        this.status = status;
        this.result = result;
    }

    /**
     * 设置状态.
     *
     * @param status 状态
     * @return 当前执行结果
     */
    public static AsyncCmdExecuteResultVO setStatus(AsyncCmdExecuteResultEnum status) {
        return new AsyncCmdExecuteResultVO(
            status,
            null
        );
    }

    /**
     * 设置业务返回值.
     *
     * <p>适用于任意业务对象，会通过 Jackson 转换为 Map.</p>
     *
     * @param data 业务返回值
     * @param <T>  业务返回值类型
     * @return 当前执行结果
     */
    public <T> AsyncCmdExecuteResultVO setResultData(T data) {
        this.result = JacksonUtil.parseMap(JacksonUtil.toJsonStr(data));
        return this;
    }

    /**
     * 设置业务返回值.
     *
     * <p>适用于已经转换好的 Map，避免重复进行 JSON 序列化和反序列化.</p>
     *
     * @param result 业务返回值
     * @return 当前执行结果
     */
    public AsyncCmdExecuteResultVO setResult(Map<String, Object> result) {
        this.result = result;
        return this;
    }

    /**
     * 执行中（不改变状态）.
     *
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO execute() {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.EXECUTE,
            null
        );
    }

    /**
     * 执行中，携带业务返回值.
     *
     * @param result 业务返回值
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO execute(Map<String, Object> result) {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.EXECUTE,
            result
        );
    }

    /**
     * 成功.
     *
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO success() {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.SUCCESS,
            null
        );
    }

    /**
     * 成功，携带业务返回值.
     *
     * @param result 业务返回值
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO success(Map<String, Object> result) {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.SUCCESS,
            result
        );
    }

    /**
     * 失败.
     *
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO failed() {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.FAILED,
            null
        );
    }

    /**
     * 失败，携带业务返回值.
     *
     * @param result 业务返回值
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO failed(Map<String, Object> result) {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.FAILED,
            result
        );
    }

    /**
     * 异步等待.
     *
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO waiting() {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.WAITING,
            null
        );
    }

    /**
     * 异步等待，携带业务返回值.
     *
     * @param result 业务返回值
     * @return 执行结果
     */
    public static AsyncCmdExecuteResultVO waiting(Map<String, Object> result) {
        return new AsyncCmdExecuteResultVO(
            AsyncCmdExecuteResultEnum.WAITING,
            result
        );
    }
}