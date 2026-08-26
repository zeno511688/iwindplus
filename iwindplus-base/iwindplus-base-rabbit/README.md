# RabbitMQ 多集群模块（iwindplus-base-rabbit）

本模块基于 Spring AMQP 封装 RabbitMQ 多集群连接、Exchange/Queue/Binding 动态声明、消息发送、批量消费、JSON 自动反序列化、重试和 Observation。

```text
rabbit.multi
  ├── clusters
  │     ├── default
  │     └── backup
  ├── bindings
  │     └── Exchange ── Binding(routingKey) ── Queue
  ├── RabbitTemplateRouter ── 按集群发送
  └── @RabbitMultiListener ── 按集群和 group 消费
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-rabbit</artifactId>
</dependency>
```

模块通过自动配置注册多集群连接工厂、RabbitTemplate、AmqpAdmin、发送路由器和动态监听注册器。项目还需要引入 Spring AMQP 及 RabbitMQ 客户端依赖。

## 2. 基础配置

配置前缀是 `rabbit.multi`，默认集群名称必须存在于 `clusters` 中：

```yaml
rabbit:
  multi:
    enabled: true
    default-cluster: default
    enabled-dynamic-register: true
    clusters:
      default:
        host: 127.0.0.1
        port: 5672
        username: guest
        password: guest
        virtual-host: /
        connection-timeout: 3000
        requested-heartbeat: 60
        channel-cache-size: 25
      backup:
        addresses: 10.0.0.11:5672,10.0.0.12:5672
        username: app
        password: ${RABBIT_PASSWORD}
        virtual-host: /business
```

集群连接参数：

| 配置项 | 说明 |
|---|---|
| `addresses` | RabbitMQ 地址列表，配置后可用于多地址连接 |
| `host` / `port` | 单节点连接地址 |
| `username` / `password` | 认证信息 |
| `virtual-host` | 虚拟主机，默认 `/` |
| `connection-timeout` | 连接超时时间，默认 `3000ms` |
| `requested-heartbeat` | 心跳间隔，默认 `60s` |
| `channel-cache-size` | Channel 缓存数量，默认 `25` |

`rabbit.multi.enabled=false` 时关闭整个模块。`default-cluster` 用于没有显式指定集群的发送和监听场景。

## 3. 动态声明 Exchange、Queue 和 Binding

当 `enabled-dynamic-register=true` 时，模块会根据 `bindings` 声明拓扑：

```yaml
rabbit:
  multi:
    enabled-dynamic-register: true
    clusters:
      default:
        bindings:
          - auto-create: true
            exchange:
              name: user.exchange
              type: DIRECT
              durable: true
              auto-delete: false
            queue:
              name: user.created.queue
              durable: true
              exclusive: false
              auto-delete: false
            routing-keys:
              - user.created
            arguments:
              x-match: all
```

`exchange.type` 使用 `RabbitExchangeTypeEnum` 支持的交换机类型，例如 `DIRECT`、`TOPIC`、`FANOUT`、`HEADERS`，具体以当前版本枚举为准。

死信配置可以写在队列字段中：

```yaml
queue:
  name: user.created.queue
  dead-letter-exchange: user.dlx
  dead-letter-routing-key: user.created.failed
  arguments:
    x-message-ttl: 60000
```

也可以直接在 `queue.arguments` 中设置 RabbitMQ 的 `x-dead-letter-exchange`、`x-dead-letter-routing-key` 等参数。生产环境建议让拓扑配置集中管理，避免多个应用以不一致的参数重复声明同名资源。

## 4. 发送消息

注入 `RabbitTemplateRouter`：

```java
@Resource
private RabbitTemplateRouter rabbitTemplateRouter;

public void publish(UserCreatedMessage message) {
    rabbitTemplateRouter.send(
        "user.exchange",
        "user.created",
        message
    );
}
```

指定集群发送：

```java
rabbitTemplateRouter.send(
    "backup",
    "user.exchange",
    "user.created",
    message
);
```

携带自定义消息头：

```java
Map<String, Object> headers = Map.of(
    "tenantId", tenantId,
    "source", "user-service"
);

rabbitTemplateRouter.send(
    "default",
    "user.exchange",
    "user.created",
    message,
    headers
);
```

路由器会自动：

- 使用默认集群或指定集群的 RabbitTemplate；
- 设置消息 ID；
- 注入 Trace 上下文；
- 写入业务自定义 Header；
- 按生产者配置执行发布确认、Returns 和重试。

`send` 是 `void` 方法，发送结果通过生产者确认、Returns 和异常日志观察，不要按返回值判断发送成功。

## 5. 发送端配置

每个集群可以独立配置生产者：

```yaml
rabbit:
  multi:
    clusters:
      default:
        producer:
          enabled: true
          enabled-observation: true
          publisher-confirm-type: CORRELATED
          publisher-returns: true
          mandatory: true
          enable-retry: true
          reply-timeout: 3000
          retry-attempts: 3
          initial-interval: 1000
          max-interval: 10000
```

主要配置：

- `publisher-confirm-type`：发布确认类型；
- `publisher-returns`：是否接收无法路由消息的 Returns；
- `mandatory`：是否要求消息成功路由到队列；
- `enable-retry`：是否启用发送重试；
- `retry-attempts`：重试次数；
- `initial-interval`：首次重试间隔；
- `max-interval`：最大重试间隔；
- `thread-pool-name`：可选的动态线程池 Bean 名称；
- `enabled-observation`：是否启用发送 Observation。

