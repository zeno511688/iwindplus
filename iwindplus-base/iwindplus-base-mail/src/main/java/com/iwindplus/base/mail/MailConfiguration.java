/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail;

import com.iwindplus.base.mail.domain.property.MailProperty;
import com.iwindplus.base.mail.service.impl.MailServiceImpl;
import com.iwindplus.base.mail.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件操作配置.
 *
 * @author zengdegui
 * @since 2020/12/6
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MailProperty.class)
public class MailConfiguration {

    /**
     * 创建MailService.
     *
     * @return MailService
     */
    @Bean
    public MailService mailService() {
        MailService mailService = new MailServiceImpl();
        log.info("MailService={}", mailService);
        return mailService;
    }
}
