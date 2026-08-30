/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task;

import com.iwindplus.base.async.task.dal.mapper.AsyncTaskSubMapper;
import com.iwindplus.base.async.task.dal.repository.AsyncTaskRepository;
import com.iwindplus.base.async.task.dal.repository.AsyncTaskSubRepository;
import com.iwindplus.base.async.task.domain.constant.AsyncTaskConstant;
import com.iwindplus.base.async.task.domain.property.AsyncTaskProperty;
import com.iwindplus.base.async.task.executor.AsyncTaskExecutor;
import com.iwindplus.base.async.task.executor.impl.AsyncTaskExecutorImpl;
import com.iwindplus.base.async.task.factory.AsyncTaskHandlerStrategyFactory;
import com.iwindplus.base.async.task.factory.AsyncTaskJobHandlerStrategyFactory;
import com.iwindplus.base.async.task.factory.AsyncTaskSubHandlerStrategyFactory;
import com.iwindplus.base.async.task.jobhandler.AsyncTaskJob;
import com.iwindplus.base.async.task.service.AsyncTaskService;
import com.iwindplus.base.async.task.service.AsyncTaskSubService;
import com.iwindplus.base.async.task.service.impl.AsyncTaskServiceImpl;
import com.iwindplus.base.async.task.service.impl.AsyncTaskSubServiceImpl;
import com.iwindplus.base.async.task.support.AsyncTaskBizProcessor;
import com.iwindplus.base.async.task.support.AsyncTaskExecuteHandler;
import com.iwindplus.base.async.task.support.AsyncTaskHandler;
import com.iwindplus.base.async.task.support.AsyncTaskJobHandler;
import com.iwindplus.base.async.task.support.AsyncTaskStateSupport;
import com.iwindplus.base.async.task.support.AsyncTaskSubHandler;
import com.iwindplus.base.async.task.support.impl.AsyncTaskExecuteHandlerGroup;
import com.iwindplus.base.async.task.support.impl.AsyncTaskExecuteHandlerMain;
import com.iwindplus.base.async.task.support.impl.AsyncTaskJobHandlerRetry;
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
 * 异步任务配置.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "async-task", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AsyncTaskProperty.class)
@MapperScan(AsyncTaskConstant.ASYNC_TASK_MAPPER_SCAN_BASE_PACKAGE)
@ComponentScan(AsyncTaskConstant.ASYNC_TASK_COMPONENT_SCAN_BASE_PACKAGE)
public class AsyncTaskConfiguration {

    @Resource(name = AsyncTaskConstant.THREAD_POOL_BEAN_NAME)
    private DtpExecutor threadPoolExecutor;

    @Resource(name = AsyncTaskConstant.THREAD_POOL_SUB_BEAN_NAME)
    private DtpExecutor subThreadPoolExecutor;

    @PostConstruct
    public void init() {
        log.info("AsyncTaskConfiguration is loaded.");
    }

    /**
     * 创建 AsyncTaskExecutor.
     *
     * @param asyncTaskService                   asyncTaskService
     * @param asyncTaskSubService                asyncTaskSubService
     * @param asyncTaskBizProcessor              asyncTaskBizProcessor
     * @param asyncTaskHandlerStrategyFactory    asyncTaskHandlerStrategyFactory
     * @param asyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory
     * @return AsyncTaskExecutor
     */
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor(
        AsyncTaskService asyncTaskService,
        AsyncTaskSubService asyncTaskSubService,
        AsyncTaskBizProcessor asyncTaskBizProcessor,
        AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory,
        AsyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory) {
        AsyncTaskExecutor asyncTaskExecutor = new AsyncTaskExecutorImpl(
            asyncTaskService, asyncTaskSubService, asyncTaskBizProcessor,
            asyncTaskHandlerStrategyFactory, asyncTaskSubHandlerStrategyFactory);
        log.info("AsyncTaskExecutor={}", asyncTaskExecutor);
        return asyncTaskExecutor;
    }

    /**
     * 创建 AsyncTaskRepository.
     *
     * @param property           property
     * @param asyncTaskSubMapper asyncTaskSubMapper
     * @return AsyncTaskRepository
     */
    @Bean
    public AsyncTaskRepository asyncTaskRepository(
        AsyncTaskProperty property,
        AsyncTaskSubMapper asyncTaskSubMapper) {
        return new AsyncTaskRepository(property, asyncTaskSubMapper);
    }

    /**
     * 创建 AsyncTaskSubRepository.
     *
     * @param property property
     * @return AsyncTaskSubRepository
     */
    @Bean
    public AsyncTaskSubRepository asyncTaskSubRepository(
        AsyncTaskProperty property) {
        return new AsyncTaskSubRepository(property);
    }

