/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.alert.domain.constant.AlertConstant.FeishuConstant;
import com.iwindplus.base.alert.domain.dto.AlertAppRequestDTO;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;
import com.iwindplus.base.alert.domain.property.AlertProperty.AppConfig;
import com.iwindplus.base.alert.domain.property.AlertProperty.FeishuConfig;
import com.iwindplus.base.alert.domain.property.AlertProperty.WebhookConfig;
import com.iwindplus.base.alert.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.alert.support.AlertExecuteHandler;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.lark.oapi.Client;
import com.lark.oapi.core.enums.AppType;
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum;
import com.lark.oapi.service.im.v1.enums.MsgTypeEnum;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 * 飞书告警执行器.
 *
 * <p>一个渠道配置对应一个执行器，内部同时持有企业应用消息配置集合和 Webhook 消息配置集合，
 * 发送时根据请求中的配置编码（code）选择具体配置。</p>
 *
 * @author zengdegui
 * @since 2026/03/03 20:02
 */
@Slf4j
public class FeishuAlertExecuteHandler extends AbstractBaseServiceImpl<FeishuConfig> implements AlertExecuteHandler {

    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;

    /**
     * 构造函数.
     *
     * @param config                          渠道配置
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器工厂
     */
    public FeishuAlertExecuteHandler(
        FeishuConfig config,
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory) {
        this.setConfig(config);
        this.httpClientExecuteHandlerFactory = httpClientExecuteHandlerFactory;
    }

    @Override
    public AlertChannelTypeEnum getChannelType() {
        return AlertChannelTypeEnum.FEI_SHU;
    }

    @Override
    public void sendAppMsg(AlertAppRequestDTO entity) {
        final AppConfig app = this.resolveAppConfig(entity.getCode());
        try {
            final Client client = Client.newBuilder(app.getAppId(), app.getAppSecret())
                .appType(AppType.SELF_BUILT)
                .build();
            final CreateMessageResp createMessageResp = client.im().message().create(
                CreateMessageReq.newBuilder()
                    .receiveIdType(CreateMessageReceiveIdTypeEnum.USER_ID)
                    .createMessageReqBody(
                        CreateMessageReqBody.newBuilder()
                            .receiveId(entity.getReceiveId())
                            .msgType(MsgTypeEnum.MSG_TYPE_TEXT.getValue())
                            .content(entity.getContent())
                            .build()
                    ).build()
            );
            log.info("飞书企业应用消息发送结果={}", createMessageResp);
        } catch (Exception e) {
            log.error("发送飞书企业应用消息失败", e);
        }
    }

    @Override
    public void sendWebhookMsg(AlertWebhookRequestDTO entity) {
        final WebhookConfig webhook = this.resolveWebhookConfig(entity.getCode());

        final String webhookUrl = webhook.getUrl();
        if (CharSequenceUtil.isBlank(webhookUrl)) {
            log.error("飞书Webhook消息缺少 url 配置, code={}", webhook.getCode());
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        final String secret = webhook.getSecretKey();

        final Map<String, Object> body = this.buildWebhookBody(entity, secret);

        httpClientExecuteHandlerFactory.getHandler(HttpClientTypeEnum.OK_HTTP)
            .postAsync(webhookUrl, body, null, String.class)
            .thenAccept(resp -> log.info("飞书webhook发送结果={}", resp))
            .exceptionally(ex -> {
                log.error("飞书Webhook发送异常", ex);
                return null;
            });
    }

    /**
     * 根据配置编码解析企业应用消息配置.
     *
     * @param code 配置编码（为空时取第一个启用配置）
     * @return 企业应用消息配置
     */
    private AppConfig resolveAppConfig(String code) {
        final FeishuConfig config = this.getConfig();
        if (CollUtil.isEmpty(config.getApps())) {
            log.error("飞书企业应用消息未配置, channelCode={}", config.getCode());
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return config.getApps().stream()
            .filter(app -> Boolean.TRUE.equals(app.getEnabled()))
            .filter(app -> CharSequenceUtil.isBlank(code) || code.equals(app.getCode()))
            .findFirst()
            .orElseThrow(() -> {
                log.error("飞书企业应用消息配置不存在, code={}", code);
                return new BizException(BizCodeEnum.INVALID_STRATEGY);
            });
    }

    /**
     * 根据配置编码解析 Webhook 消息配置.
     *
     * @param code 配置编码（为空时取第一个启用配置）
     * @return Webhook消息配置
     */
    private WebhookConfig resolveWebhookConfig(String code) {
        final FeishuConfig config = this.getConfig();
        if (CollUtil.isEmpty(config.getWebhooks())) {
            log.error("飞书Webhook消息未配置, channelCode={}", config.getCode());
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return config.getWebhooks().stream()
            .filter(webhook -> Boolean.TRUE.equals(webhook.getEnabled()))
            .filter(webhook -> CharSequenceUtil.isBlank(code) || code.equals(webhook.getCode()))
            .findFirst()
            .orElseThrow(() -> {
                log.error("飞书Webhook消息配置不存在, code={}", code);
                return new BizException(BizCodeEnum.INVALID_STRATEGY);
            });
    }

    private Map<String, Object> buildWebhookBody(AlertWebhookRequestDTO entity, String secret) {
        Map<String, Object> body = new HashMap<>(16);
        body.put(FeishuConstant.MSG_TYPE, MsgTypeEnum.MSG_TYPE_TEXT.getValue());

        Object content;
        if (CharSequenceUtil.isNotBlank(secret)) {
            Map<String, Object> contentMap = new HashMap<>(16);
            contentMap.put(MsgTypeEnum.MSG_TYPE_TEXT.getValue(), entity.getContent());
            content = contentMap;

            long timestamp = Instant.now().getEpochSecond();
            body.put(FeishuConstant.TIMESTAMP, timestamp);
            body.put(FeishuConstant.SIGN, genSign(timestamp, secret));
        } else {
            content = entity.getContent();
        }
        body.put(FeishuConstant.CONTENT, content);

        return body;
    }

    private String genSign(long timestamp, String secret) {
        try {
            String toSign = CharSequenceUtil.format("{}\n{}", timestamp, secret);

            Mac mac = Mac.getInstance(FeishuConstant.ALGORITHM);
            mac.init(new SecretKeySpec(toSign.getBytes(StandardCharsets.UTF_8), FeishuConstant.ALGORITHM));
            byte[] signData = mac.doFinal(new byte[0]);
            return Base64.getEncoder().encodeToString(signData);
        } catch (Exception e) {
            log.error(ExceptionConstant.EXCEPTION, e);

            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }
}
