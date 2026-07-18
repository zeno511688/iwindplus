/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.cache;

import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis实现共享session.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShiroRedisSessionDAO extends EnterpriseCacheSessionDAO {

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 前缀.
     */
    private String keyPrefix;

    /**
     * 失效时间，单位：秒.
     */
    private Duration timeout;

    /**
     * 创建session.
     *
     * @param session session
     * @return Serializable
     */
    @Override
    protected Serializable doCreate(Session session) {
        try {
            Serializable sessionId = super.doCreate(session);
            String key = this.getKey(sessionId);
            this.redisTemplate.opsForValue().set(key, session, this.timeout);
            return sessionId;
        } catch (IllegalStateException ex) {
            log.error(ExceptionConstant.ILLEGAL_STATE_EXCEPTION, ex);
        } catch (Exception ex) {
            log.error("创建缓存出错", ex);
        }
        return null;
    }

    /**
     * 获取session.
     *
     * @param sessionId sessionId
     * @return Session
     */
    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (Objects.isNull(sessionId)) {
            return null;
        }
        try {
            String key = this.getKey(sessionId);
            Object obj = this.redisTemplate.opsForValue().get(key);
            return (Session) obj;
        } catch (Exception ex) {
            log.error("获取缓存出错", ex);
        }
        return null;
    }

    /**
     * 更新session的最后一次访问时间.
     *
     * @param session session
     */
    @Override
    protected void doUpdate(Session session) {
        if (Objects.isNull(session) || Objects.isNull(session.getId())) {
            return;
        }
        try {
            session.setTimeout(this.timeout.toMillis());
            String key = this.getKey(session.getId());
            this.redisTemplate.opsForValue().set(key, session, this.timeout);
        } catch (Exception ex) {
            log.error("更新缓存出错", ex);
        }
    }

    /**
     * 删除session.
     *
     * @param session session
     */
    @Override
    protected void doDelete(Session session) {
        if (Objects.isNull(session) || Objects.isNull(session.getId())) {
            return;
        }
        try {
            String key = this.getKey(session.getId());
            this.redisTemplate.delete(key);
        } catch (Exception ex) {
            log.error("更新缓存出错", ex);
        }
    }

    /**
     * 拼装缓存key.
     *
     * @param key 键
     * @return String
     */
    private String getKey(Object key) {
        return new StringBuilder(this.keyPrefix).append(key).toString();
    }
}
