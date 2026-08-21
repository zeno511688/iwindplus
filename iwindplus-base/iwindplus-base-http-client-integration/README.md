# iwindplus-base-http-client-integration

HTTP 客户端集成模块，用于基于 `iwindplus-base-http-client` 调用第三方接口。

## 模块职责

- 复用 `HttpClientExecutorStrategyFactory` 提供的 HTTP 客户端执行器
- 封装第三方地址查询接口，避免业务模块直接处理请求参数和响应 JSON
- 集成 SumSub KYC 身份验证服务，提供完整的 KYC 流程支持
- 使用 DTO 接收第三方接口响应，并转换为统一的 VO 对象
- 通过 Spring Boot 自动配置注册服务（配置类位于根包）
- 提供灵活的 Webhook 处理机制，支持注解方式处理回调事件

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-http-client-integration</artifactId>
</dependency>
```

该模块依赖 `iwindplus-base-http-client`，使用方需要确保 HTTP 客户端模块的配置已生效。

## 自动配置

模块提供两个独立的自动配置类（位于根包）：

- **AddressAutoConfiguration**：地址服务配置，自动注册 `AddressService`
- **SumSubAutoConfiguration**：SumSub 服务配置，自动注册以下组件：
  - `SumSubWebhookListenerProcessor`：Webhook 监听器注解处理器
  - `SumSubWebhookHandlerStrategyFactory`：Webhook 处理器策略工厂
  - `SumSubService`：SumSub 服务
  - `SumSubController`：SumSub Webhook 控制器（可通过配置禁用）

引入模块后，Spring Boot 会通过自动配置文件加载配置，业务代码可以直接注入 `AddressService` 或 `SumSubService`。

## 地址服务

`AddressService` 提供以下第三方地址查询能力：

| 方法 | 第三方服务 | 必填参数 |
| --- | --- | --- |
| `getAddressByPconline` | 太平洋网络 | `ip` |
| `getAddressByGaode` | 高德云图 | `ip`、`appCode` |
| `getAddressByIp138` | IP138 | `ip`、`token` |
| `getAddressByBaidu` | 百度地图 | `ip`、`ak` |
| `getAddressByTencent` | 腾讯地图 | `ip`、`key` |

所有方法均返回 `Optional<AddressVO>`。当第三方接口没有返回有效地址信息时，返回空的 `Optional`。

## 使用示例

```java
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final AddressService addressService;

    public Optional<AddressVO> queryAddress(String ip, String appCode) {
        return addressService.getAddressByGaode(ip, appCode);
    }
}
```

## 返回对象

`AddressVO` 位于 `domain.vo.address` 包，统一提供以下字段：

- `ip`：IP 地址
- `province`：省份
- `city`：城市

第三方响应对象位于 `domain.dto.address` 包，目前包括：

- `PconlineAddressDTO`
- `GaodeAddressDTO`
- `Ip138AddressDTO`
- `BaiduAddressDTO`
- `TencentAddressDTO`

## 地址接口常量

第三方接口地址和响应字段常量统一维护在 `AddressConstant` 中。业务代码不需要自行拼接固定 URL。

## SumSub 服务

`SumSubService` 提供 SumSub KYC 身份验证服务的集成能力。

### 功能列表

**SumSubBaseService 基础接口：**

| 功能模块 | 方法 | 说明 |
| --- | --- | --- |
| 配置管理 | `getProperty` | 获取 SumSub 配置 |
| Webhook | `handleWebhook` | 处理 Webhook 回调 |
| 签名验证 | `verifyWebhookSignature` | 验证 Webhook 签名 |

**SumSubService 业务接口（继承 SumSubBaseService）：**

| 功能模块 | 方法 | 说明 |
| --- | --- | --- |
| 访问令牌 | `getAccessToken` | 获取访问令牌 |
| 申请人管理 | `createApplicant` | 创建申请人 |
| 申请人管理 | `getApplicant` | 获取申请人信息 |
| 申请人管理 | `getApplicantByExternalUserId` | 根据外部用户ID获取申请人信息 |
| 申请人管理 | `updateApplicant` | 更新申请人信息 |
| 申请人管理 | `resetApplicant` | 重置申请人审核状态 |
| 文档管理 | `getDocuments` | 获取申请人文档列表 |
| 文档管理 | `getDocument` | 获取文档信息 |
| 文档管理 | `getDocumentChecks` | 获取文档检查结果 |

### 配置说明

在 `application.yml` 中配置 SumSub 参数：

```yaml
sum-sub:
  api-key: your-api-key
  api-secret: your-api-secret
  default-level-name: basic-kyc-level  # 可选，默认审核级别
  default-token-ttl: 2592000  # 可选，默认令牌过期时间（秒）
  # Webhook签名验证配置（可选）
  webhook-secret-key: your-webhook-secret-key  # Webhook签名密钥
  # web接口配置（可选）
  web:
    enabled: true  # 是否启用web接口
    path: /sumsub/webhook  # 接口路径
