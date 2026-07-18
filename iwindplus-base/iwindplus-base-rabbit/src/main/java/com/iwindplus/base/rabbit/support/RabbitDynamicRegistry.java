/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.rabbit.domain.constant.RabbitConstant;
import com.iwindplus.base.rabbit.domain.enums.RabbitExchangeTypeEnum;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty.RabbitBindingConfig;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty.RabbitConsumerConfig;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty.RabbitMultiClusterConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

/**
 * rabbit动态注册.
 *
 * @author zengdegui
 * @since 2020/4/24
 */
@Slf4j
public class RabbitDynamicRegistry {

    /**
     * 创建绑定关系.
     *
     * @param clusterName   集群名称
     * @param clusterConfig 集群配置
     * @param amqpAdmin     amqpAdmin
     */
    public static void createBindingIfAbsent(String clusterName, RabbitMultiClusterConfig clusterConfig, AmqpAdmin amqpAdmin) {
        final RabbitConsumerConfig consumer = clusterConfig.getConsumer();
        if (consumer == null) {
            return;
        }
        final List<RabbitBindingConfig> bindings = consumer.getBindings();
        if (CollUtil.isEmpty(bindings)) {
            return;
        }

        log.info("Rabbit Created binding cluster={}", clusterName);

        bindings.stream().forEach(config -> {
            try {
                validate(config);

                final Queue queue = convertQueue(config.getQueue());
                final Exchange exchange = convertExchange(config.getExchange());

                // declare（幂等）
                amqpAdmin.declareQueue(queue);
                amqpAdmin.declareExchange(exchange);

                // 绑定（支持多个 routingKey）
                for (String routingKey : config.getRoutingKeys()) {
                    Binding binding = buildBinding(queue, exchange, routingKey, config.getArguments());
                    amqpAdmin.declareBinding(binding);
                }

                log.info("Created binding success for cluster {}: exchange={}, queue={}, routingKey={}",
                    clusterName, exchange.getName(), queue.getName(),
                    config.getRoutingKeys().stream().collect(Collectors.toList()));
            } catch (Exception e) {
                log.warn("Failed to create binding for cluster {}: {}", clusterName, e.getMessage(), e);
            }
        });
    }

    private static void validate(RabbitBindingConfig config) {
        Assert.notNull(config.getExchange(), "exchange is null");
        Assert.notNull(config.getQueue(), "queue is null");
        Assert.isTrue(CollUtil.isNotEmpty(config.getRoutingKeys()), "routingKeys is empty");
    }

    private static Queue convertQueue(RabbitMultiProperty.Queue queue) {
        Map<String, Object> arguments = Optional.ofNullable(queue.getArguments())
            .orElseGet(HashMap::new);

        Object ttl = arguments.get(RabbitConstant.X_MESSAGE_TTL);
        if (ttl != null) {
            arguments.put(RabbitConstant.X_MESSAGE_TTL, Convert.toLong(ttl));
        }
        // 设置队列的优先级
        Object priority = arguments.get(RabbitConstant.X_MAX_PRIORITY);
        if (priority != null) {
            arguments.put(RabbitConstant.X_MAX_PRIORITY, Convert.toInt(priority));
        }
        // 是否需要绑定死信队列
        final Object deadLetterExchange = arguments.get(RabbitConstant.X_DEAD_LETTER_EXCHANGE);
        final Object deadLetterRoutingKey = arguments.get(RabbitConstant.X_DEAD_LETTER_ROUTING_KEY);
        if (Objects.isNull(deadLetterExchange) && Objects.isNull(deadLetterRoutingKey)
            && CharSequenceUtil.isNotBlank(queue.getDeadLetterExchange())
            && CharSequenceUtil.isNotBlank(queue.getDeadLetterRoutingKey())) {
            arguments.putIfAbsent(RabbitConstant.X_DEAD_LETTER_EXCHANGE, queue.getDeadLetterExchange());
            arguments.putIfAbsent(RabbitConstant.X_DEAD_LETTER_ROUTING_KEY, queue.getDeadLetterRoutingKey());
        }

        return new Queue(
            queue.getName(),
            queue.getDurable(),
            queue.getExclusive(),
            queue.getAutoDelete(),
            arguments
        );
    }

    private static Exchange convertExchange(RabbitMultiProperty.Exchange exchange) {
        Map<String, Object> args = Optional.ofNullable(exchange.getArguments()).orElseGet(HashMap::new);

        return switch (exchange.getType()) {
            case DIRECT -> new DirectExchange(exchange.getName(), exchange.getDurable(), exchange.getAutoDelete(), args);
            case TOPIC -> new TopicExchange(exchange.getName(), exchange.getDurable(), exchange.getAutoDelete(), args);
            case FANOUT -> new FanoutExchange(exchange.getName(), exchange.getDurable(), exchange.getAutoDelete(), args);
            case HEADERS -> new HeadersExchange(exchange.getName(), exchange.getDurable(), exchange.getAutoDelete(), args);
            case DELAYED -> {
                args.put(RabbitConstant.X_DELAYED_TYPE, RabbitExchangeTypeEnum.DIRECT.getValue());
                yield new CustomExchange(
                    exchange.getName(),
                    RabbitConstant.X_DELAYED_MESSAGE,
                    exchange.getDurable(),
                    exchange.getAutoDelete(),
                    args
                );
            }
        };
    }

    private static Binding buildBinding(Queue queue, Exchange exchange, String routingKey, Map<String, Object> arguments) {
        Map<String, Object> args = Optional.ofNullable(arguments).orElse(Map.of());

        // FanoutExchange 忽略 routingKey
        String finalRoutingKey = (exchange instanceof FanoutExchange)
            ? ""
            : Optional.ofNullable(routingKey).orElse("");

        return new Binding(
            queue.getName(),
            Binding.DestinationType.QUEUE,
            exchange.getName(),
            finalRoutingKey,
            args
        );
    }
}
