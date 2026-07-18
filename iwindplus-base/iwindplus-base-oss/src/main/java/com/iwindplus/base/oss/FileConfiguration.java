/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss;

import com.iwindplus.base.oss.service.impl.FileServiceImpl;
import com.iwindplus.base.oss.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件操作配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
public class FileConfiguration {

    /**
     * 创建FileService.
     *
     * @return FileService
     */
    @Bean
    public FileService fileService() {
        FileServiceImpl fileService = new FileServiceImpl();
        log.info("FileService={}", fileService);
        return fileService;
    }
}
