/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.monitor;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.redis.domain.vo.RedisCacheInfoVO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * redis监控端点.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Endpoint(id = "redis")
public class RedisEndpoint {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * redis缓存监控信息.
     *
     * @return RedisCacheInfoVO
     */
    @ReadOperation
    public RedisCacheInfoVO getRedisInfo() {
        RedisConnection connection = redisConnectionFactory.getConnection();
        try {
            Properties commandStats = connection.commands().info("commandstats");
            List<Map<String, String>> pieList = new ArrayList<>();
            if (commandStats != null) {
                commandStats.stringPropertyNames().forEach(key -> {
                    Map<String, String> data = new HashMap<>(2);
                    String property = commandStats.getProperty(key);
                    data.put("name", CharSequenceUtil.removePrefix(key, "cmdstat_"));
                    data.put("value", CharSequenceUtil.subBetween(property, "calls=", ",usec"));
                    pieList.add(data);
                });
            }
            return RedisCacheInfoVO.builder()
                .info(connection.commands().info())
                .dbSize(connection.commands().dbSize())
                .commandStats(pieList)
                .build();
        } finally {
            connection.close();
        }
    }
}
