/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.listener;

import static cn.hutool.core.exceptions.ExceptionUtil.unwrap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant.BizRetryHeaderConstant;
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerKeyDTO;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaConsumerConfig;
import com.iwindplus.base.kafka.support.KafkaDlqHandler;
import com.iwindplus.base.kafka.support.KafkaMessageHandler;
import com.iwindplus.base.kafka.support.KafkaMetrics;
import com.iwindplus.base.kafka.support.KafkaReceiverDispatcher;
import com.iwindplus.base.kafka.support.KafkaRetryHandler;
import com.iwindplus.base.kafka.support.ReactiveKafkaMessageHandler;
import com.iwindplus.base.kafka.support.ReactiveKafkaReceiverDispatcher;
import com.iwindplus.base.util.JacksonUtil;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * kafka统一注册器.
 *
 * @author zengdegui
 * @since 2026/03/26 00:58
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMultiListenerRegistrar implements SmartLifecycle, DisposableBean {

    private final KafkaMultiListenerBeanPostProcessor bpp;
    private final KafkaClusterManager clusterManager;
    private final KafkaReceiverDispatcher dispatcher;
    private final ReactiveKafkaReceiverDispatcher reactiveDispatcher;
    private final KafkaRetryHandler retryHandler;
    private final KafkaDlqHandler dlqHandler;
    private final KafkaMetrics kafkaMetrics;

    private final Map<Method, BeanInvoker> invokerCache = new ConcurrentHashMap<>(16);
    private final Map<Method, ArgBuilder[]> argCache = new ConcurrentHashMap<>(16);

    private final Map<String, AbstractMessageListenerContainer<String, Object>> containerMap = new ConcurrentHashMap<>(16);
    private final Map<String, Disposable> reactiveContainerMap = new ConcurrentHashMap<>(16);
    private final Map<String, Disposable> reactiveRetryContainerMap = new ConcurrentHashMap<>(16);

    private final Cache<Class<?>, ObjectReader> readerCache =
        Caffeine.newBuilder()
            .maximumSize(1024)
            .build();

    private volatile boolean running;

    @Override
    public void start() {
        List<KafkaMultiListenerMetaDTO> metas = bpp.getMetadata().stream().map(this::resolve).toList();
        if (metas.isEmpty()) {
            log.warn("No Kafka listeners found");
            return;
        }

        preWarm(metas);
        registerAll(metas);
        registerReactiveRetry(clusterManager);

        running = true;
    }

    @Override
    public void stop() {
        running = false;

        containerMap.forEach((id, c) -> {
            try {
                c.stop();
                c.destroy();
                log.info("Kafka listener stopped: {}", id);
            } catch (Exception e) {
                log.error("Stop kafka listener failed: {}", id, e);
            }
        });

        reactiveContainerStop(reactiveContainerMap);
        reactiveContainerStop(reactiveRetryContainerMap);
    }

    @Override
    public void destroy() {
        stop();

        containerMap.clear();
        reactiveContainerMap.clear();
        reactiveRetryContainerMap.clear();
        invokerCache.clear();
        argCache.clear();
        readerCache.invalidateAll();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 100;
    }

    private void reactiveContainerStop(Map<String, Disposable> containerMap) {
        containerMap.forEach((id, d) -> {
            try {
                if (d != null && !d.isDisposed()) {
                    d.dispose();
                }
                log.info("Reactive kafka listener stopped: {}", id);
            } catch (Exception e) {
                log.error("Stop reactive kafka listener failed: {}", id, e);
            }
        });
    }

    private void preWarm(List<KafkaMultiListenerMetaDTO> metas) {
        for (KafkaMultiListenerMetaDTO meta : metas) {
            Method m = meta.getMethod();
            invokerCache.computeIfAbsent(m, k -> createInvoker(m, meta.getBean()));
            argCache.computeIfAbsent(m, this::buildArgBuilders);
            warmReader(m);
        }
    }

    private void warmReader(Method method) {
        Class<?>[] types = method.getParameterTypes();
        Type[] generics = method.getGenericParameterTypes();

        for (int i = 0; i < types.length; i++) {

            Class<?> c = List.class.isAssignableFrom(types[i])
                ? extractGeneric(generics[i])
                : types[i];

            if (c == ConsumerRecord.class
                || c == ReceiverRecord.class
                || c == Message.class
                || c == Acknowledgment.class) {
                continue;
            }

            readerCache.get(c, k -> JacksonUtil.getMapper().readerFor(k));
        }
    }

    private void registerAll(List<KafkaMultiListenerMetaDTO> metas) {
        Map<KafkaConsumerKeyDTO, List<KafkaMultiListenerMetaDTO>> grouped = group(metas);

        int count = 0;

        for (var entry : grouped.entrySet()) {
            KafkaMultiListenerMetaDTO meta = merge(entry.getValue());
            String id = buildId(meta);
            if (Boolean.TRUE.equals(meta.getReactive())) {
                registerReactive(id, meta);
            } else {
                registerSync(id, meta);
            }

            count++;
        }

        log.info("Kafka listeners registered, sourceSize={}, listenerSize={}", metas.size(), count);
    }

    private void registerSync(String id, KafkaMultiListenerMetaDTO meta) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            clusterManager.getFactory(meta.getCluster());

        if (factory == null) {
            throw new IllegalStateException("Kafka factory not found, cluster=" + meta.getCluster());
        }

        KafkaMultiProperty property = clusterManager.getProperty();

        AbstractMessageListenerContainer<String, Object> container =
            factory.createContainer(meta.getTopics());

        container.setBeanName(id);

        ContainerProperties p = container.getContainerProperties();

        p.setGroupId(meta.getGroup());
        p.setClientId(
            property.buildClientId(
                meta.getCluster(),
                meta.getGroup(),
                KafkaConstant.CONSUMER_SUFFIX
            )
        );

        boolean batch = property.getEnabledBatchListener(meta.getCluster());
        final AckMode ackMode = p.getAckMode();
        boolean manualAck = AckMode.MANUAL.equals(ackMode)
            || AckMode.MANUAL_IMMEDIATE.equals(ackMode);
        boolean hasAck = Arrays.stream(meta.getMethod().getParameterTypes())
            .anyMatch(Acknowledgment.class::isAssignableFrom);
        if (manualAck && !hasAck) {
            throw new IllegalStateException("AckMode.MANUAL requires Acknowledgment parameter, method=" + meta.getMethod());
        }

        if (batch) {
            if (manualAck) {
                p.setMessageListener((BatchAcknowledgingMessageListener<String, Object>)
                    (records, ack) -> dispatch(meta, records, ack)
                );
            } else {
                p.setMessageListener((BatchMessageListener<String, Object>)
                    records -> dispatch(meta, records, null)
                );
            }

        } else {
            if (manualAck) {
                p.setMessageListener((AcknowledgingMessageListener<String, Object>)
                    (record, ack) -> dispatch(meta, Collections.singletonList(record), ack)
                );

            } else {
                p.setMessageListener((MessageListener<String, Object>)
                    record -> dispatch(meta, Collections.singletonList(record), null)
                );
            }
        }

        container.start();
        containerMap.put(id, container);

        log.info("Kafka listener started, cluster={}, group={}, topics={}, id={}",
            meta.getCluster(), meta.getGroup(), Arrays.toString(meta.getTopics()), id);
    }

    private void registerReactive(String id, KafkaMultiListenerMetaDTO meta) {
        int concurrency = Math.max(1, clusterManager.getReactiveConcurrency(meta.getCluster()));

        ReceiverOptions<String, Object> receiverOptions =
            clusterManager.getReceiverOptions(meta.getCluster(), meta.getGroup())
                .subscription(Arrays.asList(meta.getTopics()));

        Disposable disposable =
            KafkaReceiver.create(receiverOptions)
                .receive()
                .groupBy(r -> r.receiverOffset().topicPartition())
                .flatMap(partitionFlux ->
                    partitionFlux
                        .bufferTimeout(
                            500,
                            Duration.ofMillis(50))
                        .concatMap(records ->
                            processReactiveRecordBatch(meta, records)
                        )
                )
                .subscribe(
                    null,
                    e ->
                        log.error(
                            "Reactive kafka consumer terminated cluster={}, group={}",
                            meta.getCluster(),
                            meta.getGroup(),
                            e
                        )
                );

        reactiveContainerMap.put(id, disposable);

        log.info(
            "Reactive kafka listener started cluster={}, group={}, topics={}, concurrency={}, id={}",
            meta.getCluster(),
            meta.getGroup(),
            Arrays.toString(meta.getTopics()),
            concurrency,
            id
        );
    }

    private void registerReactiveRetry(KafkaClusterManager clusterManager) {
        final KafkaMultiProperty property = clusterManager.getProperty();
        final Set<String> clusterSet = property.getClusters().keySet();

        for (String cluster : clusterSet) {
            final KafkaConsumerConfig consumerConfig = clusterManager.getConsumerConfig(cluster);
            if (Boolean.FALSE.equals(consumerConfig.getEnabledBizRetry())) {
                continue;
            }

            Set<String> topics = new HashSet<>(16);
            for (int i = 1; i <= retryHandler.maxRetries(cluster); i++) {
                topics.add(retryHandler.retryTopic(cluster, i));
            }

            if (topics.isEmpty()) {
                continue;
            }

            String group = consumerConfig.getBizRetryGroup();
            ReceiverOptions<String, Object> receiverOptions =
                clusterManager.getReceiverOptions(cluster, group)
                    .subscription(topics);

            KafkaMultiListenerMetaDTO meta = KafkaMultiListenerMetaDTO.builder()
                .bean(null)
                .method(null)
                .cluster(cluster)
                .topics(topics.toArray(new String[0]))
                .group(group)
                .reactive(true)
                .build();

            Disposable disposable =
                KafkaReceiver.create(receiverOptions)
                    .receive()
                    .groupBy(r -> r.receiverOffset().topicPartition())
                    .flatMap(partitionFlux ->
                        partitionFlux
                            .bufferTimeout(
                                500,
                                Duration.ofMillis(50))
                            .concatMap(records ->
                                processRetryRecordBatch(meta, records)
                            )
                    )
                    .subscribe(
                        null,
                        e ->
                            log.error(
                                "Retry kafka consumer terminated cluster={}, group={}",
                                meta.getCluster(),
                                meta.getGroup(),
                                e
                            )
                    );

            reactiveRetryContainerMap.put(cluster, disposable);

            log.info(
                "Retry consumer started cluster={}, topics={}",
                cluster,
                topics
            );
        }
    }

    private void dispatch(KafkaMultiListenerMetaDTO meta, List<ConsumerRecord<String, Object>> messages, Acknowledgment acknowledgment) {
        dispatcher.dispatch(
            new KafkaMessageHandler(meta.getCluster(), meta.getTopics(), meta.getGroup(),
                ignored -> invokeSync(meta, messages, acknowledgment)),
            messages);
    }

    private Mono<Void> dispatchReactive(KafkaMultiListenerMetaDTO meta, List<ReceiverRecord<String, Object>> messages) {
        return reactiveDispatcher.dispatch(
            new ReactiveKafkaMessageHandler(meta.getCluster(), meta.getTopics(), meta.getGroup(),
                ignored -> invokeReactive(meta, messages)),
            messages);
    }

    private void invokeSync(
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> records,
        Acknowledgment ack) {

        Method m = meta.getMethod();
        Object[] args = buildArgs(
            argCache.computeIfAbsent(m, this::buildArgBuilders),
            records,
            ack
        );

        try {
            invokerCache.computeIfAbsent(m, x -> createInvoker(m, meta.getBean()))
                .invoke(args);
        } catch (Throwable e) {
            try {
                handleSyncError(meta, records, e, ack);
            } catch (Exception ex) {
                log.error("Sync kafka listener error", ex);
            }
        }
    }

    private Mono<Void> invokeReactive(
        KafkaMultiListenerMetaDTO meta,
        List<ReceiverRecord<String, Object>> records) {

        Method m = meta.getMethod();

        return Mono.defer(() -> {
            try {
                Object[] args = buildArgs(
                    argCache.computeIfAbsent(m, this::buildArgBuilders),
                    records,
                    null
                );

                Object result = invokerCache.computeIfAbsent(m,
                        x -> createInvoker(m, meta.getBean()))
                    .invoke(args);
                if (result instanceof Mono<?> mono) {
                    return mono.then();
                }

                if (result instanceof Flux<?> flux) {
                    return flux.then();
                }

                return Mono.empty();
            } catch (Throwable e) {
                return Mono.error(e);
            }
        });
    }

    private void handleSyncError(
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> records,
        Throwable error,
        Acknowledgment ack) {

        KafkaConsumerConfig consumerConfig = clusterManager.getConsumerConfig(meta.getCluster());
        boolean enableRetry = Boolean.TRUE.equals(consumerConfig.getEnabledBizRetry());
        boolean enableDlq = Boolean.TRUE.equals(consumerConfig.getEnabledBizDlq());
        if (!enableRetry && !enableDlq) {
            log.warn(
                "Kafka consumer failed and retry/dlq disabled, cluster={}, group={}, topics={}",
                meta.getCluster(),
                meta.getGroup(),
                Arrays.toString(meta.getTopics()),
                error
            );

            return;
        }

        Throwable ex = unwrap(error);

        // 全部处理成功才Ack
        handleSyncRecordErrors(meta, records, ex, ack);
    }

    private void handleSyncRecordErrors(
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> records,
        Throwable error,
        Acknowledgment ack) {

        KafkaConsumerConfig consumerConfig = clusterManager.getConsumerConfig(meta.getCluster());
        boolean enableRetry = Boolean.TRUE.equals(consumerConfig.getEnabledBizRetry());
        boolean enableDlq = Boolean.TRUE.equals(consumerConfig.getEnabledBizDlq());
        if (!enableRetry && !enableDlq) {
            return;
        }

        List<ProducerRecord<String, Object>> producerRecords = new ArrayList<>(10);
        for (ConsumerRecord<String, Object> record : records) {
            ErrorContext ctx = buildErrorContext(meta.getCluster(), record, error);
            HandleResult result = decideHandleResult(enableRetry, enableDlq, ctx);
            if (result == HandleResult.FAIL) {
                throw new RuntimeException(error);
            }

            producerRecords.add(buildRetryDlQProducerRecord(meta, record, ctx, result));
        }

        if (CollUtil.isEmpty(producerRecords)) {
            return;
        }

        sendRetryDlQBatch(meta.getCluster(), producerRecords)
            .block(Duration.ofMillis(clusterManager.getProperty().getSendTimeoutMs(meta.getCluster())));

        if (ack != null) {
            ack.acknowledge();
        }
    }

    private Mono<Void> processReactiveRecordBatch(
        KafkaMultiListenerMetaDTO meta,
        List<ReceiverRecord<String, Object>> records) {
        return dispatchReactive(meta, records)
            // 整个 Batch 成功
            .then(commit(records))
            // Batch 执行异常
            .onErrorResume(error ->
                handleReactiveBatchError(
                    meta,
                    records,
                    unwrap(error)
                ));
    }

    private Mono<Void> handleReactiveBatchError(
        KafkaMultiListenerMetaDTO meta,
        List<ReceiverRecord<String, Object>> records,
        Throwable error) {

        KafkaConsumerConfig consumerConfig = clusterManager.getConsumerConfig(meta.getCluster());
        boolean enableRetry = Boolean.TRUE.equals(consumerConfig.getEnabledBizRetry());
        boolean enableDlq = Boolean.TRUE.equals(consumerConfig.getEnabledBizDlq());
        if (!enableRetry && !enableDlq) {
            return Mono.error(error);
        }

        List<ProducerRecord<String, Object>> producerRecords =
            new ArrayList<>(records.size());

        for (ReceiverRecord<String, Object> record : records) {
            ErrorContext ctx = buildErrorContext(meta.getCluster(), record, error);
            HandleResult result = decideHandleResult(enableRetry, enableDlq, ctx);
            if (result == HandleResult.FAIL) {
                return Mono.error(ctx.error());
            }

            producerRecords.add(buildRetryDlQProducerRecord(meta, record, ctx, result));
        }

        return sendRetryDlQBatch(meta.getCluster(), producerRecords)
            .then(commit(records));
    }

    private ProducerRecord<String, Object> buildRetryDlQProducerRecord(
        KafkaMultiListenerMetaDTO meta,
        ConsumerRecord<String, Object> record,
        ErrorContext ctx,
        HandleResult result) {
        String topic;
        Object value;
        Iterable<Header> headers = null;
        switch (result) {
            case RETRY -> {
                topic = retryHandler.retryTopic(meta.getCluster(), ctx.nextRetry());
                Map<String, byte[]> retryHeaders =
                    retryHandler.buildRetryHeaders(
                        meta,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        ctx.nextRetry(),
                        ctx.firstFailTime(),
                        ctx.error());
                headers = buildHeaders(retryHeaders);
                value = record.value();
            }

            case DLQ -> {
                topic = dlqHandler.dlqTopic(meta.getCluster(), record.topic());
                value = dlqHandler.buildDlqPayload(
                    meta,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.value(),
                    ctx.retryCount(),
                    ctx.firstFailTime(),
                    ctx.error());
            }

            default -> throw new IllegalStateException("Unsupported HandleResult: " + result);
        }

        return new ProducerRecord<>(
            topic,
            null,
            record.key(),
            value,
            headers);
    }

    private Mono<Void> sendRetryDlQBatch(
        String cluster,
        List<ProducerRecord<String, Object>> records) {

        KafkaTemplate<String, Object> template =
            clusterManager.getTemplate(cluster);

        // 启动计时器
        Timer.Sample sample = kafkaMetrics.startTimer();
        String topic = records.isEmpty() ? "unknown" : records.get(0).topic();
        final String metricName = CharSequenceUtil.isNotBlank(topic)
            && topic.contains(KafkaConstant.BizRetryConstant.KAFKA_DLQ)
            ? KafkaConstant.MetricName.DLQ_SEND_DURATION
            : KafkaConstant.MetricName.RETRY_SEND_DURATION;

        List<CompletableFuture<SendResult<String, Object>>> futures = new ArrayList<>(10);
        for (ProducerRecord<String, Object> record : records) {
            CompletableFuture<SendResult<String, Object>> future = template.send(record);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error(
                        "Kafka retry/dlq send failed. topic={}, key={}",
                        record.topic(),
                        record.key(),
                        ex);
                    // 记录失败指标
                    if (CharSequenceUtil.isNotBlank(record.topic())
                        && record.topic().contains(KafkaConstant.BizRetryConstant.KAFKA_DLQ)) {
                        kafkaMetrics.recordDlqSendFailure(cluster, record.topic());
                    } else {
                        kafkaMetrics.recordRetrySendFailure(cluster, record.topic());
                    }
                } else {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.debug(
                        "Kafka retry/dlq send success. topic={}, partition={}, offset={}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset());
                    // 记录成功指标
                    if (CharSequenceUtil.isNotBlank(record.topic())
                        && record.topic().contains(KafkaConstant.BizRetryConstant.KAFKA_DLQ)) {
                        kafkaMetrics.recordDlqSendSuccess(cluster, record.topic());
                    } else {
                        kafkaMetrics.recordRetrySendSuccess(cluster, record.topic());
                    }
                }
            });

            futures.add(future);
        }

        return Mono.fromFuture(
                CompletableFuture.allOf(
                    futures.toArray(CompletableFuture[]::new)))
            .doOnSuccess(v -> kafkaMetrics.stopTimer(sample, cluster, topic, metricName))
            .doOnError(e -> kafkaMetrics.stopTimer(sample, cluster, topic, metricName))
            .then();
    }

    private Mono<Void> commit(
        List<ReceiverRecord<String, Object>> records) {
        if (records.isEmpty()) {
            return Mono.empty();
        }

        ReceiverOffset offset =
            records.get(records.size() - 1)
                .receiverOffset();

        return offset.commit();
    }

    private int getRetryCount(ConsumerRecord<String, Object> record) {
        try {
            var h = record.headers().lastHeader(BizRetryHeaderConstant.RETRY_COUNT_HEADER);
            if (h == null) {
                return 0;
            }
            return Integer.parseInt(new String(h.value(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return 0;
        }
    }

    private long getFirstFailTime(ConsumerRecord<String, Object> record) {
        try {
            var h = record.headers().lastHeader(BizRetryHeaderConstant.FIRST_FAIL_TIME_HEADER);
            if (h == null) {
                return System.currentTimeMillis();
            }

            return Long.parseLong(new String(h.value(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private String getString(ConsumerRecord<String, Object> record, String key) {
        try {
            var h = record.headers().lastHeader(key);
            return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private Mono<Void> processRetryRecordBatch(
        KafkaMultiListenerMetaDTO meta,
        List<ReceiverRecord<String, Object>> records) {

        if (CollUtil.isEmpty(records)) {
            return Mono.empty();
        }

        List<ProducerRecord<String, Object>> producerRecords =
            new ArrayList<>(records.size());
        for (ReceiverRecord<String, Object> record : records) {
            ProducerRecord<String, Object> pr = buildOriginProducerRecord(record);
            if (pr != null) {
                producerRecords.add(pr);
            }
        }

        if (CollUtil.isEmpty(producerRecords)) {
            return Mono.empty();
        }

        return resendToOriginTopicBatch(
            meta.getCluster(),
            producerRecords)
            .then(commit(records));
    }

    private ProducerRecord<String, Object> buildOriginProducerRecord(
        ReceiverRecord<String, Object> record) {
        String originTopic = getString(record, BizRetryHeaderConstant.ORIGIN_TOPIC_HEADER);
        if (CharSequenceUtil.isBlank(originTopic)) {
            log.warn("Missing origin topic, skip record");
            return null;
        }

        return new ProducerRecord<>(
            originTopic,
            null,
            record.key(),
            record.value(),
            record.headers()
        );
    }

    private Mono<Void> resendToOriginTopicBatch(
        String cluster,
        List<ProducerRecord<String, Object>> producerRecords) {

        if (CollUtil.isEmpty(producerRecords)) {
            return Mono.empty();
        }

        // 启动计时器
        Timer.Sample sample = kafkaMetrics.startTimer();
        String topic = producerRecords.isEmpty() ? "unknown" : producerRecords.get(0).topic();
        final String metricName = CharSequenceUtil.isNotBlank(topic)
            && topic.contains(KafkaConstant.BizRetryConstant.KAFKA_RETRY)
            ? KafkaConstant.MetricName.RETRY_SEND_DURATION
            : KafkaConstant.MetricName.CONSUME_PROCESS_DURATION;

        KafkaTemplate<String, Object> kafkaTemplate = clusterManager.getTemplate(cluster);
        List<CompletableFuture<SendResult<String, Object>>> futures = new ArrayList<>(producerRecords.size());

        for (ProducerRecord<String, Object> producerRecord : producerRecords) {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(producerRecord);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error(
                        "Kafka resend to origin topic failed. topic={}, key={}",
                        producerRecord.topic(),
                        producerRecord.key(),
                        ex);
                    kafkaMetrics.recordRetrySendFailure(cluster, producerRecord.topic());
                } else {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.debug(
                        "Kafka resend to origin topic success. topic={}, partition={}, offset={}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset());
                    kafkaMetrics.recordRetrySendSuccess(cluster, producerRecord.topic());
                }
            });

            futures.add(future);
        }

        return Mono.fromFuture(
                CompletableFuture.allOf(
                    futures.toArray(CompletableFuture[]::new)))
            .doOnSuccess(v -> kafkaMetrics.stopTimer(sample, cluster, topic, metricName))
            .doOnError(e -> kafkaMetrics.stopTimer(sample, cluster, topic, metricName))
            .then();
    }

    private ErrorContext buildErrorContext(
        String cluster,
        ConsumerRecord<String, Object> record,
        Throwable error) {

        Throwable ex = unwrap(error);
        int retryCount = getRetryCount(record);
        int nextRetry = retryHandler.nextRetryCount(retryCount);
        long firstFailTime = getFirstFailTime(record);
        boolean retryable = retryHandler.isRetryable(cluster, retryCount);

        return new ErrorContext(ex, retryCount, nextRetry, firstFailTime, retryable);
    }

    private HandleResult decideHandleResult(
        boolean enableRetry,
        boolean enableDlq,
        ErrorContext ctx) {

        if (ctx.retryable() && enableRetry) {
            return HandleResult.RETRY;
        }

        if (enableDlq) {
            return HandleResult.DLQ;
        }

        return HandleResult.FAIL;
    }

    private enum HandleResult {
        RETRY,
        DLQ,
        FAIL
    }

    private record ErrorContext(
        Throwable error,
        int retryCount,
        int nextRetry,
        long firstFailTime,
        boolean retryable) {

    }

    private Iterable<Header> buildHeaders(Map<String, byte[]> map) {
        List<Header> headers = new ArrayList<>(map.size());

        map.forEach((k, v) ->
            headers.add(new RecordHeader(k, v))
        );

        return headers;
    }

    private Object[] buildArgs(
        ArgBuilder[] builders,
        List<?> records,
        Acknowledgment ack) {

        Object[] args = new Object[builders.length];
        for (int i = 0; i < builders.length; i++) {
            Object arg = builders[i].build(records);
            if (arg == null && builders[i] instanceof AckArgBuilder) {
                arg = ack;
            }

            args[i] = arg;
        }

        return args;
    }

    private ArgBuilder[] buildArgBuilders(Method m) {
        Class<?>[] t = m.getParameterTypes();
        Type[] g = m.getGenericParameterTypes();

        ArgBuilder[] arr = new ArgBuilder[t.length];

        for (int i = 0; i < t.length; i++) {
            arr[i] = createBuilder(t[i], g[i]);
        }

        return arr;
    }

    private ArgBuilder createBuilder(Class<?> type, Type generic) {
        if (Acknowledgment.class == type) {
            return new AckArgBuilder();
        }

        if (Message.class.isAssignableFrom(type)) {
            return records -> MessageBuilder.withPayload(extractValue(records.get(0))).build();
        }

        if (type == ConsumerRecord.class
            || type == ReceiverRecord.class) {
            return records -> records.get(0);
        }

        if (List.class.isAssignableFrom(type)) {
            Class<?> clazz = extractGeneric(generic);

            if (clazz == ConsumerRecord.class
                || clazz == ReceiverRecord.class) {
                return records -> records;
            }

            if (Message.class.isAssignableFrom(clazz)) {
                return records -> records.stream()
                    .map(x -> MessageBuilder.withPayload(extractValue(x)).build())
                    .toList();
            }

            ObjectReader reader = getReader(clazz);

            return records -> {
                List<Object> list = new ArrayList<>(records.size());
                for (Object x : records) {
                    list.add(read(extractValue(x), reader));
                }
                return list;
            };
        }

        ObjectReader reader = getReader(type);

        return records -> read(extractValue(records.get(0)), reader);
    }

    private Object extractValue(Object obj) {
        if (obj instanceof ConsumerRecord<?, ?> r) {
            return r.value();
        }

        if (obj instanceof ReceiverRecord<?, ?> r) {
            return r.value();
        }

        return obj;
    }

    private Class<?> extractGeneric(Type type) {
        if (type instanceof ParameterizedType pt) {
            Type actual = pt.getActualTypeArguments()[0];
            if (actual instanceof Class<?> c) {
                return c;
            }

            if (actual instanceof ParameterizedType p
                && p.getRawType() instanceof Class<?> c) {
                return c;
            }
        }

        return Object.class;
    }

    private ObjectReader getReader(Class<?> clazz) {
        return readerCache.get(clazz,
            c -> JacksonUtil.getMapper().readerFor(c));
    }

    private Object read(Object value, ObjectReader reader) {
        try {
            if (value instanceof byte[] bytes) {
                return reader.readValue(bytes);
            }

            if (value instanceof String str) {
                return reader.readValue(str);
            }

            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BeanInvoker createInvoker(Method m, Object bean) {
        try {
            MethodHandle handle = MethodHandles
                .privateLookupIn(bean.getClass(), MethodHandles.lookup())
                .unreflect(m)
                .bindTo(bean);

            return handle.asSpreader(Object[].class, m.getParameterCount())::invoke;

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private KafkaMultiListenerMetaDTO resolve(KafkaMultiListenerMetaDTO meta) {
        String cluster = CharSequenceUtil.isBlank(meta.getCluster())
            ? clusterManager.getDefaultCluster()
            : meta.getCluster();

        String group = clusterManager.getGroup(cluster, meta.getGroup());

        boolean reactive =
            Mono.class.isAssignableFrom(meta.getMethod().getReturnType())
                || Flux.class.isAssignableFrom(meta.getMethod().getReturnType());

        return KafkaMultiListenerMetaDTO.builder()
            .bean(meta.getBean())
            .method(meta.getMethod())
            .cluster(cluster)
            .topics(meta.getTopics())
            .group(group)
            .reactive(reactive)
            .build();
    }

    private KafkaMultiListenerMetaDTO merge(List<KafkaMultiListenerMetaDTO> list) {
        KafkaMultiListenerMetaDTO first = list.get(0);

        String[] topics = list.stream()
            .flatMap(x -> Arrays.stream(x.getTopics()))
            .filter(CharSequenceUtil::isNotBlank)
            .distinct()
            .toArray(String[]::new);

        return KafkaMultiListenerMetaDTO.builder()
            .bean(first.getBean())
            .method(first.getMethod())
            .cluster(first.getCluster())
            .group(first.getGroup())
            .topics(topics)
            .reactive(first.getReactive())
            .build();
    }

    private Map<KafkaConsumerKeyDTO, List<KafkaMultiListenerMetaDTO>> group(List<KafkaMultiListenerMetaDTO> metas) {
        Map<KafkaConsumerKeyDTO, List<KafkaMultiListenerMetaDTO>> grouped = new HashMap<>(16);

        for (KafkaMultiListenerMetaDTO meta : metas) {
            KafkaConsumerKeyDTO key = new KafkaConsumerKeyDTO(meta.getCluster(), meta.getGroup());

            grouped.computeIfAbsent(key, k -> new ArrayList<>(10))
                .add(meta);
        }

        return grouped;
    }

    private String buildId(KafkaMultiListenerMetaDTO meta) {
        String str = meta.getMethod().toGenericString()
            + SymbolConstant.WELL_NO
            + String.join(SymbolConstant.COMMA, meta.getTopics());

        return "kafka"
            + SymbolConstant.HORIZONTAL_LINE + meta.getCluster()
            + SymbolConstant.HORIZONTAL_LINE + meta.getGroup()
            + SymbolConstant.HORIZONTAL_LINE + SecureUtil.md5(str);
    }

    @FunctionalInterface
    private interface ArgBuilder {

        Object build(List<?> records);
    }

    private static final class AckArgBuilder implements ArgBuilder {

        @Override
        public Object build(List<?> records) {
            return null;
        }
    }

    @FunctionalInterface
    private interface BeanInvoker {

        Object invoke(Object[] args) throws Throwable;
    }
}