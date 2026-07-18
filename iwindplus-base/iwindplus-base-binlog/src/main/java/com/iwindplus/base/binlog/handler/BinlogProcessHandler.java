/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.binlog.handler;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.binlog.domain.constant.BinlogConstant;
import com.iwindplus.base.binlog.domain.dto.BinlogDTO;
import com.iwindplus.base.binlog.domain.event.BinLogEvent;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

/**
 * binlog 处理助手.
 *
 * @author zengdegui
 * @since 2025/11/22
 */
@Slf4j
public class BinlogProcessHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ApplicationEventPublisher publisher;

    /**
     * 数据处理.
     *
     * @param raw 行数据
     */
    public void processHandler(String raw) {
        Mono.fromCallable(() -> parseData(raw))
            .filter(Objects::nonNull)
            .doOnNext(data -> publisher.publishEvent(new BinLogEvent(this, data)))
            .onErrorResume(e -> {
                log.error("Message send finally failed, data={}", StrUtil.subPre(raw, 200), e);
                return Mono.empty();
            })
            .subscribe();
    }

    private BinlogDTO parseData(String raw) {
        JsonNode root;
        try {
            root = objectMapper.readTree(raw);
        } catch (JsonProcessingException ex) {
            log.error("parse binlog error, raw={}", raw, ex);
            return null;
        }

        JsonNode payload = root.get(BinlogConstant.PAYLOAD);
        if (payload == null || payload.isNull()) {
            log.warn("payload missing or null");
            return null;
        }

        // 解析操作
        String op = payload.path(BinlogConstant.OP).asText(null);
        log.info("binlog op={}", op);
        if (CharSequenceUtil.isBlank(op)) {
            return null;
        }

        // 非 DML 直接过滤
        final DbActionTypeEnum action = DbActionTypeEnum.fromAlias(op);
        if (!isDmlOp(action)) {
            return null;
        }

        final String tsMs = payload.path(BinlogConstant.TS_MS).asText(null);
        final String tsUs = payload.path(BinlogConstant.TS_US).asText(null);
        final String tsNs = payload.path(BinlogConstant.TS_NS).asText(null);
        final JsonNode transaction = payload.path(BinlogConstant.TRANSACTION);

        // 元数据
        JsonNode sourceNode = payload.get(BinlogConstant.SOURCE);
        if (sourceNode == null || sourceNode.isNull()) {
            log.warn("source missing or null");
            return null;
        }

        JsonNode beforeNode = payload.get(BinlogConstant.BEFORE);
        JsonNode afterNode = payload.get(BinlogConstant.AFTER);

        return BinlogDTO
            .builder()
            .op(op)
            .transaction(transaction)
            .tsMs(CharSequenceUtil.isBlank(tsMs) ? null : Long.valueOf(tsMs))
            .tsUs(CharSequenceUtil.isBlank(tsUs) ? null : Long.valueOf(tsUs))
            .tsNs(CharSequenceUtil.isBlank(tsNs) ? null : Long.valueOf(tsNs))
            .source(sourceNode)
            .before(Objects.nonNull(beforeNode) ? beforeNode : null)
            .after(Objects.nonNull(afterNode) ? afterNode : null)
            .build();
    }

    private boolean isDmlOp(DbActionTypeEnum action) {
        return DbActionTypeEnum.INSERT.equals(action)
            || DbActionTypeEnum.UPDATE.equals(action)
            || DbActionTypeEnum.DELETE.equals(action);
    }

}