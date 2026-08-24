# 微服务负载均衡模块（支持灰度发布）

## 功能特性

1. **Nacos版本权重负载均衡**：基于Nacos元数据的版本权重路由
2. **自定义版本权重负载均衡**：基于自定义元数据的版本权重路由
3. **灰度发布负载均衡**：支持白名单、百分比两种灰度策略
4. **请求头版本路由**：支持通过请求头 `X-Version` 指定服务版本

## 快速开始

### 一、Nacos负载均衡

**适用场景**：使用Nacos作为注册中心，需要基于版本和权重的负载均衡。

本模块通过 `NacosLoadBalancerConfiguration` 自动注册Nacos负载均衡器。引入模块并完成Nacos服务发现配置后，Nacos服务实例会按照以下规则选择：

- 有 `X-Version` 请求头：筛选对应 `version` 的实例
- 没有 `X-Version` 请求头：默认筛选 `version=stable` 的实例
- 目标版本实例不存在：降级使用当前可用实例
- 同时优先选择与当前应用相同集群的实例

**实例元数据示例**：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: stable
          nacos.weight: 100
```

**元数据说明**：
- `version`：服务版本标识（如：stable、gray、v1、v2）
- `nacos.weight`：服务权重，可选
- `nacos.cluster`：Nacos集群标识，可选

### 二、灰度发布负载均衡

灰度发布是在Nacos负载均衡基础上启用的能力，通过 `loadbalancer.gray.enabled` 控制。
**配置示例**：

```yaml
loadbalancer:
  gray:
    enabled: true
    # 灰度策略类型（whitelist: 白名单策略，percentage: 百分比策略）
    strategy: percentage
    # 灰度百分比（0-100），百分比策略时使用
    percentage: 10
    # 灰度用户ID白名单，白名单策略时使用
    user-id-whitelist:
      - vip001
      - vip002

spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: stable  # 正常版本
          nacos.weight: 100
```

## 灰度发布使用指南

### 灰度策略说明

支持两种灰度策略，通过 `strategy` 配置项选择：

#### 1. 白名单策略（whitelist）

指定用户ID列表路由到灰度版本，适用于VIP用户、内部测试用户。

**配置示例**：

```yaml
loadbalancer:
  gray:
    enabled: true
    strategy: whitelist
    user-id-whitelist:
      - vip001
      - vip002
```

**请求示例**：

```http
GET /api/user/info HTTP/1.1
X-User-Id: vip001
```

#### 2. 百分比策略（percentage）

根据用户ID哈希值按百分比路由到灰度版本，适用于逐步放量。

**配置示例**：

```yaml
loadbalancer:
  gray:
    enabled: true
    strategy: percentage
    percentage: 10
```

**请求示例**：

```http
GET /api/user/info HTTP/1.1
X-User-Id: user123456
```

系统会根据用户ID的哈希值计算是否路由到灰度版本。

### 无用户信息场景处理

对于应用启动、定时任务、内部调用等无用户信息的场景，系统会自动使用 STABLE 版本：

- 无 HTTP 请求上下文 → 使用 `version=stable` 实例
- 无 `stable` 实例 → 降级使用所有实例

### 灰度发布流程

1. **准备阶段**：部署灰度版本实例，配置元数据 `version: gray`
2. **白名单测试**：配置策略为 `whitelist`，添加测试用户白名单，验证功能
3. **小范围灰度**：切换策略为 `percentage`，设置 `percentage: 5`，观察5%流量
4. **逐步放量**：逐步增加 `percentage`，如10%、20%、50%
5. **全量发布**：将灰度版本设为正式版本（`version: stable`），旧版本下线

### 灰度版本实例配置

**正常版本**：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: stable
          nacos.weight: 100
```

**灰度版本**：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        metadata:
          version: gray
          nacos.weight: 100
```

## 请求头版本路由

### 功能说明

支持通过请求头 `X-Version` 指定要访问的服务版本，适用于：

- 灰度模式下无灰度标记的用户
- 非灰度模式下需要访问特定版本
- 服务间调用传递版本信息

### 使用方式

**请求示例**：

```http
# 访问灰度版本
GET /api/user/info HTTP/1.1
X-Version: gray

# 访问稳定版本
GET /api/user/info HTTP/1.1
X-Version: stable

# 访问自定义版本
GET /api/user/info HTTP/1.1
X-Version: v1
```

### 版本路由逻辑

**灰度模式开启**：

```
有灰度标记（白名单/百分比）→ 使用灰度实例（version=gray）
无灰度标记
  ├── 有 X-Version 请求头 → 根据版本筛选实例
  └── 无 X-Version 请求头 → 使用稳定实例（version=stable）
