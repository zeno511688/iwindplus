/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis.cache;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.util.concurrent.locks.ReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.redisson.api.RedissonClient;

/**
 * mybatis，redis实现缓存.
 *
 * @author zengdegui
 * @since 2024/01/18 00:34
 */
@Slf4j
public class MybatisRedisCache implements Cache {

    private volatile RedissonClient redissonClient;

    private String id;

    private RedissonClient getRedissonClient() {
        if (this.redissonClient == null) {
            synchronized (this) {
                if (this.redissonClient == null) {
                    this.redissonClient = SpringUtil.getBean(RedissonClient.class);
                }
            }
        }
        return this.redissonClient;
    }

    /**
     * 构造方法.
     *
     * @param id 主键
     */
    public MybatisRedisCache(final String id) {
        if (CharSequenceUtil.isBlank(id)) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        if (key == null || value == null) {
            return;
        }
        try {
            getRedissonClient().getMap(this.getId()).put(key.toString(), value);
        } catch (Exception ex) {
            log.error("缓存出错", ex);
        }
    }

    @Override
    public Object getObject(Object key) {
        if (key == null) {
            return null;
        }
        try {
            return getRedissonClient().getMap(this.getId()).get(key.toString());
        } catch (Exception ex) {
            log.error("获取缓存出错", ex);
        }
        return null;
    }

    @Override
    public Object removeObject(Object key) {
        if (key == null) {
            return null;
        }
        try {
            return getRedissonClient().getMap(this.getId()).remove(key.toString());
        } catch (Exception ex) {
            log.error("删除缓存出错", ex);
        }
        return null;
    }

    @Override
    public void clear() {
        try {
            getRedissonClient().getMap(this.getId()).delete();
        } catch (Exception ex) {
            log.error("清除缓存出错", ex);
        }
    }

    @Override
    public int getSize() {
        try {
            return getRedissonClient().getMap(this.getId()).size();
        } catch (Exception ex) {
            log.error("获取缓存个数出错", ex);
        }
        return CommonConstant.NumberConstant.NUMBER_ZERO;
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.getRedissonClient().getReadWriteLock(this.getId());
    }
}
