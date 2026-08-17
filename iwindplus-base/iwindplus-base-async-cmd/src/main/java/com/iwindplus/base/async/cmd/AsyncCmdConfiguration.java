/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd;

import com.iwindplus.base.async.cmd.dal.mapper.AsyncCmdSubMapper;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdRepository;
import com.iwindplus.base.async.cmd.dal.repository.AsyncCmdSubRepository;
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.async.cmd.executor.impl.AsyncCmdExecutorImpl;
import com.iwindplus.base.async.cmd.factory.AsyncCmdDispatchHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdJobHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdSubTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.jobhandler.AsyncCmdJob;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.service.AsyncCmdSubService;
import com.iwindplus.base.async.cmd.service.impl.AsyncCmdServiceImpl;
import com.iwindplus.base.async.cmd.service.impl.AsyncCmdSubServiceImpl;
import com.iwindplus.base.async.cmd.support.AsyncCmdBizProcessor;
import com.iwindplus.base.async.cmd.support.AsyncCmdDispatchHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdExecuteHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdJobHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdStateSupport;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdDispatchHandlerAsync;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdDispatchHandlerCenter;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdExecuteHandlerGroup;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdExecuteHandlerMain;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdJobHandlerReset;
import com.iwindplus.base.async.cmd.support.impl.AsyncCmdJobHandlerRetry;
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

    @Resource(name = "asyncCmdTaskExecutor")
    private DtpExecutor asyncCmdTaskExecutor;

    @Resource(name = "asyncCmdSubTaskExecutor")
    private DtpExecutor asyncCmdSubTaskExecutor;

    @PostConstruct
    public void init() {
        log.info("AsyncCmdConfiguration is loaded.");
    }

    /**
     * 创建 AsyncCmdExecutor.
     *
     * @param asyncCmdService                        asyncCmdService
     * @param asyncCmdSubService                     asyncCmdSubService
     * @param asyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory
     * @param asyncCmdTaskHandlerStrategyFactory     asyncCmdTaskHandlerStrategyFactory
     * @param asyncCmdSubTaskHandlerStrategyFactory  asyncCmdSubTaskHandlerStrategyFactory
     * @return AsyncCmdExecutor
     */
    @Bean
    public AsyncCmdExecutor asyncCmdExecutor(
        AsyncCmdService asyncCmdService,
        AsyncCmdSubService asyncCmdSubService,
        AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory,
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory,
        AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory) {
        AsyncCmdExecutor asyncCmdExecutor = new AsyncCmdExecutorImpl(
            asyncCmdService, asyncCmdSubService, asyncCmdDispatchHandlerStrategyFactory,
            asyncCmdTaskHandlerStrategyFactory, asyncCmdSubTaskHandlerStrategyFactory);
        log.info("AsyncCmdExecutor={}", asyncCmdExecutor);
        return asyncCmdExecutor;
    }

    /**
     * 创建 AsyncCmdRepository.
     *
     * @param property          property
     * @param asyncCmdSubMapper asyncCmdSubMapper
     * @return AsyncCmdRepository
     */
    @Bean
    public AsyncCmdRepository asyncCmdRepository(
        AsyncCmdProperty property,
        AsyncCmdSubMapper asyncCmdSubMapper) {
        AsyncCmdRepository asyncCmdRepository = new AsyncCmdRepository(property, asyncCmdSubMapper);
        return asyncCmdRepository;
    }

    /**
     * 创建 AsyncCmdSubRepository.
     *
     * @return AsyncCmdSubRepository
     */
    @Bean
    public AsyncCmdSubRepository asyncCmdSubRepository() {
        AsyncCmdSubRepository asyncCmdSubRepository = new AsyncCmdSubRepository();
        log.info("AsyncCmdSubRepository={}", asyncCmdSubRepository);
        return asyncCmdSubRepository;
    }

    /**
     * 创建 AsyncCmdService.
     *
     * @param property              property
     * @param asyncCmdRepository    asyncCmdRepository
     * @param asyncCmdSubRepository asyncCmdSubRepository
     * @param transactionTemplate   transactionTemplate
     * @return AsyncCmdService
     */
    @Bean
    public AsyncCmdService asyncCmdService(
        AsyncCmdProperty property,
        AsyncCmdRepository asyncCmdRepository,
        AsyncCmdSubRepository asyncCmdSubRepository,
        TransactionTemplate transactionTemplate) {
        AsyncCmdService asyncCmdService = new AsyncCmdServiceImpl(
            property, asyncCmdRepository, asyncCmdSubRepository,
            asyncCmdTaskExecutor, transactionTemplate);
        return asyncCmdService;
    }

    /**
     * 创建 AsyncCmdSubService.
     *
     * @param asyncCmdSubRepository asyncCmdSubRepository
     * @return AsyncCmdSubService
     */
    @Bean
    public AsyncCmdSubService asyncCmdSubService(
        AsyncCmdSubRepository asyncCmdSubRepository) {
        AsyncCmdSubService asyncCmdSubService = new AsyncCmdSubServiceImpl(
            asyncCmdSubRepository);
        log.info("AsyncCmdSubService={}", asyncCmdSubService);
        return asyncCmdSubService;
    }

    /**
     * 创建 AsyncCmdJobHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncCmdJobHandlerStrategyFactory
     */
    @Bean
    public AsyncCmdJobHandlerStrategyFactory asyncCmdJobHandlerStrategyFactory(
        ObjectProvider<AsyncCmdJobHandler> executorProvider) {
        AsyncCmdJobHandlerStrategyFactory asyncCmdJobHandlerStrategyFactory = new AsyncCmdJobHandlerStrategyFactory(executorProvider);
        log.info("AsyncCmdJobHandlerStrategyFactory={}", asyncCmdJobHandlerStrategyFactory);
        return asyncCmdJobHandlerStrategyFactory;
    }

    /**
     * 创建 AsyncCmdDispatchHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncCmdDispatchHandlerStrategyFactory
     */
    @Bean
    public AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory(
        ObjectProvider<AsyncCmdDispatchHandler> executorProvider) {
        AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory = new AsyncCmdDispatchHandlerStrategyFactory(executorProvider);
        log.info("AsyncCmdDispatchHandlerStrategyFactory={}", asyncCmdDispatchHandlerStrategyFactory);
        return asyncCmdDispatchHandlerStrategyFactory;
    }

    /**
     * 创建 AsyncCmdTaskHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncCmdTaskHandlerStrategyFactory
     */
    @Bean
    public AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory(
        ObjectProvider<AsyncCmdTaskHandler> executorProvider) {
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory =
            new AsyncCmdTaskHandlerStrategyFactory(executorProvider);
        log.info("AsyncCmdTaskHandlerStrategyFactory={}", asyncCmdTaskHandlerStrategyFactory);
        return asyncCmdTaskHandlerStrategyFactory;
    }

    /**
     * 创建 AsyncCmdSubTaskHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncCmdSubTaskHandlerStrategyFactory
     */
    @Bean
    public AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory(
        ObjectProvider<AsyncCmdSubTaskHandler> executorProvider) {
        AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory =
            new AsyncCmdSubTaskHandlerStrategyFactory(executorProvider);
        log.info("AsyncCmdSubTaskHandlerStrategyFactory={}", asyncCmdSubTaskHandlerStrategyFactory);
        return asyncCmdSubTaskHandlerStrategyFactory;
    }

    /**
     * 创建 AsyncCmdStateSupport.
     *
     * @param property            property
     * @param asyncCmdRepository  asyncCmdRepository
     * @param asyncCmdService     asyncCmdService
     * @param asyncCmdSubService  asyncCmdSubService
     * @param transactionTemplate transactionTemplate
     * @return AsyncCmdStateSupport
     */
    @Bean
    public AsyncCmdStateSupport asyncCmdStateSupport(
        AsyncCmdProperty property,
        AsyncCmdRepository asyncCmdRepository,
        AsyncCmdService asyncCmdService,
        AsyncCmdSubService asyncCmdSubService,
        TransactionTemplate transactionTemplate) {
        AsyncCmdStateSupport asyncCmdStateSupport = new AsyncCmdStateSupport(
            property, asyncCmdRepository, asyncCmdService, asyncCmdSubService, transactionTemplate);
        return asyncCmdStateSupport;
    }

    /**
     * 创建 AsyncCmdExecuteHandlerMain.
     *
     * @param asyncCmdTaskHandlerStrategyFactor asyncCmdTaskHandlerStrategyFactor
     * @param asyncCmdStateSupport              asyncCmdStateSupport
     * @param asyncCmdService                   asyncCmdService
     * @return AsyncCmdExecuteHandlerMain
     */
    @Bean
    public AsyncCmdExecuteHandlerMain asyncCmdExecuteHandlerMain(
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactor,
        AsyncCmdStateSupport asyncCmdStateSupport,
        AsyncCmdRepository asyncCmdRepository,
        AsyncCmdService asyncCmdService) {
        AsyncCmdExecuteHandlerMain asyncCmdExecuteHandlerMain = new AsyncCmdExecuteHandlerMain(
            asyncCmdTaskHandlerStrategyFactor, asyncCmdStateSupport, asyncCmdService);
        log.info("AsyncCmdExecuteHandlerMain={}", asyncCmdExecuteHandlerMain);
        return asyncCmdExecuteHandlerMain;
    }

    /**
     * 创建 AsyncCmdExecuteHandlerGroup.
     *
     * @param asyncCmdTaskHandlerStrategyFactory    asyncCmdTaskHandlerStrategyFactory
     * @param asyncCmdStateSupport                  asyncCmdStateSupport
     * @param asyncCmdService                       asyncCmdService
     * @param asyncCmdSubService                    asyncCmdSubService
     * @param asyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory
     * @return AsyncCmdExecuteHandlerGroup
     */
    @Bean
    public AsyncCmdExecuteHandlerGroup asyncCmdExecuteHandlerGroup(
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory,
        AsyncCmdStateSupport asyncCmdStateSupport,
        AsyncCmdService asyncCmdService,
        AsyncCmdSubService asyncCmdSubService,
        AsyncCmdSubTaskHandlerStrategyFactory asyncCmdSubTaskHandlerStrategyFactory) {
        AsyncCmdExecuteHandlerGroup asyncCmdExecuteHandlerGroup = new AsyncCmdExecuteHandlerGroup(
            asyncCmdTaskHandlerStrategyFactory, asyncCmdStateSupport, asyncCmdService,
            asyncCmdSubService, asyncCmdSubTaskHandlerStrategyFactory, asyncCmdSubTaskExecutor);
        log.info("AsyncCmdExecuteHandlerGroup={}", asyncCmdExecuteHandlerGroup);
        return asyncCmdExecuteHandlerGroup;
    }

    /**
     * 创建 AsyncCmdBizProcessor.
     *
     * @param property                    property
     * @param asyncCmdService             asyncCmdService
     * @param asyncCmdSubService          asyncCmdSubService
     * @param asyncCmdExecuteHandlerMain  asyncCmdExecuteHandlerMain
     * @param asyncCmdExecuteHandlerGroup asyncCmdExecuteHandlerGroup
     * @return AsyncCmdBizProcessor
     */
    @Bean
    public AsyncCmdBizProcessor asyncCmdBizProcessor(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdSubService asyncCmdSubService,
        AsyncCmdStateSupport asyncCmdStateSupport,
        AsyncCmdExecuteHandler asyncCmdExecuteHandlerMain,
        AsyncCmdExecuteHandler asyncCmdExecuteHandlerGroup) {
        AsyncCmdBizProcessor asyncCmdBizProcessor = new AsyncCmdBizProcessor(
            property, asyncCmdService, asyncCmdSubService, asyncCmdStateSupport,
            asyncCmdExecuteHandlerMain, asyncCmdExecuteHandlerGroup, asyncCmdTaskExecutor);
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
        return asyncCmdDispatchHandlerCenter;
    }

    /**
     * 创建 AsyncCmdJobHandlerRetry.
     *
     * @param property             property
     * @param asyncCmdService      asyncCmdService
     * @param asyncCmdBizProcessor asyncCmdBizProcessor
     * @return AsyncCmdJobHandlerRetry
     */
    @Bean
    public AsyncCmdJobHandlerRetry asyncCmdJobHandlerRetry(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdBizProcessor asyncCmdBizProcessor) {
        AsyncCmdJobHandlerRetry asyncCmdJobHandlerRetry = new AsyncCmdJobHandlerRetry(
            property, asyncCmdService, asyncCmdBizProcessor);
        return asyncCmdJobHandlerRetry;
    }

    /**
     * 创建 AsyncCmdJobHandlerReset.
     *
     * @param property                           property
     * @param asyncCmdService                    asyncCmdService
     * @param asyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory
     * @return AsyncCmdJobHandlerReset
     */
    @Bean
    public AsyncCmdJobHandlerReset asyncCmdJobHandlerReset(
        AsyncCmdProperty property,
        AsyncCmdService asyncCmdService,
        AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory) {
        AsyncCmdJobHandlerReset asyncCmdJobHandlerReset = new AsyncCmdJobHandlerReset(
            property, asyncCmdService, asyncCmdTaskHandlerStrategyFactory);
        return asyncCmdJobHandlerReset;
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
