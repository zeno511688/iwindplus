# HTTP 客户端模块（iwindplus-base-http-client）

HTTP 客户端模块，提供统一的 HTTP 客户端抽象，支持多种 HTTP 客户端实现（Apache HttpClient、OkHttp、RestClient、WebClient），支持同步/异步请求、文件上传、API 防护等功能。

## 功能特性

- ✅ 统一的 HTTP 客户端抽象（HttpClientExecuteHandler）
- ✅ 多种 HTTP 客户端实现
  - Apache HttpClient
  - OkHttp
  - RestClient（Spring 6.1+）
  - WebClient（响应式）
- ✅ 同步/异步请求支持
- ✅ 文件上传支持
- ✅ API 防护（签名验证）
- ✅ Micrometer 观察（监控指标）
- ✅ 限流熔断支持
- ✅ 代理支持
- ✅ 重试机制

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-http-client</artifactId>
</dependency>
```

## 架构设计

```text
业务代码
   │
   ▼
HttpClientExecuteHandlerFactory（策略工厂）
   │  getDefaultHandler()    → 使用 http.client.default-http-client
   │  getHandler(type)       → 显式选择具体客户端
   ▼
HttpClientExecuteHandler（策略接口）
   ├── ApacheHttpClientExecuteHandler（Apache HttpClient 实现）
   ├── OkHttpClientExecuteHandler（OkHttp 实现）
   ├── RestClientExecuteHandler（RestClient 实现）
   └── WebClientExecuteHandler（WebClient 实现）
```

## 核心接口

### HttpClientExecuteHandler

HTTP 客户端策略接口，定义统一的 HTTP 操作方法：

```java
public interface HttpClientExecuteHandler {
    
    /**
     * 获取当前 HTTP 客户端类型
     */
    HttpClientTypeEnum getClientType();
    
    /**
     * 同步 GET 请求
     */
    <T> T get(String url, Map<String, ?> query, Map<String, String> headers, Class<T> responseType);
    
    /**
     * 同步 GET 请求（支持泛型响应）
     */
    <T> T get(String url, Map<String, ?> query, Map<String, String> headers, TypeReference<T> typeReference);
    
    /**
     * 异步 GET 请求
     */
    <T> CompletionStage<T> getAsync(String url, Map<String, ?> query, Map<String, String> headers, Class<T> responseType);
    
    /**
     * 同步 POST 请求（JSON 请求体）
     */
    <T> T post(String url, Object body, Map<String, String> headers, Class<T> responseType);
    
    /**
     * 异步 POST 请求（JSON 请求体）
     */
    <T> CompletionStage<T> postAsync(String url, Object body, Map<String, String> headers, Class<T> responseType);
    
    /**
     * 同步 POST 请求（表单 / 文件上传）
     */
    <T> T post(String url, Map<String, ?> form, List<MultipartFile> files, Map<String, String> headers, Class<T> responseType);
    
    /**
     * 同步 PUT 请求
     */
    <T> T put(String url, Object body, Map<String, String> headers, Class<T> responseType);
    
    /**
     * 同步 DELETE 请求
     */
    <T> T delete(String url, Map<String, ?> query, Map<String, String> headers, Class<T> responseType);
    
    /**
     * 同步通用执行入口
     */
    <T> T exchange(HttpRequestSpecDTO request, Class<T> responseType);
    
    /**
     * 异步通用执行入口
     */
    <T> CompletionStage<T> exchangeAsync(HttpRequestSpecDTO request, Class<T> responseType);
}
```

### HttpClientExecuteHandlerFactory

策略工厂，负责管理和路由 HTTP 客户端策略：

```java
public class HttpClientExecuteHandlerFactory {
    
    /**
     * 获取默认执行器（根据 http.client.default-http-client 配置）
     */
    public HttpClientExecuteHandler getDefaultHandler();
    
    /**
     * 获取指定类型的执行器
     */
    public HttpClientExecuteHandler getHandler(HttpClientTypeEnum httpClientType);
}
```

## 配置

### 基础配置

```yaml
http:
  client:
    enabled: true                    # 启用 HTTP 客户端（默认 true）
    default-http-client: REST_CLIENT # 默认客户端类型
    enabled-circuit-breaker: false   # 启用限流熔断观察（默认 false）
    enabled-observation: true        # 启用每次请求观察（默认 true）
    enabled-observation-custom: false # 启用自定义观察（默认 false）
    enabled-response-log: false      # 启用响应日志打印（默认 false）
