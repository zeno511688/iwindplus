# 操作审计与敏感操作校验模块（iwindplus-base-operate）

本模块提供两个基于 Spring AOP 的能力：

1. **操作日志审计**：使用 `@OperateLog` 标记业务方法，切面执行完成后组装 `OperateLogDTO`，通过 Spring 事件异步发送到配置的日志服务 URL；
2. **敏感操作校验**：使用 `@OperateValid` 标记敏感方法，从请求头读取 GA、邮箱、短信或 YubiKey 校验信息，调用远程校验接口，校验失败时阻止目标方法执行。

```text
@OperateLog ──▶ OperateLogAspect ──▶ OperateLogEvent
                                      │
                                      └──▶ @Async OperateLogListener
                                            └──▶ HTTP POST 日志服务

@OperateValid ──▶ OperateValidAspect
                   ├──▶ 读取当前用户和请求头验证码
                   ├──▶ HTTP POST 扩展功能校验服务
                   └──▶ 校验失败抛出 BizException
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-operate</artifactId>
</dependency>
```

该模块使用 `iwindplus-base-http-client` 调用远程日志和校验服务，应用需要提供 HTTP Client 模块的默认执行器。操作日志监听使用 `@Async`，应用还需要启用 Spring 异步能力并提供可用的异步执行器。

## 2. 配置

```yaml
operate:
  enabled: true
  log:
    enabled: true
    url: lb://iwindplus-log/inner/operation/log/save
    enabled-request-param: true
    enabled-request-body: false
    enabled-response-body: false
  valid:
    enabled: true
    url: lb://iwindplus-mgt/inner/user/checkExtendFunctionByUserId
```

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `operate.enabled` | `true` | 是否注册操作日志和操作校验切面 |
| `operate.log.enabled` | `true` | 是否记录操作日志 |
| `operate.log.url` | `lb://iwindplus-log/inner/operation/log/save` | 日志保存接口地址 |
| `operate.log.enabled-request-param` | `true` | 是否记录方法参数拼接出的 Query 参数 |
| `operate.log.enabled-request-body` | `false` | 是否记录 JSON 请求体 |
| `operate.log.enabled-response-body` | `false` | 是否记录方法返回结果 |
| `operate.valid.enabled` | `true` | 是否启用敏感操作校验切面 |
| `operate.valid.url` | `lb://iwindplus-mgt/inner/user/checkExtendFunctionByUserId` | 用户扩展功能校验接口 |

`operate.enabled=false` 时，模块不会注册三个切面/监听器 Bean。日志、请求体和响应体可能包含敏感数据，生产环境建议按接口敏感程度单独评估是否开启。

## 3. 操作日志

### 3.1 基本使用

```java
@OperateLog(
    bizType = "USER",
    operateType = "UPDATE",
    operateName = "修改用户",
    operateDesc = "修改用户基本信息"
)
public ResultVO<Boolean> updateUser(UserEditDTO request) {
    userService.update(request);
    return ResultVO.success(Boolean.TRUE);
}
```

注解只能标注方法，必填属性为：

- `bizType`：业务类型；
- `operateType`：操作类型；
- `operateName`：操作名称；
- `operateDesc`：操作描述。源码在描述为空时会回退到操作名称。

上述属性都支持 SpEL 表达式，会基于当前方法参数和返回值解析。

### 3.2 生成业务流水号

默认情况下，切面使用 Spring `KeyGenerator` 根据目标对象、方法和参数生成 `bizNumber`。需要固定使用业务主键时，可以通过 `keys` 指定参数表达式：

```java
@OperateLog(
    bizType = "ORDER",
    operateType = "CANCEL",
    operateName = "取消订单",
    operateDesc = "取消订单",
    keys = {"#orderId"}
)
public void cancel(Long orderId) {
    orderService.cancel(orderId);
}
```

多个 `keys` 表达式解析结果会用下划线连接，例如 `#orderId`、`#request.itemId`。如果业务流水号无法生成，切面会回退为随机 UUID。

### 3.3 条件记录

`conditions` 支持 SpEL。当配置的任意条件解析为 `false` 时，本次调用不发布操作日志事件：

```java
@OperateLog(
    bizType = "ORDER",
    operateType = "UPDATE",
    operateName = "更新订单",
    operateDesc = "更新订单",
    conditions = {"#request != null", "#request.needAudit"}
)
public void update(OrderEditDTO request) {
    orderService.update(request);
}
```

注解自身的 `enabled=false` 只跳过当前方法的日志记录，不影响其他方法。

### 3.4 记录内容

切面会根据配置组装以下审计信息：

- 应用名、业务流水号、业务类型、操作类型、操作名称和描述；
- 当前用户 ID、组织 ID、真实姓名；
- 请求参数、请求体、返回结果；
- 请求 ID、Trace ID、IP、请求时间、响应时间和耗时；
- 平台、操作系统、浏览器、设备编号、设备版本和设备指纹。

