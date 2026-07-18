/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.support.observation;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * HTTP Client Observation Convention。
 *
 * <p>
 * 该类是<strong>指标与 Trace 语义的唯一出口</strong>：
 * <ul>
 *     <li>定义 metric 名称</li>
 *     <li>定义低 / 高基数标签</li>
 *     <li>保证 HTTP Client / Feign / Gateway 语义一致</li>
 * </ul>
 * </p>
 *
 * @author zengdegui
 * @since 2026/01/18
 */
public class HttpClientObservationConvention
    implements ObservationConvention<HttpClientObservationContext> {

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof HttpClientObservationContext;
    }

    @Override
    public String getName() {
        return ObservationConstant.HTTP_OBSERVATION_NAME;
    }

    /**
     * 低基数标签（用于聚合统计）。
     */
    @Override
    public KeyValues getLowCardinalityKeyValues(
        HttpClientObservationContext context) {

        String status = context.getStatus() == null
            ? ObservationConstant.IO_ERROR
            : String.valueOf(context.getStatus());

        String outcome = context.getError() == null
            ? ObservationConstant.OUTCOME_SUCCESS
            : ObservationConstant.OUTCOME_ERROR;

        return KeyValues.of(
            ObservationConstant.APPLICATION, SpringUtil.getApplicationName(),
            ObservationConstant.CLIENT_NAME, context.getClient(),
            ObservationConstant.HTTP_METHOD, context.getMethod(),
            ObservationConstant.HTTP_STATUS, status,
            ObservationConstant.OUTCOME, outcome
        );
    }

    /**
     * 高基数标签（仅用于排障）。
     */
    @Override
    public KeyValues getHighCardinalityKeyValues(
        HttpClientObservationContext context) {

        String error = context.getError() == null
            ? ObservationConstant.NONE
            : context.getError().getClass().getSimpleName();

        return KeyValues.of(
            ObservationConstant.HTTP_URL, context.getUrl(),
            ObservationConstant.EXCEPTION, error
        );
    }
}
