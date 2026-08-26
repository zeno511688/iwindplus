# 监控模块（iwindplus-base-monitor）

本模块对 Micrometer、Micrometer Observation、Micrometer Tracing 和 OpenTelemetry 的常用操作进行统一封装，提供三类能力：

```text
MonitorExecutor
     ├── monitor()      → MonitorTemplate
     │       ├── Counter
     │       ├── Timer
     │       └── Gauge
     ├── observation()  → ObservationExecutor
     │       ├── 同步执行
     │       ├── CompletionStage 异步执行
     │       └── Reactor Mono 执行
     └── trace()        → TraceContextPropagator
             ├── Micrometer Trace 注入/提取
             └── Reactor/OpenTelemetry Context 注入/提取
```

## 1. 引入和配置

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-monitor</artifactId>
</dependency>
```

具体版本由父工程的 Maven 依赖管理统一控制。模块通过 Spring Boot 自动配置注册以下 Bean：

- `MonitorExecutor`；
- `MonitorTemplate`；
- `ObservationExecutor`；
- `TraceContextPropagator`。

使用 Micrometer 指标或 Trace 时，还需要在业务应用中配置对应的 MeterRegistry、ObservationRegistry、Tracer 和传播器实现。该模块只负责调用封装，不提供具体的监控后端。

### 忽略 API

配置前缀为 `monitor`，目前支持忽略 API 列表：

```yaml
monitor:
  ignored-api:
    - /actuator/health
    - /favicon.ico
```

`ignored-api` 的实际使用取决于应用中如何接入 `MonitorProperty`；模块本身的配置属性只有这一项，不要配置 README 未列出的监控开关。

## 2. 统一入口

推荐注入 `MonitorExecutor`，按能力获取对应组件：

```java
@Resource
private MonitorExecutor monitorExecutor;

MonitorTemplate metrics = monitorExecutor.monitor();
ObservationExecutor observations = monitorExecutor.observation();
TraceContextPropagator traces = monitorExecutor.trace();
```

也可以直接注入三个底层 Bean，适合只使用单项能力的模块。

## 3. 指标采集

### 3.1 Counter

Counter 适合统计请求数、成功数、失败数等只增不减的事件：

```java
Tags tags = Tags.of(
    "operation", "order-create",
    "result", "success");

monitorExecutor.monitor().increment("business.request", tags);
```

需要操作原始 `Counter` 时使用 `counter`：

```java
Counter counter = monitorExecutor.monitor()
    .counter("business.request", Tags.of("operation", "order-create"));
counter.increment();
```

### 3.2 Timer

使用 `timer` 包裹有返回值的业务：

```java
OrderResult result = monitorExecutor.monitor().timer(
    "business.order.create",
    Tags.of("channel", "web"),
    () -> orderService.create(command));
```

无返回值时使用 `Runnable` 重载：

```java
monitorExecutor.monitor().timer(
    "business.order.cancel",
    Tags.of("source", "job"),
    () -> orderService.cancel(orderId));
```

需要手动操作计时器时使用 `getTimer`：

```java
Timer timer = monitorExecutor.monitor()
    .getTimer("business.order.create", Tags.of("channel", "web"));
Timer.Sample sample = Timer.start();
try {
    orderService.create(command);
} finally {
    sample.stop(timer);
}
```

### 3.3 Gauge

Gauge 绑定对象和取值函数，适合监控队列长度、缓存数量等可增可减的当前值：

```java
Queue<?> queue = orderQueue;
monitorExecutor.monitor().gauge(
    "business.order.queue.size",
    Tags.of("queue", "order"),
    queue,
    value -> value.size());
