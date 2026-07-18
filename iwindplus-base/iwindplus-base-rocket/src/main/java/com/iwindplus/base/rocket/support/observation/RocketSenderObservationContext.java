/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support.observation;

import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import io.micrometer.observation.Observation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Rocket发送上下文.
 *
 * @author zengdegui
 * @since 2026/05/08 15:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RocketSenderObservationContext extends Observation.Context {

    /**
     * 集群名称.
     */
    private String cluster;

    /**
     * 主题名称.
     */
    private String topic;

    /**
     * 标签.
     */
    private String tag;

    @Override
    public void setError(Throwable error) {
        super.setError(error);
        put(ObservationConstant.EXCEPTION, error);
    }
}