```

**配置说明：**
- `api-key` 和 `api-secret`：在 SumSub 后台获取（必填）
- `default-level-name`：默认审核级别，默认为 `basic-kyc-level`
  - 用于 `getAccessToken()` 方法：当请求中未指定 `levelName` 时使用
  - 用于 `createApplicant()` 方法：当请求中未指定 `review` 时使用
- `default-token-ttl`：默认令牌过期时间（秒），默认为 2592000（30天）
  - 用于 `getAccessToken()` 方法：当请求中未指定 `ttlInSecs` 时使用
- `webhook-secret-key`：Webhook 签名密钥，在 SumSub 后台配置 Webhook 时设置（可选）
- `web.enabled`：是否启用 web 接口，默认为 `true`
- `web.path`：web 接口路径，不同的服务可以配置不同的路径

### 使用示例

#### 1. 获取访问令牌

```java
@Service
@RequiredArgsConstructor
public class KycService {

    private final SumSubService sumSubService;

    // 方式一：使用配置中的默认值
    public Optional<SumSubAccessTokenVO> getAccessTokenWithDefaults(String externalUserId) {
        SumSubAccessTokenDTO request = SumSubAccessTokenDTO.builder()
            .externalUserId(externalUserId)
            // 不设置 ttlInSecs 和 levelName，将使用配置中的默认值
            .build();
        return sumSubService.getAccessToken(request);
    }

    // 方式二：自定义值（覆盖默认值）
    public Optional<SumSubAccessTokenVO> getAccessTokenWithCustom(String externalUserId) {
        SumSubAccessTokenDTO request = SumSubAccessTokenDTO.builder()
            .externalUserId(externalUserId)
            .ttlInSecs(3600)  // 自定义过期时间
            .levelName("advanced-kyc-level")  // 自定义审核级别
            .build();
        return sumSubService.getAccessToken(request);
    }
}
```

#### 2. 创建申请人

```java
// 方式一：使用配置中的默认审核级别
public Optional<SumSubApplicantVO> createApplicantWithDefaults(String externalUserId, String email, String phone) {
    SumSubApplicantDTO request = SumSubApplicantDTO.builder()
        .externalUserId(externalUserId)
        .email(email)
        .phone(phone)
        // 不设置 review，将使用配置中的 defaultLevelName
        .build();
    return sumSubService.createApplicant(request);
}

// 方式二：自定义审核级别
public Optional<SumSubApplicantVO> createApplicantWithCustom(String externalUserId, String email, String phone) {
    SumSubApplicantDTO request = SumSubApplicantDTO.builder()
        .externalUserId(externalUserId)
        .email(email)
        .phone(phone)
        .review("advanced-kyc-level")  // 自定义审核级别
        .build();
    return sumSubService.createApplicant(request);
}
```

#### 3. 处理 Webhook 回调

模块已提供 `SumSubController` 控制器，自动处理 Webhook 回调：

**控制器配置：**
- 默认路径：`/sumsub/handleWebhook`
- 可通过配置 `sum-sub.web.path` 自定义路径
- 可通过配置 `sum-sub.web.enabled=false` 禁用控制器

**配置示例：**
```yaml
sum-sub:
  web:
    enabled: true  # 启用web接口（默认为true）
    path: kyc  # 自定义路径，最终路径为 /kyc/handleWebhook
