/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.swagger.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.vo.AppApiVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.swagger.domain.property.SwaggerProperty;
import com.iwindplus.base.swagger.service.SwaggerService;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 应用程序就绪监听器.
 *
 * @author zengdegui
 * @since 2023/07/30 22:31
 */
@Slf4j
public class SwaggerListener {

    @Resource
    private SwaggerProperty property;

    @Resource
    private SwaggerService swaggerService;

    @Resource
    private HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    @Resource
    private ObjectProvider<DiscoveryClient> discoveryClientProvider;

    /**
     * 应用启动完成后执行.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {

        loadData().subscribe();
    }

    /**
     * 加载数据.
     * <p>
     * 等待服务注册中心可发现目标服务后， 再进行 API 注册.
     *
     * @return Mono<Void>
     */
    private Mono<Void> loadData() {
        final SwaggerProperty.ServerApiConfig cfg = property.getServerApi();

        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            log.warn("服务API注册已禁用，跳过注册流程");

            return Mono.empty();
        }

        final AppApiVO entity = swaggerService.getServerInfo();
        if (Objects.isNull(entity)) {
            log.warn("服务API注册跳过，未获取到服务信息");

            return Mono.empty();
        }

        return Flux.interval(
                Duration.ZERO,
                Duration.ofSeconds(cfg.getRetryInterval())
            )
            .take(cfg.getMaxRetry())
            .doOnNext(i ->
                log.info(
                    "服务API注册开始，第 {} 次",
                    i + 1
                )
            )
            .concatMap(i ->
                waitForDiscovery()
                    .flatMap(discoverySuccess -> {
                        if (Boolean.FALSE.equals(discoverySuccess)) {
                            return Mono.just(false);
                        }

                        return registerApiReactive(entity);
                    })
            )
            .filter(Boolean::booleanValue)
            .next()
            .doOnNext(i ->
                log.info(
                    "{} 服务API注册完成",
                    getApplication()
                )
            )
            .switchIfEmpty(Mono.defer(() -> {
                log.error(
                    "{} 服务API注册失败，已达最大重试次数={}",
                    getApplication(),
                    cfg.getMaxRetry()
                );

                return Mono.empty();
            }))
            .then();
    }

    /**
     * 等待服务发现.
     *
     * @return Mono<Boolean>
     */
    private Mono<Boolean> waitForDiscovery() {
        return Mono.fromSupplier(() -> {
            final DiscoveryClient discoveryClient =
                discoveryClientProvider.getIfAvailable();
            if (Objects.isNull(discoveryClient)) {
                log.warn("DiscoveryClient 不存在");

                return false;
            }

            final String serviceName = property.getServerApi().getServiceName();

            final List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            final boolean success = CollUtil.isNotEmpty(instances);
            if (success) {
                log.info(
                    "发现服务 {} instances={}",
                    serviceName,
                    instances.size()
                );

            } else {
                log.warn(
                    "未发现服务 {}",
                    serviceName
                );
            }

            return success;
        });
    }

    /**
     * 异步注册服务API.
     *
     * @return Mono<Boolean>
     */
    private Mono<Boolean> registerApiReactive(AppApiVO entity) {
        return Mono.fromCompletionStage(() ->
                httpClientExecutorStrategyFactory
                    .getHttpClientExecutor(
                        HttpClientTypeEnum.WEB_CLIENT
                    )
                    .postAsync(
                        property.getServerApi().getUrl(),
                        entity,
                        null,
                        new TypeReference<ResultVO<Boolean>>() {
                        }
                    )
            )
            .map(result -> {
                result.errorThrow();

                final Boolean success = result.getBizData();
                if (Boolean.TRUE.equals(success)) {
                    log.info(
                        "{} 服务API注册成功",
                        getApplication()
                    );
                }

                return Boolean.TRUE.equals(success);
            })
            .onErrorResume(ex -> {
                log.error(
                    "{} 服务API注册异常",
                    getApplication(),
                    ex
                );

                return Mono.just(false);
            });
    }

    /**
     * 获取应用名称.
     *
     * @return applicationName
     */
    private String getApplication() {
        return SpringUtil.getApplicationName();
    }
}