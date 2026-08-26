# iwindplus-base-webflux

WebFlux 模块，提供 Spring WebFlux 响应式编程支持，包括全局异常处理、跨域配置、WebFlux 配置等功能。

## 功能特性

- ✅ 全局异常处理（GlobalErrorHandler）- 统一捕获和处理异常
- ✅ 跨域配置（CrossConfiguration）- CORS 跨域配置
- ✅ WebFlux 配置（WebFluxConfiguration）- 响应式编程配置
- ✅ 响应加密 - 支持响应数据加密
- ✅ Micrometer 观察 - 支持监控指标收集

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-webflux</artifactId>
</dependency>
```

## 核心功能

### 1. GlobalErrorHandler - 全局异常处理

统一捕获和处理 WebFlux 异常，返回标准格式的错误响应。

#### 配置示例

```yaml
global:
  error:
    enabled: true  # 启用全局异常处理（默认 true）
```

#### 功能说明

GlobalErrorHandler 实现了 WebExceptionHandler 接口，会自动捕获所有异常：

1. **自定义异常处理**：捕获 CommonException（包括 BizException），返回 HTTP 200 + 业务错误码
2. **响应状态异常处理**：捕获 ResponseStatusException，返回对应的 HTTP 状态码
3. **兜底异常处理**：捕获其他异常，根据异常类型返回对应的 HTTP 状态码
4. **响应加密**：支持对错误响应进行加密
5. **日志记录**：自动记录异常日志，区分 warn 和 error 级别

#### 使用示例

```java
// Router 函数式路由
@Configuration
public class UserRouter {
    
    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return RouterFunctions.route()
            .GET("/api/user/{id}", handler::getUser)
            .POST("/api/user", handler::saveUser)
            .build();
    }
}

@Component
public class UserHandler {
    
    public Mono<ServerResponse> getUser(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return userService.getById(id)
            .flatMap(user -> {
                if (user == null) {
                    // 抛出业务异常
                    throw new BizException(BizCodeEnum.DATA_NOT_FOUND);
                }
                return ServerResponse.ok().bodyValue(user);
            });
    }
    
    public Mono<ServerResponse> saveUser(ServerRequest request) {
        return request.bodyToMono(UserDTO.class)
            .flatMap(dto -> userService.save(dto))
            .then(ServerResponse.ok().build());
    }
}

// 异常响应格式
{
    "bizCode": "data_not_found",
    "bizMessage": "数据不存在",
    "bizData": null,
    "bizTimestamp": 1234567890,
    "bizTraceId": "trace-id-123"
}
```

#### 异常处理流程

```
┌─────────────────────────────────────────────────────────┐
│                   GlobalErrorHandler                     │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  异常抛出 ──> GlobalErrorHandler 捕获                     │
│                                                          │
│  ┌──────────────────┐    ┌──────────────────┐          │
│  │ CommonException  │    │ ResponseStatus   │          │
│  │ (BizException等) │    │ Exception        │          │
│  └──────────────────┘    └──────────────────┘          │
│         │                         │                     │
│         ▼                         ▼                     │
│  ┌──────────────────┐    ┌──────────────────┐          │
│  │ HTTP 200         │    │ HTTP 4xx/5xx     │          │
│  │ + 业务错误码      │    │ + 系统错误码      │          │
│  └──────────────────┘    └──────────────────┘          │
│         │                         │                     │
│         └─────────┬───────────────┘                     │
│                   ▼                                      │
│         ┌──────────────────┐                            │
│         │ 返回 ResultVO    │                            │
│         │ (统一响应格式)    │                            │
│         └──────────────────┘                            │
└─────────────────────────────────────────────────────────┘
```

#### 日志级别

- **warn**：业务异常、客户端错误（HTTP 4xx）
- **error**：响应状态异常、服务器错误（HTTP 5xx）

### 2. CrossConfiguration - 跨域配置

配置 CORS 跨域，允许前端跨域访问后端接口。

#### 配置示例

```yaml
cross:
  enabled: true  # 启用跨域配置
  allowed-origins:  # 允许的域名
    - "http://localhost:8080"
    - "https://example.com"
  allowed-methods:  # 允许的方法
    - GET
    - POST
    - PUT
    - DELETE
  allowed-headers:  # 允许的请求头
    - "*"
  allow-credentials: true  # 允许携带凭证
  max-age: 3600  # 预检请求缓存时间（秒）
