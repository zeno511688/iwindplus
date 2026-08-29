/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document;

import com.iwindplus.base.document.dal.repository.ExportTaskRepository;
import com.iwindplus.base.document.domain.constant.DocumentConstant;
import com.iwindplus.base.document.domain.property.DocumentProperty;
import com.iwindplus.base.document.executor.ExportTaskExecutor;
import com.iwindplus.base.document.executor.impl.ExportTaskExecutorImpl;
import com.iwindplus.base.document.factory.DocumentTaskJobHandlerStrategyFactory;
import com.iwindplus.base.document.factory.ExportTaskHandlerFactory;
import com.iwindplus.base.document.jobhandler.DocumentTaskJob;
import com.iwindplus.base.document.service.ExportTaskService;
import com.iwindplus.base.document.service.impl.ExportTaskServiceImpl;
import com.iwindplus.base.document.support.DocumentTaskJobHandler;
import com.iwindplus.base.document.support.ExportTaskBizProcessor;
import com.iwindplus.base.document.support.ExportTaskExecuteHandler;
import com.iwindplus.base.document.support.ExportTaskHandler;
import com.iwindplus.base.document.support.ExportTaskStateSupport;
import com.iwindplus.base.document.support.impl.ExportTaskJobHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Document配置类.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "document", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DocumentProperty.class)
@ComponentScan(basePackages = DocumentConstant.DOCUMENT_COMPONENT_SCAN_BASE_PACKAGE)
@MapperScan(basePackages = DocumentConstant.DOCUMENT_MAPPER_SCAN_BASE_PACKAGE)
public class DocumentConfiguration {

    @Resource(name = "exportTaskExecutor")
    private DtpExecutor exportTaskExecutor;

    @PostConstruct
    public void init() {
        log.info("DocumentConfiguration is loaded.");
    }

    /**
     * 创建 ExportTaskExecutor.
     *
     * @param exportTaskService        exportTaskService
     * @param exportTaskBizProcessor   exportTaskBizProcessor
     * @param exportTaskHandlerFactory exportTaskHandlerFactory
     * @return ExportTaskExecutor
     */
    @Bean
    public ExportTaskExecutor exportTaskExecutor(
        ExportTaskService exportTaskService,
        ExportTaskBizProcessor exportTaskBizProcessor,
        ExportTaskHandlerFactory exportTaskHandlerFactory) {
        ExportTaskExecutor exportTaskExecutor = new ExportTaskExecutorImpl(
            exportTaskService, exportTaskBizProcessor, exportTaskHandlerFactory);
        log.info("ExportTaskExecutor={}", exportTaskExecutor);
        return exportTaskExecutor;
    }

    /**
     * 创建 ExportTaskRepository.
     *
     * @param property property
     * @return ExportTaskRepository
     */
    @Bean
    public ExportTaskRepository exportTaskRepository(DocumentProperty property) {
        return new ExportTaskRepository(property.getExportTask());
    }

    /**
     * 创建 ExportTaskService.
     *
     * @param exportTaskRepository exportTaskRepository
     * @return ExportTaskService
     */
    @Bean
    public ExportTaskService exportTaskService(
        DocumentProperty property,
        ExportTaskRepository exportTaskRepository) {
        return new ExportTaskServiceImpl(
            property.getExportTask(), exportTaskRepository, exportTaskExecutor);
    }

    /**
     * 创建 ExportTaskHandlerFactory.
     *
     * @param handlerProvider 处理器提供者
     * @return ExportTaskHandlerFactory
     */
    @Bean
    public ExportTaskHandlerFactory exportTaskHandlerFactory(
        ObjectProvider<ExportTaskHandler> handlerProvider) {
        ExportTaskHandlerFactory exportTaskHandlerFactory = new ExportTaskHandlerFactory(handlerProvider);
        log.info("ExportTaskHandlerFactory={}", exportTaskHandlerFactory);
        return exportTaskHandlerFactory;
    }

