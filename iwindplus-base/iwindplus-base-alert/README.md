# 告警模块（iwindplus-base-alert）

本模块提供统一的告警发送抽象和渠道策略工厂，支持多渠道、多配置。目前源码内置飞书渠道，支持：

- 飞书企业自建应用文本消息；
- 飞书机器人 Webhook 文本消息；
- 按 `AlertChannelTypeEnum` 选择渠道执行器；
- 支持多配置：同一渠道支持多个配置，通过配置编码（`code`）区分；
- 通过策略工厂直接获取指定配置的执行器。

```text
业务代码
   │
   ▼
AlertExecuteHandlerFactory（策略工厂）
   │  getHandler(channelType, code)    → 指定配置编码获取执行器
   │  getConfig(channelType, code)     → 获取配置信息
   ▼
AlertExecuteHandler（策略接口）
   ├── FeishuAlertExecuteHandler（飞书实现）
   └── ...（其他渠道实现）
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

### 2.1 飞书配置（支持多配置）

```yaml
alert:
  enabled: true
  default-code: feishu-prod    # 默认配置编码（可选）
  feishu:
    - code: feishu-prod        # 渠道配置编码（唯一标识）
      name: 生产环境飞书        # 渠道配置名称
      enabled: true            # 是否启用
      priority: 1              # 优先级
      apps:                    # 企业应用消息配置集合
        - code: app-main       # 应用消息配置编码
          name: 主应用          # 应用消息配置名称
          enabled: true
          app-id: cli_xxx      # 应用ID
          app-secret: xxx      # 应用密钥
      webhooks:                # Webhook消息配置集合
        - code: webhook-main   # Webhook配置编码
          name: 主机器人        # Webhook配置名称
          enabled: true
          url: https://open.feishu.cn/open-apis/bot/v2/hook/xxx  # Webhook地址
          secret-key: yyy      # 签名密钥（可选）
    - code: feishu-backup      # 备用渠道配置
      name: 备用环境飞书
      enabled: true
      priority: 2
      apps:
        - code: app-backup
          name: 备用应用
          app-id: cli_yyy
          app-secret: yyy
      webhooks:
        - code: webhook-backup
          name: 备用机器人
          url: https://open.feishu.cn/open-apis/bot/v2/hook/zzz
```

> **说明**：`feishu` 为飞书渠道配置列表，支持多配置。每个渠道配置可同时包含企业应用消息（`apps`）和 Webhook 消息（`webhooks`）两种配置，两者可只配其一，也可同时配置。

### 2.2 顶层配置

| 字段 | 必填 | 说明 |
|------|------|------|
| enabled | 否 | 是否启用告警模块，默认 true |
| default-code | 否 | 默认配置编码，指定后可通过 `getDefaultHandler()` 获取默认策略 |

### 2.3 渠道配置字段说明

| 字段 | 必填 | 说明 |
|------|------|------|
| code | 是 | 渠道配置编码，唯一标识，用于获取指定配置的执行器 |
| name | 否 | 渠道配置名称 |
| enabled | 否 | 是否启用，默认 true |
| priority | 否 | 优先级，数字越小优先级越高，用于选择默认策略 |
| apps | 否 | 企业应用消息配置集合 |
| webhooks | 否 | Webhook 消息配置集合 |

### 2.4 企业应用消息配置（apps）字段说明

| 字段 | 必填 | 说明 |
|------|------|------|
| code | 是 | 应用消息配置编码，唯一标识 |
| name | 否 | 应用消息配置名称 |
| enabled | 否 | 是否启用，默认 true |
| app-id | 是 | 应用ID |
| app-secret | 是 | 应用密钥 |

### 2.5 Webhook 消息配置（webhooks）字段说明

| 字段 | 必填 | 说明 |
|------|------|------|
| code | 是 | Webhook 配置编码，唯一标识 |
| name | 否 | Webhook 配置名称 |
| enabled | 否 | 是否启用，默认 true |
| url | 是 | Webhook 地址 |
| secret-key | 否 | 签名密钥（可选，用于签名验证） |

## 3. 使用方式

注入 `AlertExecuteHandlerFactory`：

```java
@Resource
private AlertExecuteHandlerFactory alertExecuteHandlerFactory;
```

### 3.1 发送应用消息

```java
// 获取指定渠道配置的执行器
AlertExecuteHandler handler = alertExecuteHandlerFactory.getHandler(
    AlertChannelTypeEnum.FEI_SHU, 
    "feishu-prod"
);

