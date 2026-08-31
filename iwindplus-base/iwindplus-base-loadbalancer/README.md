# Nacos 负载均衡模块（iwindplus-base-loadbalancer）

本模块为 Spring Cloud LoadBalancer 注册 Nacos 专用的 Reactor 负载均衡器，支持：

- Nacos 同集群优先；
- `version` 元数据版本路由；
- `nacos.weight` 权重选择；
- `X-Version` 请求头指定版本；
- `X-User-Id` 白名单或哈希百分比灰度；
- 负载均衡选择 Observation 和版本实例数量指标。

```text
Spring Cloud LoadBalancer
          │
          ▼
NacosServiceInstanceLoadBalancer
          │
          ├── 同集群实例优先
          ├── X-User-Id → 灰度判断
          ├── X-Version → 目标版本
          ├── version 元数据筛选
          └── nacos.weight 加权选择
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-loadbalancer</artifactId>
</dependency>
```

同时必须使用 Spring Cloud Nacos Discovery，并提供 `NacosDiscoveryProperties`。模块通过 `@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerConfiguration.class)` 注册默认负载均衡配置。

## 2. 服务发现配置

调用方需要配置 Nacos Discovery：

```yaml
spring:
  application:
    name: order-service
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: public
        group: DEFAULT_GROUP
        cluster-name: DEFAULT
```

下游服务实例需要在 Nacos 中提供版本和权重元数据：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: stable
          nacos.weight: "100"
          nacos.cluster: DEFAULT
          nacos.healthy: "true"
```

## 3. 服务实例元数据

模块读取以下 Metadata：

| Key | 说明 |
|---|---|
| `version` | 版本标识，内置约定为 `stable`、`gray`，也支持 `v1`、`v2` 等自定义版本 |
| `nacos.weight` | Nacos 权重，未配置时使用默认权重 |
| `nacos.cluster` | Nacos 集群标识 |
| `nacos.healthy` | 实例健康标识 |

推荐配置：

```yaml
# 稳定版本
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: stable
          nacos.weight: "100"
          nacos.cluster: DEFAULT
          nacos.healthy: "true"
---
# 灰度版本
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: gray
          nacos.weight: "100"
          nacos.cluster: DEFAULT
          nacos.healthy: "true"
```

权重越高，实例在同一版本实例集合中的被选概率越高。权重选择基于 Nacos 的随机权重算法，不是严格轮询。

## 4. 基础版本路由

默认路由规则：

```text
有 X-Version 请求头
    └── 筛选 version=X-Version 的实例

没有 X-Version 请求头
    └── 筛选 version=stable 的实例

目标版本不存在
    └── 降级使用当前候选实例集合

候选实例为空
    └── 返回 EmptyResponse
```

请求示例：

```http
GET /api/orders HTTP/1.1
Host: order-service
X-Version: v2
```

服务实例配置：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: v2
```

`X-Version` 只影响当前一次服务选择。需要跨服务传递版本时，应由网关或调用方主动复制该请求头。

## 5. 同集群优先

负载均衡器先读取调用方的 `spring.cloud.nacos.discovery.cluster-name`，再从服务实例元数据中筛选同集群实例：

```text
同集群实例存在
    └── 只在同集群实例中继续版本和权重选择

同集群实例不存在
    └── 使用原始候选实例集合
```

调用方：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        cluster-name: BEIJING
```

下游实例：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          nacos.cluster: BEIJING
```

当前实现判断同集群时读取实例元数据中的 `version` Key 与当前 `cluster-name` 比较，这是源码中的既有行为。使用时应保持注册中心实际生成的实例元数据与调用方集群配置一致。

## 6. 开启灰度发布

配置前缀是 `loadbalancer.gray`：

```yaml
loadbalancer:
  gray:
    enabled: true
    strategy: whitelist
    user-id-whitelist:
      - vip001
      - test001
    percentage: 10
    fallback-when-no-instance: false
```

配置字段：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `loadbalancer.gray.enabled` | `false` | 是否启用灰度判断 |
| `loadbalancer.gray.strategy` | `percentage` | `whitelist` 或 `percentage` |
| `loadbalancer.gray.user-id-whitelist` | 空 | 白名单策略使用的用户 ID |
| `loadbalancer.gray.percentage` | `10` | 百分比策略，范围 `0-100` |
| `loadbalancer.gray.fallback-when-no-instance` | `false` | 灰度实例不存在时是否回退到全部实例；关闭时返回空响应 |

只有请求中存在 `X-User-Id` 时才会判断灰度。没有该请求头时不会进入灰度实例。

## 7. 白名单灰度

```yaml
loadbalancer:
  gray:
    enabled: true
    strategy: whitelist
    user-id-whitelist:
      - user-001
      - user-002
```

请求：

