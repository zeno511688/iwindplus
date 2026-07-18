/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.xxl.job;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.xxl.job.domain.property.XxlJobProperty;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import jakarta.annotation.Resource;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * job配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(XxlJobProperty.class)
@ConditionalOnProperty(prefix = "xxl-job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobConfiguration {

    @Resource
    private XxlJobProperty property;

    /**
     * 创建XxlJobSpringExecutor.
     *
     * @return XxlJobSpringExecutor
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(this.property.getAdmin().getAddresses());
        xxlJobSpringExecutor.setAccessToken(this.property.getAdmin().getAccessToken());
        xxlJobSpringExecutor.setAppname(Optional.ofNullable(this.property.getExecutor().getAppName()).orElse(SpringUtil.getApplicationName()));
        xxlJobSpringExecutor.setAddress(this.property.getExecutor().getAddresses());
        xxlJobSpringExecutor.setIp(this.property.getExecutor().getIp());
        xxlJobSpringExecutor.setPort(this.property.getExecutor().getPort());
        xxlJobSpringExecutor.setLogPath(this.property.getExecutor().getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(this.property.getExecutor().getLogRetentionDays());
        log.info(">>>>>>>>>>> xxl-job config init.");
        return xxlJobSpringExecutor;
    }

}