```

**灰度模式未开启**：

```
有 X-Version 请求头 → 根据版本筛选实例
无 X-Version 请求头 → 使用稳定实例（version=stable）
```

### 降级策略

当目标版本实例不存在时，系统会自动降级：

- 目标版本实例不存在 → 降级使用所有实例
- 所有实例都没有 → 返回空响应

## 配置属性说明

### LoadBalancerProperty

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `loadbalancer.monitor.enabled` | Boolean | false | 是否开启负载均衡监控指标 |
| `loadbalancer.gray.enabled` | Boolean | false | 是否启用灰度发布 |
| `loadbalancer.gray.strategy` | Enum | percentage | 灰度策略类型（whitelist: 白名单策略，percentage: 百分比策略） |
| `loadbalancer.gray.percentage` | Integer | 10 | 灰度百分比（0-100），百分比策略时使用 |
| `loadbalancer.gray.user-id-whitelist` | List<String> | - | 灰度用户ID白名单，白名单策略时使用 |

## 注意事项

### 1. 版本一致性

确保服务实例的 `version` 元数据与路由目标一致。默认实例使用 `version: stable`，灰度实例使用 `version: gray`，也可以使用 `v1`、`v2` 等自定义版本。

### 3. 用户ID传递

确保上游服务（如网关）正确传递用户ID到请求头 `X-User-Id`。

### 4. 权重计算

权重越高，被选中的概率越大。例如，权重100的实例被选中的概率是权重50的2倍。

### 5. 灰度版本回滚

如需回滚，修改配置：
- 将 `enabled` 设为 `false`
- 或切换策略为 `percentage` 并将 `percentage` 设为 `0`
- 或切换策略为 `whitelist` 并清空 `user-id-whitelist`

### 6. 降级策略

当目标版本实例不存在时，系统会自动降级：
- 目标版本实例不存在 → 降级使用所有实例
- 所有实例都没有 → 返回空响应

### 7. 集群优先

系统会优先选择与当前应用同集群的实例，跨集群调用会记录 debug 日志。

## 最佳实践

### 1. 使用Nacos配置中心

使用Nacos配置中心动态调整灰度配置，无需重启服务：

**bootstrap.yml 配置**：

```yaml
spring:
  cloud:
    nacos:
      config:
        shared-configs:
          # 公共配置（包含负载均衡配置）
          - data-id: iwindplus-common.yml
            group: IWINDPLUS_GROUP
            refresh: true
```

**Nacos 公共配置（iwindplus-common.yml）**：

```yaml
# 开发环境（namespace: iwindplus-dev）
loadbalancer:
  gray:
    enabled: true
    strategy: whitelist
    user-id-whitelist:
      - dev001
      - dev002

# 测试环境（namespace: iwindplus-test）
loadbalancer:
  gray:
    enabled: true
    strategy: percentage
    percentage: 20

# 生产环境（namespace: iwindplus-prod）
loadbalancer:
  gray:
    enabled: false
```

### 2. 网关配置

确保网关正确传递用户ID和版本信息：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - name: RequestHeaderFilter
              args:
                headerName: X-User-Id
```

### 3. 监控指标

`iwindplus-base-loadbalancer` 可通过 `iwindplus-base-monitor` 接入 Micrometer 监控。监控默认关闭，需要显式开启：

```yaml
loadbalancer:
  monitor:
    enabled: true
```

开启后，负载均衡器使用 `ObservationExecutor` 管理实例选择的生命周期，指标统一包含以下低基数标签：

- `service`：目标服务名
- `version`：目标版本（如 `gray`、`stable`、`v1`）
- `route`：路由版本
- `outcome`：`success` 或 `empty`

| 观测项 | 类型 | 说明 |
|------|------|------|
| `loadbalancer.selection` | Observation | 实例选择耗时，并由 ObservationRegistry 生成对应的 Timer/Tracing 数据 |
| `loadbalancer.instances` | Gauge | 当前服务各版本的实例数量 |

