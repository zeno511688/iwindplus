# RocketMQ 多集群模块（iwindplus-base-rocket）

本模块基于 RocketMQ Client 封装多 NameServer 集群、同步/异步消息发送、Topic/Tag 监听、集群消费与广播消费、顺序消费以及消息 Observation。

```text
rocket.multi
  ├── clusters
  │     ├── default ── NameServer + Producer + Consumer
  │     └── backup  ── NameServer + Producer + Consumer
  ├── RocketTemplateRouter ── 同步/异步发送
  └── @RocketMultiListener ── Topic + Tag 消费
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-rocket</artifactId>
</dependency>
```

模块通过自动配置创建各集群的 RocketMQ Producer、Consumer 和监听注册器。RocketMQ NameServer 必须先可用。

## 2. 基础配置

配置前缀是 `rocket.multi`：

```yaml
rocket:
  multi:
    enabled: true
    default-cluster: default
    clusters:
      default:
        name-server: 127.0.0.1:9876
      backup:
        name-server: 10.0.0.11:9876;10.0.0.12:9876
```

每个集群都可以单独配置生产者和消费者：

```yaml
rocket:
  multi:
    clusters:
      default:
        name-server: 127.0.0.1:9876
        producer:
          enabled: true
          enabled-observation: true
          group: user-producer
          send-msg-timeout: 1000
          retry-times-when-send-failed: 3
          retry-times-when-send-async-failed: 3
          retry-another-broker-when-not-store-ok: true
        consumer:
          enabled: true
          enabled-observation: true
          group: user-consumer
          message-model: CLUSTERING
          consume-from-where: CONSUME_FROM_LAST_OFFSET
          consume-timeout: 10
          consume-thread-min: 4
          consume-thread-max: 32
          consume-message-batch-max-size: 10
          max-reconsume-times: 16
          orderly: false
```

`default-cluster` 用于发送方法未指定集群或监听注解 `cluster` 为空的场景。集群名称必须存在于 `clusters` 中。

## 3. 绑定配置

集群下的 `bindings` 用于描述 Topic、Tag 和消费组：

```yaml
rocket:
  multi:
    clusters:
      default:
        bindings:
          - topic: user-event
            tag: user.created || user.updated
            group: user-consumer
```

`tag` 支持：

- `*`：监听 Topic 下全部 Tag；
- 单个 Tag，例如 `user.created`；
- 多个 Tag，使用 `||` 分隔，例如 `user.created || user.updated`。

绑定配置用于统一管理消费关系；监听方法本身也可以直接通过注解指定 Topic、Tag 和消费组。

## 4. 同步发送

注入 `RocketTemplateRouter`：

```java
@Resource
private RocketTemplateRouter rocketTemplateRouter;

public SendResult publish(UserCreatedMessage message) {
    return rocketTemplateRouter.send(
        "user-event",
        "user.created",
        message
    );
}
```

指定集群：

```java
SendResult result = rocketTemplateRouter.send(
    "backup",
    "user-event",
    "user.created",
    message
);
```

携带扩展 Header：

```java
Map<String, Object> headers = Map.of(
    "tenantId", tenantId,
    "source", "user-service"
);

SendResult result = rocketTemplateRouter.send(
    "default",
    "user-event",
    "user.created",
    message,
    headers
);
```

同步发送返回 RocketMQ 原生 `SendResult`，业务可以根据发送状态、消息 ID 和队列信息记录发送结果。消息发送过程由模块统一增加 Observation。

## 5. 异步发送

异步发送使用 RocketMQ 原生 `SendCallback`：

```java
rocketTemplateRouter.send(
    "default",
    "user-event",
    "user.created",
    message,
    headers,
    new SendCallback() {
        @Override
        public void onSuccess(SendResult result) {
            // 发送成功
        }

        @Override
        public void onException(Throwable exception) {
            // 发送失败
        }
    }
);
```

异步发送方法返回 `void`，成功和失败必须在回调中处理。回调中的业务逻辑需要自行保证幂等。

## 6. 生产者配置

```yaml
rocket:
  multi:
    clusters:
      default:
        producer:
          enabled: true
          enabled-observation: true
          group: user-producer
          thread-pool-name: rocket-send-pool
          send-msg-timeout: 1000
          retry-times-when-send-failed: 3
          retry-times-when-send-async-failed: 3
          retry-another-broker-when-not-store-ok: true
```

主要字段：

- `group`：生产者组；为空时框架根据当前 Profile、应用名和集群生成默认组；
- `send-msg-timeout`：发送超时；
- `retry-times-when-send-failed`：同步发送失败重试次数；
- `retry-times-when-send-async-failed`：异步发送失败重试次数；
- `retry-another-broker-when-not-store-ok`：消息未可靠存储时是否尝试其他 Broker；
- `thread-pool-name`：可选动态线程池 Bean 名称；
- `enabled-observation`：是否启用发送 Observation。

