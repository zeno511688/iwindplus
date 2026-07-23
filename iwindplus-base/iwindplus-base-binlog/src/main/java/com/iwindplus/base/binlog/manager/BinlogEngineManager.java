/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.binlog.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.binlog.domain.property.BinlogProperty;
import com.iwindplus.base.binlog.domain.property.BinlogProperty.TopicHistory;
import com.iwindplus.base.binlog.domain.property.BinlogProperty.TopicOffset;
import com.iwindplus.base.binlog.handler.BinlogProcessHandler;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaMultiClusterConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaBindingConfig;
import com.iwindplus.base.kafka.support.KafkaDynamicRegistry;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.connector.binlog.BinlogConnectorConfig;
import io.debezium.connector.mysql.MySqlConnectorConfig;
import io.debezium.connector.mysql.MySqlConnectorConfig.SnapshotLockingMode;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;
import org.springframework.context.SmartLifecycle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Binlog 引擎管理器.
 *
 * @author zengdegui
 * @since 2025/11/22 00:02
 */
@Slf4j
public class BinlogEngineManager implements SmartLifecycle {

    @Resource
    private BinlogProperty property;

    @Resource
    private KafkaMultiProperty kafkaProperty;

    @Resource
    private KafkaTemplateRouter kafkaTemplateRouter;

    @Resource
    private BinlogProcessHandler handler;

    @Resource
    private DtpExecutor binlogTaskExecutor;

    @Resource
    private ScheduledDtpExecutor binlogTaskScheduler;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<String, Future<?>> engines = new ConcurrentHashMap<>(16);