## 6. 消费消息

使用 `@RabbitMultiListener`：

```java
@Component
public class UserMessageListener {

    @RabbitMultiListener(
        cluster = "default",
        group = "user-consumer",
        queues = {"user.created.queue"}
    )
    public void onMessage(UserCreatedMessage message) {
        // 业务处理
    }
}
```

监听注解只有三个属性：

- `cluster`：集群名称，空值使用默认集群；
- `group`：消费组；
- `queues`：监听队列名称数组。

同一集群、同一消费组的多个监听方法会合并到同一个监听容器中，监听队列会合并去重。

## 7. 监听器参数

监听方法参数由框架自动解析。支持以下类型：

```java
// JSON 自动反序列化为 DTO
public void handle(UserCreatedMessage message) {
}

// 批量 JSON 消息
public void handleBatch(List<UserCreatedMessage> messages) {
}

// 原始 AMQP 消息
public void handleRaw(Message message) {
}

// 批量原始消息
public void handleRawBatch(List<Message> messages) {
}

// Rabbit Channel
public void handleWithChannel(UserCreatedMessage message, Channel channel) {
}
```

参数解析规则：

- `Message`：接收第一条原始消息；
- `List<Message>`：接收当前批次原始消息；
- `Channel`：接收 RabbitMQ Channel；
- 其他类型：使用 Jackson 将消息体 JSON 转换为 DTO；
- `List<T>`：逐条反序列化为 `T`。

DTO 反序列化依赖消息体是合法 JSON，并且字段名称与 DTO 可被 Jackson 映射。消费异常会抛出异常，由容器按照重试和 Ack 配置处理。

## 8. 消费端配置

```yaml
rabbit:
  multi:
    clusters:
      default:
        consumer:
          enabled: true
          enabled-observation: true
          group: user-consumer
          prefetch: 100
          concurrency: 2
          max-concurrency: 10
          acknowledge-mode: MANUAL
          enabled-batch-listener: true
          batch-size: 20
          enable-retry: true
          retry-attempts: 3
          retry-interval: 1000
          receive-timeout: 1000
          idle-event-interval: 60000
          missing-queues-fatal: false
          priority: 1
```

常用字段：

- `prefetch`：一次推送给消费者的消息数量；
- `concurrency` / `max-concurrency`：并发消费者范围；
- `acknowledge-mode`：确认模式，默认 `MANUAL`；
- `enabled-batch-listener`：是否批量消费；
- `batch-size`：批量大小，默认 `20`；
- `enable-retry`、`retry-attempts`、`retry-interval`：消费重试；
- `missing-queues-fatal`：队列不存在时是否认为容器启动失败；
- `thread-pool-name`：可选监听线程池 Bean 名称；
- `enabled-observation`：是否启用消费 Observation。

批量监听开启后，监听方法应优先使用 `List<T>` 或 `List<Message>` 参数。

## 9. 手动 Ack

当使用 `MANUAL` 或 `MANUAL_IMMEDIATE` 时，业务需要结合 `Channel` 和消息投递标签完成确认：

```java
public void handle(UserCreatedMessage message, Channel channel, Message rawMessage)
    throws IOException {
    try {
        // 业务处理成功后确认
        channel.basicAck(
            rawMessage.getMessageProperties().getDeliveryTag(),
            false
        );
    } catch (Exception ex) {
        // 根据业务决定重新入队或拒绝
        channel.basicNack(
            rawMessage.getMessageProperties().getDeliveryTag(),
            false,
            false
        );
        throw ex;
    }
}
```

使用手动 Ack 时必须明确异常、重试、重新入队和死信之间的关系，避免消息无限重新入队。

## 10. 运行时管理

`RabbitTemplateRouter#getAdmin(cluster)` 可以获取指定集群的 `AmqpAdmin`，用于业务需要的 Rabbit 管理操作：

```java
AmqpAdmin admin = rabbitTemplateRouter.getAdmin("default");
```

拓扑的常规创建建议通过 `rabbit.multi.clusters.*.bindings` 管理，不建议在多个业务入口重复声明同一队列。

## 11. 使用注意事项

- `rabbit.multi.default-cluster` 必须对应一个已配置集群；
- 每个集群的生产者和消费者配置相互独立；
- 动态注册关闭时，模块不会依据 `bindings` 自动声明 Exchange、Queue 和 Binding；
- `@RabbitMultiListener` 的 `group` 是必填属性；
- 监听方法的 DTO 参数要求消息体是 JSON；
- 批量消费时应使用 `List<T>` 或 `List<Message>`，不要假设每次只有一条消息；
- 默认 Ack 为手动模式，业务必须明确 Ack 责任；
- 发送 `send` 无返回值，不能通过返回值判断 Broker 是否确认；
- 开启发布确认、Returns、重试时，应同时设计业务幂等；
- 消息 Header 会携带消息 ID 和 Trace 上下文，业务不要覆盖框架使用的 Header；
- 同一消费组中的监听器会合并到容器，调整监听方法可能影响同组消费并发和队列分配；
- 生产环境应配置死信交换机、死信路由键和消息 TTL，避免失败消息无限堆积。
