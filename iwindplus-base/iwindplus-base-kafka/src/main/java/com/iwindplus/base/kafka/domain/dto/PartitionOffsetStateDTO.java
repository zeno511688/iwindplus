/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.dto;

import java.io.Serializable;
import java.util.TreeSet;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * PartitionOffset 状态.
 *
 * @author zengdegui
 * @since 2026/07/28 16:30
 */
@Slf4j
@ToString
public class PartitionOffsetStateDTO implements Serializable {

    /**
     * 下一个期待完成offset
     * <p>
     * 注意: 不是Kafka提交offset
     * <p>
     * 例如:
     * <p>
     * 当前等待100
     */
    private Long nextProcessOffset;

    /**
     * Kafka已经提交成功offset
     */
    private Long committedOffset;

    /**
     * 等待提交offset
     */
    private Long pendingCommitOffset;

    /**
     * Disruptor完成offset
     * <p>
     * 可能乱序
     */
    private final TreeSet<Long> completedOffsets = new TreeSet<>();


    public PartitionOffsetStateDTO(long startOffset) {
        this.nextProcessOffset = startOffset;

        /**
         * Kafka commit offset语义:
         *
         * 下一次消费位置
         */
        this.committedOffset = startOffset;

        this.pendingCommitOffset = startOffset;
    }

    /**
     * Disruptor业务成功调用
     * <p>
     * 多线程
     *
     * @param offset kafka record offset
     */
    public synchronized void ack(long offset) {
        if (offset < nextProcessOffset) {
            /**
             * 已经提交过
             */
            return;

        }

        completedOffsets.add(offset);
    }

    /**
     * 计算可以提交的offset
     * <p>
     * Consumer线程调用
     * <p>
     * 不真正commit Kafka
     */
    public synchronized Long prepareCommit() {
        long next = nextProcessOffset;

        while (completedOffsets.remove(next)) {
            next++;
        }

        if (next > nextProcessOffset) {
            nextProcessOffset = next;
            pendingCommitOffset = next;
            return next;
        }

        return null;
    }

    /**
     * Kafka commit成功回调
     */
    public synchronized void commitSuccess() {
        if (pendingCommitOffset > committedOffset) {
            committedOffset = pendingCommitOffset;
        }
    }


    /**
     * Kafka commit失败
     * <p>
     * pending不能丢
     */
    public synchronized void commitFailed() {
        log.warn(
            "offset commit failed pending={}",
            pendingCommitOffset
        );
    }

    /**
     * rebalance清理
     */
    public synchronized void reset(long offset) {
        completedOffsets.clear();
        nextProcessOffset = offset;
        committedOffset = offset;
        pendingCommitOffset = offset;
    }
}
