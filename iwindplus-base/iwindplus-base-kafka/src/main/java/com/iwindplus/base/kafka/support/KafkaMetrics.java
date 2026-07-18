/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 监控指标收集.
 *
 * @author zengdegui
 * @since 2024/12/26
 */
@Slf4j
public record KafkaMetrics(MeterRegistry meterRegistry) {

    /**
     * 记录重试发送成功.
     *
     * @param cluster 集群
     * @param topic   Topic
     */
    public void recordRetrySendSuccess(String cluster, String topic) {
        try {
            Counter.builder(KafkaConstant.MetricName.RETRY_SEND_SUCCESS)
                .tag("cluster", cluster)
                .tag("topic", topic)
                .register(meterRegistry)
                .increment();
        } catch (Exception e) {
            log.warn("Record retry send success metric failed", e);
        }
    }

    /**
     * 记录重试发送失败.
     *
     * @param cluster 集群
     * @param topic   Topic
     */
    public void recordRetrySendFailure(String cluster, String topic) {
        try {
            Counter.builder(KafkaConstant.MetricName.RETRY_SEND_FAILURE)
                .tag("cluster", cluster)
                .tag("topic", topic)
                .register(meterRegistry)
                .increment();
        } catch (Exception e) {
            log.warn("Record retry send failure metric failed", e);
        }
    }

    /**
     * 记录DLQ发送成功.
     *
     * @param cluster 集群
     * @param topic   Topic
     */
    public void recordDlqSendSuccess(String cluster, String topic) {
        try {
            Counter.builder(KafkaConstant.MetricName.DLQ_SEND_SUCCESS)
                .tag("cluster", cluster)
                .tag("topic", topic)
                .register(meterRegistry)
                .increment();
        } catch (Exception e) {
            log.warn("Record DLQ send success metric failed", e);
        }
    }

    /**
     * 记录DLQ发送失败.
     *
     * @param cluster 集群
     * @param topic   Topic
     */
    public void recordDlqSendFailure(String cluster, String topic) {
        try {
            Counter.builder(KafkaConstant.MetricName.DLQ_SEND_FAILURE)
                .tag("cluster", cluster)
                .tag("topic", topic)
                .register(meterRegistry)
                .increment();
        } catch (Exception e) {
            log.warn("Record DLQ send failure metric failed", e);
        }
    }

    /**
     * 记录重试发送耗时.
     *
     * @param cluster  集群
     * @param topic    Topic
     * @param duration 耗时（毫秒）
     */
    public void recordRetrySendDuration(String cluster, String topic, long duration) {
        try {
            Timer.builder(KafkaConstant.MetricName.RETRY_SEND_DURATION)
                .tag("cluster", cluster)
                .tag("topic", topic)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Record retry send duration metric failed", e);
        }
    }

    /**
     * 记录DLQ发送耗时.
     *
     * @param cluster  集群
     * @param topic    Topic
     * @param duration 耗时（毫秒）
     */
    public void recordDlqSendDuration(String cluster, String topic, long duration) {
        try {
            Timer.builder(KafkaConstant.MetricName.DLQ_SEND_DURATION)
                .tag("cluster", cluster)
                .tag("topic", topic)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Record DLQ send duration metric failed", e);
        }
    }

    /**
     * 创建计时器采样.
     *
     * @return Sample
     */
    public Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止计时器并记录.
     *
     * @param sample     采样器
     * @param cluster    集群
     * @param topic      Topic
     * @param metricName 指标名称
     */
    public void stopTimer(Sample sample, String cluster, String topic, String metricName) {
        try {
            if (sample != null && CharSequenceUtil.isNotBlank(metricName)) {
                sample.stop(Timer.builder(metricName)
                    .tag("cluster", cluster)
                    .tag("topic", topic)
                    .register(meterRegistry));
            }
        } catch (Exception e) {
            log.warn("Stop timer and record metric failed", e);
        }
    }
}
