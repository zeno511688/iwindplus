/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.util.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令视图对象.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Schema(description = "异步命令视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdVO extends AsyncCmdBaseVO {

    /**
     * 调度模式（ASYNC：异步，CENTER：调度中心，UNKNOWN：未知）.
     */
    @Schema(description = "调度模式（ASYNC：异步，CENTER：调度中心，UNKNOWN：未知）")
    private DispatchModeEnum dispatchMode;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    private String executeName;

    /**
     * 内容.
     */
    @Schema(description = "内容")
    private Map<String, Object> content;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 下一次重试时间.
     */
    @Schema(description = "下一次重试时间")
    private LocalDateTime nextRetryTime;

    /**
     * 重试次数.
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 是否需要回调.
     */
    @Schema(description = "是否需要回调")
    private Boolean needCallback;

    /**
     * 等待异步结果的截止时间.
     */
    @Schema(description = "等待异步结果的截止时间")
    private LocalDateTime callbackExpireTime;

    /**
     * 是否需要显示（查进度时用）.
     */
    @Schema(description = "是否需要显示")
    private Boolean needDisplay;

    /**
     * 子任务列表.
     */
    @JsonIgnore
    @Schema(description = "子任务列表", hidden = true)
    private transient List<AsyncCmdSubVO> subTasks;

    /**
     * 获取数据并转换为指定类型.
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return T
     */
    public <T> T getData(Class<T> clazz) {
        if (content == null) {
            return null;
        }
        // 使用 JSON 序列化/反序列化转换
        String json = JacksonUtil.toJsonStr(content);
        return JacksonUtil.parseObject(json, clazz);
    }

    /**
     * 通过业务类型获取子任务列表结果数据.
     *
     * @param bizType 业务类型
     * @param clazz   目标类型
     * @param <T>     泛型
     * @return List<T>
     */
    public <T> List<T> getSubTaskResults(String bizType, Class<T> clazz) {
        if (CollUtil.isEmpty(subTasks)) {
            return null;
        }

        return subTasks.stream()
            .filter(subTask -> bizType.equals(subTask.getBizType()))
            .map(subTask -> subTask.getResultData(clazz))
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 通过业务类型获取子任务列表结果数据.
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return <T>
     */
    public <T> T getSubTaskResult(String bizType, Class<T> clazz) {
        if (CollUtil.isEmpty(subTasks)) {
            return null;
        }

        return subTasks.stream()
            .filter(subTask -> bizType.equals(subTask.getBizType()))
            .findFirst()
            .map(subTask -> subTask.getResultData(clazz))
            .orElse(null);
    }

    /**
     * 通过排序号获取子任务列表结果数据.
     *
     * @param seq   排序号
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return <T>
     */
    public <T> T getSubTaskResult(Integer seq, Class<T> clazz) {
        if (CollUtil.isEmpty(subTasks)) {
            return null;
        }

        return subTasks.stream()
            .filter(subTask -> seq.equals(subTask.getSeq()))
            .findFirst()
            .map(subTask -> subTask.getResultData(clazz))
            .orElse(null);
    }
}
