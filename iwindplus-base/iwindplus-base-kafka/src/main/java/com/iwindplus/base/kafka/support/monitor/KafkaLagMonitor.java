/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030
 *
 */

package com.iwindplus.base.kafka.support.monitor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerInfoDTO;
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerKeyDTO;
import com.iwindplus.base.kafka.domain.dto.KafkaLagDTO;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.listener.KafkaMultiListenerRegistrar;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * Kafka Lag监控及动态扩缩容.
 * <p>
 * 设计： 1. Lag按topic统计 2. resize按listener执行 3. 防抖、防频繁扩缩容 4. concurrency不超过partition数量
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaLagMonitor implements SmartLifecycle {

    private final KafkaMultiListenerRegistrar registrar;
    private final KafkaClusterManager clusterManager;
    private final ScheduledDtpExecutor scheduler;

    /**
     * topic lag缓存.
     */
    private final Cache<LagKey, List<KafkaLagDTO>> lagCache =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    /**
     * listener扩缩容状态.
     */
    private final Cache<ScaleKey, ScaleState> scaleCache =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofHours(6))
            .build();

    /**
     * resize锁，替代lock.intern().
     */
    private final Cache<String, Object> resizeLockCache =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    /**
     * partition缓存.
     */
    private final Cache<TopicKey, Integer> partitionCache =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    private final AtomicBoolean started = new AtomicBoolean();

    private volatile boolean running;

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        if (scheduler == null) {
            log.warn("Kafka lag scheduler missing");
            return;
        }

        if (Boolean.FALSE.equals(clusterManager.getProperty().getEnabledScale())) {
            log.info("Kafka auto scale disabled");
            return;
        }

        running = true;

        /*
         * 延迟启动，等待consumer group稳定.
         */
        scheduler.scheduleWithFixedDelay(
            this::monitor,
            NumberConstant.NUMBER_THIRTY,
            NumberConstant.NUMBER_THIRTY,
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

        List<KafkaLagDTO> list =
            lagCache.getIfPresent(new LagKey(cluster, group, topic));

        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 监控入口.
     */
    private void monitor() {
        if (!running) {
            return;
        }

        try {
            Map<KafkaConsumerKeyDTO, List<KafkaConsumerInfoDTO>> map =
                registrar.groupByClusterAndGroup();

            map.forEach(this::queryGroup);
        } catch (Exception e) {
            log.error(
                "Kafka lag monitor error",
                e
            );
        }
    }


    /**
     * 查询group lag.
     */
    private void queryGroup(
        KafkaConsumerKeyDTO key,
        List<KafkaConsumerInfoDTO> consumers) {
        if (consumers == null || consumers.isEmpty()) {
            return;
        }

        try {
            AdminClient admin = clusterManager.getAdmin(key.getCluster());

            Map<TopicPartition, OffsetAndMetadata> committed =
                admin.listConsumerGroupOffsets(key.getGroup())
                    .partitionsToOffsetAndMetadata()
                    .get(NumberConstant.NUMBER_FIVE, TimeUnit.SECONDS);

            if (committed == null || committed.isEmpty()) {
                return;
            }

            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest =
                queryLatest(admin, committed.keySet());

            List<KafkaLagDTO> lags = buildLag(key, committed, latest);
            if (lags.isEmpty()) {
                return;
            }

            cacheLag(key, lags);

            /*
             * 注意：
             * lag属于topic，
             * resize属于listener.
             */
            checkScale(
                key,
                consumers,
                lags
            );
        } catch (Exception e) {
            log.error(
                "Kafka lag query failed cluster={},group={}",
                key.getCluster(),
                key.getGroup(),
                e
            );
        }
    }

    private Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> queryLatest(
        AdminClient admin,
        Set<TopicPartition> partitions) throws Exception {

        Map<TopicPartition, OffsetSpec> req = partitions.stream()
            .collect(Collectors.toMap(
                p -> p,
                p -> OffsetSpec.latest()
            ));

        return admin.listOffsets(req)
            .all()
            .get(NumberConstant.NUMBER_FIVE, TimeUnit.SECONDS);
    }


    /**
     * 构建partition lag.
     */
    private List<KafkaLagDTO> buildLag(
        KafkaConsumerKeyDTO key,
        Map<TopicPartition, OffsetAndMetadata> committed,
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latest) {

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
                KafkaLagDTO.builder()
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

    /**
     * 缓存topic lag.
     */
    private void cacheLag(
        KafkaConsumerKeyDTO key,
        List<KafkaLagDTO> lags) {

        lags.stream()
            .collect(Collectors.groupingBy(KafkaLagDTO::getTopic))
            .forEach((topic, list) ->
                lagCache.put(
                    new LagKey(
                        key.getCluster(),
                        key.getGroup(),
                        topic
                    ),
                    list
                )
            );
    }

    /**
     * listener维度扩缩容判断.
     */
    private void checkScale(
        KafkaConsumerKeyDTO key,
        List<KafkaConsumerInfoDTO> consumers,
        List<KafkaLagDTO> lags) {

        final KafkaMultiProperty property = clusterManager.getProperty();

        Map<String, Long> topicLag = lags.stream()
            .collect(Collectors.groupingBy(
                KafkaLagDTO::getTopic,
                Collectors.summingLong(
                    KafkaLagDTO::getLag
                )
            ));

        for (KafkaConsumerInfoDTO consumer : consumers) {
            if (consumer == null
                || consumer.getTopics() == null
                || consumer.getTopics().isEmpty()) {
                continue;
            }

            long totalLag =
                consumer.getTopics()
                    .stream()
                    .map(topicLag::get)
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue)
                    .sum();
            if (totalLag <= 0) {
                continue;
            }

            refreshCurrentConcurrency(consumer);

            int current = getCurrentConcurrency(consumer);

            ScaleState state =
                scaleCache.get(
                    new ScaleKey(
                        key.getCluster(),
                        key.getGroup(),
                        consumer.getListenerId()
                    ),
                    k -> new ScaleState()
                );

            long maxLag = property.getMaxLag(consumer.getCluster());
            long minLag = property.getMinLag(consumer.getCluster());
            if (totalLag > maxLag) {
                handleOverload(consumer, state, current);
            } else if (totalLag < minLag) {
                handleIdle(consumer, state, current);
            } else {
                state.clear();
            }

            scaleCache.put(
                new ScaleKey(
                    key.getCluster(),
                    key.getGroup(),
                    consumer.getListenerId()
                ),
                state
            );
        }
    }

    /**
     * 高lag扩容.
     */
    private void handleOverload(
        KafkaConsumerInfoDTO consumer,
        ScaleState state,
        int current) {

        final KafkaMultiProperty property = clusterManager.getProperty();

        state.resetIdle();
        int count = state.incOverload();
        if (count < property.getMinOverloadCount(consumer.getCluster())) {
            return;
        }

        if (!state.canScale()) {
            return;
        }

        int target = calculateIncreaseTarget(consumer, current);
        if (target <= current) {
            return;
        }

        if (resize(consumer, target)) {
            state.resetOverload();
            state.markScale();
        }
    }

    /**
     * 低lag缩容.
     */
    private void handleIdle(
        KafkaConsumerInfoDTO consumer,
        ScaleState state,
        int current) {

        state.resetOverload();

        /*
         * 30秒周期.
         * 20次=10分钟.
         */
        if (state.incIdle() < NumberConstant.NUMBER_TWENTY) {
            return;
        }

        if (!state.canScale()) {
            return;
        }

        int target = Math.max(current - 1, 1);
        if (target >= current) {
            return;
        }

        if (resize(consumer, target)) {
            state.resetIdle();
            state.markScale();
        }
    }

    /**
     * 扩容策略：
     * <p>
     * 2->3 3->4 4->6
     * <p>
     * 限制： maxConcurrency partition数量
     */
    private int calculateIncreaseTarget(
        KafkaConsumerInfoDTO consumer,
        int current) {

        int max = getMaxConcurrency(consumer);

        int target = current + Math.max(1, current / 2);

        return Math.min(
            Math.min(target, max),
            getListenerPartitionLimit(consumer)
        );
    }

    /**
     * partition数量限制.
     * <p>
     * 一个partition只能被一个consumer消费.
     */
    private int getListenerPartitionLimit(
        KafkaConsumerInfoDTO consumer) {

        if (consumer.getTopics() == null
            || consumer.getTopics().isEmpty()) {

            return getMaxConcurrency(consumer);
        }

        AdminClient admin = clusterManager.getAdmin(consumer.getCluster());

        int total = 0;

        for (String topic : consumer.getTopics()) {
            Integer count =
                partitionCache.get(
                    new TopicKey(
                        consumer.getCluster(),
                        topic
                    ),
                    k -> queryPartitionCount(
                        admin,
                        topic
                    )
                );

            total += count == null ? 1 : count;
        }

        return Math.max(total, 1);
    }


    private Integer queryPartitionCount(
        AdminClient admin,
        String topic) {

        try {
            return admin.describeTopics(Collections.singleton(topic))
                .allTopicNames()
                .get(NumberConstant.NUMBER_FIVE, TimeUnit.SECONDS)
                .get(topic)
                .partitions()
                .size();
        } catch (Exception e) {
            log.warn(
                "query partition failed topic={}",
                topic,
                e
            );

            return 1;
        }
    }

    /**
     * 真正调整listener concurrency.
     */
    private boolean resize(
        KafkaConsumerInfoDTO consumer,
        int target) {

        int current = getCurrentConcurrency(consumer);

        if (current == target) {
            return false;
        }

        String lockKey =
            consumer.getCluster()
                + SymbolConstant.COLON
                + consumer.getGroup()
                + SymbolConstant.COLON
                + consumer.getListenerId();

        Object lock = resizeLockCache.get(lockKey, k -> new Object());
        synchronized (lock) {
            int latest = getCurrentConcurrency(consumer);
            if (latest == target) {
                return false;
            }

            try {
                log.warn(
                    "Kafka resize listener={},{}->{}",
                    consumer.getListenerId(),
                    latest,
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

    /**
     * 刷新真实consumer数量.
     * <p>
     * 不完全相信缓存， 防止外部动态调整导致状态错误.
     */
    private void refreshCurrentConcurrency(
        KafkaConsumerInfoDTO consumer) {
        if (!(consumer.getContainer()
            instanceof ConcurrentMessageListenerContainer<?, ?> container)) {
            return;
        }

        int size = container.getContainers().size();

        if (size > 0) {
            consumer.setCurrentConcurrency(size);
        }
    }


    private int getCurrentConcurrency(
        KafkaConsumerInfoDTO consumer) {

        Integer current = consumer.getCurrentConcurrency();

        return current == null
            || current <= 0 ? 1 : current;
    }


    private int getMaxConcurrency(
        KafkaConsumerInfoDTO consumer) {

        Integer max = consumer.getMaxConcurrency();

        return max == null
            || max <= 0 ? NumberConstant.NUMBER_TWENTY : max;
    }


    /**
     * Lag缓存key.
     */
    private record LagKey(
        String cluster,
        String group,
        String topic) {

    }

    /**
     * resize状态key.
     * <p>
     * 注意： 不包含topic.
     * <p>
     * concurrency属于listener.
     */
    private record ScaleKey(
        String cluster,
        String group,
        String listenerId) {

    }

    /**
     * partition缓存key.
     */
    private record TopicKey(
        String cluster,
        String topic) {

    }

    /**
     * 扩缩容状态.
     * <p>
     * 保存： 1.连续高lag次数 2.连续低lag次数 3.最近resize时间
     */
    private static class ScaleState {

        /**
         * 连续高lag次数.
         */
        private final AtomicInteger overload = new AtomicInteger();

        /**
         * 连续低lag次数.
         */
        private final AtomicInteger idle = new AtomicInteger();

        /**
         * 最近resize时间.
         */
        private volatile long lastScaleTime;

        /**
         * 连续高lag次数自增
         *
         * @return
         */
        int incOverload() {
            return overload.incrementAndGet();
        }

        /**
         * 连续低lag次数自增
         *
         * @return
         */
        int incIdle() {
            return idle.incrementAndGet();
        }

        /**
         * 重置连续高lag次数
         */
        void resetOverload() {
            overload.set(0);
        }

        /**
         * 重置连续低lag次数
         */
        void resetIdle() {
            idle.set(0);
        }

        /**
         * 清理
         */
        void clear() {
            resetOverload();
            resetIdle();
        }

        /**
         * 5分钟冷却.
         */
        boolean canScale() {
            return System.currentTimeMillis() - lastScaleTime
                >= TimeUnit.MINUTES.toMillis(5);
        }

        /**
         * 标记扩缩容时间.
         */
        void markScale() {
            lastScaleTime = System.currentTimeMillis();
        }
    }
}