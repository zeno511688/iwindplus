# SumSub 模块（iwindplus-base-sumsub）

SumSub 模块提供 KYC（Know Your Customer）身份验证能力，对接 SumSub 服务，支持多配置管理、自动故障转移、Webhook 回调等功能。

## 功能特性

- ✅ 多配置管理（支持多个 SumSub 应用配置）
- ✅ 访问令牌获取
- ✅ 申请人管理（创建、查询、更新、重置）
- ✅ 文档管理（查询文档、文档检查结果）
- ✅ Webhook 回调（签名验证、注解监听）
- ✅ 策略工厂（按配置编码路由）

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-sumsub</artifactId>
</dependency>
```

模块依赖 `iwindplus-base-http-client` 提供的 `HttpClientExecuteHandlerFactory`，使用前应确保 HTTP Client 的自动配置和客户端依赖已生效。

## 架构设计

```text
业务代码
   │
   ▼
SumSubExecuteHandlerFactory（策略工厂）
   │  getHandler(code)           → 获取指定配置的执行器
   │  getDefaultHandler()        → 获取默认执行器（优先级最高）
   │  getConfig(code)            → 获取指定配置
   │  getAvailableHandlers()     → 获取所有可用执行器
   ▼
SumSubExecuteHandler（策略接口）
   │  getCode()                  → 获取配置编码
   │  getAccessToken(...)        → 获取访问令牌
   │  createApplicant(...)       → 创建申请人
   │  getApplicant(...)          → 查询申请人
   │  handleWebhook(...)         → 处理 Webhook
   │  isHealthy()                → 健康检查
   │  getPriority()              → 获取优先级
   ▼
SumSubExecuteHandlerImpl（SumSub 实现）
   │
   └── HttpClientExecuteHandler（HTTP 客户端）
```

## 核心接口

### SumSubExecuteHandler

SumSub 策略接口，定义统一的 SumSub 操作方法：

```java
public interface SumSubExecuteHandler extends BaseService, BaseConfigService<SumSubConfig> {

    /**
     * 获取访问令牌
     */
    Optional<SumSubAccessTokenVO> getAccessToken(SumSubAccessTokenDTO request);

    /**
     * 创建申请人
     */
    Optional<SumSubApplicantVO> createApplicant(SumSubApplicantDTO request);

    /**
     * 获取申请人信息
     */
    Optional<SumSubApplicantVO> getApplicant(String applicantId);

    /**
     * 根据外部用户ID获取申请人信息
     */
    Optional<SumSubApplicantVO> getApplicantByExternalUserId(String externalUserId);

    /**
     * 更新申请人信息
     */
    Optional<SumSubApplicantVO> updateApplicant(String applicantId, SumSubApplicantDTO request);

    /**
     * 重置申请人审核状态
     */
    Optional<SumSubApplicantVO> resetApplicant(String applicantId);

    /**
     * 获取申请人文档列表
     */
    Optional<List<SumSubDocumentVO>> getDocuments(String applicantId);

    /**
     * 获取文档信息
     */
    Optional<SumSubDocumentVO> getDocument(String documentId);

    /**
     * 获取文档检查结果
     */
    Optional<List<SumSubDocumentCheckVO>> getDocumentChecks(String documentId);
}
```

### SumSubExecuteHandlerFactory

策略工厂，负责管理和路由 SumSub 策略：

```java
public class SumSubExecuteHandlerFactory {

    /**
     * 根据配置编码获取 SumSub 执行器
     */
    public SumSubExecuteHandler getHandler(String code);

    /**
     * 获取默认执行器（优先级最高的可用执行器）
     */
    public SumSubExecuteHandler getDefaultHandler();

    /**
     * 根据配置编码获取 SumSub 配置
     */
    public SumSubProperty.SumSubConfig getConfig(String code);

    /**
     * 获取所有可用（健康）的 SumSub 策略，按优先级排序
     */
    public List<SumSubExecuteHandler> getAvailableHandlers();
}
```

## 配置

### 基础配置

```yaml
sumsub:
  enabled: true
  configs:                         # SumSub 配置列表
    - code: "default"              # 配置编码（唯一标识）
      name: "默认SumSub配置"        # 配置名称
      enabled: true                # 是否启用（默认 true）
      priority: 1                  # 优先级（数字越小优先级越高）
      api-key: ${SUMSUB_API_KEY}   # SumSub API Key
      api-secret: ${SUMSUB_API_SECRET}  # SumSub API Secret
      webhook-secret-key: ${SUMSUB_WEBHOOK_SECRET}  # Webhook 签名密钥（可选）
      default-level-name: basic-kyc-level  # 默认审核级别
      default-token-ttl: 600       # 访问令牌默认有效期（秒）
      web:
        enabled: true              # 是否注册 Webhook Controller（默认 true）
        path: sumsub               # Controller 根路径（默认 sumsub）