```

### 客户端类型说明

| 类型 | 枚举值 | 说明 |
|------|--------|------|
| Apache HttpClient | `HTTP_CLIENT` | 功能强大、配置灵活、连接池管理 |
| OkHttp | `OK_HTTP` | 轻量级、性能优秀、易于使用 |
| RestClient | `REST_CLIENT` | Spring 官方、API 简洁、与 Spring 生态集成 |
| WebClient | `WEB_CLIENT` | 响应式、非阻塞、支持流式处理 |

### Apache HttpClient 配置

```yaml
http:
  client:
    apache:
      enabled: true                    # 启用 Apache HttpClient（默认 true）
      connection-keep-alive: 300s      # 连接保持时间
      connection-request-timeout: 5s   # 获取连接超时
      response-timeout: 60s            # 响应超时
      enabled-compression: true        # 启用压缩
      redirects-enabled: true          # 启用重定向
      max-redirects: 5                 # 最大重定向次数
      pool:
        max-conn-total: 25             # 最大连接数
        max-conn-per-route: 50         # 同路由并发数
      proxy:
        enabled: false                 # 启用代理
        host: "proxy.example.com"
        port: 8080
        username: "user"
        password: "pass"
      retry:
        enabled: true                  # 启用重试
        max-attempts: 3                # 最大重试次数
        period: 1s                     # 重试间隔
```

### OkHttp 配置

```yaml
http:
  client:
    ok:
      enabled: true                    # 启用 OkHttp（默认 true）
      protocols:                       # 协议
        - "HTTP_2"
        - "HTTP_1_1"
      connect-timeout: 5s              # 连接超时
      read-timeout: 60s                # 读取超时
      write-timeout: 10s               # 写入超时
      call-timeout: 30s                # 调用超时
      follow-redirects: true           # 允许重定向
      pool:
        max-conn-total: 25             # 最大连接数
        connection-keep-alive: 900s    # 连接保持时间
      proxy:
        enabled: false
        host: "proxy.example.com"
        port: 8080
        username: "user"
        password: "pass"
      retry:
        enabled: true                  # 启用重试
```

### RestClient 配置

```yaml
http:
  client:
    rest:
      enabled: true                    # 启用 RestClient（默认 true）
```

### WebClient 配置

```yaml
http:
  client:
    web:
      enabled: true                    # 启用 WebClient（默认 true）
      enable-logging-request-details: true  # 开启日志详情
      enabled-wiretap: false           # 启用 Wiretap
      enabled-compression: true        # 启用压缩
      max-in-memory-size: 524288       # 最大内存大小（字节）
      connect-timeout: 5s              # 连接超时
      response-timeout: 60s            # 响应超时
      read-timeout: 30s                # 读取超时
      write-timeout: 10s               # 写入超时
      max-initial-line-length: 4096    # 最大初始行长度
      max-header-size: 16384           # 最大头长度
      pool:
        name: "webClientPool"
        max-connections: 2000          # 最大连接数
        pending-acquire-max-count: 2000 # 最大等待获取连接数
        pending-acquire-timeout: 60s   # 等待超时时间
        max-idle-time: 15000           # 最大空闲时间（毫秒）
      proxy:
        enabled: false
        host: "proxy.example.com"
        port: 8080
        username: "user"
        password: "pass"
        type: HTTP                     # 代理类型：HTTP/SOCKS4/SOCKS5
      retry:
        enabled: true                  # 启用重试
```

## 使用方式

### 注入策略工厂

不要在同时启用多个客户端时直接按 `HttpClientExecuteHandler` 类型注入，因为容器中可能存在多个实现。应注入策略工厂：

```java
@Service
@RequiredArgsConstructor
public class RemoteUserService {

    private final HttpClientExecuteHandlerFactory handlerFactory;

    public UserVO getByDefault(Long userId) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        return handler.get(
            "https://api.example.com/users/" + userId,
            null,
            null,
            UserVO.class
        );
    }

    public UserVO getByWebClient(Long userId) {
        HttpClientExecuteHandler handler = handlerFactory
            .getHandler(HttpClientTypeEnum.WEB_CLIENT);
        return handler.get(
            "https://api.example.com/users/" + userId,
            null,
            null,
            UserVO.class
        );
    }
}
```

### GET 请求

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final HttpClientExecuteHandlerFactory handlerFactory;
    
    // 同步 GET 请求（返回对象）
    public UserVO getUser(Long id) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user/" + id;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        
        return handler.get(url, null, headers, UserVO.class);
    }
    
    // 同步 GET 请求（返回集合）
    public List<UserVO> listUsers() {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user/list";
        
        return handler.get(url, null, null, new TypeReference<List<UserVO>>() {});
    }
    
    // 异步 GET 请求
    public CompletionStage<UserVO> getUserAsync(Long id) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user/" + id;
        
        return handler.getAsync(url, null, null, UserVO.class);
    }
    
    // GET 请求（带查询参数）
    public PageResult<UserVO> pageUsers(Integer page, Integer size) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user/page";
        Map<String, Object> query = new HashMap<>();
        query.put("page", page);
        query.put("size", size);
        
        return handler.get(url, query, null, PageResult.class);
    }
}
```

