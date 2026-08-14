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
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异步命令回调通知数据传输对象.
 *
 * <p>业务收到外部系统回调后，通过该对象向框架上报结果，状态流转由框架完成</p>
 * <p>主任务、子任务均通过bizNumber定位，sub标识区分子任务；需要回调通知的子任务提交时须自行指定bizNumber并保证唯一</p>
 *
 * @author zengdegui
 * @since 2026/08/12 00:00
 */
@Schema(description = "异步命令回调通知数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdCallbackDTO {

    /**
     * 异步命令主键.
     */
    @Schema(description = "异步命令主键")
    private Long asyncCmdId;

    /**
     * 业务流水号（必填，主任务/子任务统一定位键）.
     */
    @Schema(description = "业务流水号（主任务/子任务统一定位键）")
    private String bizNumber;

    /**
     * 业务key，例如 ORDER.
     */
    @Schema(description = "业务key，例如 ORDER")
    private String bizKey;

    /**
     * 是否子任务（可选，默认false主任务）.
     */
    @Schema(description = "是否子任务（默认false主任务）")
    private Boolean sub;

    /**
     * 回调结果（必填，仅允许SUCCESS/FAILED）.
     */
    @Schema(description = "回调结果（仅允许SUCCESS/FAILED）")
    private AsyncCmdCallbackResultEnum result;

    /**
     * 结果数据（可选，外部系统返回的业务数据）.
     */
    @Schema(description = "结果数据")
    private Map<String, Object> resultData;

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
     * 设置回调结果.
     *
     * @param data 数据
     * @param <T>  泛型
     */
    public <T> void setCallbackData(T data) {
        this.resultData = JacksonUtil.parseMap(JacksonUtil.toJsonStr(data));
    }

    /**
     * 获取结果数据并转换为指定类型.
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return T
     */
    public <T> T getCallbackData(Class<T> clazz) {
        if (result == null) {
            return null;
        }
        // 使用 JSON 序列化/反序列化转换
        String json = JacksonUtil.toJsonStr(result);
        return JacksonUtil.parseObject(json, clazz);
    }
}