请求参数来源于方法参数名和值，使用 Query 参数格式生成；请求体仅对 JSON 请求开启读取；响应体按配置读取，`ResultVO` 会序列化为 JSON。

### 3.5 日志发送流程

目标方法成功执行后，切面发布 `OperateLogEvent`。`OperateLogListener` 使用 `@Async` 异步监听事件，并通过默认 HTTP Client 向 `operate.log.url` 发起 POST 请求：

```text
业务方法执行完成
       │
       ▼
构造 OperateLogDTO
       │
       ▼
发布 OperateLogEvent
       │
       ▼
异步 POST operate.log.url
```

因此日志发送失败不会回滚已经完成的业务方法。日志服务应提供与 `OperateLogDTO` 兼容的接收接口，并自行实现持久化、幂等和失败补偿。

## 4. 敏感操作校验

### 4.1 基本使用

```java
@OperateValid(enabledGa = true)
public void changePassword(ChangePasswordDTO request) {
    userService.changePassword(request);
}
```

`@OperateValid` 支持四种校验：

| 属性 | 请求头 | 校验内容 |
|---|---|---|
| `enabledGa` | `X-Ga-Captcha` | GA 动态验证码，必须为数字 |
| `enabledMail` | `X-Mail-Captcha` | 邮箱验证码 |
| `enabledSms` | `X-Sms-Captcha` | 短信验证码 |
| `enabledYubikey` | `X-Yubikey-Source`、`X-Yubikey-Sign` | YubiKey 来源和签名 |

源码注释明确说明：参数通过请求头传递，不能在一个注解中同时支持多种校验。实际使用时应只开启一种校验方式：

```java
@OperateValid(enabledMail = true)
public void updateSecuritySetting(SecuritySettingDTO request) {
    securityService.update(request);
}
```

### 4.2 校验流程

```text
进入目标方法前
       │
       ├── 模块和 valid 开关关闭？──▶ 直接放行
       │
       ├── 没有当前用户或 HTTP 请求？──▶ 直接放行
       │
       ├── 读取请求头中的验证码/签名
       │
       ├── 必填参数缺失或格式错误？──▶ BizException
       │
       ├── POST operate.valid.url
       │
       └── 校验绑定状态和验证码结果
             ├── 通过 ──▶ 执行目标方法
             └── 失败 ──▶ BizException，不执行目标方法
```

远程接口接收 `UserExtendFunctionValidDTO`，包含当前用户 ID、组织 ID、GA 验证码、邮箱验证码、短信验证码、YubiKey 来源和签名；接口应返回 `UserExtendFunctionValidVO`。

校验结果包括：

- `gaBindFlag`、`gaCheckFlag`；
- `mailBindFlag`、`mailCheckFlag`；
- `mobileBindFlag`、`smsCheckFlag`；
- `yubikeyBindFlag`、`yubikeyCheckFlag`。

未绑定或校验失败会抛出对应业务异常，例如 GA 未绑定、GA 验证码错误、邮箱验证码错误、短信验证码错误或 YubiKey 校验失败。

### 4.3 请求头示例

```http
POST /api/user/change-password HTTP/1.1
Content-Type: application/json
X-Ga-Captcha: 123456
```

短信校验：

```http
X-Sms-Captcha: 829311
```

YubiKey 校验：

```http
X-Yubikey-Source: yubikey-device-source
X-Yubikey-Sign: yubikey-signature
```

## 5. 扩展功能校验服务

业务应用可以实现 `OperateService`，用于承接或封装用户扩展功能校验：

```java
@Component
public class UserOperateService implements OperateService {
    @Override
    public UserExtendFunctionValidVO checkExtendFunctionByUserId(
        UserExtendFunctionValidDTO request
    ) {
        return userSecurityService.check(request);
    }
}
```

注意：内置 `OperateValidAspect` 的实际调用方式是通过 HTTP Client POST `operate.valid.url`，不是直接调用本地 `OperateService`。当该模块作为服务端实现时，可以使用 `OperateService` 提供对应的 Controller；当作为调用方使用时，应保证配置 URL 可访问。

## 6. 使用注意事项

- 操作日志是在目标方法成功执行后发布，目标方法抛出异常时不会进入正常日志构建流程；
- 日志发送使用 `@Async`，必须启用异步执行能力；
- 日志接口 URL 默认使用服务发现地址，单体应用应改为实际 HTTP 地址；
- 操作校验在目标方法执行前完成，校验失败会阻止目标方法执行；
- `X-Ga-Captcha` 必须是数字，其他验证码和 YubiKey 请求头不能为空；
- 当前校验切面在没有用户上下文或 HTTP 请求时直接放行，后台任务使用时不要误以为会自动完成二次校验；
- 请求体和响应体默认关闭，开启前应评估密码、Token、验证码等敏感信息泄露风险；
- `conditions` 任意一个条件为 `false` 即跳过操作日志；
- 本模块不提供 API 签名功能，API 签名应使用项目中独立的签名模块。