### POST 请求（JSON）

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final HttpClientExecuteHandlerFactory handlerFactory;
    
    // 同步 POST 请求（JSON 请求体）
    public UserVO saveUser(UserDTO dto) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user";
        
        return handler.post(url, dto, null, UserVO.class);
    }
    
    // 异步 POST 请求
    public CompletionStage<UserVO> saveUserAsync(UserDTO dto) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user";
        
        return handler.postAsync(url, dto, null, UserVO.class);
    }
    
    // POST 请求（带请求头）
    public UserVO saveUserWithHeaders(UserDTO dto) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("X-Request-Id", UUID.randomUUID().toString());
        
        return handler.post(url, dto, headers, UserVO.class);
    }
}
```

### POST 请求（文件上传）

```java
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final HttpClientExecuteHandlerFactory handlerFactory;
    
    // 上传单个文件
    public String uploadFile(MultipartFile file) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/file/upload";
        
        return handler.post(url, null, Collections.singletonList(file), null, String.class);
    }
    
    // 上传多个文件
    public String uploadFiles(List<MultipartFile> files) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/file/upload/batch";
        
        return handler.post(url, null, files, null, String.class);
    }
    
    // 上传文件 + 表单字段
    public String uploadFileWithForm(MultipartFile file, String userId) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/file/upload";
        Map<String, Object> form = new HashMap<>();
        form.put("userId", userId);
        form.put("description", "用户头像");
        
        return handler.post(url, form, Collections.singletonList(file), null, String.class);
    }
}
```

### PUT/DELETE 请求

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final HttpClientExecuteHandlerFactory handlerFactory;
    
    // PUT 请求
    public UserVO updateUser(Long id, UserDTO dto) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user/" + id;
        
        return handler.put(url, dto, null, UserVO.class);
    }
    
    // DELETE 请求
    public void deleteUser(Long id) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user/" + id;
        
        handler.delete(url, null, null, Void.class);
    }
}
```

### 使用 HttpRequestSpecDTO

对于复杂请求，可以使用 `HttpRequestSpecDTO` 统一构建请求：

```java
@Service
@RequiredArgsConstructor
public class ApiService {
    
    private final HttpClientExecuteHandlerFactory handlerFactory;
    
    // 使用 query 构造 GET 请求
    public UserVO getUser(Long id) {
        HttpRequestSpecDTO request = HttpRequestSpecDTO.query(
            HttpMethod.GET.name(),
            "https://api.example.com/api/user/" + id,
            null,
            null
        );
        
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        return handler.exchange(request, UserVO.class);
    }
    
    // 使用 json 构造 POST 请求
    public UserVO saveUser(UserDTO dto) {
        HttpRequestSpecDTO request = HttpRequestSpecDTO.json(
            HttpMethod.POST.name(),
            "https://api.example.com/api/user",
            null,
            dto
        );
        
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        return handler.exchange(request, UserVO.class);
    }
    
    // 使用 form 构造表单请求
    public String submitForm(String name, String email) {
        Map<String, Object> form = new HashMap<>();
        form.put("name", name);
        form.put("email", email);
        
        HttpRequestSpecDTO request = HttpRequestSpecDTO.form(
            HttpMethod.POST.name(),
            "https://api.example.com/api/form",
            null,
            form
        );
        
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        return handler.exchange(request, String.class);
    }
    
    // 使用 multipart 构造文件上传请求
    public String uploadFile(MultipartFile file, String userId) {
        Map<String, Object> form = new HashMap<>();
        form.put("userId", userId);
        
        HttpRequestSpecDTO request = HttpRequestSpecDTO.multipart(
            HttpMethod.POST.name(),
            "https://api.example.com/api/file/upload",
            null,
            form,
            Collections.singletonList(file)
        );
        
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        return handler.exchange(request, String.class);
    }
}
```

## API 防护（签名验证）

支持 API 签名验证，保护接口安全。

### 配置示例

