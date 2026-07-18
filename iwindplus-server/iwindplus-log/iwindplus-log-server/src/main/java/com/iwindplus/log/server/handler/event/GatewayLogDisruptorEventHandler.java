/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.handler.event;

import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import com.iwindplus.log.server.service.GatewayLogService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 网关日志Disruptor事件处理器.
 *
 * @author zengdegui
 * @since 2026/06/17 23:26
 */
@Slf4j
@Component
public class GatewayLogDisruptorEventHandler implements DisruptorEventHandler<List<GatewayLogDTO>> {

    @Resource
    private GatewayLogService gatewayLogService;

    @Override
    public void execute(List<GatewayLogDTO> data, long sequence, boolean endOfBatch) {
        log.info("GatewayLogDisruptorEventHandler execute size={}", data.size());
        gatewayLogService.saveBatch(data);
    }
}
