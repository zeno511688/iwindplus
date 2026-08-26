# iwindplus-base-http-client

HTTP 客户端模块，提供统一的 HTTP 客户端抽象，支持多种 HTTP 客户端实现（Apache HttpClient、OkHttp、RestClient、WebClient），支持同步/异步请求、文件上传、API 防护等功能。

## 功能特性

- ✅ 统一的 HTTP 客户端抽象（HttpClientExecutor）
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

## 核心功能

### 1. HttpClientExecutor - HTTP 客户端执行器

统一的 HTTP 客户端接口，支持多种 HTTP 客户端实现。

#### 配置示例

```yaml
http:
  client:
    enabled: true  # 启用 HTTP 客户端（默认 true）
    # 默认客户端类型：HTTP_CLIENT、REST_CLIENT、OK_HTTP、WEB_CLIENT
    default-http-client: REST_CLIENT
    enabled-circuit-breaker: false  # 启用限流熔断观察（默认 false）
    enabled-observation: true  # 启用每次请求观察（默认 true）
    enabled-observation-custom: false  # 启用自定义观察（默认 false）
    enabled-response-log: false  # 启用响应日志打印（默认 false）
```

#### 功能说明

HttpClientExecutor 提供以下功能：

1. **同步请求**：GET、POST、PUT、DELETE、PATCH 等
2. **异步请求**：支持 CompletionStage 异步响应
3. **泛型支持**：支持 TypeReference 处理泛型响应
4. **文件上传**：支持 MultipartFile 文件上传
5. **请求拦截**：支持请求拦截器
6. **响应提取**：支持多种响应类型提取

#### 使用示例

##### 1. GET 请求

```java
@Service
public class UserService {
    
    @Resource
    private HttpClientExecutor httpClientExecutor;
    
    // 同步 GET 请求（返回对象）
    public UserVO getUser(Long id) {
        String url = "https://api.example.com/api/user/" + id;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        
        return httpClientExecutor.get(url, null, headers, UserVO.class);
    }
    
    // 同步 GET 请求（返回集合）
    public List<UserVO> listUsers() {
        String url = "https://api.example.com/api/user/list";
        
        return httpClientExecutor.get(url, null, null, 
            new TypeReference<List<UserVO>>() {});
    }
    
    // 异步 GET 请求
    public CompletionStage<UserVO> getUserAsync(Long id) {
        String url = "https://api.example.com/api/user/" + id;
        
        return httpClientExecutor.getAsync(url, null, null, UserVO.class);
    }
    
    // GET 请求（带查询参数）
    public PageResult<UserVO> pageUsers(Integer page, Integer size) {
        String url = "https://api.example.com/api/user/page";
        Map<String, Object> query = new HashMap<>();
        query.put("page", page);
        query.put("size", size);
        
        return httpClientExecutor.get(url, query, null, PageResult.class);
    }
}
```

##### 2. POST 请求（JSON）

```java
@Service
public class UserService {
    
    @Resource
    private HttpClientExecutor httpClientExecutor;
    
    // 同步 POST 请求（JSON 请求体）
    public UserVO saveUser(UserDTO dto) {
        String url = "https://api.example.com/api/user";
        
        return httpClientExecutor.post(url, dto, null, UserVO.class);
    }
    
    // 异步 POST 请求
    public CompletionStage<UserVO> saveUserAsync(UserDTO dto) {
        String url = "https://api.example.com/api/user";
        
        return httpClientExecutor.postAsync(url, dto, null, UserVO.class);
    }
    
    // POST 请求（带请求头）
    public UserVO saveUserWithHeaders(UserDTO dto) {
        String url = "https://api.example.com/api/user";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("X-Request-Id", UUID.randomUUID().toString());
        
        return httpClientExecutor.post(url, dto, headers, UserVO.class);
    }
}
```

##### 3. POST 请求（文件上传）

```java
@Service
public class FileService {
    
    @Resource
    private HttpClientExecutor httpClientExecutor;
    
    // 上传单个文件
    public String uploadFile(MultipartFile file) {
        String url = "https://api.example.com/api/file/upload";
        
        return httpClientExecutor.post(url, null, 
            Collections.singletonList(file), null, String.class);
    }
    
    // 上传多个文件
    public String uploadFiles(List<MultipartFile> files) {
        String url = "https://api.example.com/api/file/upload/batch";
        
        return httpClientExecutor.post(url, null, files, null, String.class);
    }
    
    // 上传文件 + 表单字段
    public String uploadFileWithForm(MultipartFile file, String userId) {
        String url = "https://api.example.com/api/file/upload";
        Map<String, Object> form = new HashMap<>();
        form.put("userId", userId);
        form.put("description", "用户头像");
        
        return httpClientExecutor.post(url, form, 
            Collections.singletonList(file), null, String.class);
    }
}
```

##### 4. PUT/DELETE 请求

