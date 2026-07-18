/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support.observation;

import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import io.micrometer.observation.Observation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Rabbit接收上下文.
 *
 * @author zengdegui
 * @since 2026/05/08 15:34
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RabbitReceiverObservationContext extends Observation.Context {

    /**
     * 集群名称.
     */
    private String cluster;

    /**
     * 队列名称.
     */
    private String[] queues;

    /**
     * 消费组
     *
     * @return String
     */
    private String group;

    @Override
    public void setError(Throwable error) {
        super.setError(error);
        put(ObservationConstant.EXCEPTION, error);
    }
}
