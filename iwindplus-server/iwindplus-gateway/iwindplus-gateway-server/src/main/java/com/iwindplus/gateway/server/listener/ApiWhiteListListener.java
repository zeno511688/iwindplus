/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.gateway.server.filter.ApiWhiteListFilter;
import com.iwindplus.mgt.domain.dto.system.ApiWhiteListChangeDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * API白名单更新监听.
 *
 * @author zengdegui
 * @since 2024-8-26
 */
@Slf4j
@Component
public class ApiWhiteListListener {

    @Lazy
    @Resource
    private ApiWhiteListFilter apiWhiteListFilter;

    @KafkaMultiListener(
        cluster = "${kafka.multi.default-cluster}",
        topics = {"${kafka.multi.clusters.default.bindings[2].topic}"},
        group = "${kafka.multi.clusters.default.bindings[2].group}"
    )
    public void listenSingle(Message<String> records, Acknowledgment ack) {
        log.info("API白名单消息消费参数={}", records);

        try {
            final MessageBaseDTO<ApiWhiteListChangeDTO> messageDTO = JacksonUtil.parseObject(records.getPayload(), new TypeReference<>() {
            });
            this.apiWhiteListFilter.refreshWhiteList(messageDTO);
            if (ack != null) {
                ack.acknowledge();
            }
            log.info("API白名单消息消费成功");
        } catch (Exception ex) {
            log.error("API白名单消息消费失败={}", ex);
            throw ex;
        }
    }

}
