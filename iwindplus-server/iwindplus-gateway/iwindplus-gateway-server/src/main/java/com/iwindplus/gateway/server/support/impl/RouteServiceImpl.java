/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.support.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.gateway.server.support.RouteService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 路由接口实现类.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteDefinitionRepository routeDefinitionRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    public void loadRoute(List<RouteDefinition> entities) {
        log.info("[Route] 开始增量更新路由, 目标路由数={}", CollUtil.size(entities));

        // 1. 当前路由快照
        Map<String, RouteDefinition> currentMap = routeDefinitionRepository.getRouteDefinitions()
            .collectMap(RouteDefinition::getId)
            .blockOptional()
            .orElse(Collections.emptyMap());
        log.info("[Route] 当前已存在路由, ids={}", currentMap.keySet());

        // 2. 目标路由
        Map<String, RouteDefinition> targetMap = entities.stream().filter(Objects::nonNull)
            .collect(Collectors.toMap(RouteDefinition::getId, Function.identity()));

        // 3. 需要删除
        List<String> toDelete = currentMap.keySet().stream()
            .filter(Objects::nonNull)
            .filter(id -> !targetMap.containsKey(id))
            .collect(Collectors.toList());

        // 4. 需要新增/更新
        List<RouteDefinition> toSave = targetMap.values().stream()
            .filter(Objects::nonNull)
            .filter(def -> !def.equals(currentMap.get(def.getId())))
            .collect(Collectors.toList());

        // 5. 批量执行（事务化）
        try {
            // 5.1 删除
            this.updateRoute(toDelete, toSave);

            // 5.3 发布刷新事件
            publisher.publishEvent(new RefreshRoutesEvent(this));
            log.info("[Route] 增量更新完成");
        } catch (Exception e) {
            log.error("[Route] 路由增量更新失败，开始回退", e);
            rollback(currentMap);
        }
    }

    private void updateRoute(List<String> toDelete, List<RouteDefinition> toSave) {
        if (CollUtil.isNotEmpty(toDelete)) {
            log.info("[Route] 待删除路由, ids={}", toDelete);
            Flux.fromIterable(toDelete)
                .flatMap(id -> routeDefinitionRepository.delete(Mono.just(id)))
                .collectList()
                .block();
        }

        // 5.2 保存
        if (CollUtil.isNotEmpty(toSave)) {
            log.info("[Route] 待新增/更新路由, ids={}",
                toSave.stream().filter(Objects::nonNull).map(RouteDefinition::getId).collect(Collectors.toList()));
            Flux.fromIterable(toSave)
                .flatMap(def -> routeDefinitionRepository.save(Mono.just(def)))
                .collectList()
                .block();
        }
    }

    /**
     * 回滚：把整批原始路由重新写回仓库.
     *
     * @param original 原始路由
     */
    private void rollback(Map<String, RouteDefinition> original) {
        try {
            // 1. 清空现有
            routeDefinitionRepository.getRouteDefinitions()
                .map(RouteDefinition::getId)
                .flatMap(id -> routeDefinitionRepository.delete(Mono.just(id)))
                .collectList()
                .block();

            // 2. 重新写入原始路由
            Flux.fromIterable(original.values())
                .flatMap(def -> routeDefinitionRepository.save(Mono.just(def)))
                .collectList()
                .block();

            // 3. 再次发布刷新事件
            publisher.publishEvent(new RefreshRoutesEvent(this));
            log.info("[Route] 回滚完成，已恢复至更新前状态");
        } catch (Exception rollbackEx) {
            // 如果回滚也失败，只能记录致命日志
            log.error("[Route] 回滚失败，路由数据可能出现不一致，请人工介入", rollbackEx);
        }
    }
}