    @Override
    public void start() {
        if (Boolean.FALSE.equals(property.getEnabled())
            || Boolean.FALSE.equals(running.compareAndSet(false, true))) {
            log.warn("Binlog engine disabled or already started.");
            return;
        }

        kafkaTopicAutoCreate()
            .thenMany(Flux.fromIterable(property.getDataSources()))
            .doOnNext(this::submitEngine)
            .doOnComplete(() -> log.info("All binlog engines submitted, count={}", engines.size()))
            .subscribe(
                item -> {
                },
                throwable -> log.error("Failed to starting binlog engines.", throwable)
            );
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping binlog engines...");

            engines.values().forEach(f -> f.cancel(true));
            engines.clear();
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    private Future<?> buildAndRunEngine(BinlogProperty.DataSource ds) {
        String connectorName = buildConnectorName(ds.getType(), ds.getServerId());

        DebeziumEngine<ChangeEvent<String, String>> engine =
            DebeziumEngine.create(Json.class)
                .using(buildProps(ds))
                .notifying(this::handleEvent)
                .using((success, msg, err) -> {
                    if (err != null) {
                        log.error("Binlog engine {} completed with error={}", connectorName, msg, err);
                    } else {
                        log.info("Binlog engine {} completed={}", connectorName, msg);
                    }
                    // 自重启
                    scheduleRestart(ds);
                })
                .build();
        return binlogTaskExecutor.submit(engine);
    }

    private void submitEngine(BinlogProperty.DataSource ds) {
        engines.computeIfAbsent(ds.getServerId(), k -> {
            log.info("submit binlog engine, serverId={}", k);
            return buildAndRunEngine(ds);
        });
    }

    private void scheduleRestart(BinlogProperty.DataSource ds) {
        if (!running.get()) {
            return;
        }

        String serverId = ds.getServerId();
        engines.compute(serverId, (k, existing) ->
            // 延迟执行
            binlogTaskScheduler.schedule(() -> {
                engines.remove(k);
                log.info("Restarting binlog engine for serverId={}", serverId);
                submitEngine(ds);
            }, 5, TimeUnit.SECONDS)
        );
    }

    private void handleEvent(ChangeEvent<String, String> event) {
        final String value = event.value();
        if (CharSequenceUtil.isBlank(value)) {
            return;
        }

        handler.processHandler(event.value());
    }

    private Properties buildProps(BinlogProperty.DataSource ds) {
        final String defaultCluster = kafkaProperty.getDefaultCluster();
        final KafkaMultiClusterConfig kafkaClusterConfig = kafkaProperty.getClusters().get(defaultCluster);
        final String bootstrapServer = kafkaClusterConfig.getBootstrapServers();
        final TopicOffset offset = property.getOffset();
        final TopicHistory history = property.getHistory();

        Properties p = new Properties();
        p.setProperty("name", buildConnectorName(ds.getType(), ds.getServerId()));
        p.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        p.setProperty("bootstrap.servers", bootstrapServer);
        p.setProperty("offset.storage", "org.apache.kafka.connect.storage.KafkaOffsetBackingStore");
        p.setProperty("offset.storage.topic", offset.getTopic());
        p.setProperty("offset.storage.partitions", String.valueOf(offset.getPartitions()));
        p.setProperty("offset.storage.replication.factor", String.valueOf(offset.getReplicationFactor()));
        p.setProperty("schema.history.internal.kafka.bootstrap.servers", bootstrapServer);
        p.setProperty("schema.history.internal.kafka.topic", history.getTopic());
        p.setProperty("schema.history.internal.consumer.override.isolation.level", "read_committed");
        p.setProperty("offset.flush.interval.ms", "5000");
        p.setProperty("offset.flush.timeout.ms", "60000");

        p.setProperty(CommonConnectorConfig.TOPIC_PREFIX.name(), buildTopic(property.getTopicPrefix(), ds.getServerId()));
        p.setProperty(RelationalDatabaseConnectorConfig.HOSTNAME.name(), ds.getHost());
        p.setProperty(BinlogConnectorConfig.PORT.name(), String.valueOf(ds.getPort()));
        p.setProperty(RelationalDatabaseConnectorConfig.USER.name(), ds.getUsername());
        p.setProperty(RelationalDatabaseConnectorConfig.PASSWORD.name(), ds.getPassword());
        p.setProperty(BinlogConnectorConfig.SERVER_ID.name(), ds.getServerId());

        Optional.ofNullable(ds.getDatabaseIncludeList()).filter(CharSequenceUtil::isNotBlank)
            .ifPresent(v -> p.setProperty(MySqlConnectorConfig.DATABASE_INCLUDE_LIST.name(), v));
        Optional.ofNullable(ds.getDatabaseExcludeList()).filter(CharSequenceUtil::isNotBlank)
            .ifPresent(v -> p.setProperty(MySqlConnectorConfig.DATABASE_EXCLUDE_LIST.name(), v));
        Optional.ofNullable(ds.getTableIncludeList()).filter(CharSequenceUtil::isNotBlank)
            .ifPresent(v -> p.setProperty(MySqlConnectorConfig.TABLE_INCLUDE_LIST.name(), v));
        Optional.ofNullable(ds.getTableExcludeList()).filter(CharSequenceUtil::isNotBlank)
            .ifPresent(v -> p.setProperty(MySqlConnectorConfig.TABLE_EXCLUDE_LIST.name(), v));
        // 启动时做一次全量快照，然后转增量
        p.setProperty(MySqlConnectorConfig.SNAPSHOT_MODE.name(), property.getSnapshotMode().name());
        // 锁模式，仅对快照涉及的表加读锁
        p.setProperty(MySqlConnectorConfig.SNAPSHOT_LOCKING_MODE.name(), SnapshotLockingMode.MINIMAL.name());

        // 快照并发
        p.setProperty("snapshot.parallelism", "8");
        // 关闭心跳，减少 sys cpu
        p.setProperty("connect.keep.alive", "false");
        // 过滤 schema 变更，减少 30% 事件，不监听 DDL
        p.setProperty("include.schema.changes", "false");
        // 大字段不拉老值
        p.setProperty("lob.enabled", "false");
        // 性能参数
        p.setProperty("max.batch.size", "32768");
        p.setProperty("max.queue.size", "131072");
        p.setProperty("poll.interval.ms", "50");
        return p;
    }

    private Mono<Void> kafkaTopicAutoCreate() {
        if (Boolean.FALSE.equals(property.getEnabledDynamicRegister())) {
            return Mono.empty();
        }
        final String defaultCluster = kafkaProperty.getDefaultCluster();
        final KafkaMultiClusterConfig kafkaClusterConfig = kafkaProperty.getClusters().get(defaultCluster);
        KafkaMultiClusterConfig config = BeanUtil.copyProperties(kafkaClusterConfig, KafkaMultiClusterConfig.class);
        config.getBindings().addAll(buildTopicConfigs());
        return Mono.fromRunnable(() ->
            KafkaDynamicRegistry.createTopicsIfAbsent(defaultCluster,
                config, kafkaTemplateRouter.getAdmin(defaultCluster), 10)
        ).then();
    }

    private List<KafkaBindingConfig> buildTopicConfigs() {
        List<KafkaBindingConfig> list = property.getDataSources()
            .stream()
            .map(ds -> {
                KafkaBindingConfig cfg = new KafkaBindingConfig();
                cfg.setAutoCreate(Boolean.TRUE);
                cfg.setTopic(buildTopic(property.getTopicPrefix(), ds.getServerId()));
                return cfg;
            })
            .collect(Collectors.toList());

        // offset topic
        TopicOffset offset = property.getOffset();
        KafkaBindingConfig offsetCfg = new KafkaBindingConfig();
        offsetCfg.setAutoCreate(Boolean.TRUE);
        offsetCfg.setTopic(offset.getTopic());
        offsetCfg.setPartitions(offset.getPartitions());
        offsetCfg.setReplicationFactor(offset.getReplicationFactor());
        // 只保留最新一条
        offsetCfg.setArguments(Map.of(
            "cleanup.policy", "compact",
            "retention.ms", "-1"
        ));
        list.add(offsetCfg);

        // history topic
        TopicHistory history = property.getHistory();
        KafkaBindingConfig historyCfg = new KafkaBindingConfig();
        historyCfg.setAutoCreate(Boolean.TRUE);
        historyCfg.setTopic(history.getTopic());
        historyCfg.setPartitions(history.getPartitions());
        historyCfg.setReplicationFactor(history.getReplicationFactor());
        // 超过消息过期时间自动删除
        historyCfg.setArguments(Map.of(
            "cleanup.policy", "delete",
            "retention.ms", "-1"
        ));
        list.add(historyCfg);
        return list;
    }

    private String buildTopic(String topicPrefix, String serverId) {
        return topicPrefix + SymbolConstant.HORIZONTAL_LINE + serverId;
    }

    private String buildConnectorName(String type, String serverId) {
        return type + SymbolConstant.HORIZONTAL_LINE + serverId;
    }
}
