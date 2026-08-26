# iwindplus-base-http-client-integration

## 1. 模块定位

本模块基于 `iwindplus-base-http-client` 封装两类第三方 HTTP 集成：

```text
业务代码
   ├── AddressService ── 地址供应商策略 ── 百度 / 高德 / 腾讯 / IP138 / 太平洋网络
   └── KycService ───── KYC 策略 ──────── SumSub
```

模块通过 Spring Boot 自动配置注册服务，并通过供应商配置的 `priority` 和 `enabled` 实现自动路由、健康检查与故障转移。

当前源码中 KYC 只实现了 SumSub，`KycProviderEnum` 不包含 Veriff、Jumio 等其他供应商。

## 2. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-http-client-integration</artifactId>
</dependency>
```

模块依赖 HTTP Client 模块提供的 `HttpClientExecutorStrategyFactory`。使用前应确保 HTTP Client 的自动配置和客户端依赖已生效。

## 3. 地址服务

### 3.1 支持的供应商

| 编码 | 枚举 | 说明 |
|---|---|---|
| `baidu` | `AddressProviderEnum.BAIDU` | 百度地图 |
| `gaode` | `AddressProviderEnum.GAODE` | 高德地图 |
| `tencent` | `AddressProviderEnum.TENCENT` | 腾讯地图 |
| `ip138` | `AddressProviderEnum.IP138` | IP138 |
| `pconline` | `AddressProviderEnum.PCONLINE` | 太平洋网络，不建议生产使用 |

### 3.2 配置

`AddressProperty` 的配置前缀是 `address`，提供商配置是 Map，key 必须使用上表中的编码。

```yaml
address:
  enabled: true
  providers:
    baidu:
      enabled: true
      priority: 1
      api-key: your-baidu-ak
      secret-key: your-baidu-sk
    gaode:
      enabled: true
      priority: 2
      api-key: your-gaode-key
    tencent:
      enabled: true
      priority: 3
      api-key: your-tencent-key
    ip138:
      enabled: false
      priority: 4
      api-key: your-ip138-token
    pconline:
      enabled: false
      priority: 99
```

配置字段：

- `address.enabled`：是否启用地址服务，默认 `true`。
- `address.providers.<code>.enabled`：是否启用当前供应商，默认 `true`。
- `priority`：数字越小优先级越高；未配置时按最低优先级处理。
- `api-key`：供应商 API Key。
- `secret-key`：百度签名场景使用的 Secret Key。

### 3.3 自动路由调用

```java
@RequiredArgsConstructor
@Service
public class IpAddressService {

    private final AddressService addressService;

    public Optional<AddressVO> query(String ip) {
        return addressService.getAddress(ip);
    }
}
```

`getAddress(String ip)` 的处理流程：

1. 检查地址服务是否启用；
2. 跳过本机回环地址和内网 IP；
3. 按已启用供应商的 `priority` 升序遍历；
4. 跳过健康检查失败的供应商；
5. 当前供应商失败后尝试下一个供应商；
6. 全部失败时返回 `Optional.empty()`。

### 3.4 指定供应商调用

```java
Optional<AddressVO> result = addressService.getAddress(
    "8.8.8.8",
    AddressProviderEnum.BAIDU
);

List<AddressProviderEnum> providers = addressService.getAvailableProviders();
```

`AddressVO` 用于承载统一结果，具体字段以源码为准。供应商原始响应 DTO 位于 `domain.dto.address` 包中。

注意：源码会过滤内网 IP，因此不能使用 `127.0.0.1`、`192.168.x.x` 等地址验证第三方查询效果。

## 4. KYC 服务

### 4.1 当前实现

当前 `KycProviderEnum` 只有：

```java
KycProviderEnum.SUMSUB
```

因此不要按照旧文档配置或调用 Veriff、Jumio。

### 4.2 配置

`KycProperty` 的配置前缀是 `kyc`，SumSub 配置位于 `kyc.providers.sumsub`。

```yaml
kyc:
  enabled: true
  providers:
    sumsub:
      enabled: true
      priority: 1
      api-key: your-sumsub-api-key
      api-secret: your-sumsub-api-secret
      webhook-secret-key: your-webhook-secret
      default-level-name: basic-kyc-level
      default-token-ttl: 600
      web:
        enabled: true
        path: sumsub
```

配置字段：

- `kyc.enabled`：是否启用 KYC，默认 `true`。
- `providers.sumsub.enabled`：是否启用 SumSub，默认 `true`。
- `priority`：多供应商扩展时使用，数字越小优先级越高。
- `api-key`、`api-secret`：SumSub API 凭证。
- `webhook-secret-key`：配置后启用 Webhook 签名校验。
- `default-level-name`：默认审核级别。
- `default-token-ttl`：访问令牌默认有效期，单位秒。
- `web.enabled`：是否注册 SumSub Webhook Controller，默认 `true`。
- `web.path`：Controller 根路径，默认 `sumsub`。

### 4.3 统一 KYC 调用

```java
@RequiredArgsConstructor
@Service
public class KycBusinessService {

