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
import com.iwindplus.base.async.task.factory.AsyncTaskHandlerFactory;
import com.iwindplus.base.async.task.factory.AsyncTaskJobHandlerFactory;
import com.iwindplus.base.async.task.factory.AsyncTaskSubHandlerFactory;
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
import com.iwindplus.base.async.task.support.impl.GroupAsyncTaskExecuteHandler;
import com.iwindplus.base.async.task.support.impl.MainAsyncTaskExecuteHandler;
import com.iwindplus.base.async.task.support.impl.RetryAsyncTaskJobHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
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
     * @param asyncTaskService           asyncTaskService
     * @param asyncTaskSubService        asyncTaskSubService
     * @param asyncTaskBizProcessor      asyncTaskBizProcessor
     * @param asyncTaskHandlerFactory    asyncTaskHandlerFactory
     * @param asyncTaskSubHandlerFactory asyncTaskSubHandlerFactory
     * @return AsyncTaskExecutor
     */
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor(
        AsyncTaskService asyncTaskService,
        AsyncTaskSubService asyncTaskSubService,
        AsyncTaskBizProcessor asyncTaskBizProcessor,
        AsyncTaskHandlerFactory asyncTaskHandlerFactory,
        AsyncTaskSubHandlerFactory asyncTaskSubHandlerFactory) {
        AsyncTaskExecutor asyncTaskExecutor = new AsyncTaskExecutorImpl(
            asyncTaskService, asyncTaskSubService, asyncTaskBizProcessor,
            asyncTaskHandlerFactory, asyncTaskSubHandlerFactory);
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
     * 创建 AsyncTaskHandlerFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncTaskHandlerFactory
     */
    @Bean
    public AsyncTaskHandlerFactory asyncTaskHandlerFactory(
        ObjectProvider<AsyncTaskHandler> executorProvider) {
        AsyncTaskHandlerFactory asyncTaskHandlerFactory =
            new AsyncTaskHandlerFactory(executorProvider);
        log.info("AsyncTaskHandlerFactory={}", asyncTaskHandlerFactory);
        return asyncTaskHandlerFactory;
    }

    /**
     * 创建 AsyncTaskSubHandlerFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncTaskSubHandlerFactory
     */
    @Bean
    public AsyncTaskSubHandlerFactory asyncTaskSubHandlerFactory(
        ObjectProvider<AsyncTaskSubHandler> executorProvider) {
        AsyncTaskSubHandlerFactory asyncTaskSubHandlerFactory =
            new AsyncTaskSubHandlerFactory(executorProvider);
        log.info("AsyncTaskSubHandlerFactory={}", asyncTaskSubHandlerFactory);
        return asyncTaskSubHandlerFactory;
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
     * 创建 MainAsyncTaskExecuteHandler.
     *
     * @param asyncTaskHandlerFactor asyncTaskHandlerFactor
     * @param asyncTaskStateSupport  asyncTaskStateSupport
     * @param asyncTaskService       asyncTaskService
     * @return MainAsyncTaskExecuteHandler
     */
    @Bean
    public AsyncTaskExecuteHandler mainAsyncTaskExecuteHandler(
        AsyncTaskHandlerFactory asyncTaskHandlerFactor,
        AsyncTaskStateSupport asyncTaskStateSupport,
        AsyncTaskService asyncTaskService) {
        AsyncTaskExecuteHandler mainAsyncTaskExecuteHandler = new MainAsyncTaskExecuteHandler(
            asyncTaskHandlerFactor, asyncTaskStateSupport, asyncTaskService);
        log.info("MainAsyncTaskExecuteHandler={}", mainAsyncTaskExecuteHandler);
        return mainAsyncTaskExecuteHandler;
    }

    /**
     * 创建 GroupAsyncTaskExecuteHandler.
     *
     * @param asyncTaskHandlerFactory    asyncTaskHandlerFactory
     * @param asyncTaskStateSupport      asyncTaskStateSupport
     * @param asyncTaskService           asyncTaskService
     * @param asyncTaskSubService        asyncTaskSubService
     * @param asyncTaskSubHandlerFactory asyncTaskSubHandlerFactory
     * @return GroupAsyncTaskExecuteHandler
     */
    @Bean
    public AsyncTaskExecuteHandler groupAsyncTaskExecuteHandler(
        AsyncTaskHandlerFactory asyncTaskHandlerFactory,
        AsyncTaskStateSupport asyncTaskStateSupport,
        AsyncTaskService asyncTaskService,
        AsyncTaskSubService asyncTaskSubService,
        AsyncTaskSubHandlerFactory asyncTaskSubHandlerFactory) {
        AsyncTaskExecuteHandler groupAsyncTaskExecuteHandler = new GroupAsyncTaskExecuteHandler(
            asyncTaskHandlerFactory, asyncTaskStateSupport, asyncTaskService,
            asyncTaskSubService, asyncTaskSubHandlerFactory, subThreadPoolExecutor);
        log.info("GroupAsyncTaskExecuteHandler={}", groupAsyncTaskExecuteHandler);
        return groupAsyncTaskExecuteHandler;
    }

    /**
     * 创建 AsyncTaskBizProcessor.
     *
     * @param property                     property
     * @param asyncTaskService             asyncTaskService
     * @param asyncTaskSubService          asyncTaskSubService
     * @param mainAsyncTaskExecuteHandler  mainAsyncTaskExecuteHandler
     * @param groupAsyncTaskExecuteHandler groupAsyncTaskExecuteHandler
     * @return AsyncTaskBizProcessor
     */
    @Bean
    public AsyncTaskBizProcessor asyncTaskBizProcessor(
        AsyncTaskProperty property,
        AsyncTaskService asyncTaskService,
        AsyncTaskSubService asyncTaskSubService,
        AsyncTaskStateSupport asyncTaskStateSupport,
        @Qualifier("mainAsyncTaskExecuteHandler") AsyncTaskExecuteHandler mainAsyncTaskExecuteHandler,
        @Qualifier("groupAsyncTaskExecuteHandler") AsyncTaskExecuteHandler groupAsyncTaskExecuteHandler) {
        AsyncTaskBizProcessor asyncTaskBizProcessor = new AsyncTaskBizProcessor(
            property, asyncTaskService, asyncTaskSubService, asyncTaskStateSupport,
            mainAsyncTaskExecuteHandler, groupAsyncTaskExecuteHandler, threadPoolExecutor);
        return asyncTaskBizProcessor;
    }

    /**
     * 创建 RetryAsyncTaskJobHandler.
     *
     * @param property              property
     * @param asyncTaskService      asyncTaskService
     * @param asyncTaskBizProcessor asyncTaskBizProcessor
     * @param asyncTaskStateSupport asyncTaskStateSupport
     * @return RetryAsyncTaskJobHandler
     */
    @Bean
    public RetryAsyncTaskJobHandler retryAsyncTaskJobHandler(
        AsyncTaskProperty property,
        AsyncTaskService asyncTaskService,
        AsyncTaskBizProcessor asyncTaskBizProcessor,
        AsyncTaskStateSupport asyncTaskStateSupport) {
        RetryAsyncTaskJobHandler retryAsyncTaskJobHandler = new RetryAsyncTaskJobHandler(
            property, asyncTaskService, asyncTaskBizProcessor, asyncTaskStateSupport);
        return retryAsyncTaskJobHandler;
    }

    /**
     * 创建 AsyncTaskJobHandlerFactory.
     *
     * @param executorProvider 执行器提供者
     * @return AsyncTaskJobHandlerFactory
     */
    @Bean
    public AsyncTaskJobHandlerFactory asyncTaskJobHandlerFactory(
        ObjectProvider<AsyncTaskJobHandler> executorProvider) {
        AsyncTaskJobHandlerFactory asyncTaskJobHandlerFactory = new AsyncTaskJobHandlerFactory(executorProvider);
        log.info("AsyncTaskJobHandlerFactory={}", asyncTaskJobHandlerFactory);
        return asyncTaskJobHandlerFactory;
    }

    /**
     * 创建 AsyncTaskJob.
     *
     * @param asyncTaskJobHandlerFactory asyncTaskJobHandlerFactory
     * @return AsyncTaskJob
     */
    @ConditionalOnProperty(prefix = "async-task.job", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public AsyncTaskJob asyncTaskJob(
        AsyncTaskJobHandlerFactory asyncTaskJobHandlerFactory) {
        AsyncTaskJob asyncTaskJob = new AsyncTaskJob(asyncTaskJobHandlerFactory);
        log.info("AsyncTaskJob={}", asyncTaskJob);
        return asyncTaskJob;
    }
}
