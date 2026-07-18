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
 * Rocket接收转换器.
 *
 * @author zengdegui
 * @since 2026/05/08 15:51
 */
public class ClusterRocketReceiverObservationConvention implements ObservationConvention<RocketReceiverObservationContext> {

    @Override
    public boolean supportsContext(Context context) {
        return context instanceof RocketReceiverObservationContext;
    }

    @Override
    public String getName() {
        return RocketConstant.ROCKET_CONSUMER;
    }

    @Override
    public String getContextualName(RocketReceiverObservationContext context) {
        return "rocketmq consumer";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RocketReceiverObservationContext context) {
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

        if (CharSequenceUtil.isNotBlank(context.getGroup())) {
            list.add(KeyValue.of(
                RocketConstant.GROUP,
                context.getGroup()
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
    public KeyValues getHighCardinalityKeyValues(RocketReceiverObservationContext context) {
        String error = context.getError() == null
            ? ObservationConstant.NONE
            : context.getError().getClass().getSimpleName();

        return KeyValues.of(
            ObservationConstant.EXCEPTION, error
        );
    }
}