```yaml
http:
  client:
    api-protection:
      enabled: true                    # 启用 API 防护（默认 false）
      enabled-remote: false            # 启用远程应用凭证配置（默认 false）
      url: "lb://iwindplus-mgt/inner/appCert/getByCertType"  # 获取远程凭证的 URL
      enabled-local-cache: true        # 启用本地缓存应用凭证（默认 true）
      ignored-api:                     # 忽略的 API
        - "/api/public/**"
        - "/api/health"
      access-key: "your-access-key"    # 访问 key
      secret-key: "your-secret-key"    # 密钥
      timeout: 30                      # 签名超时时间（秒）
```

### 功能说明

API 防护提供以下功能：

1. **签名验证**：对请求进行签名验证，防止篡改
2. **时间戳验证**：验证请求时间戳，防止重放攻击
3. **本地缓存**：缓存应用凭证，减少远程调用
4. **忽略 API**：支持配置忽略的 API 路径

### 使用示例

```java
// API 防护会自动对请求进行签名
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final HttpClientExecuteHandlerFactory handlerFactory;
    
    // 请求会自动添加签名
    public UserVO getUser(Long id) {
        HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
        String url = "https://api.example.com/api/user/" + id;
        
        // 请求头会自动添加：
        // X-Access-Key: your-access-key
        // X-Timestamp: 1234567890
        // X-Signature: 签名字符串
        return handler.get(url, null, null, UserVO.class);
    }
}
```

## Micrometer 观察

支持 Micrometer 观察，收集 HTTP 请求的监控指标。

### 配置示例

```yaml
http:
  client:
    enabled-observation: true        # 启用每次请求观察（默认 true）
    enabled-observation-custom: false # 启用自定义观察（默认 false）
```

### 功能说明

Micrometer 观察提供以下功能：

1. **请求指标**：记录请求次数、响应时间、错误率等
2. **分布式追踪**：支持分布式追踪（如 Zipkin、Jaeger）
3. **自定义观察**：支持自定义观察逻辑

## 限流熔断

支持限流熔断，保护系统稳定性。

### 配置示例

```yaml
http:
  client:
    enabled-circuit-breaker: true  # 启用限流熔断观察（默认 false）
```

### 功能说明

限流熔断提供以下功能：

1. **限流**：限制请求速率，防止系统过载
2. **熔断**：当错误率达到阈值时，自动熔断
3. **降级**：熔断后返回降级响应

## HTTP 客户端选择指南

### 架构对比

```
┌─────────────────────────────────────────────────────────┐
│                HTTP 客户端架构对比                       │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Apache HttpClient                                       │
│  ├─ 特点：功能强大、配置灵活、连接池管理                 │
│  ├─ 适用：传统应用、需要复杂配置                         │
│  └─ 性能：★★★★☆                                        │
│                                                          │
│  OkHttp                                                  │
│  ├─ 特点：轻量级、性能优秀、易于使用                     │
│  ├─ 适用：移动应用、微服务                               │
│  └─ 性能：★★★★★                                        │
│                                                          │
│  RestClient                                              │
│  ├─ 特点：Spring 官方、API 简洁、与 Spring 生态集成      │
│  ├─ 适用：Spring Boot 应用                               │
│  └─ 性能：★★★★☆                                        │
│                                                          │
│  WebClient                                               │
│  ├─ 特点：响应式、非阻塞、支持流式处理                   │
│  ├─ 适用：响应式应用、高并发场景                         │
│  └─ 性能：★★★★★                                        │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 使用场景对比

| 场景 | Apache HttpClient | OkHttp | RestClient | WebClient |
|------|-------------------|--------|------------|-----------|
| 传统 Spring Boot 应用 | ✅ 推荐 | ✅ 推荐 | ✅ 推荐 | ⚠️ 可用 |
| 微服务应用 | ✅ 推荐 | ✅ 推荐 | ✅ 推荐 | ✅ 推荐 |
| 响应式应用 | ❌ 不推荐 | ❌ 不推荐 | ❌ 不推荐 | ✅ 推荐 |
| 高并发场景 | ✅ 推荐 | ✅ 推荐 | ✅ 推荐 | ✅ 推荐 |
| 需要复杂配置 | ✅ 推荐 | ⚠️ 可用 | ⚠️ 可用 | ⚠️ 可用 |
| 移动应用 | ⚠️ 可用 | ✅ 推荐 | ❌ 不适用 | ❌ 不适用 |

## 调度规则

| 调用方式 | 选择规则 | 适用场景 |
|------|------|------|
| `getDefaultHandler()` | 读取 `http.client.default-http-client` | 业务统一使用默认客户端 |
| `getHttpClientExecutor(HttpClientTypeEnum.HTTP_CLIENT)` | 显式选择 Apache HttpClient | 需要固定某个客户端实现 |
| `getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)` | 显式选择 Spring RestClient | Spring MVC 同步调用 |
| `getHttpClientExecutor(HttpClientTypeEnum.OK_HTTP)` | 显式选择 OkHttp | 需要使用 OkHttp 能力 |
| `getHttpClientExecutor(HttpClientTypeEnum.WEB_CLIENT)` | 显式选择 Spring WebClient | 响应式或非阻塞调用 |

显式指定的客户端必须已经通过对应配置启用，否则工厂找不到策略，会抛出 `BizException`，错误码为 `BizCodeEnum.INVALID_STRATEGY`。例如：

```yaml
http:
  client:
    default-http-client: WEB_CLIENT
    web:
      enabled: true
    rest:
      enabled: false
