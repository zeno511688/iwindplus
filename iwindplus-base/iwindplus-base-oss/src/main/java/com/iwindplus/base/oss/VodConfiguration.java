/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss;

import com.iwindplus.base.oss.domain.property.VodProperty;
import com.iwindplus.base.oss.service.impl.VodAliyunServiceImpl;
import com.iwindplus.base.oss.service.VodAliyunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 视频点播操作配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(VodProperty.class)
@ConditionalOnProperty(prefix = "vod.aliyun", name = "enabled", havingValue = "true")
public class VodConfiguration {

    /**
     * 创建 VodAliyunService.
     *
     * @return VodAliyunService
     */
    @Bean
    public VodAliyunService vodAliyunService() {
        VodAliyunServiceImpl vodService = new VodAliyunServiceImpl();
        log.info("VodAliyunService={}", vodService);
        return vodService;
    }
}
