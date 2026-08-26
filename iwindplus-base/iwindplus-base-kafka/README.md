# iwindplus-base-kafka

基于 Spring Kafka 的多集群封装。模块通过 `kafka.multi` 配置管理多个 Kafka 集群，并提供按集群路由的同步发送器、注解式多集群监听器、Topic 绑定及消费者重试配置。

> 自动配置由 `KafkaConfiguration` 提供，并在 `kafka.multi.enabled=true` 时生效；该属性默认开启。

## 依赖

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-kafka</artifactId>
</dependency>
```

## 工作方式

```text
应用启动
   │
   ▼
KafkaConfiguration
   │
   ├─ KafkaClusterManager：创建并管理各集群的 Producer、Consumer、AdminClient
   ├─ KafkaTemplateRouter：根据集群和 Topic 路由发送
   ├─ @KafkaMultiListener：注册多集群监听容器
   └─ KafkaReceiverDispatcher：接收消息并交给监听方法处理
```

## 1. 配置 Kafka 集群

配置前缀为 `kafka.multi`。`default-cluster` 用于无集群参数发送时选择默认集群；`clusters` 的 key 就是代码中传入的集群名称。

```yaml
kafka:
  multi:
    enabled: true
    default-cluster: default
    # 是否根据 bindings 动态注册 Topic
    enabled-dynamic-register: false
    clusters:
      default:
        bootstrap-servers: localhost:9092
        producer:
          enabled: true
          default-topic: demo-topic
          acks: all
          enable-idempotence: true
          retries: 10
          compression-type: lz4
          key-serializer: org.apache.kafka.common.serialization.StringSerializer
          value-serializer: org.apache.kafka.common.serialization.StringSerializer
        consumer:
          enabled: true
          auto-offset-reset: earliest
          enabled-auto-commit: false
          enabled-batch-listener: true
          ack-mode: MANUAL
          max-poll-records: 500
          concurrency: 1
          key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
          value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
          retry-type: FIXED
          retry-attempts: 3
          retry-interval-ms: 10000
          retry-max-interval-ms: 300000
          retry-multiplier: 3.0
          enabled-dlt: false
        bindings:
          - topic: demo-topic
            group: demo-group
            auto-create: false
            partitions: -1
            replication-factor: -1
      secondary:
        bootstrap-servers: localhost:9093
        bindings:
          - topic: secondary-topic
            group: secondary-group
```

### 配置规则

- `clusters` 必须包含 `default-cluster` 指向的集群。
- 生产者、消费者的 `bootstrap-servers` 可以覆盖集群级地址。
- `producer.properties`、`consumer.properties` 可以覆盖模块已映射的 Kafka 原生参数。
- `bindings` 只包含 `auto-create`、`topic`、`group`、`partitions`、`replication-factor` 和 `arguments`；不存在 `group-id`、`config`、`disruptor`、`tracing`、`metrics` 这些层级配置。
- `enabled-dynamic-register=true` 后，模块才会按绑定配置处理 Topic 的动态注册；是否创建由绑定项的 `auto-create` 控制。

## 2. Topic 绑定和动态注册

`KafkaBindingConfig` 中的主要字段如下：

| 属性 | 作用 |
| --- | --- |
| `topic` | Topic 名称 |
| `group` | 该绑定对应的消费组名称 |
| `auto-create` | 是否自动创建 Topic，默认 `true` |
| `partitions` | 分区数，默认 `-1`，交由 Kafka 默认处理 |
| `replication-factor` | 副本数，默认 `-1`，交由 Kafka 默认处理 |
| `arguments` | 创建 Topic 时传递的额外参数 |

建议在生产环境提前创建 Topic，并将 `auto-create` 设为 `false`；只有明确需要由应用管理 Topic 时，才开启动态注册。

## 3. 消息发送

注入 `KafkaTemplateRouter`。它实际提供以下发送方式：

- `send(Message<String> message)`：发送到默认集群。
- `send(String cluster, Message<String> message)`：发送到指定集群。
- `send(String cluster, String topic, Map<String, Object> headers, String message)`：指定集群和 Topic。
- `send(String cluster, String topic, String key, Map<String, Object> headers, String message)`：额外指定消息 Key。

这些方法最终调用 `KafkaTemplate.send(...).get()`，因此是同步发送；发送失败会抛出运行时异常，而不是返回异步 Future。

```java
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final KafkaTemplateRouter kafkaTemplateRouter;

    public void sendToDefaultCluster(String message) {
        Message<String> request = MessageBuilder.withPayload(message)
            .setHeader(KafkaHeaders.TOPIC, "demo-topic")
            .setHeader(KafkaHeaders.KEY, "order-001")
            .build();
        kafkaTemplateRouter.send(request);
    }

    public void sendToCluster(String cluster, String topic, String key, String message) {
        kafkaTemplateRouter.send(
            cluster,
            topic,
            key,
            Map.of("business", "order"),
            message
        );
    }
}
```

发送时 Topic 从 `KafkaHeaders.TOPIC` 或 `topic` 参数取得；Key 使用 `KafkaHeaders.KEY` 或 `key` 参数。发送消息会自动补充集群、Topic、Key 和链路上下文相关 Header。

## 4. 消息消费

使用 `@KafkaMultiListener` 注册监听方法。注解定义如下：

| 属性 | 是否必填 | 说明 |
| --- | --- | --- |
| `cluster` | 否 | 集群名称；为空时使用 `default-cluster` |
| `group` | 是 | 消费组名称 |
| `topics` | 是 | 一个或多个 Topic，支持 `${...}` 和 `#{...}` 占位表达式 |

