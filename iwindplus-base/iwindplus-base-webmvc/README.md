# iwindplus-base-webmvc

WebMVC 模块，提供 Spring MVC 相关的全局异常处理、统一响应体处理、跨域配置等功能。

## 功能特性

- ✅ 全局异常处理（GlobalErrorHandler）- 统一捕获和处理异常
- ✅ 统一响应体处理（ResponseBodyHandler）- 自动包装响应体为 ResultVO
- ✅ 跨域配置（CrossConfiguration）- CORS 跨域配置
- ✅ 响应加密 - 支持响应数据加密

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-webmvc</artifactId>
</dependency>
```

## 核心功能

### 1. GlobalErrorHandler - 全局异常处理

统一捕获和处理异常，返回标准格式的错误响应。

#### 配置示例

```yaml
global:
  error:
    enabled: true  # 启用全局异常处理（默认 true）
```

#### 功能说明

GlobalErrorHandler 会自动捕获所有异常，并返回统一格式的响应：

1. **自定义异常处理**：捕获 CommonException（包括 BizException），返回 HTTP 200 + 业务错误码
2. **兜底异常处理**：捕获其他异常，根据异常类型返回对应的 HTTP 状态码
3. **响应加密**：支持对错误响应进行加密
4. **日志记录**：自动记录异常日志，区分 warn 和 error 级别

#### 使用示例

```java
// Controller 中抛出异常
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public UserVO getUser(@PathVariable Long id) {
        UserVO user = userService.getById(id);
        if (user == null) {
            // 抛出业务异常
            throw new BizException(BizCodeEnum.DATA_NOT_FOUND);
        }
        return user;
    }
    
    @PostMapping("/save")
    public void save(@RequestBody UserDTO dto) {
        // 参数验证失败会抛出 MethodArgumentNotValidException
        userService.save(dto);
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
│  │ CommonException  │    │ 其他异常          │          │
│  │ (BizException等) │    │ (系统异常)        │          │
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
- **error**：服务器错误（HTTP 5xx）

### 2. ResponseBodyHandler - 统一响应体处理

自动将 Controller 返回的对象包装为 ResultVO 格式。

#### 配置示例

```yaml
response:
  body:
    enabled: true  # 启用统一响应体处理（默认 true）
    ignored-classes:  # 忽略的类（不包装响应体）
      - SwaggerConfigResource
      - OpenApiWebMvcResource
```

#### 功能说明

ResponseBodyHandler 会自动处理 Controller 返回值：

1. **自动包装**：将返回值自动包装为 ResultVO 格式
2. **状态码处理**：根据 HTTP 状态码决定是成功还是失败响应
3. **忽略特定类**：支持配置忽略的类（如 Swagger 相关类）
4. **响应加密**：支持对响应数据进行加密

#### 使用示例

```java
// Controller 返回任意对象，自动包装为 ResultVO
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    // 返回单个对象，自动包装为 ResultVO.success(user)
    @GetMapping("/{id}")
    public UserVO getUser(@PathVariable Long id) {
        UserVO user = userService.getById(id);
        return user;
    }
    
    // 返回集合，自动包装为 ResultVO.success(userList)
    @GetMapping("/list")
    public List<UserVO> listUsers() {
        List<UserVO> users = userService.list();
        return users;
    }
    
    // 返回分页对象，自动包装为 ResultVO.success(page)
    @GetMapping("/page")
    public Page<UserVO> pageUsers(@RequestParam Integer page, @RequestParam Integer size) {
        Page<UserVO> userPage = userService.page(page, size);
        return userPage;
    }
    
    // 返回 void，自动包装为 ResultVO.success()
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }
    
    // 返回 ResultVO，不重复包装
    @GetMapping("/custom")
    public ResultVO<UserVO> customResult() {
        UserVO user = userService.getById(1L);
        return ResultVO.success(user);
    }
}

// 响应格式
{
    "bizCode": "ok",
    "bizMessage": "OK",
    "bizData": {
        "id": 1,
        "name": "张三",
        "email": "zhangsan@example.com"
    },
    "bizTimestamp": 1234567890,
    "bizTraceId": "trace-id-123"
}
```

#### 处理流程

```
┌─────────────────────────────────────────────────────────┐
│                 ResponseBodyHandler                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Controller 返回值 ──> ResponseBodyHandler 拦截          │
│                                                          │
│  ┌──────────────────┐                                  │
│  │ 判断是否忽略      │                                  │
│  │ (Swagger等)      │                                  │
│  └──────────────────┘                                  │
│         │                                               │
│         ▼                                               │
│  ┌──────────────────┐                                  │
│  │ 判断返回值类型    │                                  │
│  └──────────────────┘                                  │
│         │                                               │
│         ├─> ResultVO ──> 直接返回（不重复包装）          │
│         │                                               │
│         ├─> 其他对象 ──> 包装为 ResultVO                 │
│         │                                               │
│         ▼                                               │
│  ┌──────────────────┐                                  │
│  │ 判断 HTTP 状态码  │                                  │
│  └──────────────────┘                                  │
│         │                                               │
│         ├─> 200 ──> ResultVO.success(data)              │
│         │                                               │
│         └─> 其他 ──> ResultVO.error(status)             │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 3. CrossConfiguration - 跨域配置

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

CrossConfiguration 会创建 CorsFilter，处理 CORS 跨域请求：

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

#### 跨域处理流程

```
┌─────────────────────────────────────────────────────────┐
│                  CORS 跨域请求流程                       │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  前端（http://localhost:8080）                           │
│         │                                                │
│         ├─> 发送预检请求（OPTIONS）                       │
│         │   - Origin: http://localhost:8080              │
│         │   - Access-Control-Request-Method: GET         │
│         │                                                │
│         ▼                                                │
│  后端（https://api.example.com）                         │
│         │                                                │
│         ├─> CorsFilter 处理预检请求                       │
│         │   - 检查 Origin 是否在允许列表中                │
│         │   - 检查 Method 是否允许                        │
│         │   - 返回 CORS 响应头                            │
│         │     - Access-Control-Allow-Origin              │
│         │     - Access-Control-Allow-Methods             │
│         │     - Access-Control-Allow-Headers             │
│         │     - Access-Control-Allow-Credentials         │
│         │     - Access-Control-Max-Age                   │
│         │                                                │
│         ▼                                                │
│  前端收到预检响应，发送实际请求                           │
│         │                                                │
│         ├─> 发送实际请求（GET）                           │
│         │   - Origin: http://localhost:8080              │
│         │                                                │
│         ▼                                                │
│  后端处理请求，返回响应                                   │
│         │                                                │
│         └─> 返回数据 + CORS 响应头                        │
│                                                          │
└─────────────────────────────────────────────────────────┘
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
    # publicKey: ""  # RSA/SM2 公钥
    # privateKey: ""  # RSA/SM2 私钥
```

#### 使用示例

```java
// Controller 返回敏感数据，自动加密
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public UserVO getUser(@PathVariable Long id) {
        UserVO user = userService.getById(id);
        // 返回的数据会被自动加密
        return user;
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

### ResponseBodyProperty - 响应体处理配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用响应体处理 | `true` |
| `ignored-classes` | 忽略的类（不包装响应体） | `[]` |

### CrossProperty - 跨域配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用跨域配置 | `false` |
| `allowed-origins` | 允许的域名 | `["*"]` |
| `allowed-methods` | 允许的方法 | `["*"]` |
| `allowed-headers` | 允许的请求头 | `["*"]` |
| `allow-credentials` | 允许携带凭证 | `true` |
| `max-age` | 预检请求缓存时间（秒） | `null` |

## 最佳实践

### 1. 异常处理

```java
// ✅ 推荐：使用 BizException 抛出业务异常
@GetMapping("/{id}")
public UserVO getUser(@PathVariable Long id) {
    UserVO user = userService.getById(id);
    if (user == null) {
        throw new BizException(BizCodeEnum.DATA_NOT_FOUND);
    }
    return user;
}

// ✅ 推荐：使用 BizCodeEnum 定义业务错误码
throw new BizException(BizCodeEnum.PARAM_ERROR);

// ❌ 不推荐：抛出 RuntimeException
throw new RuntimeException("用户不存在");
```

### 2. 响应体处理

```java
// ✅ 推荐：直接返回对象，自动包装为 ResultVO
@GetMapping("/{id}")
public UserVO getUser(@PathVariable Long id) {
    return userService.getById(id);
}

// ✅ 推荐：返回 ResultVO，不重复包装
@GetMapping("/custom")
public ResultVO<UserVO> customResult() {
    return ResultVO.success(userService.getById(1L));
}

// ❌ 不推荐：手动包装（除非需要自定义响应）
@GetMapping("/{id}")
public ResultVO<UserVO> getUser(@PathVariable Long id) {
    UserVO user = userService.getById(id);
    return ResultVO.success(user);  // 多余的包装
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

### 4. 响应加密

```yaml
# ✅ 推荐：使用 AES-GCM 模式
filter:
  crypto:
    enabled: true
    algorithm: AES
    key: "32位密钥"

# ✅ 推荐：使用国密算法
filter:
  crypto:
    enabled: true
    algorithm: SM2
    publicKey: "公钥"
    privateKey: "私钥"
```

## 注意事项

1. **GlobalErrorHandler**：优先级最高（@Order(HIGHEST_PRECEDENCE)），确保能捕获所有异常
2. **ResponseBodyHandler**：只处理 JSON 类型的响应，其他类型（如文件下载）不受影响
3. **CrossConfiguration**：生产环境建议配置具体的域名，不要使用 `*`
4. **响应加密**：启用响应加密后，前端需要解密才能获取原始数据
5. **忽略类配置**：Swagger 相关类默认忽略，避免影响 API 文档功能

## 相关模块

- `iwindplus-base-web`：Web 基础模块，提供 Controller 基类、过滤器等
- `iwindplus-base-webflux`：WebFlux 模块，提供响应式编程支持
- `iwindplus-base-domain`：领域模型模块，提供 ResultVO、BizException 等
- `iwindplus-base-util`：工具类模块，提供加解密、异常处理工具