```

**业务方只需实现 Webhook 处理器：**

```java
@Slf4j
@Component
public class KycWebhookListener {

    @SumSubWebhookListener(SumSubConstant.WEBHOOK_TYPE_APPLICANT_REVIEWED)
    public void handleApplicantReviewed(SumSubWebhookDTO webhookData) {
        log.info("申请人审核完成: applicantId={}", webhookData.getApplicantId());
        // 更新数据库中的 KYC 状态
        // 发送通知给用户
    }
}
```

**如果需要自定义控制器：**
1. 配置 `sum-sub.web.enabled=false` 禁用默认控制器
2. 创建自定义控制器并注入 `SumSubService`
3. 参考 `SumSubController` 实现签名验证和业务处理

### Webhook 处理器

业务方使用 `@SumSubWebhookListener` 注解处理 Webhook 事件：

```java
@Slf4j
@Component
public class KycWebhookListener {

    // 使用常量（推荐）
    @SumSubWebhookListener(SumSubConstant.WEBHOOK_TYPE_APPLICANT_REVIEWED)
    public void handleApplicantReviewed(SumSubWebhookDTO webhookData) {
        log.info("申请人审核完成: applicantId={}", webhookData.getApplicantId());
        // 更新数据库中的 KYC 状态
        // 发送通知给用户
    }

    // 使用字符串
    @SumSubWebhookListener("applicantPending")
    public void handleApplicantPending(SumSubWebhookDTO webhookData) {
        // 处理待审核逻辑
    }

