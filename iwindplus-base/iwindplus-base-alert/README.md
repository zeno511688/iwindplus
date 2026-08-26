# 告警模块（iwindplus-base-alert）

本模块提供统一的告警发送抽象和渠道策略工厂。目前源码内置飞书渠道，支持：

- 飞书企业自建应用文本消息；
- 飞书机器人 Webhook 文本消息；
- 按 `AlertChannelTypeEnum` 选择渠道执行器；
- 业务模块通过统一 `AlertExecutor` 接口发送，不直接依赖具体渠道实现。

```text
业务代码
   │
   ▼
AlertExecutorStrategyFactory
   │  getDefaultAlertExecutor()
   │  getAlertExecutor(channelType)
   ▼
AlertExecutor
   ├── sendAppMsg(AlertAppRequestDTO)
   └── sendWebhookMsg(AlertWebhookRequestDTO)
             │
             ▼
       FeishuAlertExecutor
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-alert</artifactId>
</dependency>
```

Webhook 发送依赖 HTTP Client 能力；项目中通常还需要引入 `iwindplus-base-http-client` 或由上层模块传递该依赖。

## 2. 配置

配置前缀为 `alert`，对应属性类为 `AlertProperty`：

```yaml
alert:
  enabled: true
  default-alert-channel: FEI_SHU
  feishu:
    enabled: true
    app-id: cli_xxx
    app-secret: xxx
```

### 配置说明

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `alert.enabled` | `true` | 是否启用告警自动配置 |
| `alert.default-alert-channel` | `FEI_SHU` | 默认告警渠道 |
| `alert.feishu.enabled` | `true` | 是否注册飞书执行器；该开关由自动配置读取 |
| `alert.feishu.app-id` | 空 | 飞书自建应用 App ID |
| `alert.feishu.app-secret` | 空 | 飞书自建应用 App Secret |

当前 `AlertChannelTypeEnum` 只有 `FEI_SHU`。配置其他渠道值不会自动产生对应执行器，除非业务自行实现并注册新的 `AlertExecutor`。

## 3. 统一发送方式

注入 `AlertExecutorStrategyFactory`，根据业务场景获取执行器：

```java
@Resource
private AlertExecutorStrategyFactory alertExecutorStrategyFactory;
```

### 3.1 使用默认渠道

```java
AlertExecutor executor = alertExecutorStrategyFactory
    .getDefaultAlertExecutor();
```

默认渠道来自 `alert.default-alert-channel`。

### 3.2 指定渠道

```java
AlertExecutor executor = alertExecutorStrategyFactory
    .getAlertExecutor(AlertChannelTypeEnum.FEI_SHU);
```

如果指定渠道没有对应的 `AlertExecutor`，工厂会抛出业务异常，而不是静默忽略。

## 4. 飞书 Webhook 消息

使用 `AlertWebhookRequestDTO`，字段为：

- `webhookUrl`：飞书机器人 Webhook 地址；
- `content`：文本消息内容，继承自 `AlertBaseRequestDTO`；
- `secret`：可选签名密钥。

```java
AlertWebhookRequestDTO request = AlertWebhookRequestDTO.builder()
    .webhookUrl(webhookUrl)
    .secret(webhookSecret)
    .content("订单服务发生异常，请及时处理")
    .build();

alertExecutorStrategyFactory
    .getAlertExecutor(AlertChannelTypeEnum.FEI_SHU)
    .sendWebhookMsg(request);
```

也可以使用构造方法：

```java
AlertWebhookRequestDTO request = new AlertWebhookRequestDTO(
    webhookUrl,
    "订单服务发生异常，请及时处理",
    webhookSecret);
```

飞书执行器的行为：

- 无 `secret` 时发送普通文本 Webhook 消息；
- 有 `secret` 时计算时间戳和 HMAC-SHA256 签名；
- 通过 HTTP Client 的 OkHttp 执行异步 POST；
- 发送结果记录在日志中，异步异常也会记录错误日志。

## 5. 飞书企业应用消息

企业应用消息使用 `AlertAppRequestDTO`：

- `receiveId`：接收人用户 ID；
- `content`：文本消息内容。

```java
AlertAppRequestDTO request = AlertAppRequestDTO.builder()
    .receiveId(userId)
    .content("您的订单已处理完成")
    .build();

alertExecutorStrategyFactory
    .getAlertExecutor(AlertChannelTypeEnum.FEI_SHU)
    .sendAppMsg(request);
```

飞书执行器使用配置中的 `app-id` 和 `app-secret` 创建自建应用客户端，并按用户 ID 发送文本消息。

## 6. 自定义告警渠道

如果需要增加其他告警渠道，实现 `AlertExecutor` 并注册为 Spring Bean：

```java
@Component
public class CustomAlertExecutor implements AlertExecutor {

    @Override
    public AlertChannelTypeEnum getChannelType() {
        return AlertChannelTypeEnum.FEI_SHU;
    }

    @Override
    public void sendAppMsg(AlertAppRequestDTO entity) {
        // 自定义企业应用消息发送逻辑
    }

    @Override
    public void sendWebhookMsg(AlertWebhookRequestDTO entity) {
        // 自定义 Webhook 发送逻辑
    }
}
```

实际新增渠道时，还需要在 `AlertChannelTypeEnum` 中增加对应枚举值，并确保同一渠道只注册一个最终执行器。工厂会收集所有 `AlertExecutor`，按 `getChannelType()` 建立策略映射。

## 7. 与告警日志模块配合

引入 `iwindplus-base-log` 后，日志模块会复用本模块的 `AlertExecutorStrategyFactory`，把 ERROR 日志发送到配置的 Webhook：

```yaml
alert:
  enabled: true
  default-alert-channel: FEI_SHU
  feishu:
    enabled: true

alert:
  log:
    enabled: true
    webhook:
      channel-type: FEI_SHU
      url: https://open.feishu.cn/open-apis/bot/v2/hook/your-token
      secret: your-secret
```

日志告警还有采样、排除表达式、滑动窗口限流和堆栈截断配置，详见 `iwindplus-base-log/README.md`。

## 8. 注意事项

- 不要在业务代码中直接依赖 `FeishuAlertExecutor`，优先使用工厂和 `AlertExecutor` 接口；
- 飞书 Webhook URL、App Secret 和签名 Secret 应通过环境变量或配置中心提供；
- `sendWebhookMsg` 使用异步 HTTP 请求，方法返回时不代表第三方已经处理成功；
- 企业应用消息发送失败由飞书执行器记录日志，业务是否重试需要自行设计；
- `alert.enabled=false` 时，工厂和执行器不会按本模块自动配置注册；
- 当前模块只内置飞书渠道，README 中没有列出的渠道不能直接配置使用。
