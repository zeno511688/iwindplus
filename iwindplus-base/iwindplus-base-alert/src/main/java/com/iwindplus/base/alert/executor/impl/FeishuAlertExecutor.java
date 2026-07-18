/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.executor.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.alert.domain.constant.AlertConstant.FeishuConstant;
import com.iwindplus.base.alert.domain.dto.AlertAppRequestDTO;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;
import com.iwindplus.base.alert.domain.property.AlertProperty;
import com.iwindplus.base.alert.domain.property.AlertProperty.FeishuConfig;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
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
 * @author zengdegui
 * @since 2026/03/03 20:02
 */
@Slf4j
public class FeishuAlertExecutor extends AbstractAlertExecutor {

    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;
    private final FeishuConfig cfg;

    public FeishuAlertExecutor(
        AlertProperty property,
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory) {
        super(property);
        this.httpClientExecutorStrategyFactory = httpClientExecutorStrategyFactory;
        this.cfg = property.getFeishu();
    }

    @Override
    public AlertChannelTypeEnum getChannelType() {
        return AlertChannelTypeEnum.FEI_SHU;
    }

    @Override
    public void sendAppMsg(AlertAppRequestDTO entity) {
        try {
            final Client client = getClient();
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
        final Map<String, Object> body = this.buildWebhookBody(entity);

        httpClientExecutorStrategyFactory.getHttpClientExecutor(HttpClientTypeEnum.OK_HTTP)
            .postAsync(entity.getWebhookUrl(), body, null, String.class)
            .thenAccept(resp -> log.info("飞书webhook发送结果={}", resp))
            .exceptionally(ex -> {
                log.error("飞书Webhook发送异常", ex);
                return null;
            });
    }

    private Client getClient() {
        return Client.newBuilder(cfg.getAppId(), cfg.getAppSecret())
            .appType(AppType.SELF_BUILT)
            .build();
    }

    private Map<String, Object> buildWebhookBody(AlertWebhookRequestDTO entity) {
        Map<String, Object> body = new HashMap<>(16);
        body.put(FeishuConstant.MSG_TYPE, MsgTypeEnum.MSG_TYPE_TEXT.getValue());

        Object content;
        if (CharSequenceUtil.isNotBlank(entity.getSecret())) {
            Map<String, Object> contentMap = new HashMap<>(16);
            contentMap.put(MsgTypeEnum.MSG_TYPE_TEXT.getValue(), entity.getContent());
            content = contentMap;

            long timestamp = Instant.now().getEpochSecond();
            body.put(FeishuConstant.TIMESTAMP, timestamp);
            body.put(FeishuConstant.SIGN, genSign(timestamp, entity.getSecret()));
        } else {
            content = entity.getContent();
        }
        body.put(FeishuConstant.CONTENT, content);

        return body;
    }

    private String genSign(long timestamp, String secret) {
        String algorithm = "HmacSHA256";
        try {
            String toSign = CharSequenceUtil.format("{}\n{}", timestamp, secret);

            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(toSign.getBytes(StandardCharsets.UTF_8), algorithm));
            byte[] signData = mac.doFinal(new byte[0]);
            return Base64.getEncoder().encodeToString(signData);
        } catch (Exception e) {
            log.error(ExceptionConstant.EXCEPTION, e);

            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }
}
