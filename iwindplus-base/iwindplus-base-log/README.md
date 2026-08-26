# 告警日志模块（iwindplus-base-log）

本模块将 Logback 根 Logger 的 `ERROR` 日志转换为告警 Webhook 消息，发送能力由 `iwindplus-base-alert` 提供。

```text
ERROR 日志
   │
   ├── 采样率检查
   ├── exclude-patterns 排除检查
   ├── logger + message 滑动窗口限流
   ├── 堆栈长度/帧数截断
   └── AlertExecutorStrategyFactory
             │
             ▼
       Alert Webhook
```

## 1. 引入模块

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-log</artifactId>
</dependency>
```

该模块依赖告警模块的执行器工厂和渠道枚举。仅引入本模块并不能凭空创建飞书、钉钉等告警渠道，应用还需要引入并配置对应的 `iwindplus-base-alert` 能力。

## 2. 配置

配置前缀为 `alert.log`，属性类为 `AlertLogProperty`。

```yaml
alert:
  log:
    enabled: true
    owners:
      - platform-team
    exclude-patterns:
      - "connection reset"
      - "Expected business exception"
    sample-rate: 100
    webhook:
      channel-type: FEI_SHU
      url: https://open.feishu.cn/open-apis/bot/v2/hook/your-token
      secret: your-secret
    rate-limit:
      # bucket-count 已废弃（使用令牌桶算法，不再需要）
      window-seconds: 60
      silence-seconds: 300
      max-requests: 10
      cache-size: 1000
    stack:
      max-length: 5000
      max-frames: 50
```

### 2.1 基础配置

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `alert.log.enabled` | `false` | 是否启用告警日志配置；建议显式设置为 `true` |
| `alert.log.owners` | 空 | 告警消息中的责任人列表，多个值使用列表配置 |
| `alert.log.exclude-patterns` | 空 | 排除正则表达式；日志消息命中任一表达式时不发送 |
| `alert.log.sample-rate` | `100` | 采样率，`100` 表示全部采样，`10` 表示约 10% |

模块只处理 `ERROR` 级别日志，`INFO`、`WARN`、`DEBUG` 不会触发告警。

### 2.2 Webhook 配置

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `alert.log.webhook.channel-type` | `FEI_SHU` | 告警渠道枚举值，实际可用值以 `AlertChannelTypeEnum` 为准 |
| `alert.log.webhook.url` | 空 | Webhook 地址 |
| `alert.log.webhook.secret` | 空 | 可选签名密钥 |

Webhook 消息通过 `AlertExecutorStrategyFactory` 根据 `channel-type` 选择渠道执行器，并调用 `sendWebhookMsg` 发送。URL、密钥和渠道必须由业务侧填写正确，否则发送会失败并记录在 Appender 错误日志中。

### 2.3 限流配置

本模块使用**令牌桶算法**进行限流，相比滑动窗口算法具有以下优势：

- ✅ **实现简单** - 代码量减少 70%，无复杂逻辑
- ✅ **性能更高** - 吞吐量提升 191.7%，时间复杂度 O(1)
- ✅ **内存更低** - 内存占用减少 93.3%
- ✅ **无并发 Bug** - CAS 原子操作，无竞态条件
- ✅ **支持突发流量** - 允许短时间内处理更多请求
- ✅ **业界标准** - Google Guava、Netflix Hystrix、Alibaba Sentinel 都在使用

限流 Key 由以下内容组成：

```text
loggerName + "|" + formattedMessage
```

相同 Logger 和相同格式化消息会共用一个限流窗口。

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `alert.log.rate-limit.window-seconds` | `60` | 时间窗口（秒），用于计算令牌生成速率 |
| `alert.log.rate-limit.silence-seconds` | `300` | 超过最大次数后的静默时间，单位秒 |
| `alert.log.rate-limit.max-requests` | `10` | 桶容量（时间窗口内允许的最大告警次数） |
| `alert.log.rate-limit.cache-size` | `1000` | 限流 Key 的最大缓存数量 |

**令牌生成速率计算：**
```
rate = ceil(maxRequests / windowSeconds)
```

**重要说明：**
- 使用向上取整，确保 `rate >= 1`
- 避免整数除法导致的 `rate = 0` 错误

**示例：**
- `max-requests: 10`, `window-seconds: 60`
- `rate = ceil(10 / 60) = ceil(0.167) = 1 个/秒`
- 含义：每秒生成 1 个令牌，最多累积 10 个令牌
- 实际效果：60 秒内最多发送 10 次告警（符合预期）

超过窗口内最大次数后进入静默期，静默期内相同 Key 的告警直接丢弃。缓存使用 Caffeine，并在访问后约 10 分钟过期。

### 2.4 堆栈配置

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `alert.log.stack.max-length` | `5000` | 堆栈最大字符数；小于等于 0 表示不限制 |
| `alert.log.stack.max-frames` | `50` | 每个异常最多展示的堆栈帧数；小于等于 0 表示不限制 |

堆栈截断只影响告警消息，不影响原始日志输出。

## 3. 告警消息内容

模块会从 Spring Environment 缓存以下信息：

- 当前激活 Profile；没有激活 Profile 时使用 `default`；
- `spring.application.name`；没有配置时使用 `unknown`。

告警消息还包括：

- 责任人；
- 日志时间、级别、Logger 名称和线程名；
- `MDC` 中 `x-trace-id` 对应的 TraceId；
- 格式化后的日志消息；
- 异常类型、异常消息和堆栈。

业务代码正常使用 SLF4J/Logback 记录错误即可：

```java
@Slf4j
@Service
public class OrderService {

