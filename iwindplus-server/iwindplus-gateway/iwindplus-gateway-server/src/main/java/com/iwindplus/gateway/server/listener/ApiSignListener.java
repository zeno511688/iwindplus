/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.gateway.server.filter.ApiSignFilter;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * API签名更新监听.
 *
 * @author zengdegui
 * @since 2024-8-26
 */
@Slf4j
@Component
public class ApiSignListener {

    @Lazy
    @Resource
    private ApiSignFilter apiSignFilter;

    @KafkaMultiListener(
        cluster = "${kafka.multi.default-cluster}",
        topics = {"${kafka.multi.clusters.default.bindings[1].topic}"},
        group = "${kafka.multi.clusters.default.bindings[1].group}"
    )
    public void listenSingle(Message<String> records, Acknowledgment ack) {
        log.info("API签名配置消息消费参数={}", records);

        try {
            final MessageBaseDTO<List<BaseSignVO>> messageDTO = JacksonUtil.parseObject(records.getPayload(), new TypeReference<>() {
            });
            this.apiSignFilter.refreshAppCert(messageDTO);
            if (ack != null) {
                ack.acknowledge();
            }
            log.info("API签名配置消息消费成功");
        } catch (Exception ex) {
            log.error("API签名配置消息消费失败={}", ex);
            throw ex;
        }
    }
}
