# iwindplus-base-feign

Feign 模块，提供 Spring Cloud OpenFeign 的基础增强功能，包括请求拦截、统一异常解码、响应解码和 CircuitBreaker 默认兜底。启用 Feign CircuitBreaker 后，未声明 `fallback` / `fallbackFactory` 的客户端可以使用模块提供的默认回退；显式配置的客户端优先使用自己的回退逻辑。

## 功能特性

- ✅ 请求拦截器（FeignRequestInterceptor）- 自动传递请求头和追踪上下文
- ✅ 统一异常解码器（FeignErrorDecoder）- 将 Feign 异常转换为 BizException
- ✅ 响应解码器（FeignResponseDecoder）- 处理响应体自动解包
- ✅ 表单编码器 - 支持 Spring Multipart 文件上传

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-feign</artifactId>
</dependency>
```

## 核心功能

### 1. FeignRequestInterceptor - 请求拦截器

自动传递请求头和追踪上下文，实现微服务间的上下文传递。

#### 配置示例

```yaml
feign:
  request:
    enabled: true  # 启用请求拦截器（默认 true）
```

#### 功能说明

FeignRequestInterceptor 会自动处理以下内容：

1. **追踪上下文传递**：自动传递 Trace ID、Span ID 等追踪信息
2. **请求头传递**：自动传递 HeaderContextHolder 中的请求头
3. **不覆盖已有头**：如果请求头已存在，不会覆盖

#### 使用示例

```java
// 服务 A 调用服务 B
@RestController
@RequestMapping("/api/order")
public class OrderController {
    
    @Resource
    private UserClient userClient;
    
    @GetMapping("/{orderId}")
    public OrderVO getOrder(@PathVariable Long orderId, HttpServletRequest request) {
        // 设置请求头（会自动传递到服务 B）
        HeaderContextHolder.setContext(Map.of(
            "Authorization", request.getHeader("Authorization"),
            "X-User-Id", request.getHeader("X-User-Id"),
            "X-Tenant-Id", request.getHeader("X-Tenant-Id")
        ));
        
        OrderVO order = orderService.getById(orderId);
        
        // Feign 调用用户服务（自动传递请求头）
        ResultVO<UserVO> userResult = userClient.getUser(order.getUserId());
        order.setUserName(userResult.getBizData().getName());
        
        return order;
    }
}

// 服务 B 接收请求
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public ResultVO<UserVO> getUser(@PathVariable Long id) {
        // 可以获取到服务 A 传递的请求头
        Map<String, String> headers = HeaderContextHolder.getContext();
        String userId = headers.get("X-User-Id");
        String tenantId = headers.get("X-Tenant-Id");
        
        UserVO user = userService.getById(id);
        return ResultVO.success(user);
    }
}
```

#### 请求头传递流程

```
┌─────────────────────────────────────────────────────────┐
│              Feign 请求头传递流程                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  服务 A                                                  │
│    │                                                     │
│    ├─> 设置请求头到 HeaderContextHolder                  │
│    │   - Authorization: Bearer token                     │
│    │   - X-User-Id: 12345                                │
│    │   - X-Tenant-Id: 100                                │
│    │                                                     │
│    ├─> Feign 调用服务 B                                  │
│    │                                                     │
│    └─> FeignRequestInterceptor 拦截请求                  │
│        │                                                 │
│        ├─> 注入追踪上下文（Trace ID、Span ID）           │
│        │                                                 │
│        └─> 注入 HeaderContextHolder 中的请求头          │
│                                                          │
│  服务 B                                                  │
│    │                                                     │
│    └─> 接收请求，可以从 HeaderContextHolder 获取请求头   │
│        - Authorization: Bearer token                     │
│        - X-User-Id: 12345                                │
│        - X-Tenant-Id: 100                                │
│        - X-Trace-Id: trace-123                           │
│        - X-Span-Id: span-456                             │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 2. FeignErrorDecoder - 分类错误解码器

`FeignErrorDecoder` 将非 2xx 响应按“业务错误”和“技术故障”分类；两类异常都继承 `BizException`，能继续使用项目的统一异常响应，但只有技术故障默认应计入 CircuitBreaker。

#### 配置示例

```yaml
feign:
  error:
    enabled: true
    # 内部服务使用 RESULT_VO；第三方接口用 GENERIC；NONE 时保留 Feign 默认异常
    response-format: RESULT_VO
    # 非成功响应最多缓存 16KB，避免错误页面或超大响应占用内存
    max-response-body-size: 16384
    # 默认不把错误响应体写入异常，防止敏感信息泄漏
    include-response-body-in-exception: false
    # 保留 Feign RetryableException 的原有重试语义
    preserve-retryable-exception: true
```

