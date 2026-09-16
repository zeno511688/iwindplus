/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss;

import com.iwindplus.base.oss.support.FileExecuteHandler;
import com.iwindplus.base.oss.support.impl.LocalFileExecuteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

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
     * 创建 FileExecuteHandler.
     *
     * @param multipartProperties 文件上传配置
     * @param resourceLoader      资源加载器
     * @return FileExecuteHandler
     */
    @Bean
    public FileExecuteHandler fileExecuteHandler(
        MultipartProperties multipartProperties,
        ResourceLoader resourceLoader) {
        LocalFileExecuteHandler fileExecuteHandler = new LocalFileExecuteHandler(multipartProperties, resourceLoader);
        log.info("FileExecuteHandler={}", fileExecuteHandler);
        return fileExecuteHandler;
    }
}
