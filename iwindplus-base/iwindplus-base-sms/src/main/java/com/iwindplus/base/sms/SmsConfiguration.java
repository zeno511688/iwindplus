/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms;

import com.iwindplus.base.sms.domain.property.SmsProperty;
import com.iwindplus.base.sms.service.SmsAliyunService;
import com.iwindplus.base.sms.service.SmsLingkaiService;
import com.iwindplus.base.sms.service.SmsMxtongService;
import com.iwindplus.base.sms.service.SmsQiniuService;
import com.iwindplus.base.sms.service.impl.SmsAliyunServiceImpl;
import com.iwindplus.base.sms.service.impl.SmsLingkaiServiceImpl;
import com.iwindplus.base.sms.service.impl.SmsMxtongServiceImpl;
import com.iwindplus.base.sms.service.impl.SmsQiniuServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SmsProperty.class)
public class SmsConfiguration {

    /**
     * 创建 SmsAliyunService.
     *
     * @return SmsAliyunService
     */
    @ConditionalOnProperty(prefix = "sms.aliyun", name = "enabled", havingValue = "true")
    @Bean
    public SmsAliyunService smsAliyunService() {
        SmsAliyunServiceImpl smsService = new SmsAliyunServiceImpl();
        log.info("SmsAliyunService={}", smsService);
        return smsService;
    }

    /**
     * 创建 SmsQiniuService.
     *
     * @return SmsQiniuService
     */
    @ConditionalOnProperty(prefix = "sms.qiniu", name = "enabled", havingValue = "true")
    @Bean
    public SmsQiniuService smsQiniuService() {
        SmsQiniuServiceImpl smsService = new SmsQiniuServiceImpl();
        log.info("SmsQiniuService={}", smsService);
        return smsService;
    }

    /**
     * 创建 SmsLingkaiService.
     *
     * @return SmsLingkaiService
     */
    @ConditionalOnProperty(prefix = "sms.lingkai", name = "enabled", havingValue = "true")
    @Bean
    public SmsLingkaiService smsLingkaiService() {
        SmsLingkaiServiceImpl smsService = new SmsLingkaiServiceImpl();
        log.info("SmsLingkaiService={}", smsService);
        return smsService;
    }

    /**
     * 创建 SmsMxtongService.
     *
     * @return SmsMxtongService
     */
    @ConditionalOnProperty(prefix = "sms.mxtong", name = "enabled", havingValue = "true")
    @Bean
    public SmsMxtongService smsMxtongService() {
        SmsMxtongServiceImpl smsService = new SmsMxtongServiceImpl();
        log.info("SmsMxtongService={}", smsService);
        return smsService;
    }
}