```

对象必须保持存活，否则 Gauge 可能无法继续读取到有效值。

### 指标命名建议

- 使用稳定、可检索的指标名；
- 标签使用低基数值；
- 不要把用户 ID、订单号、完整 URL 等高基数数据作为标签；
- 指标标签和指标名应保持长期兼容，避免频繁改变监控面板查询条件。

## 4. Observation 生命周期

`ObservationExecutor` 会负责 Observation 的创建、启动、Scope 打开、异常记录和停止。同步执行支持简单名称或自定义 `ObservationFunction`：

```java
String result = monitorExecutor.observation().execute(
    "business.order.create",
    observation -> {
        observation.lowCardinalityKeyValue("channel", "web");
        return orderService.create(command);
    });
```

也可以传入普通 `Supplier`：

```java
OrderResult result = monitorExecutor.observation().execute(
    "business.order.create",
    () -> orderService.create(command));
```

业务异常会被记录到 Observation 后继续向上抛出，调用方仍需按业务需要处理异常。

### 自定义 Convention 和 Context

需要自定义 Observation 名称、标签或上下文时，使用 `ObservationConvention`、Context Supplier 和业务 Supplier 的重载：

```java
OrderObservationConvention convention = new OrderObservationConvention();

OrderResult result = monitorExecutor.observation().execute(
    convention,
    OrderObservationContext::new,
    () -> orderService.create(command));
```

### CompletionStage 异步执行

异步执行要求业务 Supplier 返回非空 `CompletionStage`。阶段完成时，模块会记录异常并停止 Observation：

```java
CompletionStage<OrderResult> stage =
    monitorExecutor.observation().executeAsync(
        convention,
        OrderObservationContext::new,
        () -> orderService.createAsync(command));
```

不要在 Supplier 中返回 `null`，否则会立即抛出异常并结束 Observation。

### Reactor Mono

响应式业务使用 `executeMono`：

```java
Mono<OrderResult> result = monitorExecutor.observation().executeMono(
    convention,
    OrderObservationContext::new,
    () -> orderService.createMono(command));
```

方法通过 `Mono.defer` 创建 Observation，因此每次订阅都会建立独立的生命周期；异常会记录到 Observation，结束时统一停止。

## 5. Trace 上下文传播

### 5.1 普通载体注入

当 HTTP Header、消息 Header 或自定义 Map 作为传播载体时，使用 `inject`：

```java
Map<String, String> headers = new HashMap<>();
monitorExecutor.trace().inject(
    headers,
    (carrier, key, value) -> carrier.put(key, value));
```

`inject` 从当前 Micrometer Tracer 获取当前 Span。当前没有 Span 时不会创建新 Span，只记录警告并返回。

### 5.2 消费侧提取

消费消息或接收请求时使用 `extract`，返回的 `SpanInScope` 必须关闭：

```java
try (Tracer.SpanInScope ignored = monitorExecutor.trace().extract(
    headers,
    (carrier, key) -> carrier.get(key))) {
    handleMessage(message);
}
```

若提取失败，方法可能返回 `null`，调用方应根据实际传播器实现处理这一情况。

### 5.3 Reactor/OpenTelemetry Context

Reactor 链路使用 `injectReactor` 和 `extractReactor`：

```java
Context current = Context.current();
Map<String, String> headers = new HashMap<>();

monitorExecutor.trace().injectReactor(
    current,
    headers,
    (carrier, key, value) -> carrier.put(key, value));

Context extracted = monitorExecutor.trace().extractReactor(
    headers,
    (carrier, key) -> carrier.get(key));
```

`extractReactor` 提取不到有效 Context 时返回当前 Context，而不是返回空值。

## 6. 使用边界

- 本模块不负责暴露 `/actuator` 端点，也不负责选择 Prometheus、OTLP 或 Zipkin 等后端；
- 本模块不自动替业务创建所有指标，业务需要在合适的位置调用模板；
- Trace 注入前必须存在当前 Span，否则不会产生传播数据；
- 跨线程、跨服务和消息消费场景必须成对使用注入与提取；
- 指标和 Observation 的名称应保持稳定，标签应避免高基数；
- `ObservationExecutor` 的方法会抛出业务异常，不能把监控封装误认为异常吞掉组件。
