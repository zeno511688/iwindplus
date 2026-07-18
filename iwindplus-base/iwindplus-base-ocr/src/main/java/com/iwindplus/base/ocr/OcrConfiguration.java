/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr;

import com.iwindplus.base.ocr.domain.property.OcrProperty;
import com.iwindplus.base.ocr.service.OcrPrintWordService;
import com.iwindplus.base.ocr.service.OcrXiangyunService;
import com.iwindplus.base.ocr.service.impl.OcrPrintWordServiceImpl;
import com.iwindplus.base.ocr.service.impl.OcrXiangyunServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ocr配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(OcrProperty.class)
public class OcrConfiguration {
    /**
     * 创建 OcrPrintWordService.
     *
     * @return OcrPrintWordService
     */
    @ConditionalOnProperty(prefix = "ocr.print-word", name = "enabled", havingValue = "true")
    @Bean
    public OcrPrintWordService ocrPrintService() {
        OcrPrintWordServiceImpl ocrPrintService = new OcrPrintWordServiceImpl();
        log.info("OcrPrintWordService={}", ocrPrintService);
        return ocrPrintService;
    }

    /**
     * 创建 OcrXiangyunService.
     *
     * @return OcrXiangyunService
     */
    @ConditionalOnProperty(prefix = "ocr.xiangyun", name = "enabled", havingValue = "true")
    @Bean
    public OcrXiangyunService ocrXiangyunService() {
        OcrXiangyunServiceImpl ocrXiangyunService = new OcrXiangyunServiceImpl();
        log.info("OcrXiangyunService={}", ocrXiangyunService);
        return ocrXiangyunService;
    }
}
