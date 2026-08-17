/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.util.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令回调通知基础数据传输对象.
 *
 * @author zengdegui
 * @since 2026/08/12 00:00
 */
@Schema(description = "异步命令回调通知数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdCallbackBaseDTO implements Serializable {

    /**
     * 回调结果（必填，仅允许SUCCESS/FAILED）.
     */
    @Schema(description = "回调结果（仅允许SUCCESS/FAILED）")
    private AsyncCmdCallbackResultEnum callbackResult;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 结果（可选）.
     */
    @Schema(description = "结果")
    private Map<String, Object> result;

    /**
     * 进度比例（可选，0-100，回调时上报进度）.
     */
    @Schema(description = "进度比例（0-100）")
    private Integer progress;

    /**
     * 错误信息（可选，结果为FAILED时携带）.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 耗时（可选，单位毫秒，业务方可上报实际处理耗时）.
     */
    @Schema(description = "耗时（毫秒）")
    private Long costTime;

    /**
     * 设置回调结果.
     *
     * @param data 数据
     * @param <T>  泛型
     */
    public <T> void setResultData(T data) {
        this.result = JacksonUtil.parseMap(JacksonUtil.toJsonStr(data));
    }

    /**
     * 获取回调结果数据并转换为指定类型.
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return T
     */
    public <T> T getResultData(Class<T> clazz) {
        if (this.result == null) {
            return null;
        }
        // 使用 JSON 序列化/反序列化转换
        String json = JacksonUtil.toJsonStr(this.result);
        return JacksonUtil.parseObject(json, clazz);
    }
}
