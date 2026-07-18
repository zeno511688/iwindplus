/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.gateway.server.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.listener.Listener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.constant.CommonConstant.GatewayRouteConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.gateway.server.client.MgtClient;
import com.iwindplus.gateway.server.domain.converter.RouteDefinitionConverter;
import com.iwindplus.gateway.server.support.RouteService;
import com.iwindplus.mgt.domain.vo.system.ServerRouteDefinitionVO;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 路由监听.
 *
 * @author zengdegui
 * @since 2024-8-26
 */
@Slf4j
@Component
public class RouteListener {

    @Resource
    private NacosConfigManager nacosConfigManager;

    @Resource
    private DtpExecutor routeTaskExecutor;

    @Resource
    private MgtClient mgtClient;

    @Resource
    private RouteService routeService;

    @Resource
    private ApplicationContext applicationContext;

    private static final AtomicReference<String> LAST_DIGEST = new AtomicReference<>();

    /**
     * 应用启动完成后： 1. 非阻塞读取 Nacos； 2. 若 Nacos 为空，则非阻塞读取 DB 并推送回 Nacos； 3. 加载路由到本地； 4. 注册 Nacos 监听器。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        this.fetchRoutes()
            .flatMap(this::loadRouteIfChanged)
            .then(registerNacosListener())
            .subscribe(
                v -> log.info("路由初始化成功"),
                ex -> {
                    log.warn("路由初始化失败，关闭应用", ex);
                    SpringApplication.exit(applicationContext);
                });
    }

    /**
     * 获取路由：先读 Nacos，为空则读 DB 并回写 Nacos
     */
    private Mono<List<ServerRouteDefinitionVO>> fetchRoutes() {
        return getConfigFromNacos()
            .filter(CharSequenceUtil::isNotBlank)
            .map(this::parse)
            .switchIfEmpty(
                mgtClient.listRouteDefinition()
                    .defaultIfEmpty(Collections.emptyList())
                    .flatMap(this::publishToNacos)
            );
    }

    /**
     * 读取 Nacos 配置
     */
    private Mono<String> getConfigFromNacos() {
        return Mono.fromCallable(() ->
            nacosConfigManager.getConfigService()
                .getConfig(
                    GatewayRouteConstant.GATEWAY_ROUTE_FILE_NAME,
                    GatewayRouteConstant.GATEWAY_GROUP,
                    NumberConstant.NUMBER_THREE_THOUSAND)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 将 DB 路由推送到 Nacos
     */
    private Mono<List<ServerRouteDefinitionVO>> publishToNacos(List<ServerRouteDefinitionVO> routes) {
        return Mono.fromCallable(() -> {
            String json = JacksonUtil.toJsonStr(routes);
            nacosConfigManager.getConfigService()
                .publishConfig(
                    GatewayRouteConstant.GATEWAY_ROUTE_FILE_NAME,
                    GatewayRouteConstant.GATEWAY_GROUP,
                    json,
                    ConfigType.JSON.getType());
            return routes;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 注册 Nacos 监听器（仅注册一次）
     */
    private Mono<Void> registerNacosListener() {
        return Mono.fromCallable(() -> {
            nacosConfigManager.getConfigService()
                .addListener(
                    GatewayRouteConstant.GATEWAY_ROUTE_FILE_NAME,
                    GatewayRouteConstant.GATEWAY_GROUP,
                    buildListener());
            return Mono.empty();
        }).then();
    }

    /**
     * 构造 Nacos 监听器
     */
    private Listener buildListener() {
        return new Listener() {
            @Override
            public Executor getExecutor() {
                return routeTaskExecutor;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                List<ServerRouteDefinitionVO> routes = parse(configInfo);
                loadRouteIfChanged(routes);
            }
        };
    }

    private List<ServerRouteDefinitionVO> parse(String cfg) {
        if (CharSequenceUtil.isBlank(cfg)) {
            return Collections.emptyList();
        }

        List<ServerRouteDefinitionVO> list = JacksonUtil.parseObject(cfg, new TypeReference<>() {
        });

        if (list == null) {
            return Collections.emptyList();
        }

        return list.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                ServerRouteDefinitionVO::getId,
                Function.identity(),
                (a, b) -> a))
            .values()
            .stream().toList();
    }

    /**
     * 仅在路由内容变化时加载
     */
    private Mono<Void> loadRouteIfChanged(List<ServerRouteDefinitionVO> routes) {
        if (CollUtil.isEmpty(routes)) {
            log.info("路由列表为空，跳过加载");
            return Mono.empty();
        }

        List<ServerRouteDefinitionVO> sorted = routes.stream()
            .sorted(Comparator.comparing(ServerRouteDefinitionVO::getOrder))
            .toList();

        String json = JacksonUtil.toJsonStr(sorted);
        String digest = CryptoUtil.encryptBySm3(json);

        LAST_DIGEST.updateAndGet(last -> {
            if (last == null || !Objects.equals(last, digest)) {
                List<RouteDefinition> entities = sorted.stream()
                    .map(RouteDefinitionConverter::convertToRouteDefinition)
                    .filter(Objects::nonNull)
                    .toList();
                routeService.loadRoute(entities);
                log.info("加载路由成功");
                return digest;
            }

            log.info("路由配置未变化，跳过加载");
            return last;
        });

        return Mono.empty();
    }
}