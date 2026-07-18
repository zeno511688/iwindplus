/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss;

import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.service.OssAliyunService;
import com.iwindplus.base.oss.service.impl.OssAliyunServiceImpl;
import com.iwindplus.base.oss.service.impl.OssMinioServiceImpl;
import com.iwindplus.base.oss.service.impl.OssQiniuServiceImpl;
import com.iwindplus.base.oss.service.OssMinioService;
import com.iwindplus.base.oss.service.OssQiniuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对象存储配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(OssProperty.class)
public class OssConfiguration {

    /**
     * 创建 OssAliyunService.
     *
     * @return OssAliyunService
     */
    @ConditionalOnProperty(prefix = "oss.aliyun", name = "enabled", havingValue = "true")
    @Bean
    public OssAliyunService ossAliyunService() {
        OssAliyunServiceImpl ossService = new OssAliyunServiceImpl();
        log.info("OssAliyunService={}", ossService);
        return ossService;
    }

    /**
     * 创建 OssQiniuService.
     *
     * @return OssQiniuService
     */
    @ConditionalOnProperty(prefix = "oss.qiniu", name = "enabled", havingValue = "true")
    @Bean
    public OssQiniuService ossQiniuService() {
        OssQiniuServiceImpl ossService = new OssQiniuServiceImpl();
        log.info("OssQiniuService={}", ossService);
        return ossService;
    }

    /**
     * 创建 OssMinioService.
     *
     * @return OssMinioService
     */
    @ConditionalOnProperty(prefix = "oss.minio", name = "enabled", havingValue = "true")
    @Bean
    public OssMinioService ossMinioService() {
        OssMinioServiceImpl ossService = new OssMinioServiceImpl();
        log.info("OssMinioService={}", ossService);
        return ossService;
    }
}
