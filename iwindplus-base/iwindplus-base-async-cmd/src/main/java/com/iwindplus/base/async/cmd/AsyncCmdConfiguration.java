/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd;

import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.async.cmd.executor.impl.AsyncCmdExecutorImpl;
import com.iwindplus.base.async.cmd.factory.AsyncCmdDispatchHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdJobHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.jobhandler.AsyncCmdJob;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.impl.AsyncCmdServiceImpl;
import com.iwindplus.base.async.cmd.support.AsyncCmdBizProcessor;
import com.iwindplus.base.async.cmd.support.AsyncCmdDispatchHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdJobHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdDispatchHandlerAsync;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdDispatchHandlerCenter;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdJobResetHandler;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdJobRetryHandler;
import jakarta.annotation.PostConstruct;
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
 * 异步命令配置.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "async-cmd", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AsyncCmdProperty.class)
@MapperScan(AsyncCmdConstant.ASYNC_CMD_MAPPER_SCAN_BASE_PACKAGE)
@ComponentScan(AsyncCmdConstant.ASYNC_CMD_COMPONENT_SCAN_BASE_PACKAGE)
public class AsyncCmdConfiguration {

    @PostConstruct
    public void init() {
        log.info("AsyncCmdConfiguration is loaded.");
    }

    /**
     * 创建 AsyncCmdExecutor.
     *
     * @param asyncCmdService                        asyncCmdService
     * @param asyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory
     * @param asyncCmdTaskHandlerStrategyFactory     asyncCmdTaskHandlerStrategyFactory
     * @return AsyncCmdExecutor
     */
    @Bean
    public AsyncCmdExecutor asyncCmdExecutor(
        AsyncCmdService asyncCmdService,
        AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory,
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory) {
        AsyncCmdExecutor asyncCmdExecutor = new AsyncCmdExecutorImpl(
            asyncCmdService, asyncCmdDispatchHandlerStrategyFactory,
            asyncCmdTaskHandlerStrategyFactory);
        log.info("AsyncCmdExecutor={}", asyncCmdExecutor);
        return asyncCmdExecutor;
    }

    /**
     * 创建 AsyncCmdRepository.
     *
     * @return AsyncCmdRepository
     */
    @Bean
    public AsyncCmdRepository asyncCmdRepository() {
        AsyncCmdRepository asyncCmdRepository = new AsyncCmdRepository();
        log.info("AsyncCmdRepository={}", asyncCmdRepository);
        return asyncCmdRepository;
    }

    /**
     * 创建 AsyncCMdService.
     *
     * @param property             property
     * @param asyncCmdRepository   asyncCmdRepository
     * @param asyncCmdTaskExecutor asyncCmdTaskExecutor
     * @return AsyncCmdService
     */
    @Bean
    public AsyncCmdService asyncCmdService(
        AsyncCmdProperty property,
        AsyncCmdRepository asyncCmdRepository,
        DtpExecutor asyncCmdTaskExecutor) {
        AsyncCmdService asyncCmdService = new AsyncCmdServiceImpl(
            property, asyncCmdRepository, asyncCmdTaskExecutor);
        log.info("AsyncCmdService={}", asyncCmdService);
        return asyncCmdService;
    }

    /**
     * 创建 AsyncCmdJobStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncCmdJobStrategyFactory
     */
    @Bean
    public AsyncCmdJobHandlerStrategyFactory asyncCmdJobStrategyFactory(
        ObjectProvider<AsyncCmdJobHandler> executorProvider) {
        AsyncCmdJobHandlerStrategyFactory asyncCmdJobStrategyFactory = new AsyncCmdJobHandlerStrategyFactory(executorProvider);
        log.info("AsyncCmdJobStrategyFactory={}", asyncCmdJobStrategyFactory);
        return asyncCmdJobStrategyFactory;
    }

    /**
     * 创建 AsyncCmdDispatchStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncCmdDispatchStrategyFactory
     */
    @Bean
    public AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchStrategyFactory(
        ObjectProvider<AsyncCmdDispatchHandler> executorProvider) {
        AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchStrategyFactory = new AsyncCmdDispatchHandlerStrategyFactory(executorProvider);
        log.info("AsyncCmdDispatchStrategyFactory={}", asyncCmdDispatchStrategyFactory);
        return asyncCmdDispatchStrategyFactory;
    }

    /**
     * 创建 AsyncCmdExecutorStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncCmdExecutorStrategyFactory
     */
    @Bean
    public AsyncCmdTaskHandlerStrategyFactory asyncCmdExecutorStrategyFactory(
        ObjectProvider<AsyncCmdTaskHandler> executorProvider) {
        AsyncCmdTaskHandlerStrategyFactory asyncCmdExecutorStrategyFactory = new AsyncCmdTaskHandlerStrategyFactory(executorProvider);
        log.info("AsyncCmdExecutorStrategyFactory={}", asyncCmdExecutorStrategyFactory);
        return asyncCmdExecutorStrategyFactory;
    }

