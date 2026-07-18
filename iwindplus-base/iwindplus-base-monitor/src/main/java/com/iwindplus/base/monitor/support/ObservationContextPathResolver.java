/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.monitor.domain.property.MonitorProperty;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationRegistry;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.util.AntPathMatcher;

/**
 * ObservationRegistry 自定义配置.
 *
 * @author zengdegui
 * @since 2025/09/24
 */
@Slf4j
public final class ObservationContextPathResolver {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 默认忽略的 URI
     */
    private static final List<String> DEFAULT_IGNORED_APIS = List.of(
        "/actuator/**"
    );

    public ObservationRegistryCustomizer<ObservationRegistry> customizer(
        MonitorProperty property) {

        Set<String> ignoredApis = new HashSet<>(DEFAULT_IGNORED_APIS);

        if (CollUtil.isNotEmpty(property.getIgnoredApi())) {
            ignoredApis.addAll(property.getIgnoredApi());
        }

        log.info("[Observation] ignored uri={}", ignoredApis);

        return registry -> registry.observationConfig()
            .observationPredicate((name, context) -> {
                String path = extractPath(context);

                if (CharSequenceUtil.isBlank(path)) {
                    return true;
                }

                return ignoredApis.stream()
                    .noneMatch(pattern -> PATH_MATCHER.match(pattern, path));
            });
    }

    /**
     * 提取 URI Path
     */
    private String extractPath(Context context) {
        if (context instanceof ServerRequestObservationContext serverContext) {
            return serverContext.getCarrier().getRequestURI();
        }

        if (context instanceof ClientRequestObservationContext clientContext) {
            URI uri = clientContext.getCarrier().getURI();
            return uri == null ? null : uri.getPath();
        }

        return null;
    }

}