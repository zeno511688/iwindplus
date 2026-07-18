/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.dto;

import com.iwindplus.base.alert.domain.enums.AlertMessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 告警请求基础数据传输对象（策略模式支持）.
 * 使用继承结构区分 App 和 Webhook 的不同字段
 *
 * @author zengdegui
 * @since 2026/01/19 23:21
 */
@Schema(description = "告警请求基础数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlertBaseRequestDTO implements Serializable {

    /**
     * 告警消息类型（用于策略路由）.
     */
    @Schema(description = "告警消息类型")
    private AlertMessageTypeEnum type;

    /**
     * 告警内容（通用字段）.
     */
    @Schema(description = "告警内容")
    private String content;
}