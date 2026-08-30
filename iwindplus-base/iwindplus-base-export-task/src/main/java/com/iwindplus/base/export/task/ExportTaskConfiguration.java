/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task;

import com.iwindplus.base.export.task.dal.repository.ExportTaskRepository;
import com.iwindplus.base.export.task.domain.constant.ExportTaskConstant;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.executor.ExportTaskExecutor;
import com.iwindplus.base.export.task.executor.impl.ExportTaskExecutorImpl;
import com.iwindplus.base.export.task.factory.ExportTaskHandlerStrategyFactory;
import com.iwindplus.base.export.task.factory.ExportTaskJobHandlerStrategyFactory;
import com.iwindplus.base.export.task.jobhandler.ExportTaskJob;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.export.task.service.impl.ExportTaskServiceImpl;
import com.iwindplus.base.export.task.support.ExportTaskBizProcessor;
import com.iwindplus.base.export.task.support.ExportTaskExecuteHandler;
import com.iwindplus.base.export.task.support.ExportTaskHandler;
import com.iwindplus.base.export.task.support.ExportTaskJobHandler;
import com.iwindplus.base.export.task.support.ExportTaskStateSupport;
import com.iwindplus.base.export.task.support.impl.ExportTaskJobHandlerRetry;
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
 * 导出任务配置类.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "export-task", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ExportTaskProperty.class)
@ComponentScan(basePackages = ExportTaskConstant.EXPORT_COMPONENT_SCAN_BASE_PACKAGE)
@MapperScan(basePackages = ExportTaskConstant.EXPORT_MAPPER_SCAN_BASE_PACKAGE)
public class ExportTaskConfiguration {

    @Resource(name = ExportTaskConstant.THREAD_POOL_BEAN_NAME)
    private DtpExecutor threadPoolExecutor;

    @PostConstruct
    public void init() {
        log.info("ExportTaskConfiguration is loaded.");
    }

    /**
     * 创建 ExportTaskExecutor.
     *
     * @param exportTaskService                exportTaskService
     * @param exportTaskBizProcessor           exportTaskBizProcessor
     * @param exportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory
     * @return ExportTaskExecutor
     */
    @Bean
    public ExportTaskExecutor exportTaskExecutor(
        ExportTaskService exportTaskService,
        ExportTaskBizProcessor exportTaskBizProcessor,
        ExportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory) {
        ExportTaskExecutor exportTaskExecutor = new ExportTaskExecutorImpl(
            exportTaskService, exportTaskBizProcessor, exportTaskHandlerStrategyFactory);
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
    public ExportTaskRepository exportTaskRepository(ExportTaskProperty property) {
        return new ExportTaskRepository(property);
    }

    /**
     * 创建 ExportTaskService.
     *
     * @param exportTaskRepository exportTaskRepository
     * @return ExportTaskService
     */
    @Bean
    public ExportTaskService exportTaskService(
        ExportTaskProperty property,
        ExportTaskRepository exportTaskRepository) {
        return new ExportTaskServiceImpl(
            property, exportTaskRepository, threadPoolExecutor);
    }

    /**
     * 创建 ExportTaskHandlerStrategyFactory.
     *
     * @param handlerProvider 处理器提供者
     * @return ExportTaskHandlerStrategyFactory
     */
    @Bean
    public ExportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory(
        ObjectProvider<ExportTaskHandler> handlerProvider) {
        ExportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory = new ExportTaskHandlerStrategyFactory(handlerProvider);
        log.info("ExportTaskHandlerStrategyFactory={}", exportTaskHandlerStrategyFactory);
        return exportTaskHandlerStrategyFactory;
    }

    /**
     * 创建 ExportTaskTaskJobHandlerStrategyFactory.
     *
     * @param handlerProvider 处理器提供者
     * @return ExportTaskTaskJobHandlerStrategyFactory
     */
    @Bean
    public ExportTaskJobHandlerStrategyFactory exportTaskTaskJobHandlerStrategyFactory(
        ObjectProvider<ExportTaskJobHandler> handlerProvider) {
        ExportTaskJobHandlerStrategyFactory factory = new ExportTaskJobHandlerStrategyFactory(handlerProvider);
        log.info("ExportTaskTaskJobHandlerStrategyFactory={}", factory);
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
        ExportTaskProperty property,
        ExportTaskRepository exportTaskRepository,
        ExportTaskService exportTaskService,
        TransactionTemplate transactionTemplate) {
        return new ExportTaskStateSupport(
            property, exportTaskRepository, exportTaskService, transactionTemplate);
    }

    /**
     * 创建 ExportTaskExecuteHandler.
     *
     * @param exportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory
     * @param exportTaskStateSupport           exportTaskStateSupport
     * @param exportTaskService                exportTaskService
     * @return ExportTaskExecuteHandler
     */
    @Bean
    public ExportTaskExecuteHandler exportTaskExecuteHandler(
        ExportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory,
        ExportTaskStateSupport exportTaskStateSupport,
        ExportTaskService exportTaskService) {
        ExportTaskExecuteHandler handler = new ExportTaskExecuteHandler(
            exportTaskHandlerStrategyFactory, exportTaskStateSupport, exportTaskService);
        log.info("ExportTaskExecuteHandler={}", handler);
        return handler;
    }

    /**
     * 创建 ExportTaskJobHandlerRetry.
     *
     * @param property               property
     * @param exportTaskService      exportTaskService
     * @param exportTaskBizProcessor exportTaskBizProcessor
     * @return ExportTaskJobHandlerRetry
     */
    @Bean
    public ExportTaskJobHandlerRetry exportTaskJobHandlerRetry(
        ExportTaskProperty property,
        ExportTaskService exportTaskService,
        ExportTaskBizProcessor exportTaskBizProcessor,
        ExportTaskStateSupport exportTaskStateSupport) {
        return new ExportTaskJobHandlerRetry(
            property, exportTaskService, exportTaskBizProcessor, exportTaskStateSupport);
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
        ExportTaskProperty property,
        ExportTaskService exportTaskService,
        ExportTaskStateSupport exportTaskStateSupport,
        ExportTaskExecuteHandler exportTaskExecuteHandler) {
        ExportTaskBizProcessor exportTaskBizProcessor = new ExportTaskBizProcessor(
            property, exportTaskService, exportTaskStateSupport,
            exportTaskExecuteHandler, threadPoolExecutor);
        return exportTaskBizProcessor;
    }

    /**
     * 创建 ExportTaskTaskJob.
     *
     * @param factory factory
     * @return ExportTaskTaskJob
     */
    @ConditionalOnProperty(prefix = "export-task.job", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public ExportTaskJob exportTaskTaskJob(
        ExportTaskJobHandlerStrategyFactory factory) {
        ExportTaskJob exportTaskTaskJob = new ExportTaskJob(factory);
        log.info("ExportTaskTaskJob={}", exportTaskTaskJob);
        return exportTaskTaskJob;
    }
}
