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
import java.util.HashMap;
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
            .expireAfterWrite(Duration.ofHours(2))
            .build();

    /**
     * resize锁.
     */
    private final Cache<String, Object> resizeLockCache =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    /**
     * topic partition缓存.
     */
    private final Cache<TopicKey, Integer> partitionCache =
        Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    /**
     * latest offset缓存.
     */
    private final Cache<PartitionKey, OffsetCacheValue> endOffsetCache =
        Caffeine.newBuilder()
            .maximumSize(100000)
            .expireAfterWrite(Duration.ofSeconds(20))
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

            map.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                    e -> e.getKey().getCluster()
                ))
                .forEach((cluster, list) ->
                    processCluster(
                        cluster,
                        list
                    )
                );

            cleanup();
        } catch (Exception e) {
            log.error(
                "Kafka lag monitor error",
                e
            );
        }
    }

    /**
     * cluster维度处理.
     *
     * <p>
     * 这里做两个批量优化:
     *
     * <ol>
     *     <li>一次加载cluster所有topic partition数量</li>
     *     <li>多个group共享endOffset缓存</li>
     * </ol>
     */
    private void processCluster(
        String cluster,
        List<Map.Entry<KafkaConsumerKeyDTO, List<KafkaConsumerInfoDTO>>> groups) {

        if (groups == null || groups.isEmpty()) {
            return;
        }

        AdminClient admin = clusterManager.getAdmin(cluster);

        // 收集cluster所有topic.
        Set<String> topics =
            groups.stream()
                .flatMap(e -> e.getValue().stream())
                .filter(Objects::nonNull)
                .flatMap(c -> c.getTopics().stream())
                .collect(Collectors.toSet());

        //  批量加载partition数量.
        loadPartitionMetadata(admin, cluster, topics);

        // group逐个处理
        groups.forEach(entry ->
            queryGroup(
                entry.getKey(),
                entry.getValue()
            )
        );
    }

    /**
     * 批量查询topic partition数量.
     * <p>
     * Kafka:
     * <p>
     * describeTopics(Set<String>)
     * <p>
     * 一次请求多个topic.
     */
    private void loadPartitionMetadata(
        AdminClient admin,
        String cluster,
        Set<String> topics) {

        if (topics == null || topics.isEmpty()) {
            return;
        }

        Set<String> missing = topics
            .stream()
            .filter(topic ->
                partitionCache.getIfPresent(
                    new TopicKey(
                        cluster,
                        topic
                    )
                ) == null
            ).collect(Collectors.toSet());
        if (missing.isEmpty()) {
            return;
        }

        try {
            admin.describeTopics(missing)
                .allTopicNames()
                .get(NumberConstant.NUMBER_FIVE, TimeUnit.SECONDS)
                .forEach((topic, info) ->
                    partitionCache.put(
                        new TopicKey(
                            cluster,
                            topic
                        ),
                        info.partitions().size()
                    )
                );
        } catch (Exception e) {
            log.warn(
                "batch describe topics failed cluster={}",
                cluster,
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
            Map<TopicPartition, Long> latest =
                queryLatest(key.getCluster(), admin, committed.keySet());

            List<KafkaLagDTO> lags = buildLag(key, committed, latest);
            if (lags.isEmpty()) {
                return;
            }

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

    /**
     * 查询partition最新offset.
     *
     * <p>
     * 返回offset数值，不暴露Kafka内部对象.
     */
    private Map<TopicPartition, Long> queryLatest(
        String cluster,
        AdminClient admin,
        Set<TopicPartition> partitions)
        throws Exception {

        Map<TopicPartition, Long> result = new HashMap<>(16);
        Map<TopicPartition, OffsetSpec> remote = new HashMap<>(16);
        for (TopicPartition tp : partitions) {
            OffsetCacheValue cache =
                endOffsetCache.getIfPresent(
                    new PartitionKey(
                        cluster,
                        tp
                    )
                );

            if (cache != null && !cache.expired()) {
                result.put(tp, cache.offset());
            } else {
                remote.put(tp, OffsetSpec.latest());
            }
        }

        // 批量查询Kafka.
        if (!remote.isEmpty()) {
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> response =
                admin.listOffsets(remote)
                    .all()
                    .get(NumberConstant.NUMBER_FIVE, TimeUnit.SECONDS);

            response.forEach(
                (tp, info) -> {
                    long offset = info.offset();

                    endOffsetCache.put(
                        new PartitionKey(
                            cluster,
                            tp
                        ),
                        OffsetCacheValue.of(
                            offset
                        )
                    );

                    result.put(tp, offset);
                }
            );
        }

        return result;
    }

    /**
     * 构建partition lag.
     */
    private List<KafkaLagDTO> buildLag(
        KafkaConsumerKeyDTO key,
        Map<TopicPartition, OffsetAndMetadata> committed,
        Map<TopicPartition, Long> latest) {

        List<KafkaLagDTO> result = new ArrayList<>(committed.size());

        committed.forEach((tp, offset) -> {
            if (offset == null) {
                return;
            }

            Long end = latest.get(tp);
            if (end == null) {
                return;
            }

            long current = offset.offset();

            long lag = Math.max(end - current, 0);

            result.add(
                KafkaLagDTO.builder()
                    .cluster(key.getCluster())
                    .group(key.getGroup())
                    .topic(tp.topic())
                    .partition(tp.partition())
                    .currentOffset(current)
                    .endOffset(end)
                    .lag(lag)
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

        if (lags == null || lags.isEmpty()) {
            return;
        }

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

    private void cleanup() {
        scaleCache.cleanUp();

        resizeLockCache.cleanUp();

        partitionCache.cleanUp();

        endOffsetCache.cleanUp();
    }

    /**
     * listener维度扩缩容判断.
     */
    private void checkScale(
        KafkaConsumerKeyDTO key,
        List<KafkaConsumerInfoDTO> consumers,
        List<KafkaLagDTO> lags) {

        if (consumers == null || consumers.isEmpty()) {
            return;
        }

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

            refreshCurrentConcurrency(consumer);

            long totalLag =
                consumer.getTopics()
                    .stream()
                    .map(topicLag::get)
                    .filter(Objects::nonNull)
                    .mapToLong(Long::longValue)
                    .sum();
            int current = getCurrentConcurrency(consumer);

            ScaleKey scaleKey = new ScaleKey(
                key.getCluster(),
                key.getGroup(),
                consumer.getListenerId()
            );

            ScaleState state = scaleCache.get(
                scaleKey,
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
                scaleKey,
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

        if (state.incOverload() < property.getOverloadLagThreshold(consumer.getCluster())) {
            return;
        }

        if (!state.canScale()) {
            return;
        }

        int target = calculateIncreaseTarget(consumer, current);
        if (target <= current) {
            state.resetOverload();
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

        final KafkaMultiProperty property = clusterManager.getProperty();

        state.resetOverload();

        if (state.incIdle() < property.getIdleLagThreshold(consumer.getCluster())) {
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
     * 扩容策略:
     * <p>
     * 2 -> 3 3 -> 4 4 -> 6
     * <p>
     * 限制:
     * <p>
     * 1. listener maxConcurrency 2. topic partition数量
     */
    private int calculateIncreaseTarget(
        KafkaConsumerInfoDTO consumer,
        int current) {

        int max = getMaxConcurrency(consumer);
        int concurrencyLimit = getListenerConcurrencyLimit(consumer);

        int target = current + Math.max(1, current / 2);

        return Math.min(
            Math.min(target, max),
            concurrencyLimit
        );
    }

    /**
     * listener concurrency限制.
     *
     * <p>
     * Kafka规则: 一个partition同一时间只能被group中的一个consumer实例消费。
     *
     * <p>
     * 对于单个listener: concurrency最大不建议超过订阅topic的partition总数。
     *
     * <p>
     * 注意: 这里限制的是listener开启多少个consumer线程， 不是group总consumer数量限制。
     *
     * @param consumer kafka消费者信息
     * @return listener最大允许concurrency
     */
    private int getListenerConcurrencyLimit(
        KafkaConsumerInfoDTO consumer) {
        if (consumer == null
            || consumer.getTopics() == null
            || consumer.getTopics().isEmpty()) {

            return getMaxConcurrency(consumer);
        }

        int partitions = consumer.getTopics()
            .stream()
            .filter(Objects::nonNull)
            .mapToInt(topic -> {
                Integer count =
                    partitionCache.getIfPresent(
                        new TopicKey(
                            consumer.getCluster(),
                            topic
                        )
                    );

                // metadata未加载成功:不返回1，避免错误限制扩容。 返回0，后面由maxConcurrency兜底。
                return count == null ? 0 : count;
            }).sum();

        // partition metadata不可用时: 放弃partition限制，只使用listener自身最大限制。
        if (partitions <= 0) {
            return getMaxConcurrency(consumer);
        }

        return Math.min(
            partitions,
            getMaxConcurrency(
                consumer
            )
        );
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
                if (isRebalancing(consumer)) {
                    log.info(
                        "Kafka container rebalance, skip resize listener={}",
                        consumer.getListenerId()
                    );

                    return false;
                }

                log.warn(
                    "Kafka resize listener={} {} -> {}",
                    consumer.getListenerId(),
                    latest,
                    target
                );

                return registrar.resize(consumer, target);
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
     * 判断container状态.
     */
    private boolean isRebalancing(
        KafkaConsumerInfoDTO consumer) {

        if (!(consumer.getContainer()
            instanceof ConcurrentMessageListenerContainer<?, ?> container)) {

            return false;
        }

        return !container.isRunning();
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
     * topic partition缓存key.
     */
    private record TopicKey(
        String cluster,
        String topic) {

    }

    /**
     * partition offset缓存key.
     *
     * <p>
     * 不关联group.
     */
    private record PartitionKey(
        String cluster,
        TopicPartition partition) {

    }

    /**
     * listener扩缩容状态key.
     *
     * <p>
     * concurrency属于listener， 不属于topic。
     */
    private record ScaleKey(
        String cluster,
        String group,
        String listenerId) {

    }

    /**
     * offset缓存对象.
     */
    private record OffsetCacheValue(
        long offset,
        long createTime) {

        static OffsetCacheValue of(long offset) {
            return new OffsetCacheValue(
                offset,
                System.currentTimeMillis()
            );
        }

        /**
         * offset缓存有效期.
         *
         * <p>
         * 默认10秒。
         */
        boolean expired() {
            return System.currentTimeMillis() - createTime
                > TimeUnit.SECONDS.toMillis(10);
        }
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