应用需要引入监控模块：

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-monitor</artifactId>
</dependency>
```

当 `loadbalancer.monitor.enabled=false` 或未配置时，不创建实例选择 Observation，也不注册或更新版本实例 Gauge，不影响原有负载均衡功能。

**重要说明**：

- `ObservationExecutor` 会自动记录实例选择耗时，并在执行过程抛出异常时标记 Observation 错误。
- 负载均衡器只能感知实例选择结果，不能直接感知下游业务 HTTP 响应，因此实例选择 Observation 不是业务错误率。
- 灰度版本请求量、业务错误率和业务响应时间应在网关或服务 HTTP 入口通过 Micrometer/Observation 采集，并按 `version` 维度统计。
- 不建议把完整用户 ID 放进指标标签，否则会产生高基数时间序列。用户分布应按灰度结果、策略或用户分组统计。

### 4. 全链路灰度

如需实现全链路灰度（网关 → 服务A → 服务B），需要：

1. 所有服务引入 `iwindplus-base-loadbalancer` 模块
2. 网关或上游服务透传 `X-Version` 请求头
3. 各服务根据 `X-Version` 选择对应版本实例

## 常见问题

### Q1: 如何确保同一用户始终路由到同一版本？

A: 使用百分比策略时，系统根据用户ID哈希值计算百分比，确保同一用户ID的哈希值固定，从而保证路由一致性。

### Q2: 灰度版本实例全部下线会怎样？

A: 系统会自动降级到所有可用实例，确保服务可用性。

### Q3: 应用启动时如何选择版本？

A: 应用启动、定时任务、内部调用等无 HTTP 请求上下文的场景，系统会自动使用 STABLE 版本。如果 STABLE 版本不存在，会降级使用所有实例。

### Q4: 权重如何影响实例选择？

A: 权重越高，被选中的概率越大。例如，权重100的实例被选中的概率是权重50的2倍。

### Q5: 白名单和百分比策略可以同时使用吗？

A: 不可以。两种策略是互斥的，通过 `strategy` 配置项选择其中一种。

### Q6: 如何通过请求头指定版本？

A: 在请求头中添加 `X-Version` 字段，值为目标版本（如：gray、stable、v1、v2）。系统会根据该值筛选对应版本的服务实例。

### Q7: 无灰度标记的用户如何访问特定版本？

A: 灰度模式下，无灰度标记的用户可以通过请求头 `X-Version` 指定要访问的版本。如果没有指定，系统会使用 STABLE 版本。

## 技术实现

### 核心类说明

| 类名 | 说明 |
|------|------|
| `LoadBalancerConfiguration` | 负载均衡器自动配置类 |
| `NacosLoadBalancerConfiguration` | Nacos负载均衡器配置 |
| `NacosServiceInstanceLoadBalancer` | Nacos版本权重负载均衡器实现，支持灰度发布和请求头版本路由 |
| `GrayStrategyEnum` | 灰度策略类型枚举（whitelist、percentage） |
| `VersionTypeEnum` | 版本类型枚举（gray、stable） |
| `NacosMetadataKeyEnum` | Nacos元数据键枚举 |
| `LoadBalancerProperty` | 负载均衡配置属性 |

### 负载均衡流程

```
入口（getInstanceResponse）
├── 集群过滤（优先同集群实例）
├── 提取 HTTP 请求头
├── 判断是否使用灰度实例
│   ├── 灰度模式开启
│   │   ├── 有请求头 → shouldUseGrayVersion(headers)
│   │   │   ├── 白名单策略 → 用户ID在白名单中
│   │   │   └── 百分比策略 → 用户ID哈希值 < 百分比
│   │   └── 无请求头 → useGrayInstance = false
│   └── 灰度模式未开启 → useGrayInstance = false
├── 根据判断结果选择实例
│   ├── useGrayInstance = true → getGrayInstanceResponse
│   │   ├── 有灰度实例 → 使用灰度实例（version=gray）
│   │   └── 无灰度实例 → 使用所有实例
│   └── useGrayInstance = false → getVersionInstanceResponse
│       ├── 有 X-Version 请求头 → 根据版本筛选实例
│       └── 无 X-Version 请求头 → 使用稳定实例（version=stable）
│           └── 无稳定实例 → 使用所有实例
└── 权重随机选择（selectInstanceByWeight）
```

### 版本筛选逻辑

```java
// 筛选指定版本的实例
List<ServiceInstance> targetInstances = instances.stream()
    .filter(instance -> {
        Map<String, String> metadata = instance.getMetadata();
        String version = metadata.get("version");
        return targetVersion.equals(version);
    })
    .toList();

// 降级策略
if (targetInstances.isEmpty()) {
    log.warn("未找到{}实例，降级使用所有实例", versionDesc);
    targetInstances = instances;
}
```

## 更新日志

### v1.0.0
- 实现Nacos版本权重负载均衡
- 实现自定义版本权重负载均衡

### v2.0.0
- 新增灰度发布负载均衡器
- 支持白名单、百分比两种灰度策略
- 新增请求头版本路由功能
- 优化无用户信息场景处理（自动使用STABLE版本）
- 优化权重随机算法
- 完善日志输出
- 新增自动降级策略
- 新增集群优先策略
- 使用枚举替代字符串常量，提升类型安全

## 许可证

Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