    /**
     * 创建 AsyncTaskService.
     *
     * @param property               property
     * @param asyncTaskRepository    asyncTaskRepository
     * @param asyncTaskSubRepository asyncTaskSubRepository
     * @param transactionTemplate    transactionTemplate
     * @return AsyncTaskService
     */
    @Bean
    public AsyncTaskService asyncTaskService(
        AsyncTaskProperty property,
        AsyncTaskRepository asyncTaskRepository,
        AsyncTaskSubRepository asyncTaskSubRepository,
        TransactionTemplate transactionTemplate) {
        AsyncTaskService asyncTaskService = new AsyncTaskServiceImpl(
            property, asyncTaskRepository, asyncTaskSubRepository,
            threadPoolExecutor, transactionTemplate);
        return asyncTaskService;
    }

    /**
     * 创建 AsyncTaskSubService.
     *
     * @param asyncTaskSubRepository asyncTaskSubRepository
     * @return AsyncTaskSubService
     */
    @Bean
    public AsyncTaskSubService asyncTaskSubService(
        AsyncTaskSubRepository asyncTaskSubRepository) {
        AsyncTaskSubService asyncTaskSubService = new AsyncTaskSubServiceImpl(
            asyncTaskSubRepository);
        log.info("AsyncTaskSubService={}", asyncTaskSubService);
        return asyncTaskSubService;
    }

    /**
     * 创建 AsyncTaskHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncTaskHandlerStrategyFactory
     */
    @Bean
    public AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory(
        ObjectProvider<AsyncTaskHandler> executorProvider) {
        AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory =
            new AsyncTaskHandlerStrategyFactory(executorProvider);
        log.info("AsyncTaskHandlerStrategyFactory={}", asyncTaskHandlerStrategyFactory);
        return asyncTaskHandlerStrategyFactory;
    }

    /**
     * 创建 AsyncTaskSubHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncTaskSubHandlerStrategyFactory
     */
    @Bean
    public AsyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory(
        ObjectProvider<AsyncTaskSubHandler> executorProvider) {
        AsyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory =
            new AsyncTaskSubHandlerStrategyFactory(executorProvider);
        log.info("AsyncTaskSubHandlerStrategyFactory={}", asyncTaskSubHandlerStrategyFactory);
        return asyncTaskSubHandlerStrategyFactory;
    }

    /**
     * 创建 AsyncTaskStateSupport.
     *
     * @param property            property
     * @param asyncTaskRepository asyncTaskRepository
     * @param asyncTaskService    asyncTaskService
     * @param asyncTaskSubService asyncTaskSubService
     * @param transactionTemplate transactionTemplate
     * @return AsyncTaskStateSupport
     */
    @Bean
    public AsyncTaskStateSupport asyncTaskStateSupport(
        AsyncTaskProperty property,
        AsyncTaskRepository asyncTaskRepository,
        AsyncTaskService asyncTaskService,
        AsyncTaskSubRepository asyncTaskSubRepository,
        AsyncTaskSubService asyncTaskSubService,
        TransactionTemplate transactionTemplate) {
        AsyncTaskStateSupport asyncTaskStateSupport = new AsyncTaskStateSupport(
            property, asyncTaskRepository, asyncTaskService, asyncTaskSubRepository,
            asyncTaskSubService, transactionTemplate);
        return asyncTaskStateSupport;
    }

    /**
     * 创建 AsyncTaskExecuteHandlerMain.
     *
     * @param asyncTaskHandlerStrategyFactor asyncTaskHandlerStrategyFactor
     * @param asyncTaskStateSupport          asyncTaskStateSupport
     * @param asyncTaskService               asyncTaskService
     * @return AsyncTaskExecuteHandlerMain
     */
    @Bean
    public AsyncTaskExecuteHandlerMain asyncTaskExecuteHandlerMain(
        AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactor,
        AsyncTaskStateSupport asyncTaskStateSupport,
        AsyncTaskService asyncTaskService) {
        AsyncTaskExecuteHandlerMain asyncTaskExecuteHandlerMain = new AsyncTaskExecuteHandlerMain(
            asyncTaskHandlerStrategyFactor, asyncTaskStateSupport, asyncTaskService);
        log.info("AsyncTaskExecuteHandlerMain={}", asyncTaskExecuteHandlerMain);
        return asyncTaskExecuteHandlerMain;
    }

