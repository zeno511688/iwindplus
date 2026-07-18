/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.support;

import java.util.List;
import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * 路由接口类.
 *
 * @author zengdegui
 * @since 2020/5/29
 */
public interface RouteService {

    /**
     * 加载路由.
     *
     * @param entities 对象集合
     */
    void loadRoute(List<RouteDefinition> entities);
}