```

#### 功能说明

CrossConfiguration 会创建 CorsWebFilter，处理 CORS 跨域请求：

1. **允许域名**：配置允许访问的域名
2. **允许方法**：配置允许的 HTTP 方法
3. **允许请求头**：配置允许的请求头
4. **允许凭证**：是否允许携带 Cookie 等凭证
5. **预检缓存**：配置预检请求的缓存时间

#### 使用示例

```java
// 前端跨域请求
// 前端代码（http://localhost:8080）
fetch('https://api.example.com/api/user/1', {
    method: 'GET',
    credentials: 'include',  // 携带 Cookie
    headers: {
        'Content-Type': 'application/json'
    }
})
.then(response => response.json())
.then(data => console.log(data));

// 后端配置（application.yml）
cross:
  enabled: true
  allowed-origins:
    - "http://localhost:8080"
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
  allowed-headers:
    - "*"
  allow-credentials: true
  max-age: 3600
```

### 3. WebFluxConfiguration - WebFlux 配置

配置 WebFlux 相关功能，包括观察注册、JSON 序列化等。

#### 配置示例

```yaml
web:
  flux:
    enabled: true  # 启用 WebFlux 配置（默认 true）
```

#### 功能说明

WebFluxConfiguration 提供以下功能：

1. **Micrometer 观察**：集成 Micrometer，支持监控指标收集
2. **JSON 序列化**：配置 Jackson ObjectMapper，统一 JSON 序列化
3. **自定义配置**：支持自定义 WebFlux 配置

#### 使用示例

```java
// 使用 Router 函数式路由
@Configuration
public class UserRouter {
    
    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return RouterFunctions.route()
            .GET("/api/user/{id}", handler::getUser)
            .POST("/api/user", handler::saveUser)
            .DELETE("/api/user/{id}", handler::deleteUser)
            .build();
    }
}

// 使用注解式 Controller
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public Mono<UserVO> getUser(@PathVariable Long id) {
        return userService.getById(id);
    }
    
    @PostMapping
    public Mono<Void> saveUser(@RequestBody UserDTO dto) {
        return userService.save(dto);
    }
    
    @DeleteMapping("/{id}")
    public Mono<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteById(id);
    }
}
```

### 4. 响应加密

支持对响应数据进行加密，保护敏感数据。

#### 配置示例

```yaml
filter:
  crypto:
    enabled: true  # 启用响应加密
    algorithm: AES  # 加密算法：AES、RSA、SM2
    key: "12345678901234567890123456789012"  # AES 密钥（32 位）
```

#### 使用示例

```java
// Router 返回敏感数据，自动加密
@Component
public class UserHandler {
    
    public Mono<ServerResponse> getUser(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return userService.getById(id)
            .flatMap(user -> ServerResponse.ok().bodyValue(user));
        // 返回的数据会被自动加密
    }
}

// 响应格式（加密后）
{
    "bizCode": "ok",
    "bizMessage": "OK",
    "bizData": "加密后的字符串",
    "bizTimestamp": 1234567890,
    "bizTraceId": "trace-id-123"
}
```

## 配置属性

### GlobalErrorProperty - 全局异常处理配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用全局异常处理 | `true` |

### CrossProperty - 跨域配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用跨域配置 | `false` |
| `allowed-origins` | 允许的域名 | `["*"]` |
| `allowed-methods` | 允许的方法 | `["*"]` |
| `allowed-headers` | 允许的请求头 | `["*"]` |
| `allow-credentials` | 允许携带凭证 | `true` |
| `max-age` | 预检请求缓存时间（秒） | `null` |

### WebFluxProperty - WebFlux 配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用 WebFlux 配置 | `true` |

## WebFlux vs WebMVC

### 架构对比

```
┌─────────────────────────────────────────────────────────┐
│                    WebMVC 架构                           │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  请求 ──> Servlet 容器 ──> DispatcherServlet             │
│         │                                                │
│         └─> 线程阻塞等待                                 │
│                                                          │
│  响应 <── 业务处理完成 <── Controller                    │
│                                                          │
│  特点：同步阻塞，一个请求占用一个线程                     │
│                                                          │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   WebFlux 架构                           │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  请求 ──> Reactor 容器 ──> HttpHandler                   │
│         │                                                │
│         └─> 非阻塞异步处理                               │
│                                                          │
│  响应 <── Mono/Flux 回调 <── Controller                 │
│                                                          │
│  特点：异步非阻塞，少量线程处理大量请求                   │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 使用场景对比

| 场景 | WebMVC | WebFlux |
|------|--------|---------|
| 传统 CRUD 应用 | ✅ 推荐 | ⚠️ 可用 |
| 高并发 API 网关 | ⚠️ 可用 | ✅ 推荐 |
| 微服务调用 | ✅ 推荐 | ✅ 推荐 |
| 实时数据推送 | ❌ 不推荐 | ✅ 推荐 |
| 流式数据处理 | ❌ 不推荐 | ✅ 推荐 |
| 阻塞数据库操作 | ✅ 推荐 | ⚠️ 需包装 |

### 编程模型对比

