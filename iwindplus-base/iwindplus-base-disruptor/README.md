# Disruptor 多实例模块（iwindplus-base-disruptor）

本模块基于 LMAX Disruptor 封装多个独立环形队列，支持按处理器名称路由事件、动态线程池、背压阈值、Trace 上下文传播和 Observation 监控。

```text
业务线程
   │
   ▼
DisruptorManager#getTemplate(name)
   │
   ▼
DisruptorTemplate#publish(DisruptorPublishDTO)
   │
   ▼
RingBuffer
   │
   ▼
DisruptorDispatcherHandler
   │
   ▼
DisruptorEventHandler#execute(data, sequence, endOfBatch)
```

## 1. 适用场景

适合将生产速度较快的同步流程转为进程内异步消费，例如：

- Kafka、RabbitMQ、RocketMQ 拉取后在本地削峰；
- 日志、审计、埋点等异步处理；
- 单进程内多个业务处理器并行消费；
- 对消息顺序、低延迟和较少对象复制有要求的场景。

Disruptor 是 JVM 进程内队列，不提供持久化、跨进程投递和宕机恢复。需要可靠投递时，应使用消息中间件或数据库任务表。

## 2. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-disruptor</artifactId>
</dependency>
```

模块自动配置依赖：

- `iwindplus-base-monitor`：Trace 和 Observation；
- Dynamic TP：配置 `thread-pool-name` 时使用动态线程池；
- Spring Bean：发现业务实现的 `DisruptorEventHandler`。

## 3. 基础配置

配置前缀是 `disruptor.multi`：

```yaml
disruptor:
  multi:
    enabled: true
    enabled-monitor: true
    default-name: default
    configs:
      default:
        enabled-observation: true
        ring-buffer-size: 65536
        pause-threshold: 0.1
        resume-threshold: 0.5
        producer-type: SINGLE
        wait-strategy: YIELDING
        timeout: 10
        time-unit: MILLISECONDS
        thread-pool-name: disruptorTaskExecutor
```

顶层配置：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `enabled` | `true` | 是否启用模块自动配置 |
| `enabled-monitor` | `true` | 是否启用队列使用率监控 |
| `default-name` | `default` | 默认 Disruptor 名称 |
| `configs` | 自动补齐 | 多个 Disruptor 实例配置，Key 是实例名称 |

如果 `configs` 中没有 `default-name` 对应的配置，模块会在初始化时自动创建一个默认配置。

## 4. 定义事件处理器

实现 `DisruptorEventHandler<T>` 并注册为 Spring Bean：

```java
@Component
public class UserEventHandler implements DisruptorEventHandler<UserEvent> {

    @Override
    public String getName() {
        return "userEventHandler";
    }

    @Override
    public void execute(UserEvent data, long sequence, boolean endOfBatch) {
        // 业务处理
    }
}
```

如果不重写 `getName()`，默认名称是当前实现类简单类名的首字母小写。例如：

```text
UserEventHandler -> userEventHandler
```

`getName()` 返回值必须与发布 DTO 的 `handlerName` 一致。处理器名称不是 Disruptor 实例名称；一个实例可以承载多个事件处理器，具体路由由处理器名称决定。

## 5. 配置多个 Disruptor 实例

通常以处理器类型或业务场景区分实例：

```yaml
disruptor:
  multi:
    default-name: default
    configs:
      default:
        ring-buffer-size: 65536
        wait-strategy: YIELDING
        thread-pool-name: disruptorTaskExecutor
      userEventHandler:
        ring-buffer-size: 16384
        wait-strategy: BLOCKING
        producer-type: MULTI
        thread-pool-name: user-disruptor-pool
      auditEventHandler:
        ring-buffer-size: 8192
        wait-strategy: LITE_BLOCKING
        thread-pool-name: audit-disruptor-pool
```

`DisruptorManager#getTemplate(name)` 通过配置名称获取对应模板。如果业务使用了 `userEventHandler` 作为实例名称，就必须存在同名配置；没有对应配置时不要假设会自动复制 default 配置。

## 6. 获取模板并发布消息

注入 `DisruptorManager`：

```java
@Resource
private DisruptorManager<UserEvent> disruptorManager;

public boolean publish(UserEvent data) {
    DisruptorTemplate<UserEvent> template =
        disruptorManager.getTemplate("userEventHandler");

    DisruptorPublishDTO<UserEvent> publish =
        DisruptorPublishDTO.<UserEvent>builder()
            .handlerName("userEventHandler")
            .data(data)
            .source("user-service")
            .destination("user-event-handler")
            .headers(Map.of("tenantId", tenantId))
            .build();

    return template.publish(publish);
}
```

发布 DTO 字段：

