/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.SourceMetaDTO;
import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty;
import com.iwindplus.binlog.comsumer.server.handler.BinlogActionHandler;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.function.Tuples;

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
    private BinlogActionHandler handler;

    @KafkaMultiListener(
        cluster = "${kafka.multi.default-cluster}",
        topics = {"${kafka.multi.clusters.default.consumer.bindings[0].topic}"},
        group = "${kafka.multi.clusters.default.consumer.bindings[0].group}"
    )
    public Mono<Void> listenBatch(List<ReceiverRecord<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            return Mono.empty();
        }

        AtomicInteger recordCount = new AtomicInteger(0);
        AtomicInteger parsedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        return Flux.fromIterable(records)
            // 统计原始 Kafka 数量
            .doOnNext(r -> recordCount.incrementAndGet())
            .filter(r -> r != null && r.value() != null)
            // 解析
            .flatMap(record ->
                Mono.fromCallable(() -> parseData(record.value()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMapMany(Flux::fromIterable)
                    .map(dto -> Tuples.of(record, dto))
            )
            // 统计解析后的数量
            .doOnNext(t -> parsedCount.incrementAndGet())
            // 处理
            .flatMap(tuple -> {
                ReceiverRecord<String, Object> record = tuple.getT1();
                BinlogRowDataDTO dto = tuple.getT2();

                return Mono.fromRunnable(() -> processData(dto))
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSuccess(v -> successCount.incrementAndGet())
                    .onErrorResume(e -> {
                        log.error("处理失败, offset={}", record.offset(), e);
                        return Mono.empty();
                    });
            })
            .then()
            // 统一输出统计
            .doOnSuccess(v -> log.info(
                "Kafka批量处理完成: 原始记录={}, 解析后={}, 成功处理={}",
                recordCount.get(),
                parsedCount.get(),
                successCount.get()
            ));
    }

    private void processData(BinlogRowDataDTO entity) {
        try {
            this.handler.processHandler(entity);
        } catch (Exception ex) {
            if (ex instanceof BizException bizEx) {
                throw bizEx;
            }

            log.error("binlog日志消息消费失败={}", ex);
        }
    }

    private List<BinlogRowDataDTO> parseData(Object value) {
        // log.info("binlog日志消息消费参数={}", value);
        if (value == null) {
            return null;
        }

        final BinlogRowDataDTO data = JacksonUtil.parseObject(value.toString(), new TypeReference<>() {
        });

        if (Objects.isNull(data)) {
            return null;
        }

        if (!checkTableNeedSign(data)) {
            return null;
        }

        return List.of(data);
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