```http
GET /api/orders HTTP/1.1
X-User-Id: user-001
```

处理结果：

- `user-001` 在白名单中：优先选择 `version=gray`；
- 不在白名单中：按 `X-Version` 或 `stable` 版本路由；
- 灰度实例不存在：默认返回空响应；开启 `fallback-when-no-instance` 后才降级使用候选实例集合。

## 8. 百分比灰度

```yaml
loadbalancer:
  gray:
    enabled: true
    strategy: percentage
    percentage: 10
```

请求：

```http
GET /api/orders HTTP/1.1
X-User-Id: user-001
```

源码使用：

```text
floorMod(userId.hashCode(), 100) < percentage
```

因此这是基于用户 ID 哈希的稳定分流：同一个用户在相同配置下通常会稳定落在同一分组，不是每次请求重新随机。`percentage=10` 表示哈希桶 `0-9` 的用户进入灰度。

注意：

- 用户 ID 必须稳定；
- 用户 ID 为空时不会进入灰度；
- `percentage` 必须大于 `0` 且不超过 `100` 才会生效；
- 修改百分比可能改变部分用户的路由结果。

## 9. 灰度路由优先级

```text
候选服务实例
      │
      ▼
同集群筛选
      │
      ▼
loadbalancer.gray.enabled && X-User-Id 存在？
      │
      ├── 灰度命中 → version=gray
      │
      └── 未命中 → X-Version 或 version=stable
      │
      ▼
目标版本存在？
      │
      ├── 是 → 版本实例集合
      └── 否 → 全部候选实例
      │
      ▼
按 nacos.weight 选择实例
```

灰度模式命中后使用 `version=gray`，不会再使用请求头 `X-Version` 选择其他版本。未命中灰度时才按 `X-Version` 或 `stable` 继续选择。

## 10. 监控指标

配置：

```yaml
loadbalancer:
  monitor:
    enabled: true
```

开启后，模块通过 `MonitorExecutor` 记录：

- 选择 Observation：`loadbalancer.select`；
- 服务名；
- 选择版本；
- 路由版本；
- 选择结果；
- 各版本实例数量 Gauge。

监控是可选能力。没有 `MonitorExecutor` 或未开启配置时，负载均衡仍然可以执行，但不会记录模块自定义指标。

## 11. Feign、RestClient 和 WebClient

该模块只提供 Spring Cloud LoadBalancer 实现，具体客户端必须使用 Spring Cloud LoadBalancer 能识别的服务名地址：

```java
@FeignClient(name = "order-service")
public interface OrderClient {
    // Feign 请求会通过 Spring Cloud LoadBalancer 选择实例
}
```

或使用带服务名的 URL：

```text
http://order-service/api/orders
```

不要在客户端中写死下游实例 IP，否则不会经过该负载均衡器，也不会执行版本和灰度策略。

## 12. 无实例和降级行为

```text
服务实例列表为空
    ├── 监控关闭：返回 EmptyResponse
    └── 监控开启：记录空结果后返回 EmptyResponse

目标版本不存在
    └── 使用当前候选列表重新按权重选择
```

目标版本不存在时的降级意味着灰度或版本隔离可能被绕过。生产环境发布前必须确保目标版本实例已注册，并通过监控确认版本元数据正确。

## 13. 推荐发布流程

1. 稳定版本实例配置 `version=stable`；
2. 灰度实例配置 `version=gray`；
3. 开启灰度白名单，只放入内部测试用户；
4. 确认请求头包含 `X-User-Id`；
5. 验证灰度用户和普通用户的实例选择；
6. 切换为百分比策略并从小比例开始；
7. 逐步增加百分比并观察业务指标；
8. 灰度完成后将新版本切换为稳定版本；
9. 下线旧版本前确认没有请求仍指定旧的 `X-Version`。

## 14. 使用注意事项

- 必须使用 Nacos Discovery 注册中心和服务实例列表；
- 版本路由依赖实例 metadata 中的 `version`；
- 权重依赖 `nacos.weight`，缺失时使用默认权重；
- 灰度依赖 `X-User-Id`，没有用户 ID 时不会命中灰度；
- `X-Version` 不是模块自动生成的请求头，需要调用方自行传递；
- 目标版本不存在时会降级到全部候选实例，不能把它当作严格隔离；
- 灰度百分比基于用户 ID 哈希，不是随机百分比分流；
- 同集群优先依赖 Nacos 集群名称和实例元数据保持一致；
- 服务调用不能使用固定 IP，否则不会经过 Spring Cloud LoadBalancer；
- 配置 `percentage=0` 或大于 `100` 时不会命中百分比灰度；
- 生产环境建议开启监控并观察空实例、版本路由和降级次数；
- 发布和回滚时应同时检查 Nacos 实例 metadata，而不只是应用配置。