```

### 多配置示例

```yaml
sumsub:
  configs:
    # 默认配置（优先级最高）
    - code: "default"
      name: "默认SumSub配置"
      enabled: true
      priority: 1
      api-key: ${SUMSUB_API_KEY}
      api-secret: ${SUMSUB_API_SECRET}
      webhook-secret-key: ${SUMSUB_WEBHOOK_SECRET}
      default-level-name: basic-kyc-level
      default-token-ttl: 600
      web:
        enabled: true
        path: sumsub

    # 备用配置（优先级次之）
    - code: "backup"
      name: "备用SumSub配置"
      enabled: true
      priority: 2
      api-key: ${SUMSUB_BACKUP_API_KEY}
      api-secret: ${SUMSUB_BACKUP_API_SECRET}
      webhook-secret-key: ${SUMSUB_BACKUP_WEBHOOK_SECRET}
      default-level-name: basic-kyc-level
      default-token-ttl: 600
      web:
        enabled: false
```

### 配置字段说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `enabled` | `true` | 是否启用 SumSub 服务（全局开关） |
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `true` | 是否启用当前配置 |
| `priority` | 无 | 优先级（数字越小优先级越高） |
| `api-key` | 无 | SumSub API Key |
| `api-secret` | 无 | SumSub API Secret |
| `webhook-secret-key` | 无 | Webhook 签名密钥（配置后启用签名校验） |
| `default-level-name` | 无 | 默认审核级别 |
| `default-token-ttl` | 无 | 访问令牌默认有效期（秒） |
| `web.enabled` | `true` | 是否注册 Webhook Controller |
| `web.path` | `sumsub` | Webhook Controller 根路径 |

## 使用方式

### 注入策略工厂

```java
@Service
@RequiredArgsConstructor
public class KycService {

    private final SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory;
}
```

### 使用默认配置

```java
@Service
@RequiredArgsConstructor
public class KycService {

    private final SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory;

    /**
     * 使用默认配置（优先级最高的可用配置）
     */
    public Optional<SumSubApplicantVO> createApplicant(SumSubApplicantDTO request) {
        SumSubExecuteHandler handler = sumSubExecuteHandlerFactory.getDefaultHandler();
        return handler.createApplicant(request);
    }
}
```

### 使用指定配置

```java
@Service
@RequiredArgsConstructor
public class KycService {

    private final SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory;

    /**
     * 使用指定配置编码
     */
    public Optional<SumSubApplicantVO> createApplicantByCode(String code, SumSubApplicantDTO request) {
        SumSubExecuteHandler handler = sumSubExecuteHandlerFactory.getHandler(code);
        return handler.createApplicant(request);
    }
}
```

### 获取访问令牌

```java
@Service
@RequiredArgsConstructor
public class KycService {

    private final SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory;

    public Optional<SumSubAccessTokenVO> getAccessToken(String externalUserId) {
        SumSubAccessTokenDTO request = SumSubAccessTokenDTO.builder()
            .externalUserId(externalUserId)
            .build();

        return sumSubExecuteHandlerFactory.getDefaultHandler().getAccessToken(request);
    }
}
```

### 查询申请人

```java
@Service
@RequiredArgsConstructor
public class KycService {

    private final SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory;

    public Optional<SumSubApplicantVO> getApplicant(String applicantId) {
        return sumSubExecuteHandlerFactory.getDefaultHandler().getApplicant(applicantId);
    }

    public Optional<SumSubApplicantVO> getByExternalUserId(String externalUserId) {
        return sumSubExecuteHandlerFactory.getDefaultHandler()
            .getApplicantByExternalUserId(externalUserId);
    }
}
```

### 查询文档

```java
@Service
@RequiredArgsConstructor
public class KycService {

    private final SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory;

    public Optional<List<SumSubDocumentVO>> getDocuments(String applicantId) {
        return sumSubExecuteHandlerFactory.getDefaultHandler().getDocuments(applicantId);
    }

