/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.gateway.server.filter.IpBlackListFilter;
import com.iwindplus.mgt.domain.dto.system.IpBlackListChangeDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * IP黑名单更新监听.
 *
 * @author zengdegui
 * @since 2024-8-26
 */
@Slf4j
@Component
public class IpBlackListListener {

    @Lazy
    @Resource
    private IpBlackListFilter ipBlackListFilter;

    @KafkaMultiListener(
        cluster = "${kafka.multi.default-cluster}",
        topics = {"${kafka.multi.clusters.default.bindings[3].topic}"},
        group = "${kafka.multi.clusters.default.bindings[3].group}"
    )
    public void listenSingle(Message<String> records, Acknowledgment ack) {
        log.info("IP黑名单消息消费参数={}", records);

        try {
            final MessageBaseDTO<IpBlackListChangeDTO> messageDTO = JacksonUtil.parseObject(records.getPayload(), new TypeReference<>() {
            });
            this.ipBlackListFilter.refreshBlackList(messageDTO);
            if (ack != null) {
                ack.acknowledge();
            }
            log.info("IP黑名单消息消费成功");
        } catch (Exception ex) {
            log.error("IP黑名单消息消费失败={}", ex);
            throw ex;
        }
    }

}
