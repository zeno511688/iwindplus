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
 * Webhook 告警请求 DTO. 包含 webhookUrl 和 secret 字段，用于 Webhook 消息发送
 *
 * @author zengdegui
 * @since 2026/03/03 19:34
 */
@Schema(description = "Webhook告警请求DTO")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlertWebhookRequestDTO extends AlertBaseRequestDTO {

    /**
     * WebHook地址（Webhook消息使用）.
     */
    @Schema(description = "WebHook地址")
    private String webhookUrl;

    /**
     * 密钥（Webhook消息使用，用于签名）.
     */
    @Schema(description = "密钥")
    private String secret;

    /**
     * 构造函数.
     *
     * @param webhookUrl WebHook地址
     * @param content    告警内容
     * @param secret     密钥
     */
    public AlertWebhookRequestDTO(String webhookUrl, String content, String secret) {
        super(AlertMessageTypeEnum.WEBHOOK, content);
        this.webhookUrl = webhookUrl;
        this.secret = secret;
    }
}
