/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.support.observation;

import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import io.micrometer.observation.Observation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Disruptor观察上下文.
 *
 * @author zengdegui
 * @since 2026/05/08 15:34
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DisruptorObservationContext extends Observation.Context {

    /**
     * Handler名称. 例如： OrderHandler AccountHandler
     */
    private String handler;

    /**
     * 序列号.
     */
    private String sequence;

    /**
     * 数据来源. Kafka RabbitMQ HTTP Timer
     */
    private String source;

    /**
     * 数据去向. MySQL Redis ES Business
     */
    private String destination;

    @Override
    public void setError(Throwable error) {
        super.setError(error);
        put(ObservationConstant.EXCEPTION, error);
    }
}
