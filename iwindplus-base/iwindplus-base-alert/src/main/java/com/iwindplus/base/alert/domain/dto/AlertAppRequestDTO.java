/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.dto;

import com.iwindplus.base.alert.domain.enums.AlertMessageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * App 企业应用告警请求 DTO. 包含 receiveId 字段，用于企业应用消息发送
 *
 * @author zengdegui
 * @since 2026/03/03 19:32
 */
@Schema(description = "App企业应用告警请求DTO")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlertAppRequestDTO extends AlertBaseRequestDTO {

    /**
     * 接收人ID（企业应用消息使用）.
     */
    @Schema(description = "接收人ID")
    private String receiveId;

    /**
     * 构造函数.
     *
     * @param receiveId 接收人ID
     * @param content   告警内容
     */
    public AlertAppRequestDTO(String receiveId, String content) {
        super(AlertMessageTypeEnum.APP, content);
        this.receiveId = receiveId;
    }
}
