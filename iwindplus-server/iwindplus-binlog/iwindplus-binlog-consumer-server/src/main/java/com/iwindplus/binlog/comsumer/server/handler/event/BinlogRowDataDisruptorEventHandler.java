/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.handler.event;

import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataDTO;
import com.iwindplus.binlog.comsumer.server.handler.BinlogActionHandler;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * binlog 行数据处理Disruptor事件处理器.
 *
 * @author zengdegui
 * @since 2026/06/17 23:26
 */
@Slf4j
@Component
public class BinlogRowDataDisruptorEventHandler implements DisruptorEventHandler<List<BinlogRowDataDTO>> {

    @Resource
    private BinlogActionHandler handler;

    @Override
    public void execute(List<BinlogRowDataDTO> data, long sequence, boolean endOfBatch) {
        log.info(BinlogRowDataDisruptorEventHandler.class.getSimpleName() + " execute size={}", data.size());
        processData(data);
    }

    private void processData(List<BinlogRowDataDTO> entities) {
        try {
            this.handler.processHandler(entities);
        } catch (Exception ex) {
            if (ex instanceof BizException bizEx) {
                throw bizEx;
            }

            log.error("binlog日志消息消费失败={}", ex);
            throw new RuntimeException(ex);
        }
    }
}
