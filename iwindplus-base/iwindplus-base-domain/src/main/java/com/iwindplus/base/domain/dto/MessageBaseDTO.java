/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import cn.hutool.core.util.IdUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 消息基础数据传输对象.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2023/10/15 11:29
 */
@Schema(description = "消息基础数据传输对象")
@Data
@SuperBuilder
@AllArgsConstructor
public class MessageBaseDTO<T> implements Serializable {

    /**
     * 任务Id.
     */
    @Schema(description = "任务Id")
    private String taskId;

    /**
     * 操作类型.
     */
    @Schema(description = "操作类型")
    private String operateType;

    /**
     * 业务类型.
     */
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 数据.
     */
    @Schema(description = "数据")
    private T data;

    /**
     * 构造方法.
     */
    public MessageBaseDTO() {
        this.taskId = IdUtil.simpleUUID();
    }
}