## 7. 消费监听

使用 `@RocketMultiListener`：

```java
@Component
public class UserMessageListener {

    @RocketMultiListener(
        cluster = "default",
        group = "user-consumer",
        topic = "user-event",
        tag = "user.created"
    )
    public void onMessage(UserCreatedMessage message) {
        // 处理消息
    }
}
```

注解属性：

- `cluster`：集群名称，默认使用 `default-cluster`；
- `group`：消费组，空值使用当前集群消费者组或默认组；
- `topic`：必填 Topic；
- `tag`：默认 `*`，支持 `||` 分隔多个 Tag；
- `orderly`：是否顺序消费，默认 `false`。

## 8. 监听器参数

监听器会根据方法参数自动解析 RocketMQ 消息：

```java
// JSON 自动转换为业务 DTO
public void handle(UserCreatedMessage message) {
}

// 批量 JSON 消息
public void handleBatch(List<UserCreatedMessage> messages) {
}

// 原始 RocketMQ 消息
public void handleRaw(MessageExt message) {
}

// 批量原始消息
public void handleRawBatch(List<MessageExt> messages) {
}
```

参数解析规则：

- `MessageExt`：接收当前批次第一条原始消息；
- `List<MessageExt>`：接收当前批次所有原始消息；
- 其他类型：使用 Jackson 将第一条消息体反序列化为 DTO；
- `List<T>`：逐条将消息体反序列化为 `T`。

消息体必须是可被 Jackson 解析的 JSON。业务方法抛出异常时，RocketMQ 客户端会依据消费模式和 `max-reconsume-times` 进行重试。

## 9. 消费配置

```yaml
rocket:
  multi:
    clusters:
      default:
        consumer:
          enabled: true
          enabled-observation: true
          group: user-consumer
          message-model: CLUSTERING
          consume-from-where: CONSUME_FROM_LAST_OFFSET
          consume-timeout: 10
          consume-thread-min: 4
          consume-thread-max: 32
          consume-message-batch-max-size: 10
          max-reconsume-times: 16
          suspend-current-queue-time-millis: 1000
          pull-batch-size: 32
          pull-interval: 0
          orderly: false
```

主要字段：

- `message-model`：`CLUSTERING` 集群消费或 `BROADCASTING` 广播消费；
- `consume-from-where`：首次消费位置；
- `consume-timeout`：消费超时，单位分钟；
- `consume-thread-min` / `consume-thread-max`：消费线程范围；
- `consume-message-batch-max-size`：一次消费最大消息数；
- `max-reconsume-times`：最大重试次数；
- `suspend-current-queue-time-millis`：消费失败后的挂起时间；
- `pull-batch-size`：拉取批量大小；
- `pull-interval`：拉取间隔；
- `orderly`：是否顺序消费；
- `thread-pool-name`：当前版本配置对象中没有消费者线程池字段，不能按自定义线程池配置使用。

## 10. 顺序消费

监听注解或消费者配置开启 `orderly`：

```java
@RocketMultiListener(
    cluster = "default",
    group = "order-consumer",
    topic = "order-event",
    tag = "order.paid",
    orderly = true
)
public void handleOrderPaid(OrderPaidMessage message) {
    // 同一消息队列内按顺序处理
}
```

顺序消费要求生产端按照业务规则选择消息队列，并且消费者不能使用会破坏顺序的并行处理方式。跨队列不保证全局顺序。

## 11. Observation

模块会为发送和接收增加 Observation，名称和属性由 Rocket 发送/接收 Observation Convention 统一提供。启用方式：

```yaml
rocket:
  multi:
    clusters:
      default:
        producer:
          enabled-observation: true
        consumer:
          enabled-observation: true
```

监控模块未引入或对应观察未启用时，不应依赖具体监控后端指标。

## 12. 使用注意事项

- `rocket.multi.default-cluster` 必须对应一个已配置的集群；
- 每个集群的 NameServer、生产者和消费者独立初始化；
- `RocketTemplateRouter` 的同步发送返回 `SendResult`，异步发送通过 `SendCallback` 返回结果；
- 业务 Tag 必须与监听器 Tag 规则一致；
- `tag="*"` 会监听 Topic 下所有 Tag；
- 多个 Tag 使用 `||` 分隔，不是逗号分隔；
- DTO 消费依赖消息体 JSON 格式；
- 批量消费要关注单条失败后的整体重试行为；
- 开启顺序消费后不要在业务方法内部再并行处理同一队列消息；
- RocketMQ 重试不是业务幂等机制，生产者和消费者都需要设计幂等；
- `max-reconsume-times` 到达后应结合 RocketMQ 的失败处理策略检查消息是否进入死信队列；
- 生产环境应将 NameServer、账号信息和重试参数放入外部配置，不要硬编码。