```java
@Service
public class UserService {
    
    @Resource
    private HttpClientExecutor httpClientExecutor;
    
    // PUT 请求
    public UserVO updateUser(Long id, UserDTO dto) {
        String url = "https://api.example.com/api/user/" + id;
        
        return httpClientExecutor.put(url, dto, null, UserVO.class);
    }
    
    // DELETE 请求
    public void deleteUser(Long id) {
        String url = "https://api.example.com/api/user/" + id;
        
        httpClientExecutor.delete(url, null, null, Void.class);
    }
}
```

### 2. HttpClientExecutorStrategyFactory - 统一调度

`HttpClientExecutorStrategyFactory` 是模块的统一执行器调度入口。它在 Spring 单例初始化完成后，从容器中收集所有已启用的 `HttpClientExecutor` 实现，按 `HttpClientTypeEnum` 建立策略缓存：

```text
业务代码
   │
   ▼
HttpClientExecutorStrategyFactory
   ├── getDefaultHttpClientExecutor()
   │       └── 使用 http.client.default-http-client
   └── getHttpClientExecutor(HttpClientTypeEnum)
           └── 显式选择具体客户端
                    │
     ┌──────────────┼──────────────┬──────────────┐
     ▼              ▼              ▼              ▼
 HTTP_CLIENT     REST_CLIENT     OK_HTTP       WEB_CLIENT
 Apache          Spring         OkHttp        WebClient
```

#### 注入方式

不要在同时启用多个客户端时直接按 `HttpClientExecutor` 类型注入，因为容器中可能存在多个实现。应注入策略工厂：

```java
@Service
@RequiredArgsConstructor
public class RemoteUserService {

    private final HttpClientExecutorStrategyFactory executorFactory;

    public UserVO getByDefault(Long userId) {
        HttpClientExecutor executor = executorFactory.getDefaultHttpClientExecutor();
        return executor.get(
            "https://api.example.com/users/" + userId,
            null,
            null,
            UserVO.class
        );
    }

    public UserVO getByWebClient(Long userId) {
        HttpClientExecutor executor = executorFactory
            .getHttpClientExecutor(HttpClientTypeEnum.WEB_CLIENT);
        return executor.get(
            "https://api.example.com/users/" + userId,
            null,
            null,
            UserVO.class
        );
    }
}
```

#### 调度规则

| 调用方式 | 选择规则 | 适用场景 |
|------|------|------|
| `getDefaultHttpClientExecutor()` | 读取 `http.client.default-http-client` | 业务统一使用默认客户端 |
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

此时调用 `getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)` 会失败；调用默认入口会选择 WebClient。

#### 与 API 防护的关系

`ApiProtectionProvider` 也依赖 `HttpClientExecutorStrategyFactory` 获取执行器。当 API 防护启用远程凭证配置时，远程凭证请求会使用默认 HTTP 客户端，因此应确保 `default-http-client` 对应的执行器已启用。



#### Apache HttpClient

```yaml
http:
  client:
    apache:
      enabled: true  # 启用 Apache HttpClient（默认 true）
      connection-keep-alive: 300s  # 连接保持时间
      connection-request-timeout: 5s  # 获取连接超时
      response-timeout: 60s  # 响应超时
      enabled-compression: true  # 启用压缩
      redirects-enabled: true  # 启用重定向
      max-redirects: 5  # 最大重定向次数
      pool:
        max-conn-total: 25  # 最大连接数
        max-conn-per-route: 50  # 同路由并发数
      proxy:
        enabled: false  # 启用代理
        host: "proxy.example.com"
        port: 8080
        username: "user"
        password: "pass"
      retry:
        enabled: true  # 启用重试
        max-retries: 3  # 最大重试次数
        retry-interval: 1000  # 重试间隔（毫秒）
```

#### OkHttp

```yaml
http:
  client:
    ok:
      enabled: true  # 启用 OkHttp（默认 true）
      connect-timeout: 60s  # 连接超时
      read-timeout: 60s  # 读取超时
      write-timeout: 60s  # 写入超时
      ping-interval: 0  # ping 间隔
      max-idle-connections: 5  # 最大空闲连接数
      keep-alive-duration: 300s  # 保持连接时间
```

#### RestClient（Spring 6.1+）

```yaml
http:
  client:
    rest:
      enabled: true  # 启用 RestClient（默认 true）
      connect-timeout: 60s  # 连接超时
      read-timeout: 60s  # 读取超时
```

#### WebClient（响应式）

```yaml
http:
  client:
    web:
      enabled: true  # 启用 WebClient（默认 true）
      connect-timeout: 60s  # 连接超时
      read-timeout: 60s  # 读取超时
      response-timeout: 60s  # 响应超时
      max-connections: 500  # 最大连接数
      pending-acquire-timeout: 60s  # 获取连接超时
      pending-acquire-max-count: -1  # 最大等待连接数
```

### 3. API 防护（签名验证）

支持 API 签名验证，保护接口安全。

#### 配置示例