#### 分类规则

| HTTP 场景 | 解码结果 | CircuitBreaker | Retry |
| --- | --- | --- | --- |
| 400、404、409、422 | `FeignBusinessException` | 不计入失败 | 不重试 |
| 401、403 | `FeignAuthenticationException` | 不计入失败 | 不重试 |
| 429 | `FeignRateLimitException` | 不计入失败 | 仅由调用方按 `Retry-After` 决定 |
| 408、500、502、503、504 | `FeignTechnicalException` | 计入失败 | 408/502/503/504 建议仅对幂等请求重试 |
| Feign `RetryableException` | 保留原始异常 | 由 Retry/CircuitBreaker 配置决定 | 保留 Feign 语义 |

`RESULT_VO` 只适用于内部统一返回 `ResultVO` 的服务；第三方、网关、HTML 或纯文本错误响应请配置 `GENERIC`，它只保留 HTTP 状态和 Feign 原始消息，不会强行解析业务字段。`NONE` 可完全恢复 Feign 默认错误解码行为。

#### 使用示例

```java
// 服务 A 调用服务 B
@Service
public class OrderService {
    
    @Resource
    private UserClient userClient;
    
    public OrderVO getOrder(Long orderId) {
        OrderVO order = orderService.getById(orderId);
        
        try {
            // Feign 调用用户服务
            ResultVO<UserVO> userResult = userClient.getUser(order.getUserId());
            order.setUserName(userResult.getBizData().getName());
        } catch (BizException e) {
            // 捕获 Feign 异常解码器转换的异常
            log.error("调用用户服务失败: {}", e.getMessage());
            throw e;
        }
        
        return order;
    }
}

// 服务 B 抛出业务异常
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public ResultVO<UserVO> getUser(@PathVariable Long id) {
        UserVO user = userService.getById(id);
        if (user == null) {
            // 抛出业务异常
            throw new BizException(BizCodeEnum.DATA_NOT_FOUND);
        }
        return ResultVO.success(user);
    }
}

// 服务 A 收到的异常
// BizException: code=data_not_found, message=数据不存在
```

#### 异常类型

| 异常 | 用途 | 默认是否计入熔断 |
| --- | --- | --- |
| `FeignBusinessException` | 参数、资源不存在、冲突等业务错误 | 否 |
| `FeignAuthenticationException` | 401、403 认证和权限错误 | 否 |
| `FeignRateLimitException` | 429 远程限流 | 否 |
| `FeignTechnicalException` | 408、500、502、503、504 等技术故障 | 是 |

默认 `FallbackFactory` 对业务类异常直接透传；对技术故障才返回 503 远程服务不可用。这样不会把“用户不存在”“权限不足”伪装成“远程服务宕机”，也不会让默认兜底伪造写操作成功。


```
┌─────────────────────────────────────────────────────────┐
│              Feign 异常解码流程                          │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  服务 B 抛出异常                                         │
│    │                                                     │
│    ├─> BizException                                      │
│    │   └─> 直接透传                                      │
│    │                                                     │
│    ├─> 其他异常                                          │
│    │   └─> 返回 HTTP 错误响应                            │
│    │                                                     │
│  Feign 客户端接收响应                                    │
│    │                                                     │
│    └─> FeignErrorDecoder 解码                            │
│        │                                                 │
│        ├─> 503 ──> BizException(SERVICE_UNAVAILABLE)     │
│        │                                                 │
│        ├─> BizException ──> 直接透传                     │
│        │                                                 │
│        ├─> RetryableException ──> 直接透传               │
│        │                                                 │
│        └─> 其他 ──> 解析响应体                           │
│            │                                             │
│            └─> BizException(code, message)               │
│                                                          │
│  服务 A 捕获异常                                         │
│    │                                                     │
│    └─> BizException                                      │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 3. Resilience4j CircuitBreaker - 客户端级熔断

模块通过 `spring-cloud-starter-circuitbreaker-resilience4j` 接入 Spring Cloud OpenFeign 的官方熔断构建器。启用后，每个 `@FeignClient` 方法都由 CircuitBreaker 包装；失败率达到阈值后，后续请求会在本地快速失败，不再发起远程连接。

```text
业务调用
   │
   ▼
@FeignClient 代理
   │
   ▼