    private final KycService kycService;

    public Optional<String> create(Object request) {
        // 当前 SumSub 实现会把请求转换为 SumSubApplicantDTO
        return kycService.createVerification(request);
    }

    public Optional<String> createBySumSub(Object request) {
        return kycService.createVerification(request, KycProviderEnum.SUMSUB);
    }

    public Optional<String> query(String applicantId) {
        return kycService.getVerificationStatus(
            KycProviderEnum.SUMSUB,
            applicantId
        );
    }

    public List<KycProviderEnum> providers() {
        return kycService.getAvailableProviders();
    }
}
```

`KycService` 的真实方法：

- `createVerification(Object)`：按优先级自动创建验证流程。
- `createVerification(Object, KycProviderEnum)`：指定供应商创建验证流程。
- `getVerificationStatus(KycProviderEnum, String)`：查询验证状态。
- `handleWebhook(KycProviderEnum, String)`：处理原始 Webhook JSON。
- `verifyWebhookSignature(...)`：校验 Webhook 签名。
- `getAvailableProviders()`：返回健康的供应商。

调用失败或服务未启用时，统一服务返回 `Optional.empty()`；业务代码应明确处理空结果。

### 4.4 SumSub 原生服务

需要使用 SumSub 专有能力时，可以注入 `SumSubService`：

```java
@RequiredArgsConstructor
@Service
public class SumSubBusinessService {

    private final SumSubService sumSubService;

    public Optional<SumSubApplicantVO> create(SumSubApplicantDTO request) {
        return sumSubService.createApplicant(request);
    }

    public Optional<SumSubApplicantVO> getApplicant(String applicantId) {
        return sumSubService.getApplicant(applicantId);
    }

    public Optional<SumSubApplicantVO> getByExternalUserId(String externalUserId) {
        return sumSubService.getApplicantByExternalUserId(externalUserId);
    }

    public Optional<List<SumSubDocumentVO>> documents(String applicantId) {
        return sumSubService.getDocuments(applicantId);
    }

    public Optional<List<SumSubDocumentCheckVO>> checks(String documentId) {
        return sumSubService.getDocumentChecks(documentId);
    }
}
```

`SumSubService` 还提供：

- `getAccessToken(SumSubAccessTokenDTO)`；
- `updateApplicant(String, SumSubApplicantDTO)`；
- `resetApplicant(String)`；
- `getDocument(String)`；
- `handleWebhook(SumSubWebhookDTO)`；
- `verifyWebhookSignature(String, String, String)`。

### 4.5 Webhook 接口

当 `kyc.providers.sumsub.web.enabled=true` 时，自动注册 Controller：

```text
POST /sumsub/handleWebhook
```

实际路径由 `kyc.providers.sumsub.web.path` 决定。Controller 从以下请求头读取签名信息：

- `X-App-Access-TS`
- `X-App-Access-Sign`

配置 `webhook-secret-key` 后，Controller 会先校验签名，再解析 `SumSubWebhookDTO` 并交给 `SumSubService`。

建议：

- 生产环境必须使用 HTTPS；
- 不要关闭签名校验；
- Webhook 业务处理必须幂等；
- 快速返回 HTTP 响应，耗时业务异步处理。

### 4.6 使用注解监听 Webhook

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

## 5. 自动配置

模块包含两个自动配置：

- `AddressAutoConfiguration`：由 `address.enabled` 控制，注册地址供应商策略工厂和 `AddressService`。
- `KycAutoConfiguration`：由 `kyc.enabled` 控制，注册 KYC 策略工厂、`KycService`、SumSub 服务和 Webhook 监听处理器。

供应商没有配置或必要凭证缺失时，对应策略可能不会注册或会被健康检查过滤。

## 6. 相关对象

地址对象：

- `AddressProviderEnum`
- `AddressVO`
- `AddressProperty`
- `AddressService`

KYC 对象：

- `KycProviderEnum`
- `KycProperty`
- `KycService`
- `SumSubService`
- `SumSubApplicantDTO`
- `SumSubApplicantVO`
- `SumSubWebhookDTO`
- `SumSubDocumentVO`
- `SumSubDocumentCheckVO`

固定请求路径、请求头、事件类型和字段常量分别维护在 `AddressConstant`、`HttpClientIntegrationConstant` 和 `SumSubConstant` 中。

## 7. 注意事项

- 地址服务对内网 IP 直接跳过，不代表供应商接口不可用；
- 自动路由只遍历配置中启用且有对应执行策略的供应商；
- `Optional.empty()` 表示调用失败、无结果或服务未启用，业务必须处理；
- 当前 KYC 只实现 SumSub，不要配置不存在的供应商；
- API 密钥、Secret 和 Webhook 密钥应放入配置中心或密钥管理系统；
- Webhook 签名校验必须使用原始请求体，不能先格式化 JSON 再校验；
- Webhook 和审核结果处理必须具备幂等能力。