```yaml
http:
  client:
    api-protection:
      enabled: true  # 启用 API 防护（默认 false）
      enabled-remote: false  # 启用远程应用凭证配置（默认 false）
      url: "lb://iwindplus-mgt/inner/appCert/getByCertType"  # 获取远程凭证的 URL
      enabled-local-cache: true  # 启用本地缓存应用凭证（默认 true）
      ignored-api:  # 忽略的 API
        - "/api/public/**"
        - "/api/health"
      access-key: "your-access-key"  # 访问 key
      secret-key: "your-secret-key"  # 密钥
      timeout: 30  # 签名超时时间（秒）
```

#### 功能说明

API 防护提供以下功能：

1. **签名验证**：对请求进行签名验证，防止篡改
2. **时间戳验证**：验证请求时间戳，防止重放攻击
3. **本地缓存**：缓存应用凭证，减少远程调用
4. **忽略 API**：支持配置忽略的 API 路径

#### 使用示例

```java
// API 防护会自动对请求进行签名
@Service
public class UserService {
    
    @Resource
    private HttpClientExecutor httpClientExecutor;
    
    // 请求会自动添加签名
    public UserVO getUser(Long id) {
        String url = "https://api.example.com/api/user/" + id;
        
        // 请求头会自动添加：
        // X-Access-Key: your-access-key
        // X-Timestamp: 1234567890
        // X-Signature: 签名字符串
        return httpClientExecutor.get(url, null, null, UserVO.class);
    }
}
```

### 4. Micrometer 观察

支持 Micrometer 观察，收集 HTTP 请求的监控指标。

#### 配置示例

```yaml
http:
  client:
    enabled-observation: true  # 启用每次请求观察（默认 true）
    enabled-observation-custom: false  # 启用自定义观察（默认 false）
```

#### 功能说明

Micrometer 观察提供以下功能：

1. **请求指标**：记录请求次数、响应时间、错误率等
2. **分布式追踪**：支持分布式追踪（如 Zipkin、Jaeger）
3. **自定义观察**：支持自定义观察逻辑

### 5. 限流熔断

支持限流熔断，保护系统稳定性。

#### 配置示例

```yaml
http:
  client:
    enabled-circuit-breaker: true  # 启用限流熔断观察（默认 false）
```

#### 功能说明

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

## 配置属性

### HttpClientProperty - HTTP 客户端配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用 HTTP 客户端 | `true` |
| `default-http-client` | 默认客户端类型 | `REST_CLIENT` |
| `enabled-circuit-breaker` | 启用限流熔断观察 | `false` |
| `enabled-observation` | 启用每次请求观察 | `true` |
| `enabled-observation-custom` | 启用自定义观察 | `false` |
| `enabled-response-log` | 启用响应日志打印 | `false` |

### ApiProtectionConfig - API 防护配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用 API 防护 | `false` |
| `enabled-remote` | 启用远程应用凭证配置 | `false` |
| `url` | 获取远程凭证的 URL | `lb://iwindplus-mgt/inner/appCert/getByCertType` |
| `enabled-local-cache` | 启用本地缓存应用凭证 | `true` |
| `ignored-api` | 忽略的 API | `[]` |
| `access-key` | 访问 key | `null` |
| `secret-key` | 密钥 | `null` |
| `timeout` | 签名超时时间（秒） | `30` |

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
    String url = "https://api.example.com/api/user/" + id;
    return httpClientExecutor.getAsync(url, null, null, UserVO.class);
}

// ❌ 不推荐：在异步环境中使用同步请求
public CompletionStage<UserVO> getUserAsync(Long id) {
    String url = "https://api.example.com/api/user/" + id;
    UserVO user = httpClientExecutor.get(url, null, null, UserVO.class);  // 阻塞
    return CompletableFuture.completedFuture(user);
}
```

### 3. 超时配置

```yaml
# ✅ 推荐：配置合理的超时时间
http:
  client:
    apache:
      connection-request-timeout: 5s  # 获取连接超时
      response-timeout: 60s  # 响应超时

# ❌ 不推荐：超时时间过长
http:
  client:
    apache:
      connection-request-timeout: 30s  # 过长
      response-timeout: 300s  # 过长
```

### 4. 连接池配置

```yaml
# ✅ 推荐：根据并发量配置连接池
http:
  client:
    apache:
      pool:
        max-conn-total: 100  # 最大连接数
        max-conn-per-route: 50  # 同路由并发数

# ❌ 不推荐：连接池过小
http:
  client:
    apache:
      pool:
        max-conn-total: 10  # 过小
        max-conn-per-route: 5  # 过小
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

## 相关模块

- `iwindplus-base-domain`：领域模型模块，提供 DTO、VO 等基础定义
- `iwindplus-base-util`：工具类模块，提供 JSON 序列化、加解密工具
- `iwindplus-base-web`：Web 模块，提供 Web 相关功能
- `iwindplus-base-feign`：Feign 模块，提供声明式 HTTP 客户端