#### WebMVC（注解式）

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public UserVO getUser(@PathVariable Long id) {
        return userService.getById(id);
    }
    
    @PostMapping
    public void saveUser(@RequestBody UserDTO dto) {
        userService.save(dto);
    }
}
```

#### WebFlux（注解式）

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public Mono<UserVO> getUser(@PathVariable Long id) {
        return userService.getById(id);
    }
    
    @PostMapping
    public Mono<Void> saveUser(@RequestBody UserDTO dto) {
        return userService.save(dto);
    }
}
```

#### WebFlux（函数式）

```java
@Configuration
public class UserRouter {
    
    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return RouterFunctions.route()
            .GET("/api/user/{id}", handler::getUser)
            .POST("/api/user", handler::saveUser)
            .build();
    }
}

@Component
public class UserHandler {
    
    public Mono<ServerResponse> getUser(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return userService.getById(id)
            .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }
    
    public Mono<ServerResponse> saveUser(ServerRequest request) {
        return request.bodyToMono(UserDTO.class)
            .flatMap(dto -> userService.save(dto))
            .then(ServerResponse.ok().build());
    }
}
```

## 最佳实践

### 1. 异常处理

```java
// ✅ 推荐：使用 BizException 抛出业务异常
public Mono<UserVO> getUser(Long id) {
    return userService.getById(id)
        .flatMap(user -> {
            if (user == null) {
                throw new BizException(BizCodeEnum.DATA_NOT_FOUND);
            }
            return Mono.just(user);
        });
}

// ✅ 推荐：使用 Mono.error() 包装异常
public Mono<UserVO> getUser(Long id) {
    return userService.getById(id)
        .switchIfEmpty(Mono.error(new BizException(BizCodeEnum.DATA_NOT_FOUND)));
}

// ❌ 不推荐：抛出 RuntimeException
throw new RuntimeException("用户不存在");
```

### 2. 响应式编程

```java
// ✅ 推荐：使用 Mono/Flux 链式调用
public Mono<UserVO> getUser(Long id) {
    return userService.getById(id)
        .flatMap(user -> {
            user.setDepartment(departmentService.getById(user.getDeptId()));
            return Mono.just(user);
        });
}

// ✅ 推荐：使用 switchIfEmpty 处理空值
public Mono<UserVO> getUser(Long id) {
    return userService.getById(id)
        .switchIfEmpty(Mono.error(new BizException(BizCodeEnum.DATA_NOT_FOUND)));
}

// ❌ 不推荐：阻塞调用
public Mono<UserVO> getUser(Long id) {
    UserVO user = userService.getById(id).block();  // 阻塞调用
    return Mono.just(user);
}
```

### 3. 跨域配置

```yaml
# ✅ 推荐：生产环境配置具体域名
cross:
  enabled: true
  allowed-origins:
    - "https://www.example.com"
    - "https://admin.example.com"

# ❌ 不推荐：生产环境使用 "*"（不安全）
cross:
  enabled: true
  allowed-origins:
    - "*"
```

### 4. 选择合适的编程模型

```java
// ✅ 推荐：简单 CRUD 使用注解式
@RestController
public class UserController {
    @GetMapping("/{id}")
    public Mono<UserVO> getUser(@PathVariable Long id) {
        return userService.getById(id);
    }
}

// ✅ 推荐：复杂路由使用函数式
@Configuration
public class UserRouter {
    @Bean
    public RouterFunction<ServerResponse> routes(UserHandler handler) {
        return RouterFunctions.route()
            .path("/api/user", builder -> builder
                .GET("/{id}", handler::getUser)
                .POST(handler::saveUser)
                .before(request -> ServerResponse.ok().build()))
            .build();
    }
}
```

## 注意事项

1. **GlobalErrorHandler**：优先级最高（@Order(HIGHEST_PRECEDENCE)），确保能捕获所有异常
2. **WebFlux 异步特性**：不要在 WebFlux 中使用阻塞调用（如 .block()），会阻塞线程
3. **数据库访问**：需要使用响应式数据库驱动（如 R2DBC、Reactive MongoDB）
4. **线程模型**：WebFlux 使用少量线程处理大量请求，不要在业务代码中使用 ThreadLocal
5. **跨域配置**：生产环境建议配置具体的域名，不要使用 `*`
6. **响应加密**：启用响应加密后，前端需要解密才能获取原始数据

## 相关模块

- `iwindplus-base-web`：Web 基础模块，提供 Controller 基类、过滤器等
- `iwindplus-base-webmvc`：WebMVC 模块，提供 Spring MVC 相关功能
- `iwindplus-base-domain`：领域模型模块，提供 ResultVO、BizException 等
- `iwindplus-base-util`：工具类模块，提供加解密、异常处理工具