// 构建请求（code 指定使用哪个应用消息配置，可省略则取第一个启用配置）
AlertAppRequestDTO appRequest = AlertAppRequestDTO.builder()
    .code("app-main")     // 应用消息配置编码
    .receiveId("ou_xxx")  // 接收人ID
    .content("告警内容")
    .build();

// 发送消息
handler.sendAppMsg(appRequest);
```

### 3.2 发送 Webhook 消息

```java
// 获取指定渠道配置的执行器
AlertExecuteHandler handler = alertExecuteHandlerFactory.getHandler(
    AlertChannelTypeEnum.FEI_SHU, 
    "feishu-prod"
);

// 构建请求（webhook 地址和密钥已配置在 feishu 中，无需在 DTO 中重复传入）
AlertWebhookRequestDTO webhookRequest = AlertWebhookRequestDTO.builder()
    .code("webhook-main")  // Webhook 配置编码
    .content("告警内容")
    .build();

// 发送消息
handler.sendWebhookMsg(webhookRequest);
```

> **说明**：Webhook 地址和密钥统一从配置 `feishu[].webhooks[]` 中读取，无需在 DTO 中传入。若配置中缺少 `url`，将抛出 `BizException(INVALID_STRATEGY)`。

### 3.3 获取配置信息

```java
// 获取指定配置的信息
AlertProperty.BaseConfig config = alertExecuteHandlerFactory.getConfig(
    AlertChannelTypeEnum.FEI_SHU, 
    "feishu-prod"
);
```

### 3.4 按渠道类型获取策略

```java
// 获取该渠道下优先级最高的可用策略
AlertExecuteHandler handler = alertExecuteHandlerFactory.getHandler(
    AlertChannelTypeEnum.FEI_SHU
);
```

### 3.5 获取默认策略

```java
// 根据 alert.default-code 配置的编码查找，未配置时返回 null
AlertExecuteHandler defaultHandler = alertExecuteHandlerFactory.getDefaultHandler();
```

## 4. 飞书 Webhook 消息

使用 `AlertWebhookRequestDTO`，字段为：

- `code`：Webhook 配置编码（可选，未传时取第一个启用配置）；
- `content`：文本消息内容，继承自 `AlertBaseRequestDTO`。

Webhook 地址（`url`）和签名密钥（`secret-key`）统一从配置 `feishu[].webhooks[]` 中读取，无需在 DTO 中传入。

```java
AlertWebhookRequestDTO request = AlertWebhookRequestDTO.builder()
    .code("webhook-main")
    .content("订单服务发生异常，请及时处理")
    .build();

AlertExecuteHandler handler = alertExecuteHandlerFactory.getHandler(
    AlertChannelTypeEnum.FEI_SHU, 
    "feishu-prod"
);
handler.sendWebhookMsg(request);
```

飞书执行器的行为：

- 无 `secret` 时发送普通文本 Webhook 消息；
- 有 `secret` 时计算时间戳和 HMAC-SHA256 签名；
- 通过 HTTP Client 的 OkHttp 执行异步 POST；
- 发送结果记录在日志中，异步异常也会记录错误日志。

## 5. 飞书企业应用消息

企业应用消息使用 `AlertAppRequestDTO`：

- `code`：应用消息配置编码（可选，未传时取第一个启用配置）；
- `receiveId`：接收人用户 ID；
- `content`：文本消息内容。

```java
AlertAppRequestDTO request = AlertAppRequestDTO.builder()
    .code("app-main")
    .receiveId(userId)
    .content("您的订单已处理完成")
    .build();