    // 处理自定义事件类型（SumSub新增的事件）
    @SumSubWebhookListener("newEventType")
    public void handleNewEvent(SumSubWebhookDTO webhookData) {
        // 处理新事件类型
    }
}
```

**优势：**
- 零侵入：业务方只需添加注解，无需实现接口
- 动态扩展：支持任意事件类型，无需修改枚举
- 自动发现：Spring 自动扫描并注册处理器
- 灵活简洁：一个类可以处理多种事件类型

### Webhook 事件类型

`SumSubConstant` 中定义了常用的 Webhook 事件类型常量：

| 常量名 | 事件类型 | 说明 |
|--------|---------|------|
| `WEBHOOK_TYPE_APPLICANT_REVIEWED` | applicantReviewed | 申请人审核完成 |
| `WEBHOOK_TYPE_APPLICANT_PENDING` | applicantPending | 申请人待审核 |
| `WEBHOOK_TYPE_APPLICANT_PERSONAL_INFO_CHANGED` | applicantPersonalInfoChanged | 申请人个人信息变更 |
| `WEBHOOK_TYPE_APPLICANT_DOCUMENT_UPLOADED` | applicantDocumentUploaded | 申请人文档上传 |
| `WEBHOOK_TYPE_APPLICANT_DOCUMENT_STATUS_CHANGED` | applicantDocumentStatusChanged | 申请人文档状态变更 |
| `WEBHOOK_TYPE_APPLICANT_CREATED` | applicantCreated | 申请人已创建 |
| `WEBHOOK_TYPE_APPLICANT_DELETED` | applicantDeleted | 申请人已删除 |
| `WEBHOOK_TYPE_APPLICANT_RESET` | applicantReset | 申请人已重置 |
| `WEBHOOK_TYPE_APPLICANT_SUSPENDED` | applicantSuspended | 申请人已暂停 |
| `WEBHOOK_TYPE_APPLICANT_RESUMED` | applicantResumed | 申请人已恢复 |

### Webhook 签名验证

为了确保 Webhook 请求来自 SumSub，建议配置 `webhook-secret-key` 并启用签名验证：

**签名算法：**
```
signature = HMAC-SHA256(timestamp + requestBody, secretKey)
```

**使用方式：**
```java
// 验证签名
boolean isValid = sumSubService.verifyWebhookSignature(timestamp, body, signature);
```

**注意事项：**
- 签名验证需要配置 `sum-sub.webhook-secret-key`
- 如果未配置 `webhook-secret-key`，签名验证将返回 `false`
- 签名验证失败时会记录警告日志

### 默认值使用说明

`SumSubProperty` 中提供了两个默认值配置，可以简化业务代码：

**1. defaultLevelName（默认审核级别）**

适用场景：
- `getAccessToken()`：当请求中未指定 `levelName` 时使用
- `createApplicant()`：当请求中未指定 `review` 时使用

**2. defaultTokenTtl（默认令牌过期时间）**

适用场景：
- `getAccessToken()`：当请求中未指定 `ttlInSecs` 时使用

**使用原则：**
- 请求参数优先：如果请求中指定了值，使用请求中的值
- 配置默认值兜底：如果请求中未指定值，使用配置中的默认值
- 灵活覆盖：可以在不同环境使用不同的默认值（如测试环境使用较短的过期时间）

### DTO 对象

所有 SumSub 相关的 DTO 对象位于 `domain.dto.sumsub` 包：

- `SumSubAccessTokenDTO`：访问令牌请求
- `SumSubApplicantDTO`：申请人请求
- `SumSubWebhookDTO`：Webhook 回调数据

### VO 对象

所有 SumSub 相关的 VO 对象位于 `domain.vo.sumsub` 包：

- `SumSubAccessTokenVO`：访问令牌响应
- `SumSubApplicantVO`：申请人响应
- `SumSubDocumentVO`：文档信息
- `SumSubDocumentCheckVO`：文档检查结果

### 常量定义

SumSub 相关常量统一维护在 `SumSubConstant` 中：

- **请求头常量**：HTTP 请求头（如 `HEADER_X_APP_TOKEN`、`HEADER_X_APP_SECRET` 等）
- **请求参数常量**：请求参数名（如 `PARAM_USER_ID`、`PARAM_EXTERNAL_USER_ID` 等）
- **签名算法常量**：签名算法名称（如 `ALGORITHM_HMAC_SHA256`）
- **Webhook 事件类型常量**：Webhook 事件类型（如 `WEBHOOK_TYPE_APPLICANT_REVIEWED` 等）
- **URL 常量**：API 接口地址（内部类 `SumSubConstant.Url`）

地址相关常量统一维护在 `AddressConstant` 中：

- **请求参数常量**：请求参数名（如 `PARAM_IP`、`PARAM_AK` 等）
- **JSON 字段常量**：响应字段名（如 `FIELD_ADDRESS_DETAIL` 等）
- **URL 常量**：第三方接口地址（内部类 `AddressConstant.Url`）

## 详细文档

- [SumSub Webhook 处理器使用指南](docs/SumSub-Webhook-Handler-Guide.md)
- [SumSub Webhook 配置指南](docs/SumSub-Webhook-Configuration-Guide.md)

## 最佳实践

### 1. 安全性
- ✅ 配置 `webhook-secret-key` 启用 Webhook 签名验证
- ✅ 使用 HTTPS
- ✅ 不要在代码中硬编码密钥

### 2. 可靠性
- ✅ 实现 Webhook 处理的幂等性
- ✅ 添加异常处理和日志
- ✅ 返回正确的 HTTP 状态码

### 3. 性能
- ✅ 异步处理耗时操作
- ✅ 快速响应 Webhook（避免超时）

## 注意事项

1. **方法签名要求**：`@SumSubWebhookListener` 注解的方法必须只有一个参数，且参数类型为 `SumSubWebhookDTO`
2. **Spring Bean 要求**：包含注解方法的类必须使用 `@Component` 或其他 Spring 注解注册为 Bean
3. **唯一性**：每个事件类型只能有一个处理器，如果有多个，后注册的会覆盖先注册的
4. **幂等性**：Webhook 可能会重复发送，建议实现幂等性处理
5. **控制器配置**：默认提供 `SumSubController`，可通过配置 `sum-sub.web.enabled=false` 禁用
6. **签名验证**：签名验证失败时会抛出 `BizException` 异常，建议在全局异常处理器中处理