    public Optional<List<SumSubDocumentCheckVO>> getDocumentChecks(String documentId) {
        return sumSubExecuteHandlerFactory.getDefaultHandler().getDocumentChecks(documentId);
    }
}
```

## Webhook 回调

### 接口路径

当 `sumsub.configs.<code>.web.enabled=true` 时，自动注册 Controller：

```text
POST /sumsub/handleWebhook
```

实际路径由 `sumsub.configs.<code>.web.path` 决定。Controller 从以下请求头读取签名信息：

- `X-App-Access-TS`
- `X-App-Access-Sign`

配置 `webhook-secret-key` 后，Controller 会先校验签名，再解析 `SumSubWebhookDTO` 并交给对应处理器。

### 使用注解监听 Webhook

在 Spring Bean 方法上使用 `@SumSubWebhookListener`：

```java
@Component
public class SumSubWebhookHandler {

    @SumSubWebhookListener("applicantReviewed")
    public void onApplicantReviewed(SumSubWebhookDTO event) {
        String applicantId = event.getApplicantId();
        String externalUserId = event.getExternalUserId();
        // 更新业务审核状态
    }
}
```

约束：

1. 方法必须是 Spring Bean 的方法；
2. 方法必须只有一个参数；
3. 参数类型必须是 `SumSubWebhookDTO`；
4. 每个事件类型只能保留一个处理器，重复注册时后注册的方法覆盖前一个；
5. 事件类型可以使用 `SumSubConstant` 中的常量，也可以使用自定义字符串。

## 请求 DTO

### SumSubAccessTokenDTO

访问令牌请求：

```java
public class SumSubAccessTokenDTO {

    /**
     * 外部用户ID（必填）
     */
    private String externalUserId;

    /**
     * 过期时间（秒），默认为30天
     */
    private Integer ttlInSecs;

    /**
     * 安全级别
     */
    private String levelName;
}
```

### SumSubApplicantDTO

申请人请求：

```java
public class SumSubApplicantDTO {

    /**
     * 外部用户ID（必填）
     */
    private String externalUserId;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 名
     */
    private String firstName;

    /**
     * 姓
     */
    private String lastName;

    /**
     * 出生日期（格式：YYYY-MM-DD）
     */
    private String dob;

    /**
     * 国籍（ISO 3166-1 alpha-3）
     */
    private String country;

    /**
     * 审核级别
     */
    private String review;
}
```

## 响应 VO

### SumSubAccessTokenVO

访问令牌响应：

```java
public class SumSubAccessTokenVO {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 外部用户ID
     */
    private String externalUserId;

    /**
     * 过期时间戳（毫秒）
     */
    private Long expiredAt;
}
```

### SumSubApplicantVO

申请人响应：

```java
public class SumSubApplicantVO {

    /**
     * 申请人ID
     */
    private String id;

    /**
     * 外部用户ID
     */
    private String externalUserId;

    /**
     * 审核状态
     */
    private ReviewResult review;
}
```

## 自动配置

模块包含一个自动配置：

- `SumSubConfiguration`：由 `sumsub.enabled` 控制，注册 SumSub 策略工厂、Webhook 监听处理器和 Webhook Controller。

配置缺失或必要凭证缺失时，对应策略可能不会注册或会被健康检查过滤。

## 相关对象

- `SumSubProperty`
- `SumSubExecuteHandler`
- `SumSubExecuteHandlerFactory`
- `SumSubWebhookHandlerFactory`
- `SumSubAccessTokenDTO`
- `SumSubApplicantDTO`
- `SumSubWebhookDTO`
- `SumSubAccessTokenVO`
- `SumSubApplicantVO`
- `SumSubDocumentVO`
- `SumSubDocumentCheckVO`

固定请求路径、请求头、事件类型和字段常量维护在 `SumSubConstant` 中。

## 注意事项

- 自动路由只遍历配置中启用且有对应执行策略的配置；
- `Optional.empty()` 表示调用失败、无结果或服务未启用，业务必须处理；
- API 密钥、Secret 和 Webhook 密钥应放入配置中心或密钥管理系统；
- Webhook 签名校验必须使用原始请求体，不能先格式化 JSON 再校验；
- Webhook 和审核结果处理必须具备幂等能力；
- 生产环境必须使用 HTTPS；
- 不要关闭签名校验；
- Webhook 业务处理必须幂等；
- 快速返回 HTTP 响应，耗时业务异步处理。
