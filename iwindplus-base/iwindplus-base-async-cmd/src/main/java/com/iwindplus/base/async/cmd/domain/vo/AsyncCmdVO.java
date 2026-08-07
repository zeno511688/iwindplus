/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.vo;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
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
public class AsyncCmdVO extends DbVersionBaseVO {

    /**
     * 状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）.
     */
    @Schema(description = "状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败，DISCARD：丢弃）")
    private AsyncCmdStatusEnum status;

    /**
     * 环境.
     */
    @Schema(description = "环境")
    private String env;

    /**
     * 业务key，例如 ORDER.
     */
    @Schema(description = "业务key，例如 ORDER")
    private String bizKey;

    /**
     * 业务类型，例如 ORDER_CREATE.
     */
    @Schema(description = "业务类型，例如 ORDER_CREATE")
    private String bizType;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

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
     * 子任务总数.
     */
    @Schema(description = "子任务总数")
    private Integer subTaskCount;

    /**
     * 累计耗时.
     */
    @Schema(description = "耗时")
    private Long costTime;

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
