/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonClient;

/**
 * redis分布式锁类型.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
@RequiredArgsConstructor
public enum RedisLockTypeEnum implements BaseEnum<String> {

    /**
     * 非公平锁
     */
    LOCK("lock", "非公平锁") {
        @Override
        public RLock getLock(RedissonClient client, String key) {
            return client.getLock(key);
        }

        @Override
        public RLockReactive getLockReactive(RedissonClient client, String key) {
            return client.reactive().getLock(key);
        }
    },

    /**
     * 自旋锁
     */
    SPIN_LOCK("spinLock", "自旋锁") {
        @Override
        public RLock getLock(RedissonClient client, String key) {
            return client.getSpinLock(key);
        }

        @Override
        public RLockReactive getLockReactive(RedissonClient client, String key) {
            return client.reactive().getSpinLock(key);
        }
    },

    /**
     * 栅栏锁（Fenced Lock）
     */
    FENCED_LOCK("fencedLock", "栅栏锁") {
        @Override
        public RLock getLock(RedissonClient client, String key) {
            return client.getFencedLock(key);
        }

        @Override
        public RLockReactive getLockReactive(RedissonClient client, String key) {
            return client.reactive().getFencedLock(key);
        }
    },

    /**
     * 公平锁
     */
    FAIR_LOCK("fairLock", "公平锁") {
        @Override
        public RLock getLock(RedissonClient client, String key) {
            return client.getFairLock(key);
        }

        @Override
        public RLockReactive getLockReactive(RedissonClient client, String key) {
            return client.reactive().getFairLock(key);
        }
    },

    /**
     * 读锁
     */
    READ_LOCK("readLock", "读锁") {
        @Override
        public RLock getLock(RedissonClient client, String key) {
            return client.getReadWriteLock(key).readLock();
        }

        @Override
        public RLockReactive getLockReactive(RedissonClient client, String key) {
            return client.reactive().getReadWriteLock(key).readLock();
        }
    },

    /**
     * 写锁
     */
    WRITE_LOCK("writeLock", "写锁") {
        @Override
        public RLock getLock(RedissonClient client, String key) {
            return client.getReadWriteLock(key).writeLock();
        }

        @Override
        public RLockReactive getLockReactive(RedissonClient client, String key) {
            return client.reactive().getReadWriteLock(key).writeLock();
        }
    };

    /**
     * 值
     */
    private final String value;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 同步锁获取
     */
    public abstract RLock getLock(RedissonClient client, String key);

    /**
     * 响应式锁获取
     */
    public abstract RLockReactive getLockReactive(RedissonClient client, String key);
}