AlertExecuteHandler handler = alertExecuteHandlerFactory.getHandler(
    AlertChannelTypeEnum.FEI_SHU, 
    "feishu-prod"
);
handler.sendAppMsg(request);
```

飞书执行器使用配置中的 `app-id` 和 `app-secret` 创建自建应用客户端，并按用户 ID 发送文本消息。

## 6. 多配置使用场景

同一渠道支持多个配置，适用于以下场景：

- **多环境隔离**：生产环境、测试环境使用不同的飞书应用
- **多租户场景**：不同租户使用不同的告警配置
- **业务隔离**：不同业务线使用独立的告警通道

示例配置：

```yaml
alert:
  enabled: true
  feishu:
    - code: feishu-order      # 订单业务告警
      name: 订单告警
      enabled: true
      apps:
        - code: app-order
          name: 订单应用
          app-id: cli_order
          app-secret: order_secret
      webhooks:
        - code: webhook-order
          name: 订单机器人
          url: https://open.feishu.cn/open-apis/bot/v2/hook/order
    - code: feishu-payment    # 支付业务告警
      name: 支付告警
      enabled: true
      apps:
        - code: app-payment
          name: 支付应用
          app-id: cli_payment
          app-secret: payment_secret
      webhooks:
        - code: webhook-payment
          name: 支付机器人
          url: https://open.feishu.cn/open-apis/bot/v2/hook/payment
```

使用时根据业务选择对应配置：

```java
// 订单业务告警
AlertExecuteHandler orderHandler = alertExecuteHandlerFactory.getHandler(
    AlertChannelTypeEnum.FEI_SHU, 
    "feishu-order"
);
orderHandler.sendWebhookMsg(orderAlertRequest);

// 支付业务告警
AlertExecuteHandler paymentHandler = alertExecuteHandlerFactory.getHandler(
    AlertChannelTypeEnum.FEI_SHU, 
    "feishu-payment"
);
paymentHandler.sendWebhookMsg(paymentAlertRequest);
```

## 7. 自定义告警渠道

### 7.1 添加渠道类型

在 `AlertChannelTypeEnum` 中添加新的渠道类型：

```java
public enum AlertChannelTypeEnum implements BaseEnum<Integer> {
    FEI_SHU(0, "飞书"),
    DING_TALK(1, "钉钉"),  // 新增
    ;
}
```

### 7.2 创建执行器

```java
@Slf4j
public class DingTalkAlertExecuteHandler extends AbstractBaseServiceImpl<DingTalkConfig> 
    implements AlertExecuteHandler {

    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;

    public DingTalkAlertExecuteHandler(
            DingTalkConfig config,
            HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory) {
        this.setConfig(config);
        this.httpClientExecuteHandlerFactory = httpClientExecuteHandlerFactory;
    }

    @Override
    public AlertChannelTypeEnum getChannelType() {
        return AlertChannelTypeEnum.DING_TALK;
    }

    @Override
    public void sendAppMsg(AlertAppRequestDTO entity) {
        // 实现钉钉应用消息发送
    }

    @Override
    public void sendWebhookMsg(AlertWebhookRequestDTO entity) {
        // 实现钉钉 Webhook 消息发送
    }
}
```

### 7.3 注册 Bean

在 `AlertConfiguration` 中添加：

```java
@Bean
public List<AlertExecuteHandler> dingTalkAlertExecuteHandlers(
        AlertProperty property,
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory) {
    List<AlertExecuteHandler> handlers = new ArrayList<>(10);
    Set<String> codes = new HashSet<>(16);
    property.getDingTalk().stream()
        .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
        .filter(config -> codes.add(config.getCode()))
        .forEach(config -> {
            log.info("Initializing Alert dingTalk strategy [code={}]", config.getCode());
            handlers.add(new DingTalkAlertExecuteHandler(config, httpClientExecuteHandlerFactory));
        });
    return handlers;
}
```

> **说明**：新增第三方渠道时，需要在 `AlertProperty` 中新增对应的配置列表字段（如 `dingTalk`）和配置类（如 `DingTalkConfig`，继承 `BaseConfig`），并同步在 `AlertExecuteHandlerFactory#getConfig` 的 `switch` 分支中补充该渠道的配置来源。然后添加渠道枚举、执行器和注册 Bean 即可。