    /**
     * 创建 DocumentTaskJobHandlerStrategyFactory.
     *
     * @param handlerProvider 处理器提供者
     * @return DocumentTaskJobHandlerStrategyFactory
     */
    @Bean
    public DocumentTaskJobHandlerStrategyFactory documentTaskJobHandlerStrategyFactory(
        ObjectProvider<DocumentTaskJobHandler> handlerProvider) {
        DocumentTaskJobHandlerStrategyFactory factory = new DocumentTaskJobHandlerStrategyFactory(handlerProvider);
        log.info("DocumentTaskJobHandlerStrategyFactory={}", factory);
        return factory;
    }

    /**
     * 创建 ExportTaskStateSupport.
     *
     * @param property             property
     * @param exportTaskRepository exportTaskRepository
     * @param exportTaskService    exportTaskService
     * @param transactionTemplate  transactionTemplate
     * @return ExportTaskStateSupport
     */
    @Bean
    public ExportTaskStateSupport exportTaskStateSupport(
        DocumentProperty property,
        ExportTaskRepository exportTaskRepository,
        ExportTaskService exportTaskService,
        TransactionTemplate transactionTemplate) {
        return new ExportTaskStateSupport(
            property.getExportTask(), exportTaskRepository, exportTaskService, transactionTemplate);
    }

    /**
     * 创建 ExportTaskExecuteHandler.
     *
     * @param exportTaskHandlerFactory exportTaskHandlerFactory
     * @param exportTaskStateSupport   exportTaskStateSupport
     * @param exportTaskService        exportTaskService
     * @return ExportTaskExecuteHandler
     */
    @Bean
    public ExportTaskExecuteHandler exportTaskExecuteHandler(
        ExportTaskHandlerFactory exportTaskHandlerFactory,
        ExportTaskStateSupport exportTaskStateSupport,
        ExportTaskService exportTaskService) {
        ExportTaskExecuteHandler handler = new ExportTaskExecuteHandler(
            exportTaskHandlerFactory, exportTaskStateSupport, exportTaskService);
        log.info("ExportTaskExecuteHandler={}", handler);
        return handler;
    }

    /**
     * 创建 ExportTaskJobHandler.
     *
     * @param property               property
     * @param asyncCmdService        asyncCmdService
     * @param exportTaskBizProcessor exportTaskBizProcessor
     * @return ExportTaskJobHandler
     */
    @Bean
    public ExportTaskJobHandler exportTaskJobHandler(
        DocumentProperty property,
        ExportTaskService asyncCmdService,
        ExportTaskBizProcessor exportTaskBizProcessor,
        ExportTaskStateSupport exportTaskStateSupport) {
        return new ExportTaskJobHandler(
            property, asyncCmdService, exportTaskBizProcessor, exportTaskStateSupport);
    }

    /**
     * 创建 ExportTaskBizProcessor.
     *
     * @param property                 property
     * @param exportTaskService        exportTaskService
     * @param exportTaskExecuteHandler exportTaskExecuteHandler
     * @param exportTaskExecuteHandler exportTaskExecuteHandler
     * @return ExportTaskBizProcessor
     */
    @Bean
    public ExportTaskBizProcessor exportTaskBizProcessor(
        DocumentProperty property,
        ExportTaskService exportTaskService,
        ExportTaskStateSupport exportTaskStateSupport,
        ExportTaskExecuteHandler exportTaskExecuteHandler) {
        ExportTaskBizProcessor exportTaskBizProcessor = new ExportTaskBizProcessor(
            property, exportTaskService, exportTaskStateSupport,
            exportTaskExecuteHandler, exportTaskExecutor);
        return exportTaskBizProcessor;
    }

    /**
     * 创建 DocumentTaskJob.
     *
     * @param factory factory
     * @return DocumentTaskJob
     */
    @ConditionalOnProperty(prefix = "document.job", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public DocumentTaskJob documentTaskJob(
        DocumentTaskJobHandlerStrategyFactory factory) {
        DocumentTaskJob documentTaskJob = new DocumentTaskJob(factory);
        log.info("DocumentTaskJob={}", documentTaskJob);
        return documentTaskJob;
    }
}
