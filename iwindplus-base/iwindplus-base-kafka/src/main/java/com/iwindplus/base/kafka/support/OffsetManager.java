/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

/**
 * Kafka 位点管理器.
 *
 * <p>
 * 负责异步消费场景下的 offset 管理：
 *
 * <ul>
 *     <li>记录业务处理成功的 offset</li>
 *     <li>计算连续可提交的最大 offset</li>
 *     <li>支持 Consumer 线程提交 offset</li>
 *     <li>支持 commit 成功后的状态确认</li>
 * </ul>
 *
 * <p>
 * 注意：
 * <ul>
 *     <li>不持有 KafkaConsumer</li>
 *     <li>不执行 commitSync/commitAsync</li>
 *     <li>业务线程只调用 success/successBatch</li>
 *     <li>Consumer线程调用 prepareCommit/commitSuccess</li>
 * </ul>
 *
 * @author zengdegui
 * @since 2026/07/26 10:35
 */
public class OffsetManager {

    /**
     * offset状态缓存.
     * <p>
     * key: cluster + topic + partition
     */
    private final ConcurrentMap<OffsetKey, PartitionOffsetState> states =
        new ConcurrentHashMap<>(16);

    /**
     * 单条消息处理成功.
     *
     * @param cluster   集群
     * @param topic     topic
     * @param partition 分区
     * @param offset    offset
     */
    public void success(
        String cluster,
        String topic,
        int partition,
        long offset) {

        OffsetKey key = new OffsetKey(
            cluster,
            topic,
            partition
        );

        states.computeIfAbsent(
            key,
            k -> new PartitionOffsetState()
        ).success(offset);
    }

    /**
     * 批量消息处理成功.
     *
     * <p>
     * Disruptor批量消费完成后调用.
     *
     * @param cluster 集群
     * @param records 消息列表
     */
    public void successBatch(
        String cluster,
        List<ConsumerRecord<?, ?>> records) {

        if (records == null || records.isEmpty()) {
            return;
        }

        Map<OffsetKey, List<Long>> grouped =
            new HashMap<>(16);

        for (ConsumerRecord<?, ?> record : records) {
            OffsetKey key = new OffsetKey(
                cluster,
                record.topic(),
                record.partition()
            );

            grouped.computeIfAbsent(
                key,
                k -> new ArrayList<>()
            ).add(record.offset());
        }

        grouped.forEach((key, offsets) ->
            states.computeIfAbsent(
                key,
                k -> new PartitionOffsetState()
            ).successBatch(offsets)
        );
    }

    /**
     * 计算可提交offset.
     *
     * <p>
     * Consumer线程调用.
     *
     * <p>
     * 不直接修改已提交状态，
     * 等 Kafka commit 成功后调用 commitSuccess.
     *
     * @param cluster 集群
     * @return Kafka提交offset
     */
    public Map<TopicPartition, OffsetAndMetadata> prepareCommit(
        String cluster) {

        Map<TopicPartition, OffsetAndMetadata> result =
            new HashMap<>(16);

        states.forEach((key, state) -> {
            if (!key.getCluster().equals(cluster)) {
                return;
            }

            long offset = state.prepareCommit();

            if (offset >= 0) {
                result.put(
                    new TopicPartition(
                        key.getTopic(),
                        key.getPartition()
                    ),
                    new OffsetAndMetadata(
                        offset + 1
                    )
                );
            }
        });

        return result;
    }

    /**
     * Kafka提交成功回调.
     *
     * @param cluster 集群
     * @param offsets 提交成功offset
     */
    public void commitSuccess(
        String cluster,
        Map<TopicPartition, OffsetAndMetadata> offsets) {

        if (offsets == null || offsets.isEmpty()) {
            return;
        }

        offsets.forEach((tp, metadata) -> {
            OffsetKey key = new OffsetKey(
                cluster,
                tp.topic(),
                tp.partition()
            );

            PartitionOffsetState state =
                states.get(key);

            if (state != null) {
                state.commitSuccess(
                    metadata.offset() - 1
                );
            }
        });
    }