```

此时调用 `getHandler(HttpClientTypeEnum.REST_CLIENT)` 会失败；调用默认入口会选择 WebClient。

## 最佳实践

### 1. 选择合适的 HTTP 客户端

```yaml
# Spring Boot 同步调用使用 RestClient
http:
  client:
    default-http-client: REST_CLIENT

# 响应式或非阻塞调用使用 WebClient
http:
  client:
    default-http-client: WEB_CLIENT

# 需要 Apache HttpClient 配置能力时使用 HTTP_CLIENT
http:
  client:
    default-http-client: HTTP_CLIENT
```

### 2. 异步请求

```java
// ✅ 推荐：使用异步请求提升性能
public CompletionStage<UserVO> getUserAsync(Long id) {
    HttpClientExecuteHandler handler = handlerFactory.getDefaultHandler();
    String url = "https://api.example.com/api/user/" + id;
    return handler.getAsync(url, null, null, UserVO.class);
}

// ❌ 不推荐：在异步环境中使用同步请求
public CompletionStage<UserVO> getUserAsync(Long id) {
    HttpClientExecuteHandler handler = handlerFactory.getDefaultHttpClientExecuteHandler();
    String url = "https://api.example.com/api/user/" + id;
    UserVO user = handler.get(url, null, null, UserVO.class);  // 阻塞
    return CompletableFuture.completedFuture(user);
}
```

### 3. 超时配置

```yaml
# ✅ 推荐：配置合理的超时时间
http:
  client:
    apache:
      connection-request-timeout: 5s   # 获取连接超时
      response-timeout: 60s            # 响应超时

# ❌ 不推荐：超时时间过长
http:
  client:
    apache:
      connection-request-timeout: 30s  # 过长
      response-timeout: 300s           # 过长
```

### 4. 连接池配置

```yaml
# ✅ 推荐：根据并发量配置连接池
http:
  client:
    apache:
      pool:
        max-conn-total: 100        # 最大连接数
        max-conn-per-route: 50     # 同路由并发数

# ❌ 不推荐：连接池过小
http:
  client:
    apache:
      pool:
        max-conn-total: 10         # 过小
        max-conn-per-route: 5      # 过小
```

### 5. API 防护

```yaml
# ✅ 推荐：生产环境启用 API 防护
http:
  client:
    api-protection:
      enabled: true
      access-key: "your-access-key"
      secret-key: "your-secret-key"
      timeout: 30

# ❌ 不推荐：生产环境不启用 API 防护
http:
  client:
    api-protection:
      enabled: false
```

## 注意事项

1. **HTTP 客户端选择**：根据应用类型选择合适的 HTTP 客户端
2. **超时配置**：配置合理的超时时间，避免请求长时间阻塞
3. **连接池配置**：根据并发量配置连接池大小
4. **异步请求**：在高并发场景下使用异步请求
5. **API 防护**：生产环境建议启用 API 防护
6. **监控指标**：启用 Micrometer 观察，收集监控指标
7. **重试机制**：配置合理的重试次数和间隔
8. **策略工厂注入**：不要直接注入 `HttpClientExecuteHandler`，应注入 `HttpClientExecuteHandlerFactory`

## 相关模块

- `iwindplus-base-domain`：领域模型模块，提供 DTO、VO 等基础定义
- `iwindplus-base-util`：工具类模块，提供 JSON 序列化、加解密工具
- `iwindplus-base-web`：Web 模块，提供 Web 相关功能
- `iwindplus-base-feign`：Feign 模块，提供声明式 HTTP 客户端