    /**
     * 创建 AsyncCmdBizProcessor.
     *
     * @return AsyncCmdBizProcessor
     */
    /**
     * @param property                           property
     * @param asyncCmdService                    asyncCmdService
     * @param asyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory
     * @param asyncCmdTaskExecutor               asyncCmdTaskExecutor
     * @param transactionTemplate                transactionTemplate
     * @return
     */
    @Bean
    public AsyncCmdBizProcessor asyncCmdBizProcessor(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory,
        DtpExecutor asyncCmdTaskExecutor,
        TransactionTemplate transactionTemplate) {
        AsyncCmdBizProcessor asyncCmdBizProcessor = new AsyncCmdBizProcessor(
            property, asyncCmdService, asyncCmdTaskHandlerStrategyFactory,
            asyncCmdTaskExecutor, transactionTemplate);
        log.info("AsyncCmdBizProcessor={}", asyncCmdBizProcessor);
        return asyncCmdBizProcessor;
    }

    /**
     * 创建 AsyncCmdDispatchHandlerAsync.
     *
     * @param property             property
     * @param asyncCmdService      asyncCmdService
     * @param asyncCmdBizProcessor asyncCmdBizProcessor
     * @return AsyncCmdDispatchHandlerAsync
     */
    @Bean
    public AsyncCmdDispatchHandlerAsync asyncCmdDispatchHandlerAsync(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdBizProcessor asyncCmdBizProcessor) {
        AsyncCmdDispatchHandlerAsync asyncCmdDispatchHandlerAsync = new AsyncCmdDispatchHandlerAsync(
            property, asyncCmdService, asyncCmdBizProcessor);
        log.info("AsyncCmdDispatchHandlerAsync={}", asyncCmdDispatchHandlerAsync);
        return asyncCmdDispatchHandlerAsync;
    }

    /**
     * 创建 AsyncCmdDispatchHandlerCenter.
     *
     * @param property        property
     * @param asyncCmdService asyncCmdService
     * @return AsyncCmdDispatchHandlerCenter
     */
    @Bean
    public AsyncCmdDispatchHandlerCenter asyncCmdDispatchHandlerCenter(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService) {
        AsyncCmdDispatchHandlerCenter asyncCmdDispatchHandlerCenter = new AsyncCmdDispatchHandlerCenter(
            property, asyncCmdService);
        log.info("AsyncCmdDispatchHandlerCenter={}", asyncCmdDispatchHandlerCenter);
        return asyncCmdDispatchHandlerCenter;
    }

    /**
     * 创建 AsyncCmdJobRetryHandler.
     *
     * @param property             property
     * @param asyncCmdService      asyncCmdService
     * @param asyncCmdBizProcessor asyncCmdBizProcessor
     * @return AsyncCmdJobRetryHandler
     */
    @Bean
    public AsyncCmdJobRetryHandler asyncCmdJobRetryHandler(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdBizProcessor asyncCmdBizProcessor) {
        AsyncCmdJobRetryHandler asyncCmdJobRetryHandler = new AsyncCmdJobRetryHandler(
            property, asyncCmdService, asyncCmdBizProcessor);
        log.info("AsyncCmdJobRetryHandler={}", asyncCmdJobRetryHandler);
        return asyncCmdJobRetryHandler;
    }

    /**
     * 创建 AsyncCmdJobResetHandler.
     *
     * @param property             property
     * @param asyncCmdService      asyncCmdService
     * @return AsyncCmdJobResetHandler
     */
    @Bean
    public AsyncCmdJobResetHandler asyncCmdJobResetHandler(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService) {
        AsyncCmdJobResetHandler asyncCmdJobResetHandler = new AsyncCmdJobResetHandler(
            property, asyncCmdService);
        log.info("AsyncCmdJobResetHandler={}", asyncCmdJobResetHandler);
        return asyncCmdJobResetHandler;
    }

    /**
     * 创建 AsyncCmdJob.
     *
     * @param asyncCmdJobHandlerStrategyFactory asyncCmdJobHandlerStrategyFactory
     * @return AsyncCmdJob
     */
    @ConditionalOnProperty(prefix = "async-cmd.job", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public AsyncCmdJob asyncCmdJob(
        AsyncCmdJobHandlerStrategyFactory asyncCmdJobHandlerStrategyFactory) {
        AsyncCmdJob asyncCmdJob = new AsyncCmdJob(asyncCmdJobHandlerStrategyFactory);
        log.info("AsyncCmdJob={}", asyncCmdJob);
        return asyncCmdJob;
    }

}
