/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.handler.event;

import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import com.iwindplus.log.server.service.LoginLogService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 登陆日志Disruptor事件处理器.
 *
 * @author zengdegui
 * @since 2026/06/17 23:26
 */
@Slf4j
@Component
public class LoginLogDisruptorEventHandler implements DisruptorEventHandler<List<LoginLogDTO>> {

    @Resource
    private LoginLogService loginLogService;

    @Override
    public void execute(List<LoginLogDTO> data, long sequence, boolean endOfBatch) {
        log.info("LoginLogDisruptorEventHandler execute size={}", data.size());
        loginLogService.saveBatch(data);
    }
}