    /**
     * 创建 AsyncTaskExecuteHandlerGroup.
     *
     * @param asyncTaskHandlerStrategyFactory    asyncTaskHandlerStrategyFactory
     * @param asyncTaskStateSupport              asyncTaskStateSupport
     * @param asyncTaskService                   asyncTaskService
     * @param asyncTaskSubService                asyncTaskSubService
     * @param asyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory
     * @return AsyncTaskExecuteHandlerGroup
     */
    @Bean
    public AsyncTaskExecuteHandlerGroup asyncTaskExecuteHandlerGroup(
        AsyncTaskHandlerStrategyFactory asyncTaskHandlerStrategyFactory,
        AsyncTaskStateSupport asyncTaskStateSupport,
        AsyncTaskService asyncTaskService,
        AsyncTaskSubService asyncTaskSubService,
        AsyncTaskSubHandlerStrategyFactory asyncTaskSubHandlerStrategyFactory) {
        AsyncTaskExecuteHandlerGroup asyncTaskExecuteHandlerGroup = new AsyncTaskExecuteHandlerGroup(
            asyncTaskHandlerStrategyFactory, asyncTaskStateSupport, asyncTaskService,
            asyncTaskSubService, asyncTaskSubHandlerStrategyFactory, subThreadPoolExecutor);
        log.info("AsyncTaskExecuteHandlerGroup={}", asyncTaskExecuteHandlerGroup);
        return asyncTaskExecuteHandlerGroup;
    }

    /**
     * 创建 AsyncTaskBizProcessor.
     *
     * @param property                     property
     * @param asyncTaskService             asyncTaskService
     * @param asyncTaskSubService          asyncTaskSubService
     * @param asyncTaskExecuteHandlerMain  asyncTaskExecuteHandlerMain
     * @param asyncTaskExecuteHandlerGroup asyncTaskExecuteHandlerGroup
     * @return AsyncTaskBizProcessor
     */
    @Bean
    public AsyncTaskBizProcessor asyncTaskBizProcessor(
        AsyncTaskProperty property,
        AsyncTaskService asyncTaskService,
        AsyncTaskSubService asyncTaskSubService,
        AsyncTaskStateSupport asyncTaskStateSupport,
        AsyncTaskExecuteHandler asyncTaskExecuteHandlerMain,
        AsyncTaskExecuteHandler asyncTaskExecuteHandlerGroup) {
        AsyncTaskBizProcessor asyncTaskBizProcessor = new AsyncTaskBizProcessor(
            property, asyncTaskService, asyncTaskSubService, asyncTaskStateSupport,
            asyncTaskExecuteHandlerMain, asyncTaskExecuteHandlerGroup, threadPoolExecutor);
        return asyncTaskBizProcessor;
    }

    /**
     * 创建 AsyncTaskJobHandlerRetry.
     *
     * @param property              property
     * @param asyncTaskService      asyncTaskService
     * @param asyncTaskBizProcessor asyncTaskBizProcessor
     * @param asyncTaskStateSupport asyncTaskStateSupport
     * @return AsyncTaskJobHandlerRetry
     */
    @Bean
    public AsyncTaskJobHandlerRetry asyncTaskJobHandlerRetry(
        AsyncTaskProperty property,
        AsyncTaskService asyncTaskService,
        AsyncTaskBizProcessor asyncTaskBizProcessor,
        AsyncTaskStateSupport asyncTaskStateSupport) {
        AsyncTaskJobHandlerRetry asyncTaskJobHandlerRetry = new AsyncTaskJobHandlerRetry(
            property, asyncTaskService, asyncTaskBizProcessor, asyncTaskStateSupport);
        return asyncTaskJobHandlerRetry;
    }

    /**
     * 创建 AsyncTaskJobHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncTaskJobHandlerStrategyFactory
     */
    @Bean
    public AsyncTaskJobHandlerStrategyFactory asyncTaskJobHandlerStrategyFactory(
        ObjectProvider<AsyncTaskJobHandler> executorProvider) {
        AsyncTaskJobHandlerStrategyFactory asyncTaskJobHandlerStrategyFactory = new AsyncTaskJobHandlerStrategyFactory(executorProvider);
        log.info("AsyncTaskJobHandlerStrategyFactory={}", asyncTaskJobHandlerStrategyFactory);
        return asyncTaskJobHandlerStrategyFactory;
    }

    /**
     * 创建 AsyncTaskJob.
     *
     * @param asyncTaskJobHandlerStrategyFactory asyncTaskJobHandlerStrategyFactory
     * @return AsyncTaskJob
     */
    @ConditionalOnProperty(prefix = "async-task.job", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public AsyncTaskJob asyncTaskJob(
        AsyncTaskJobHandlerStrategyFactory asyncTaskJobHandlerStrategyFactory) {
        AsyncTaskJob asyncTaskJob = new AsyncTaskJob(asyncTaskJobHandlerStrategyFactory);
        log.info("AsyncTaskJob={}", asyncTaskJob);
        return asyncTaskJob;
    }
}
