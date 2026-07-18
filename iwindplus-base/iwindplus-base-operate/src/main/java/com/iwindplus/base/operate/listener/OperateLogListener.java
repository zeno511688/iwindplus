/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.operate.domain.dto.OperateLogDTO;
import com.iwindplus.base.operate.domain.event.OperateLogEvent;
import com.iwindplus.base.operate.domain.property.OperateProperty;
import com.iwindplus.base.operate.domain.property.OperateProperty.OperateLogConfig;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * 操作日志监听器.
 *
 * @author zengdegui
 * @since 2025/03/21 21:51
 */
@Slf4j
public class OperateLogListener {

    @Resource
    private OperateProperty property;

    @Resource
    private HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    /**
     * 操作日志监听保存数据.
     *
     * @param operateLogEvent 日志事件
     */
    @Async
    @EventListener(OperateLogEvent.class)
    public void onApplicationEvent(OperateLogEvent operateLogEvent) {
        final OperateLogDTO logData = operateLogEvent.getOperateLogData();
        if (Objects.isNull(logData)) {
            log.warn("操作日志发送失败，日志数据为空");
            return;
        }

        final OperateLogConfig cfg = property.getLog();
        httpClientExecutorStrategyFactory
            .getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)
            .post(
                cfg.getUrl(),
                logData,
                null,
                new TypeReference<ResultVO<Boolean>>() {
                }
            );
        log.info("{} 操作日志发送成功", SpringUtil.getApplicationName());
    }
}
