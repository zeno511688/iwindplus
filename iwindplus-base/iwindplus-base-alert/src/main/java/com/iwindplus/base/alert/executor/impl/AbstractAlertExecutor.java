/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.executor.impl;

import com.iwindplus.base.alert.domain.property.AlertProperty;
import com.iwindplus.base.alert.executor.AlertExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象告警执行器接口.
 *
 * @author zengdegui
 * @since 2026/03/03 19:43
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAlertExecutor implements AlertExecutor {

    protected final AlertProperty property;
}