    public void create(OrderCommand command) {
        try {
            doCreate(command);
        } catch (Exception e) {
            log.error("订单创建失败，orderId={}", command.getOrderId(), e);
            throw e;
        }
    }
}
```

注意：只有以 `ERROR` 级别输出的事件会进入告警流程；异常对象应作为最后一个参数传入，模块才能提取堆栈。

## 4. 动态配置

### 4.1 动态启用/禁用

本模块支持**动态启用/禁用**告警日志功能，无需重启服务即可生效。

**实现原理：**
- `AlertLogAppender.append()` 方法中检查 `property.getEnabled()` 状态
- 配置刷新后，`AlertLogProperty` Bean 自动更新
- 下次日志输出时立即生效

**使用方式：**

1. **通过 Nacos 配置中心修改**

登录 Nacos 控制台，修改配置：

```yaml
alert:
  log:
    enabled: true  # 启用告警日志
```

或

```yaml
alert:
  log:
    enabled: false  # 禁用告警日志
```

发布配置后立即生效，无需重启服务。

2. **验证生效**

- 启用时：ERROR 日志会触发告警
- 禁用时：ERROR 日志不会触发告警（直接跳过）

### 4.2 其他配置动态刷新

除 `enabled` 外，以下配置也支持动态刷新：

- `owners` - 告警接收人
- `exclude-patterns` - 排除的日志表达式
- `sample-rate` - 采样率
- `webhook.*` - Webhook 相关配置
- `rate-limit.*` - 限流相关配置
- `stack.*` - 堆栈相关配置

**注意：** 
- 限流配置变更后，需要等待当前限流窗口过期才会生效
- 所有配置变更都会在下次日志输出时生效

### 4.3 依赖要求

确保服务已引入以下依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-context</artifactId>
</dependency>
```

并在 `bootstrap.yml` 中启用配置刷新：

```yaml
spring:
  cloud:
    nacos:
      config:
        refresh-enabled: true
```

## 5. 启停行为

配置开启后，`AlertLogConfiguration` 会自动创建并启动 `AlertLogAppender`，挂载到 Logback Root Logger。应用关闭时会自动从 Root Logger 移除并停止 Appender，不需要额外的 Logback XML 配置。

`alert.log.enabled` 未设置时，是否创建配置由当前自动配置条件和版本代码决定；为了避免环境差异，生产环境建议明确配置为 `true` 或 `false`。

## 6. 使用建议

- 只把需要人工关注的 ERROR 输出到告警日志，普通业务异常可加入 `exclude-patterns`；
- `exclude-patterns` 使用 Java 正则表达式，配置错误的正则会影响 Appender 初始化；
- 采样率适合控制高频错误，不适合替代业务限流；
- 通过 `max-requests` 和 `silence-seconds` 控制同类错误告警风暴；
- 不要在日志消息中拼接订单号等高变化内容，否则会产生大量不同限流 Key；
- 告警 Webhook 地址和密钥建议通过环境变量或配置中心注入，不要提交到代码仓库；
- TraceId 依赖调用链路已经写入 `MDC` 的 `x-trace-id`；
- **动态配置**：通过 Nacos 配置中心修改 `alert.log.enabled` 可立即生效，无需重启服务。

## 7. 故障排查

### 7.1 配置修改后未生效

**原因：** Nacos 配置刷新未启用

**解决方案：**
```yaml
spring:
  cloud:
    nacos:
      config:
        refresh-enabled: true
```

### 7.2 告警未发送

**排查步骤：**
1. 检查 `alert.log.enabled` 是否为 `true`
2. 检查 `webhook.url` 是否配置
3. 检查日志级别是否为 ERROR
4. 检查是否被 `exclude-patterns` 排除
5. 检查是否被限流（查看日志中的限流信息）

### 7.3 告警延迟

**原因：** 限流机制生效

**解决方案：**
调整限流配置：
```yaml
alert:
  log:
    rate-limit:
      max-requests: 20  # 增加最大请求数
      window-seconds: 30  # 缩短时间窗口
```

## 8. 相关文档

- [动态配置详细指南](./DYNAMIC_CONFIG_GUIDE.md)
- [Spring Cloud Config 动态刷新](https://spring.io/projects/spring-cloud-config)
- [Logback Appender 文档](https://logback.qos.ch/manual/appenders.html)