```java
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DemoKafkaListener {

    @KafkaMultiListener(
        cluster = "default",
        group = "demo-group",
        topics = {"demo-topic"}
    )
    public void onMessage(ConsumerRecord<String, Object> record) {
        log.info("收到 Kafka 消息：topic={}, key={}, value={}",
            record.topic(), record.key(), record.value());
        // 业务处理失败时抛出异常，交由模块配置的错误处理和重试流程处理
    }
}
```

也可以一次监听多个 Topic，或使用配置占位符：

```java
@KafkaMultiListener(
    cluster = "${kafka.multi.default-cluster}",
    group = "order-group",
    topics = {"order-created", "order-paid"}
)
public void onOrderMessage(ConsumerRecord<String, Object> record) {
    // 可通过 record.topic() 区分不同 Topic
}
```

消费者默认开启批量监听，并使用 `MANUAL` Ack 模式；具体容器行为由对应集群的 `consumer.enabled-batch-listener`、`ack-mode`、`ack-count`、`ack-time` 和 `enable-async-acks` 配置决定。

## 5. 消费重试和死信

消费者配置直接提供重试字段，不使用额外的 `retry` 或 `dlq` 子节点：

```yaml
kafka:
  multi:
    clusters:
      default:
        consumer:
          retry-type: FIXED
          retry-attempts: 3
          retry-interval-ms: 10000
          retry-max-interval-ms: 300000
          retry-multiplier: 3.0
          enabled-dlt: true
```

处理方法发生异常时应继续抛出异常，不要在业务层吞掉异常，否则模块无法根据失败状态执行后续错误处理：

```java
@KafkaMultiListener(
    cluster = "default",
    group = "order-group",
    topics = {"order-topic"}
)
public void onOrder(ConsumerRecord<String, Object> record) {
    try {
        orderService.process(record.value());
    } catch (Exception ex) {
        log.error("订单消费失败，topic={}", record.topic(), ex);
        throw ex;
    }
}
```

## 6. 并发、批量和 Ack

消费者并发、批量拉取和确认行为均通过消费者配置控制：

```yaml
kafka:
  multi:
    clusters:
      default:
        consumer:
          concurrency: 3
          max-concurrency: 20
          enabled-batch-listener: true
          ack-mode: MANUAL
          ack-count: 100
          ack-time: 5000
          enable-async-acks: false
          max-poll-records: 500
```

- `concurrency`：初始消费者并发数。
- `max-concurrency`：动态扩缩容允许的最大并发数。
- `enabled-batch-listener`：是否启用批量监听。
- `ack-mode`：容器 Ack 模式，默认 `MANUAL`。
- `ack-count`、`ack-time`：批量确认的数量和时间阈值。
- `enable-async-acks`：是否允许异步 Ack。
- `max-poll-records`：一次 poll 返回的最大记录数。

模块中存在 Kafka Disruptor 事件类型和事件处理器，但当前 Kafka 配置类没有 `disruptor` 配置项，不能通过 `disruptor.enabled` 这类配置开启它。

## 7. 观察和扩展

消费者配置提供 `enabled-observation`，默认开启，用于控制消费请求的观察能力：

```yaml
kafka:
  multi:
    clusters:
      default:
        consumer:
          enabled-observation: true
```

模块还提供 `KafkaMessageHandler`、`KafkaSenderDispatcher`、`KafkaReceiverDispatcher`、`KafkaDynamicRegistry` 等扩展接口。通常业务代码只需要使用 `KafkaTemplateRouter` 和 `@KafkaMultiListener`；需要替换消息分发、动态注册或观察行为时，再实现对应接口并按 Spring Bean 方式接入。

## 8. 使用流程

```text
1. 引入 iwindplus-base-kafka
        │
        ▼
2. 配置 kafka.multi.clusters
   ├─ bootstrap-servers
   ├─ producer / consumer
   └─ bindings
        │
        ▼
3. 发送：KafkaTemplateRouter.send(...)
        │
        ▼
4. 接收：@KafkaMultiListener
        │
        ▼
5. 失败：抛出异常，按 retry-* 配置处理
        │
        ▼
6. 超过重试次数：按 enabled-dlt 配置处理死信
```

## 使用建议

1. **集群名称保持一致**：发送方法、`@KafkaMultiListener.cluster` 和 `clusters` 下的 key 必须一致。
2. **生产环境关闭自动建 Topic**：提前完成 Topic、分区和副本规划，绑定项使用 `auto-create: false`。
3. **不要吞掉消费异常**：业务处理失败时抛出异常，才能进入重试和死信处理流程。
4. **关注同步发送阻塞**：`KafkaTemplateRouter` 当前发送实现会等待发送结果；高吞吐场景应结合业务线程模型评估调用位置。
5. **合理设置 poll 参数**：批量处理耗时较长时，应同步评估 `max-poll-records` 和 `max-poll-interval-ms`。
6. **谨慎开启异步 Ack**：只有确认业务能够处理乱序确认时，才设置 `enable-async-acks: true`。
7. **自定义参数最后覆盖**：`producer.properties` 和 `consumer.properties` 会覆盖模块已生成的同名 Kafka 原生配置，修改时应确认最终生效值。