| 字段 | 是否必填 | 说明 |
|---|---|---|
| `handlerName` | 是 | 事件处理器名称，对应 `DisruptorEventHandler#getName()` |
| `data` | 是 | 业务数据 |
| `headers` | 否 | 消息 Header |
| `source` | 否 | 发送方来源，监控时使用 |
| `destination` | 否 | 发送目标，监控时使用 |

`publish` 返回 `boolean`：

- `true`：事件成功发布到环形缓冲区；
- `false`：当前事件未成功发布，业务需要记录、丢弃或转入补偿流程。

## 7. 背压和队列可用性

模板提供队列状态方法：

```java
DisruptorTemplate<UserEvent> template =
    disruptorManager.getTemplate("userEventHandler");

if (template.needPause()) {
    // 暂停上游拉取或降低提交速度
}

if (!template.available()) {
    // 当前不适合继续发布
}
```

配置示例：

```yaml
disruptor:
  multi:
    configs:
      userEventHandler:
        ring-buffer-size: 16384
        pause-threshold: 0.1
        resume-threshold: 0.5
```

含义：

- 剩余容量低于 `10%` 时，`needPause()` 为 `true`；
- 剩余容量恢复超过 `50%` 后，允许继续消费；
- 阈值应配置为 `0` 到 `1` 之间的小数；
- 阈值设置过低可能造成 OOM 风险，设置过高则会降低吞吐。

如果上游是 Kafka 或其他消息中间件，建议在 `needPause()` 为 true 时暂停拉取，而不是持续调用 `publish` 并丢弃消息。

## 8. RingBuffer 和等待策略

每个实例可以独立配置：

```yaml
disruptor:
  multi:
    configs:
      default:
        ring-buffer-size: 65536
        producer-type: SINGLE
        wait-strategy: YIELDING
        timeout: 10
        time-unit: MILLISECONDS
```

配置说明：

- `ring-buffer-size`：环形缓冲区容量，通常应使用 2 的幂；
- `producer-type`：`SINGLE` 或 `MULTI`；
- `wait-strategy`：使用 `DisruptorWaitStrategyEnum`，具体枚举值以当前版本为准；
- `timeout`：超时等待时间；
- `time-unit`：等待时间单位。

常见策略取舍：

| 策略方向 | 特点 |
|---|---|
| `BLOCKING` | CPU 占用较低，延迟相对更高 |
| `YIELDING` | 延迟和 CPU 消耗折中 |
| `BUSY_SPIN` | 延迟低，但 CPU 消耗高 |
| `LITE_BLOCKING` | 相对轻量的阻塞等待 |

不要仅因为追求低延迟就使用 `BUSY_SPIN`，应结合机器 CPU 核数、消费者耗时和实际吞吐压测。

## 9. 线程池配置

`thread-pool-name` 默认是：

```text
disruptorTaskExecutor
```

如果使用 Dynamic TP，必须创建同名线程池；否则应按项目当前线程池配置准备该 Bean。多个 Disruptor 实例可以使用不同线程池：

```yaml
disruptor:
  multi:
    configs:
      userEventHandler:
        thread-pool-name: user-disruptor-pool
      auditEventHandler:
        thread-pool-name: audit-disruptor-pool
```

线程池大小需要根据 `execute` 的业务耗时和 RingBuffer 容量配置。不要在事件处理器中执行长时间阻塞操作而不调整队列和线程池。

## 10. 监控和 Trace

当 `enabled-monitor=true` 且实例 `enabled-observation=true` 时，模块会记录：

- RingBuffer 使用率；
- 事件发布和消费 Observation；
- `source`、`destination` 等发布上下文；
- Trace 上下文传播。

```yaml
disruptor:
  multi:
    enabled-monitor: true
    configs:
      default:
        enabled-observation: true
```

监控字段不是业务可靠性保障。即使关闭 Observation，事件发布和消费逻辑仍然存在。

## 11. 使用注意事项

- `handlerName` 必须与 `DisruptorEventHandler#getName()` 完全一致；
- `getTemplate(name)` 的 `name` 是 Disruptor 实例配置名称，不一定等于处理器名称；
- `publish` 返回 false 时不能认为消息已经进入队列；
- Disruptor 不持久化事件，进程重启会丢失内存中尚未消费的数据；
- Disruptor 不提供跨节点消费，集群环境每个节点都有自己的 RingBuffer；
- `ring-buffer-size` 应使用 2 的幂，并根据峰值积压量规划；
- `MULTI` 生产者模式下要注意生产线程并发和业务事件可见性；
- 事件处理器中的异常不能替代业务重试机制，失败事件需要自行记录或补偿；
- 使用动态线程池时必须确认 `thread-pool-name` 对应 Bean 已注册；
- `headers`、`source`、`destination` 不会自动替代业务幂等键；
- 上游消息中间件削峰时应结合 `needPause()` 和 `available()` 控制消费速率。