## 8. 与告警日志模块配合

引入 `iwindplus-base-logging-alert` 后，日志模块会复用本模块的策略工厂，把 ERROR 日志发送到配置的 Webhook：

```yaml
alert:
  enabled: true
  feishu:
    - code: feishu-prod
      enabled: true
      webhooks:
        - code: webhook-main
          url: https://open.feishu.cn/open-apis/bot/v2/hook/your-token
          secret-key: your-secret
  log:
    enabled: true
    alert:
      channel-type: FEI_SHU
      code: feishu-prod
      webhook-code: webhook-main
```

日志告警还有采样、排除表达式、滑动窗口限流和堆栈截断配置，详见 `iwindplus-base-logging-alert/README.md`。

## 9. 核心接口

### 9.1 AlertExecuteHandlerFactory

策略工厂，负责管理和路由告警策略。

```java
public class AlertExecuteHandlerFactory {
    /**
     * 根据渠道类型获取告警策略（该渠道下优先级最高的可用策略）
     */
    public AlertExecuteHandler getHandler(AlertChannelTypeEnum channelType);

    /**
     * 根据渠道类型和配置编码获取告警策略
     */
    public AlertExecuteHandler getHandler(AlertChannelTypeEnum channelType, String code);

    /**
     * 获取默认告警策略（根据 alert.default-code 配置的编码查找，未配置时返回 null）
     */
    public AlertExecuteHandler getDefaultHandler();

    /**
     * 根据渠道类型和配置编码获取告警配置
     */
    public AlertProperty.BaseConfig getConfig(AlertChannelTypeEnum channelType, String code);
}
```

> **注意**：`getHandler(channelType)` 和 `getHandler(channelType, code)` 在找不到对应策略时会抛出 `BizException`（`INVALID_STRATEGY`）；`getDefaultHandler()` 在未配置 `default-code` 或编码不存在时返回 `null`。

### 9.2 AlertExecuteHandler

策略接口，定义告警渠道的具体操作。

```java
public interface AlertExecuteHandler extends BaseService {
    AlertChannelTypeEnum getChannelType();

    void sendAppMsg(AlertAppRequestDTO entity);
    void sendWebhookMsg(AlertWebhookRequestDTO entity);
}
```

### 9.3 BaseService

通用业务层接口，提供健康检查、优先级和配置编码。

```java
public interface BaseService {
    default boolean isHealthy() { return true; }
    default int getPriority() { return Integer.MAX_VALUE; }
    String getCode();
}
```

### 9.4 BaseConfigService

通用配置业务层接口，管理配置对象。

```java
public interface BaseConfigService<T> {
    T getConfig();
    void setConfig(T config);
}
```

## 10. 注意事项

- **配置编码唯一性**：同一渠道下的渠道配置编码必须唯一；同一渠道配置下的 app/webhook 配置编码也必须唯一
- **健康检查**：通过 `enabled` 字段控制策略是否可用
- **消息类型支持**：一个渠道配置可同时支持企业应用消息和 Webhook 消息，也可只配置其一；发送时按 `code` 解析对应配置，缺少对应配置时抛出 `BizException(INVALID_STRATEGY)`
- **异步发送**：Webhook 消息采用异步发送，不阻塞主线程
- 不要在业务代码中直接依赖 `FeishuAlertExecuteHandler`，优先使用 `AlertExecuteHandlerFactory` 获取执行器
- 飞书 Webhook URL、App Secret 和签名 Secret 应通过环境变量或配置中心提供
- `sendWebhookMsg` 使用异步 HTTP 请求，方法返回时不代表第三方已经处理成功
- 企业应用消息发送失败由飞书执行器记录日志，业务是否重试需要自行设计
