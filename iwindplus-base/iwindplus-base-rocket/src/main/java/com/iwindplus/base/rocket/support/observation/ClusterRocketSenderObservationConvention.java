/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support.observation;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import com.iwindplus.base.rocket.domain.constant.RocketConstant;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import java.util.ArrayList;
import java.util.List;

/**
 * Rocket监控转换器.
 *
 * @author zengdegui
 * @since 2026/05/08 15:22
 */
public class ClusterRocketSenderObservationConvention implements ObservationConvention<RocketSenderObservationContext> {

    @Override
    public boolean supportsContext(Context context) {
        return context instanceof RocketSenderObservationContext;
    }

    @Override
    public String getName() {
        return RocketConstant.ROCKET_PRODUCER;
    }

    @Override
    public String getContextualName(RocketSenderObservationContext context) {
        return "rocketmq producer";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RocketSenderObservationContext context) {
        List<KeyValue> list = new ArrayList<>(10);
        list.add(KeyValue.of(
            RocketConstant.CLUSTER,
            context.getCluster()
        ));

        if (CharSequenceUtil.isNotBlank(context.getTopic())) {
            list.add(KeyValue.of(
                RocketConstant.TOPIC,
                context.getTopic()
            ));
        }

        if (CharSequenceUtil.isNotBlank(context.getTag())) {
            list.add(KeyValue.of(
                RocketConstant.TAG,
                context.getTag()
            ));
        }

        return KeyValues.of(list);
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RocketSenderObservationContext context) {
        String error = context.getError() == null
            ? ObservationConstant.NONE
            : context.getError().getClass().getSimpleName();

        return KeyValues.of(
            ObservationConstant.EXCEPTION, error
        );
    }
}
