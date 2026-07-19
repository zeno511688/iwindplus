/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.producer.server.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.binlog.domain.dto.BinlogDTO;
import com.iwindplus.base.binlog.domain.event.BinLogEvent;
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * binlog生产方日志监听器.
 *
 * @author zengdegui
 * @since 2025/08/24 13:04
 */
@Slf4j
@Component
public class BinLogProducerListener {

    @Resource
    private KafkaMultiProperty property;

    @Resource
    private KafkaTemplateRouter kafkaTemplateRouter;

    /**
     * binlog日志监听保存数据.
     *
     * @param event 登录日志事件
     */
    @Async
    @EventListener(BinLogEvent.class)
    public void onApplicationEvent(BinLogEvent event) {
        final BinlogDTO dto = event.getLogData();
        if (Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getOp())) {
            return;
        }

        final String topicName = property.listTopic(property.getDefaultCluster()).get(0);

        final String json = JacksonUtil.toJsonStr(dto);
        kafkaTemplateRouter.send(property.getDefaultCluster(), topicName, null, json);
    }
}
