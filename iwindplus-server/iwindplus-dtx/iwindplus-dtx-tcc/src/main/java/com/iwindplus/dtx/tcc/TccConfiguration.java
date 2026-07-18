/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.tcc;

import com.iwindplus.dtx.tcc.aspect.TccBranchTransactionalAspect;
import com.iwindplus.dtx.tcc.aspect.TccGlobalTransactionalAspect;
import com.iwindplus.dtx.tcc.domain.property.TccProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * tcc 配置.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({TccProperty.class})
@ConditionalOnProperty(prefix = "tcc", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TccConfiguration {

    /**
     * 创建 TccGlobalTransactionalAspect.
     *
     * @return TccGlobalTransactionalAspect
     */
    @Bean
    public TccGlobalTransactionalAspect tccGlobalTransactionalAspect() {
        TccGlobalTransactionalAspect tccGlobalTransactionalAspect = new TccGlobalTransactionalAspect();
        log.info("TccGlobalTransactionalAspect={}", tccGlobalTransactionalAspect);
        return tccGlobalTransactionalAspect;
    }

    /**
     * 创建 TccBranchTransactionalAspect.
     *
     * @return TccBranchTransactionalAspect
     */
    @Bean
    public TccBranchTransactionalAspect tccBranchTransactionalAspect() {
        TccBranchTransactionalAspect tccBranchTransactionalAspect = new TccBranchTransactionalAspect();
        log.info("TccBranchTransactionalAspect={}", tccBranchTransactionalAspect);
        return tccBranchTransactionalAspect;
    }
}
