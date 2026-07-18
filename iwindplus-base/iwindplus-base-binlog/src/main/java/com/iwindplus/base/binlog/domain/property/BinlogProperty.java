/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.binlog.domain.property;

import io.debezium.connector.binlog.BinlogConnectorConfig.SnapshotMode;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Binlog配置..
 *
 * @author zengdegui
 * @since 2025/11/21 21:53
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "binlog")
public class BinlogProperty {

    /**
     * 是否开启.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 是否开启动态注册主题.
     */
    @Builder.Default
    private Boolean enabledDynamicRegister = true;

    /**
     * 快照模式（默认：先全表快照 → 再增量）.
     */
    @Builder.Default
    private SnapshotMode snapshotMode = SnapshotMode.INITIAL;

    /**
     * topic前缀（发送数据）.
     */
    @Builder.Default
    private String topicPrefix = "iwindplus-binlog-topic";

    /**
     * 数据源列表.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private List<DataSource> dataSources = new ArrayList<>(10);

    /**
     * 主题偏移量（binlog 消费位点）.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private TopicOffset offset = new TopicOffset();

    /**
     * （同步表结构变化历史）.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private TopicHistory history = new TopicHistory();

    /**
     * 数据源相关属性.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSource {

        /**
         * 数据源类型.
         */
        @Builder.Default
        private String type = "mysql";

        /**
         * 数据源id.
         */
        @Builder.Default
        private String serverId = "1";

        /**
         * 数据库主机.
         */
        @Builder.Default
        private String host = "127.0.0.1";

        /**
         * 数据库端口.
         */
        @Builder.Default
        private Integer port = 3306;

        /**
         * 数据库用户名.
         */
        @Builder.Default
        private String username = "root";

        /**
         * 数据库密码.
         */
        @Builder.Default
        private String password = "root";

        /**
         * 包含的数据库（逗号分隔，支持通配符）.
         */
        private String databaseIncludeList;

        /**
         * 排除的数据库（逗号分隔，支持通配符）.
         */
        private String databaseExcludeList;

        /**
         * 包含的表（需要包含库名，逗号分隔，支持通配符，如：db.user）.
         */
        private String tableIncludeList;

        /**
         * 排除的的表（需要包含库名，逗号分隔，支持通配符，如：db.user）.
         */
        private String tableExcludeList;
    }

    /**
     * 主题偏移量相关属性（binlog 消费位点）.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicOffset {

        /**
         * 主题（必填）.
         */
        @Builder.Default
        private String topic = "iwindplus-binlog-topic-offset";

        /**
         * 分区数量（Broker默认配置项）.
         */
        @Builder.Default
        private Integer partitions = -1;

        /**
         * 副本数量（Broker默认配置项）.
         */
        @Builder.Default
        private Short replicationFactor = -1;
    }

    /**
     * 历史主题相关属性（同步表结构变化历史）.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicHistory {

        /**
         * 主题（必填）.
         */
        @Builder.Default
        private String topic = "iwindplus-binlog-topic-schema-history";

        /**
         * 分区数量（Broker默认配置项）.
         */
        @Builder.Default
        private Integer partitions = -1;

        /**
         * 副本数量（Broker默认配置项）.
         */
        @Builder.Default
        private Short replicationFactor = -1;
    }
}
