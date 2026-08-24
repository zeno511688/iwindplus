# 监控模块

## 一、模块能力

本模块提供三类独立能力：

- `MonitorTemplate`：Counter、Timer、Gauge 等 Micrometer 指标采集。
- `ObservationExecutor`：Observation 的同步、异步和 Reactor 执行生命周期管理。
- `TraceContextPropagator`：Trace 上下文注入、提取以及跨线程、跨服务传播。

## 二、统一监控执行接口

模块提供 `MonitorExecutor` 作为统一调用接口，按能力暴露底层组件：

- `monitor()`：获取 `MonitorTemplate`，负责 Counter、Timer、Gauge 等指标。
- `observation()`：获取 `ObservationExecutor`，负责 Observation 生命周期。
- `trace()`：获取 `TraceContextPropagator`，负责 Trace 上下文传播。

`ObservabilityTemplate` 是 `MonitorExecutor` 的默认实现，同时提供常用的指标和 Observation 委托方法。

```java
@Resource
private MonitorExecutor monitorExecutor;

monitorExecutor.monitor().increment("business.request", Tags.of("result", "success"));
monitorExecutor.observation().execute("business.request", observation -> doRequest(observation));
monitorExecutor.trace().inject(carrier, setter);
```

三个底层组件仍然分别注册为 Spring Bean，原有注入方式保持兼容。


注入 `TraceContextPropagator`，调用 `inject` 或 `extract`。在线程池、异步任务等切换线程场景中，需要在任务提交前注入上下文，并在消费侧提取和恢复上下文。

Reactor 场景使用 `injectReactor` 和 `extractReactor`。

## 四、Observation 埋点

注入 `ObservationExecutor`，使用 `execute`/`observe` 包裹需要记录耗时和异常的业务逻辑。执行器会负责 Observation 的启动、Scope 管理、异常记录和停止。

建议指标标签使用低基数值，不要将完整用户 ID、订单号等高基数字段作为标签。