    /**
     * Kafka提交失败回调.
     *
     * <p>
     * commitAsync失败后恢复pending状态.
     *
     * @param cluster 集群
     */
    public void commitFailed(String cluster) {
        states.forEach((key, state) -> {
            if (!key.getCluster().equals(cluster)) {
                return;
            }

            state.commitFailed();
        });
    }

    /**
     * 获取当前已确认offset.
     *
     * <p>
     * rebalance或者shutdown时使用.
     *
     * @param cluster 集群
     * @return 当前offset
     */
    public Map<TopicPartition, OffsetAndMetadata> snapshot(
        String cluster) {

        Map<TopicPartition, OffsetAndMetadata> result =
            new HashMap<>(16);

        states.forEach((key, state) -> {
            if (!key.getCluster().equals(cluster)) {
                return;
            }

            long offset = state.current();

            if (offset >= 0) {
                result.put(
                    new TopicPartition(
                        key.getTopic(),
                        key.getPartition()
                    ),
                    new OffsetAndMetadata(
                        offset + 1
                    )
                );
            }
        });

        return result;
    }

    /**
     * 删除partition状态.
     *
     * <p>
     * rebalance释放partition时调用.
     *
     * @param cluster   集群
     * @param topic     topic
     * @param partition 分区
     */
    public void remove(
        String cluster,
        String topic,
        int partition) {

        states.remove(
            new OffsetKey(
                cluster,
                topic,
                partition
            )
        );
    }

    /**
     * Offset唯一标识.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OffsetKey implements Serializable {

        /**
         * 集群名称.
         */
        private String cluster;

        /**
         * topic名称.
         */
        private String topic;

        /**
         * 分区.
         */
        private int partition;
    }

    /**
     * 单partition offset状态.
     *
     * <p>
     * 一个partition内必须保证offset连续提交.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PartitionOffsetState implements Serializable {

        /**
         * 已经处理成功但未提交的offset.
         */
        private final TreeSet<Long> completed =
            new TreeSet<>();

        /**
         * 已经准备提交，等待Kafka确认的offset.
         */
        private final TreeSet<Long> pending =
            new TreeSet<>();

        /**
         * Kafka已经确认提交的最大offset.
         */
        @Builder.Default
        private Long committed = -1L;

        /**
         * 下一个期待完成的offset.
         */
        @Builder.Default
        private Long nextOffset = -1L;

        /**
         * 单条成功.
         */
        public synchronized void success(long offset) {
            init(offset);
            completed.add(offset);
        }

        /**
         * 批量加入.
         */
        public synchronized void successBatch(
            Collection<Long> offsets) {

            if (offsets == null || offsets.isEmpty()) {
                return;
            }

            init(
                offsets.stream()
                    .min(Long::compare)
                    .orElse(-1L)
            );

            completed.addAll(offsets);
        }

        /**
         * 计算连续可提交offset.
         *
         * <p>
         * 注意：
         * 不删除completed.
         * 等Kafka提交成功后再清理.
         */
        public synchronized long prepareCommit() {
            if (nextOffset < 0) {
                return -1;
            }

            long current = nextOffset;

            while (completed.contains(current)) {
                pending.add(current);
                current++;
            }

            if (pending.isEmpty()) {
                return -1;
            }

            return pending.last();
        }

        /**
         * Kafka提交成功后更新状态.
         */
        public synchronized void commitSuccess(
            long offset) {

            /**
             * 防止commitAsync乱序回调导致offset回退.
             */
            if (offset <= committed) {
                return;
            }

            pending.removeIf(
                value -> value <= offset
            );

            completed.removeIf(
                value -> value <= offset
            );

            committed = offset;

            nextOffset = offset + 1;
        }

        /**
         * Kafka提交失败恢复状态.
         */
        public synchronized void commitFailed() {
            completed.addAll(
                pending
            );

            pending.clear();
        }

        /**
         * 当前已经提交offset.
         */
        public synchronized long current() {
            return committed;
        }

        /**
         * 初始化期望offset.
         */
        private void init(long offset) {
            if (nextOffset < 0) {

                nextOffset = offset;
            }
        }
    }
}