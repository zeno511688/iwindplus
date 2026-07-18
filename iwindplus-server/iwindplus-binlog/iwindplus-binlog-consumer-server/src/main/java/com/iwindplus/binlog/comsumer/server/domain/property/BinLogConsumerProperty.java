/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.property;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.binlog.comsumer.server.domain.enums.BinlogConsumerCodeEnum;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * binlog 消费者配置.
 *
 * @author zengdegui
 * @since 2025/11/29 23:15
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "binlog.consumer")
public class BinLogConsumerProperty {

    /**
     * 是否开启.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 数据库配置（key为数据库名称）.
     */
    @Builder.Default
    private Map<String, Database> databases = new HashMap<>(16);

    /**
     * webhook配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private Webhook webhook = new Webhook();

    /**
     * 数据库相关属性.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Database {

        /**
         * 密钥（验签用）.
         */
        private String secretKey;

        /**
         * 需要验签的表.
         */
        private Set<String> tableList;
    }

    /**
     * webhook相关属性.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Webhook {

        /**
         * 路径.
         */
        private String url;

        /**
         * 密钥（可选）.
         */
        private String secret;
    }

    /**
     * 获取数据库配置.
     *
     * @param db 数据库名称
     * @return Database
     */
    public Database getDatabase(String db) {
        final Database database = databases.get(db);
        if (Objects.isNull(database)) {
            throw new BizException(BinlogConsumerCodeEnum.INVALID_DATA_SOURCE_CFG);
        }
        return database;
    }

    /**
     * 获取密钥.
     *
     * @param db 数据库名称
     * @return String
     */
    public String getSecretKey(String db) {
        final Database database = getDatabase(db);
        return database.getSecretKey();
    }

    /**
     * 检查是否需要验签.
     *
     * @param db    数据库名称
     * @param table 表名称
     * @return boolean
     */
    public boolean checkNeedSign(String db, String table) {
        if (CharSequenceUtil.isBlank(db) || CharSequenceUtil.isBlank(table)) {
            return false;
        }
        final Database database = getDatabase(db);
        return CollUtil.defaultIfEmpty(database.getTableList(), Collections.emptyList())
            .contains(table);
    }
}