CircuitBreaker（CLOSED / OPEN / HALF_OPEN）
   │
   ├── CLOSED：正常请求远程服务，并统计成功/失败
   ├── OPEN：立即执行 fallback；未提供 fallback 时抛出异常
   └── HALF_OPEN：放行少量探测请求，根据结果恢复或再次打开
   │
   ▼
Feign Client → 远程服务
```

#### 启用熔断

```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true

resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 5
        automatic-transition-from-open-to-half-open-enabled: true
        record-exceptions:
          - feign.FeignException
          - feign.RetryableException
          - java.io.IOException
```

`spring.cloud.openfeign.circuitbreaker.enabled` 默认是 `false`；不设置或设为 `false` 时，Feign 仍按原方式调用，不会创建熔断链路。

#### 客户端回退

熔断器打开、网络错误或被记录为失败的异常发生后，OpenFeign 会调用声明在客户端上的 `fallbackFactory`。回退必须是 Spring Bean，推荐使用 Factory 获取失败原因：

```java
@FeignClient(
    name = "user-service",
    fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    ResultVO<UserVO> getUser(@PathVariable Long id);
}

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return userId -> ResultVO.error("user-service 不可用");
    }
}
```

查询类接口可以返回明确的错误响应或缓存结果；创建、更新、删除等写操作不得伪造成功，应返回失败结果或继续抛出业务异常，交由调用方重试或补偿。

未配置 `fallback` 或 `fallbackFactory` 时，如果 `feign.fallback.enabled=true`，模块会自动注入默认 `FallbackFactory`。默认回退不会返回空对象或伪造成功，而是记录客户端、方法和原始异常，并抛出 `503 Service Unavailable` 对应的 `BizException`。如果关闭默认回退，则恢复 OpenFeign 未配置回退时的默认行为。

#### 实例命名与个性化阈值

默认熔断器实例名由 OpenFeign 根据客户端接口、方法及参数类型生成。若需要使用只含字母数字的实例名，可开启：

```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
        alphanumeric-ids:
          enabled: true
```

然后在运行时通过 Actuator 的 CircuitBreaker 指标或日志确认实际实例名，再在 `resilience4j.circuitbreaker.instances` 下配置该实例的专属阈值。没有单独实例配置时，客户端会继承 `configs.default`。

#### 配置边界

- CircuitBreaker 负责按失败率熔断，不负责限流；限流应使用 `resilience4j.ratelimiter`。
- 仅把确定表示远程调用失败的异常放入 `record-exceptions`；业务校验异常是否计入失败需由业务决定。
- 不要对不可重试的写操作在 fallback 中自动重放；需要重试时必须结合幂等键或业务流水号。
- 熔断状态和阈值是单个应用实例内存态；集群级全局限流、全局熔断需要额外的共享状态方案。

### 3. FeignResponseDecoder - 响应解码器

自动解包响应体，提取 ResultVO 中的 data 字段。

#### 功能说明

FeignResponseDecoder 会自动处理响应体：

1. **自动解包**：从 ResultVO 中提取 data 字段
2. **类型转换**：支持泛型类型转换
3. **错误处理**：响应错误时抛出 BizException

#### 使用示例

```java
// Feign Client
@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {
    
    // 返回值直接是 UserVO，不是 ResultVO<UserVO>
    @GetMapping("/api/user/{id}")
    UserVO getUser(@PathVariable Long id);
    
    // 返回值直接是 List<UserVO>
    @GetMapping("/api/user/list")
    List<UserVO> listUsers();
}

// 服务调用
@Service
public class OrderService {
    
    @Resource
    private UserClient userClient;
    
