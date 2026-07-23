/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030
 *
 */

package com.iwindplus.base.kafka.support.monitor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerInfoDTO;
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerKeyDTO;
import com.iwindplus.base.kafka.domain.dto.KafkaLagDTO;
import com.iwindplus.base.kafka.listener.KafkaMultiListenerRegistrar;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;


/**
 * Kafka消息堆积监控及动态扩缩容.
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaLagMonitor implements SmartLifecycle {

    private final KafkaMultiListenerRegistrar registrar;

    private final KafkaClusterManager clusterManager;

    private final ScheduledDtpExecutor scheduler;

    /**
     * lag缓存 key: cluster/group/topic
     */
    private final Cache<LagKey, List<KafkaLagDTO>> lagCache =
        Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build();

    /**
     * 扩缩容状态
     */
    private final Cache<ScaleKey, ScaleState> scaleCache =
        Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofHours(2))
                .build();

    private final AtomicBoolean started =
        new AtomicBoolean(false);

    private volatile boolean running;

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        if (scheduler == null
            || clusterManager.getProperty() == null
            || Boolean.FALSE.equals(
            clusterManager.getProperty().getEnabledScale())) {

            return;
        }

        running = true;

        scheduler.scheduleWithFixedDelay(
            this::monitor,
            30,
            30,
            TimeUnit.SECONDS
        );

        log.info("Kafka lag monitor started");
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * 查询topic lag.
     */
    public List<KafkaLagDTO> getLag(
        String cluster,
        String group,
        String topic) {

        return lagCache.getIfPresent(new LagKey(cluster, group, topic));
    }

    private void monitor() {
        if (!running) {
            return;
        }

        try {
            Map<KafkaConsumerKeyDTO, List<KafkaConsumerInfoDTO>> groups =
                registrar.groupByClusterAndGroup();
            groups.forEach(this::queryGroup);
        } catch (Exception e) {
            log.error(
                "Kafka lag monitor error",
                e
            );
        }
    }

    private void queryGroup(
        KafkaConsumerKeyDTO key,
        List<KafkaConsumerInfoDTO> consumers) {

        try {
            AdminClient admin = clusterManager.getAdmin(key.getCluster());
            Map<TopicPartition, OffsetAndMetadata> committed =
                admin.listConsumerGroupOffsets(key.getGroup())
                     .partitionsToOffsetAndMetadata()
                     .get(5, TimeUnit.SECONDS);
            if (committed.isEmpty()) {
                return;
            }

            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest =
                queryLatest(admin, committed.keySet());
            List<KafkaLagDTO> lags = buildLag(key, committed, latest);
            cacheLag(key, lags);
            checkScale(key, consumers, lags);
        } catch (Exception e) {
            log.error(
                "Kafka lag query failed cluster={},group={}",
                key.getCluster(),
                key.getGroup(),
                e
            );
        }
    }

    private Map<TopicPartition,
        ListOffsetsResult.ListOffsetsResultInfo> queryLatest(
        AdminClient admin,
        Set<TopicPartition> partitions)
        throws Exception {

        Map<TopicPartition, OffsetSpec> request =
            partitions.stream()
                      .collect(Collectors.toMap(
                          p -> p,
                          p -> OffsetSpec.latest()
                      ));

        return admin
            .listOffsets(request)
            .all()
            .get(5, TimeUnit.SECONDS);
    }

    private List<KafkaLagDTO> buildLag(
        KafkaConsumerKeyDTO key,
        Map<TopicPartition, OffsetAndMetadata> committed,
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest) {
        if (committed == null || committed.isEmpty()) {
            return Collections.emptyList();
        }

        List<KafkaLagDTO> result = new ArrayList<>(committed.size());
        committed.forEach((tp, offset) -> {
            if (offset == null) {
                return;
            }

            ListOffsetsResult.ListOffsetsResultInfo info = latest.get(tp);
            if (info == null) {
                return;
            }

            long current = offset.offset();
            long end = info.offset();
            result.add(
                KafkaLagDTO
                    .builder()
                    .cluster(key.getCluster())
                    .group(key.getGroup())
                    .topic(tp.topic())
                    .partition(tp.partition())
                    .currentOffset(current)
                    .endOffset(end)
                    .lag(Math.max(end - current, 0))
                    .build()
            );
        });

        return result;
    }

    private void cacheLag(
        KafkaConsumerKeyDTO key,
        List<KafkaLagDTO> lags) {

        lags.stream()
            .collect(Collectors.groupingBy(
                KafkaLagDTO::getTopic
            ))
            .forEach((topic, list) ->
                lagCache.put(
                    new LagKey(key.getCluster(), key.getGroup(), topic),
                    list
                )
            );
    }

    /**
     * 扩缩容判断.
     * <p>
     * topic维度
     */
    private void checkScale(
        KafkaConsumerKeyDTO key,
        List<KafkaConsumerInfoDTO> consumers,
        List<KafkaLagDTO> lags) {
        if (lags == null || lags.isEmpty()) {
            return;
        }

        Map<String, Long> topicLag =
            lags.stream()
                .collect(Collectors.groupingBy(
                    KafkaLagDTO::getTopic,
                    Collectors.summingLong(
                        KafkaLagDTO::getLag
                    )
                ));

        for (KafkaConsumerInfoDTO consumer : consumers) {
            Set<String> topics = consumer.getTopics();
            if (topics == null || topics.isEmpty()) {
                continue;
            }

            refreshCurrentConcurrency(consumer);
            int current = getCurrentConcurrency(consumer);
            for (String topic : topics) {
                Long lag = topicLag.get(topic);
                if (lag == null) {
                    continue;
                }

                ScaleKey scaleKey = new ScaleKey(key.getCluster(), key.getGroup(), topic);
                ScaleState state = scaleCache.get(scaleKey, k -> new ScaleState());
                long maxLag = clusterManager.getProperty().getMaxLag(consumer.getCluster());
                long minLag = clusterManager.getProperty().getMinLag(consumer.getCluster());
                if (lag > maxLag) {
                    state.resetIdle();
                    int count = state.incOverload();
                    if (count >= clusterManager.getProperty().getMinOverloadCount(consumer.getCluster())) {
                        int target = Math.min(current + 2, getMaxConcurrency(consumer));
                        if (target > current
                            && resize(consumer, target)) {
                            state.resetOverload();
                        }
                    }
                } else if (lag < minLag) {
                    state.resetOverload();
                    if (state.incIdle() >= 20) {
                        int target = Math.max(current - 1, 1);
                        if (target < current && resize(consumer, target)) {
                            state.resetIdle();
                        }
                    }
                } else {
                    state.clear();
                }

                scaleCache.put(scaleKey, state);
            }
        }
    }

    private boolean resize(KafkaConsumerInfoDTO consumer, int target) {
        int current = getCurrentConcurrency(consumer);
        if (target == current) {
            return false;
        }
        String lock = consumer.getListenerId() + ":" + consumer.getGroup();
        synchronized (lock.intern()) {
            try {
                log.warn(
                    "Kafka resize cluster={},group={},target={}",
                    consumer.getCluster(),
                    consumer.getGroup(),
                    target
                );

                registrar.resize(consumer, target);

                consumer.setCurrentConcurrency(target);
                return true;
            } catch (Exception e) {
                log.error(
                    "Kafka resize failed listener={}",
                    consumer.getListenerId(),
                    e
                );

                return false;
            }
        }
    }

    private void refreshCurrentConcurrency(KafkaConsumerInfoDTO consumer) {
        if (!(consumer.getContainer()
            instanceof ConcurrentMessageListenerContainer<?, ?> container)) {
            return;
        }

        int size = container.getContainers().size();
        if (size > 0) {
            consumer.setCurrentConcurrency(size);
        }
    }

    private int getCurrentConcurrency(KafkaConsumerInfoDTO consumer) {
        Integer current = consumer.getCurrentConcurrency();
        return current == null
            || current <= 0 ? 1 : current;
    }

    private int getMaxConcurrency(KafkaConsumerInfoDTO consumer) {
        Integer max = consumer.getMaxConcurrency();
        return max == null
            || max <= 0 ? 20 : max;
    }

    private record LagKey(
        String cluster,
        String group,
        String topic) {

    }

    private record ScaleKey(
        String cluster,
        String group,
        String topic) {

    }

    private static class ScaleState {
        private final AtomicInteger overload =
            new AtomicInteger();

        private final AtomicInteger idle =
            new AtomicInteger();

        int incOverload() {
            return overload.incrementAndGet();
        }

        int incIdle() {
            return idle.incrementAndGet();
        }

        void resetOverload() {
            overload.set(0);
        }

        void resetIdle() {
            idle.set(0);
        }

        void clear() {
            resetOverload();
            resetIdle();
        }
    }
}