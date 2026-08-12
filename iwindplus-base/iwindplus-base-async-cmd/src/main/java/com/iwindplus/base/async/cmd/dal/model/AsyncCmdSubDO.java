/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.dal.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令子表对象.
 */

/**
 * 异步命令表.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@TableName(value = "`async_cmd_sub`", autoResultMap = true)
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubDO extends DbBaseDO {

    /**
     * 状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败）.
     */
    @Schema(description = "状态（TO_BE_EXECUTE：待执行，EXECUTE：执行，ASYNC_WAIT：异步等待，SUCCESS：成功，FAILED：失败）")
    private AsyncCmdStatusEnum status;

    /**
     * 业务名称.
     */
    @Schema(description = "业务名称")
    private String bizName;

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
     * 阶段.
     */
    @Schema(description = "阶段")
    private Integer stage;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    private String executeName;

    /**
     * 参数.
     */
    @Schema(description = "参数")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> param;

    /**
     * 结果（供后续任务读取，同一批互相不可见，由于是并发）.
     */
    @Schema(description = "结果")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> result;

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
     * 耗时.
     */
    @Schema(description = "耗时")
    private Long costTime;

    /**
     * 是否需要回调.
     */
    @Schema(description = "是否需要回调")
    private Boolean needCallback;

    /**
     * 回调等待截止时间（异步等待超时兜底）.
     */
    @Schema(description = "回调等待截止时间")
    private Long expireTime;

    /**
     * 是否需要显示（查进度时用）.
     */
    @Schema(description = "是否需要显示")
    private Boolean needDisplay;

    /**
     * 异步命令主键.
     */
    @Schema(description = "异步命令主键")
    private Long asyncCmdId;
}