    public OrderVO getOrder(Long orderId) {
        OrderVO order = orderService.getById(orderId);
        
        // 直接获取 UserVO，不需要从 ResultVO 中提取
        UserVO user = userClient.getUser(order.getUserId());
        order.setUserName(user.getName());
        
        return order;
    }
}
```

## 配置属性

### FeignProperty - Feign 配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `feign.request.enabled` | 是否启用请求拦截器 | `true` |
| `feign.error.enabled` | 是否启用分类错误解码器 | `true` |
| `feign.error.response-format` | 错误响应解析策略：`RESULT_VO`、`GENERIC`、`NONE` | `RESULT_VO` |
| `feign.error.max-response-body-size` | 错误响应体最大读取字节数 | `16384` |
| `feign.error.include-response-body-in-exception` | 是否将错误响应体放入异常对象 | `false` |
| `feign.error.preserve-retryable-exception` | 是否保留 Feign `RetryableException` | `true` |
| `feign.fallback.enabled` | 是否为未声明回退的 Feign 客户端启用默认 `FallbackFactory` | `true` |
| `spring.cloud.openfeign.circuitbreaker.enabled` | 是否启用 OpenFeign CircuitBreaker | `false` |
| `resilience4j.circuitbreaker.configs.default.*` | CircuitBreaker 默认阈值和状态配置 | 由 Resilience4j 提供默认值 |
| `resilience4j.circuitbreaker.instances.*.*` | 指定熔断器实例的个性化配置 | 继承 `configs.default` |

### CircuitBreaker 异常分类

错误解码器已经把 HTTP 错误分成业务类和技术类。建议 Resilience4j 只统计技术异常，忽略参数、权限和限流错误：

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        record-exceptions:
          - com.iwindplus.base.feign.exception.FeignTechnicalException
        ignore-exceptions:
          - com.iwindplus.base.feign.exception.FeignBusinessException
          - com.iwindplus.base.feign.exception.FeignAuthenticationException
          - com.iwindplus.base.feign.exception.FeignRateLimitException
```

如果配置了 `record-exceptions`，原始 `IOException`、`TimeoutException` 等异常可能会在进入解码器前或其他调用链中出现；生产配置应结合实际异常链验证，不要同时配置互相冲突的白名单和黑名单。业务错误不会因为返回了 HTTP 4xx 就触发熔断。


### 1. 请求头传递

```java
// ✅ 推荐：使用 HeaderContextHolder 传递请求头
@GetMapping("/api/order/{id}")
public OrderVO getOrder(@PathVariable Long id, HttpServletRequest request) {
    // 设置请求头
    HeaderContextHolder.setContext(Map.of(
        "Authorization", request.getHeader("Authorization"),
        "X-User-Id", request.getHeader("X-User-Id")
    ));
    
    // Feign 调用会自动传递请求头
    ResultVO<UserVO> userResult = userClient.getUser(userId);
    
    return order;
}

// ❌ 不推荐：手动设置请求头
@PostMapping("/api/user")
public void saveUser(@RequestBody UserDTO dto, @RequestHeader("Authorization") String token) {
    // 需要手动传递每个请求头
    userClient.saveUser(dto, token);
}
```

### 2. 熔断和异常处理

```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
```

需要降级的 Feign 客户端使用 `fallbackFactory`，不使用模块级全局 AOP：

```java
@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    @GetMapping("/api/user/{id}")
    UserVO getUser(@PathVariable Long id);
}

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return id -> {
            log.warn("user-service 调用失败", cause);
            throw new BizException(BizCodeEnum.SERVICE_UNAVAILABLE);
        };
    }
}
```

熔断配置使用 `resilience4j.circuitbreaker`，限流使用 `resilience4j.ratelimiter`，两者是不同能力。写操作不得在 fallback 中伪造成功；需要重试时应配合幂等键或业务流水号。

### 3. 响应解码

```java
// ✅ 推荐：直接返回数据对象
@GetMapping("/api/user/{id}")
UserVO getUser(@PathVariable Long id);

// ✅ 推荐：调用方直接使用
UserVO user = userClient.getUser(userId);

// ❌ 不推荐：返回 ResultVO
@GetMapping("/api/user/{id}")
ResultVO<UserVO> getUser(@PathVariable Long id);

// ❌ 不推荐：调用方需要解包
ResultVO<UserVO> result = userClient.getUser(userId);
UserVO user = result.getBizData();
```

## 注意事项

1. **请求拦截器**：默认启用，会自动传递请求头和追踪上下文
2. **异常解码器**：默认启用，将 Feign 异常转换为 BizException
3. **CircuitBreaker**：通过 `spring.cloud.openfeign.circuitbreaker.enabled=true` 开启；未配置客户端回退时，再由 `feign.fallback.enabled=true` 提供默认回退
4. **限流**：CircuitBreaker 不负责限流，需要单独接入 `resilience4j.ratelimiter`
5. **响应解码**：自动解包 ResultVO，返回值直接是数据对象

## 相关模块

- `iwindplus-base-domain`：领域模型模块，提供 ResultVO、BizException 等
- `iwindplus-base-web`：Web 模块，提供 HeaderContextHolder 等
- `iwindplus-base-monitor`：监控模块，提供 TraceContextPropagator 等
- `iwindplus-base-http-client`：HTTP 客户端模块，提供 HTTP 客户端功能
