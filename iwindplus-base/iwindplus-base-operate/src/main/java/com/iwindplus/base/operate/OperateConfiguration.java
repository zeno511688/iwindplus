/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate;

import com.iwindplus.base.operate.aspect.OperateLogAspect;
import com.iwindplus.base.operate.aspect.OperateValidAspect;
import com.iwindplus.base.operate.domain.property.OperateProperty;
import com.iwindplus.base.operate.listener.OperateLogListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 操作配置.
 *
 * @author zengdegui
 * @since 2023/08/31 20:32
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({OperateProperty.class})
@ConditionalOnProperty(prefix = "operate", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OperateConfiguration {

    /**
     * 创建 OperateLogAspect.
     *
     * @return OperateLogAspect
     */
    @Bean
    public OperateLogAspect operateLogAspect() {
        OperateLogAspect operateLogAspect = new OperateLogAspect();
        log.info("OperateLogAspect={}", operateLogAspect);
        return operateLogAspect;
    }

    /**
     * 创建 OperateLogListener.
     *
     * @return OperateLogListener
     */
    @Bean
    public OperateLogListener operateLogListener() {
        final OperateLogListener operateLogListener = new OperateLogListener();
        log.info("OperateLogListener={}", operateLogListener);
        return operateLogListener;
    }

    /**
     * 创建 OperateValidAspect.
     *
     * @return OperateValidAspect
     */
    @Bean
    public OperateValidAspect operateValidAspect() {
        OperateValidAspect operateValidAspect = new OperateValidAspect();
        log.info("OperateValidAspect={}", operateValidAspect);
        return operateValidAspect;
    }
}
