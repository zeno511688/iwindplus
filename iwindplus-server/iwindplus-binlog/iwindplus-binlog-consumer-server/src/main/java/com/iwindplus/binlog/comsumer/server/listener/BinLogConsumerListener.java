/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.iwindplus.base.disruptor.core.DisruptorManager;
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.SourceMetaDTO;
import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty;
import com.iwindplus.binlog.comsumer.server.handler.event.BinlogRowDataDisruptorEventHandler;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

/**
 * binlog消费方日志监听器.
 *
 * @author zengdegui
 * @since 2025/08/24 13:04
 */
@Slf4j
public class BinLogConsumerListener {

    @Resource
    protected BinLogConsumerProperty property;

    @Resource
    private DisruptorManager<List<BinlogRowDataDTO>> disruptorManager;

    @KafkaMultiListener(
        cluster = "${kafka.multi.default-cluster}",
        topics = {"${kafka.multi.clusters.default.consumer.bindings[0].topic}"},
        group = "${kafka.multi.clusters.default.consumer.bindings[0].group}"
    )
    public void listenBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (records == null || records.isEmpty()) {
            return;
        }

        try {
            final List<BinlogRowDataDTO> batchList = buildBinlogRowData(records);

            if (!batchList.isEmpty()) {
                final String name = StrUtil.lowerFirst(BinlogRowDataDisruptorEventHandler.class.getSimpleName());
                disruptorManager.getTemplate(name).publish("kafka", "elasticsearch", batchList);
            }

            if (ack != null) {
                ack.acknowledge();
            }
        } catch (Exception ex) {
            log.error("binlog日志消息批量消费失败, size={}", records.size(), ex);
            throw ex;
        }
    }

    private List<BinlogRowDataDTO> buildBinlogRowData(List<ConsumerRecord<String, String>> records) {
        List<BinlogRowDataDTO> entities = new ArrayList<>(records.size());
        for (ConsumerRecord<String, String> record : records) {
            if (record == null || CharSequenceUtil.isBlank(record.value())) {
                continue;
            }

            final BinlogRowDataDTO data = parseData(record.value());
            entities.add(data);
        }
        return entities;
    }

    private BinlogRowDataDTO parseData(Object value) {
        if (value == null) {
            return null;
        }

        final BinlogRowDataDTO data = JacksonUtil.parseObject(value.toString(), BinlogRowDataDTO.class);

        if (Objects.isNull(data)) {
            return null;
        }

        if (!checkTableNeedSign(data)) {
            return null;
        }

        return data;
    }

    private boolean checkTableNeedSign(BinlogRowDataDTO data) {
        final SourceMetaDTO source = data.getSource();
        if (Objects.isNull(source)) {
            return false;
        }
        final String db = source.getDb();
        final String table = source.getTable();
        if (CharSequenceUtil.isBlank(db) || CharSequenceUtil.isBlank(table)) {
            return false;
        }
        // 判断是否是需要验签的表，不需要则跳过
        if (!property.checkNeedSign(db, table)) {
            return false;
        }

        return true;
    }
}
