# iwindplus-base-web

Web 基础模块，提供 Controller 基类、过滤器、配置等 Web 开发常用功能。

## 功能特性

- ✅ BaseController 基类（获取用户信息、请求头、签名验证等）
- ✅ 请求过滤器（RequestFilter）- 自动解析用户信息、设置真实 IP、国际化语言
- ✅ XSS 过滤器（XssFilter）- 防止 XSS 攻击
- ✅ Jackson 配置（JacksonConfiguration）- 统一 JSON 序列化配置
- ✅ 验证器配置（ValidatorConfiguration）- 参数验证配置
- ✅ 线程池配置（ThreadPoolConfiguration）- 异步线程池配置
- ✅ 密码加密器（PasswordEncoder）- BCrypt 密码加密
- ✅ 跨域配置（CrossProperty）- CORS 跨域配置
- ✅ 全局异常处理（GlobalErrorProperty）- 统一异常处理
- ✅ 响应体处理（ResponseBodyProperty）- 统一响应格式

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-web</artifactId>
</dependency>
```

## 核心功能

### 1. BaseController - Controller 基类

提供常用的请求处理方法，所有 Controller 可以继承此类。

#### 使用示例

```java
@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @GetMapping("/info")
    public ResultVO<UserBaseVO> getUserInfo() {
        // 获取当前登录用户信息
        UserBaseVO user = getUserInfo();
        return ResultVO.success(user);
    }

    @GetMapping("/headers")
    public ResultVO<Map<String, String>> getHeaders() {
        // 获取请求头信息
        Map<String, String> headers = getHeaderMap();
        return ResultVO.success(headers);
    }

    @GetMapping("/trace-id")
    public ResultVO<String> getTraceId() {
        // 获取跟踪唯一标识
        String traceId = getTraceId();
        return ResultVO.success(traceId);
    }

    @GetMapping("/request-id")
    public ResultVO<String> getRequestId() {
        // 获取请求唯一标识
        String requestId = getRequestId();
        return ResultVO.success(requestId);
    }

    @GetMapping("/real-ip")
    public ResultVO<String> getRealIp() {
        // 获取真实 IP
        String realIp = getRealId();
        return ResultVO.success(realIp);
    }

    @GetMapping("/params")
    public ResultVO<Map<String, String>> getParams() {
        // 获取请求参数
        Map<String, String> params = getParameterMap();
        return ResultVO.success(params);
    }

    @PostMapping("/check-sign")
    public ResultVO<Void> checkSign(@RequestBody SignDTO dto) {
        // 验证签名（默认不需要 AK/SK）
        checkSign(request, Duration.ofMinutes(5));
        return ResultVO.success();
    }

    @PostMapping("/check-sign-aksk")
    public ResultVO<Void> checkSignByAkSk(@RequestBody SignDTO dto) {
        // 验证签名（使用 AK/SK）
        BaseSignVO signVO = new BaseSignVO();
        signVO.setAccessKey("your-access-key");
        signVO.setSecretKey("your-secret-key");
        signVO.setTimeout(300L);
        checkSignByAkSk(request, signVO);
        return ResultVO.success();
    }
}
```

#### 主要方法

| 方法 | 说明 |
|------|------|
| `getUserInfo()` | 获取当前登录用户信息 |
| `getHeaderMap()` | 获取请求头信息 |
| `getTraceId()` | 获取跟踪唯一标识 |
| `getRequestId()` | 获取请求唯一标识 |
| `getRealId()` | 获取真实 IP |
| `getParameterMap()` | 获取请求参数 |
| `checkSign(HttpServletRequest request, Duration timeout)` | 验证签名（默认不需要 AK/SK） |
| `checkSignByAkSk(HttpServletRequest request, BaseSignVO entity)` | 验证签名（使用 AK/SK） |

### 2. RequestFilter - 请求过滤器

自动解析请求头中的用户信息、设置真实 IP、国际化语言等。

#### 配置示例

```yaml
filter:
  enabled: true
  request:
    enabled: true  # 启用请求过滤器
  crypto:
    enabled: true
    algorithm: AES  # 加密算法：AES、RSA、SM2
    key: "12345678901234567890123456789012"  # AES 密钥（32 位）
    # publicKey: ""  # RSA/SM2 公钥
    # privateKey: ""  # RSA/SM2 私钥
```

#### 功能说明

RequestFilter 会自动处理以下内容：

1. **设置字符集**：设置请求和响应字符集为 UTF-8
2. **设置国际化语言**：从请求头 `Accept-Language` 获取语言，设置到 MDC
3. **设置真实 IP**：从请求头 `X-Real-IP` 获取真实 IP，如果没有则从请求中解析
4. **解析用户信息**：从请求头 `X-User-Info` 获取加密的用户信息，解密后设置到 UserContextHolder
5. **解析 TCC 信息**：从请求头 `X-TCC-XID` 获取 TCC 事务 ID

#### 请求头说明

| 请求头 | 说明 | 示例 |
|--------|------|------|
| `Accept-Language` | 国际化语言 | `zh-CN` |
| `X-Real-IP` | 真实 IP | `192.168.1.100` |
| `X-User-Info` | 加密的用户信息 | `加密后的字符串` |
| `X-TCC-XID` | TCC 事务 ID | `tcc-transaction-id` |
| `X-Requested-Id` | 请求唯一标识 | `uuid` |

#### 使用示例

```java
// 在 Controller 中获取用户信息
@RestController
public class UserController extends BaseController {
    
    @GetMapping("/user-info")
    public ResultVO<UserBaseVO> getUserInfo() {
        // 从 UserContextHolder 获取用户信息
        UserBaseVO user = getUserInfo();
        return ResultVO.success(user);
    }
}

// 在其他组件中获取用户信息
public class UserService {
    public void doSomething() {
        // 从 UserContextHolder 获取用户信息
        UserBaseVO user = UserContextHolder.getContext();
        String userId = user.getUserId();
        String username = user.getUsername();
    }
}
```

### 3. XssFilter - XSS 过滤器

防止 XSS 攻击，过滤请求参数中的危险字符。

#### 配置示例

```yaml
filter:
  enabled: true
  xss:
    enabled: true  # 启用 XSS 过滤器
    enabled-skip: false  # 是否跳过 XSS 过滤
    tag-white-list:  # 标签白名单（需要忽略的标签）
      - "<p>"
      - "<br>"
      - "<b>"
      - "<i>"
      - "<u>"
    ignored-api:  # 忽略的 API（不需要 XSS 过滤）
      - "/api/upload"
      - "/api/rich-text"
    ignored-symbol:  # 忽略的符号
      - "<"
      - ">"
```

#### 功能说明

XssFilter 会过滤请求参数中的危险字符，防止 XSS 攻击：

- 过滤 `<script>` 标签
- 过滤 `javascript:` 协议
- 过滤 `on*` 事件属性（如 `onclick`、`onload`）
- 支持标签白名单
- 支持忽略特定 API
- 支持忽略特定符号

### 4. JacksonConfiguration - Jackson 配置

统一配置 Jackson ObjectMapper，支持 Java 8 日期时间类型。

#### 配置示例

```yaml
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss  # 日期格式
    time-zone: GMT+8  # 时区
    default-property-inclusion: non_null  # 不序列化 null 值
    serialization:
      write-dates-as-timestamps: false  # 不将日期转为时间戳
      fail-on-empty-beans: false  # 空对象不报错
    deserialization:
      fail-on-unknown-properties: false  # 忽略未知属性
```

#### 功能说明

- 支持 Java 8 日期时间类型（LocalDateTime、LocalDate、LocalTime、Instant）
- 支持自定义日期格式
- 支持敏感字段脱敏
- 支持 MyBatis Page 分页对象序列化

### 5. ValidatorConfiguration - 验证器配置

配置参数验证器，支持快速失败模式。

#### 配置示例

```yaml
validator:
  enabled: true  # 启用验证器
  fail-fast: true  # 快速失败模式（遇到第一个错误就返回）
```

#### 使用示例

```java
@Data
public class UserDTO {
    @NotBlank(message = "用户名不能为空", groups = {SaveGroup.class, EditGroup.class})
    private String username;
    
    @NotBlank(message = "密码不能为空", groups = {SaveGroup.class})
    @Length(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;
}

@RestController
public class UserController {
    
    @PostMapping("/save")
    public ResultVO<Void> save(@RequestBody @Validated(SaveGroup.class) UserDTO dto) {
        // 参数验证通过，执行业务逻辑
        return ResultVO.success();
    }
    
    @PostMapping("/edit")
    public ResultVO<Void> edit(@RequestBody @Validated(EditGroup.class) UserDTO dto) {
        // 参数验证通过，执行业务逻辑
        return ResultVO.success();
    }
}
```

### 6. ThreadPoolConfiguration - 线程池配置

配置异步线程池，支持自定义线程池参数。

#### 配置示例

```yaml
spring:
  task:
    execution:
      pool:
        core-size: 10  # 核心线程数
        max-size: 50  # 最大线程数
        queue-capacity: 1000  # 队列容量
        keep-alive: 60s  # 空闲线程存活时间
        allow-core-thread-timeout: true  # 允许核心线程超时
      thread-name-prefix: async-  # 线程名前缀
```

#### 使用示例

```java
@Service
public class UserService {
    
    @Async
    public CompletableFuture<Void> asyncMethod() {
        // 异步执行
        return CompletableFuture.completedFuture(null);
    }
}
```

### 7. PasswordEncoder - 密码加密器

使用 BCrypt 算法对密码进行加密和验证。

#### 使用示例

```java
@Service
public class UserService {
    
    @Resource
    private PasswordEncoder passwordEncoder;
    
    public void register(String username, String password) {
        // 加密密码
        String encodedPassword = passwordEncoder.encode(password);
        // 保存用户信息
        user.setPassword(encodedPassword);
        userMapper.insert(user);
    }
    
    public boolean login(String username, String rawPassword) {
        // 查询用户
        User user = userMapper.selectByUsername(username);
        // 验证密码
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        return matches;
    }
}
```

### 8. 跨域配置

配置 CORS 跨域。

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
  max-age: 3600  # 预检请求缓存时间
```

### 9. 全局异常处理

统一处理异常，返回标准格式的错误响应。

#### 配置示例

```yaml
global-error:
  enabled: true  # 启用全局异常处理
```

#### 使用示例

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BizException.class)
    public ResultVO<Void> handleBizException(BizException e) {
        return ResultVO.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return ResultVO.fail(BizCodeEnum.PARAM_ERROR.getCode(), message);
    }
    
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultVO.fail(BizCodeEnum.SYSTEM_ERROR.getCode(), "系统异常");
    }
}
```

### 10. 响应体处理

统一处理响应体，返回标准格式的响应。

#### 配置示例

```yaml
response-body:
  enabled: true  # 启用响应体处理
```

#### 使用示例

```java
@RestController
public class UserController {
    
    @GetMapping("/user/{id}")
    public ResultVO<UserVO> getUser(@PathVariable Long id) {
        UserVO user = userService.getById(id);
        return ResultVO.success(user);
    }
    
    @GetMapping("/users")
    public ResultVO<List<UserVO>> listUsers() {
        List<UserVO> users = userService.list();
        return ResultVO.success(users);
    }
    
    @GetMapping("/page")
    public ResultVO<Page<UserVO>> pageUsers(@RequestParam Integer page, @RequestParam Integer size) {
        Page<UserVO> userPage = userService.page(page, size);
        return ResultVO.success(userPage);
    }
}
```

## 配置属性

### FilterProperty - 过滤器配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用过滤器 | `true` |
| `xss.enabled` | 是否启用 XSS 过滤器 | `true` |
| `xss.enabled-skip` | 是否跳过 XSS 过滤 | `false` |
| `xss.tag-white-list` | 标签白名单 | `[]` |
| `xss.ignored-api` | 忽略的 API | `[]` |
| `xss.ignored-symbol` | 忽略的符号 | `[]` |
| `request.enabled` | 是否启用请求过滤器 | `true` |
| `crypto.enabled` | 是否启用加解密 | `true` |
| `crypto.algorithm` | 加密算法 | `AES` |
| `crypto.key` | AES 密钥（32 位） | - |
| `crypto.public-key` | RSA/SM2 公钥 | - |
| `crypto.private-key` | RSA/SM2 私钥 | - |

### CrossProperty - 跨域配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用跨域配置 | `true` |
| `allowed-origins` | 允许的域名 | `[]` |
| `allowed-methods` | 允许的方法 | `[]` |
| `allowed-headers` | 允许的请求头 | `[]` |
| `allow-credentials` | 允许携带凭证 | `true` |
| `max-age` | 预检请求缓存时间 | `3600` |

### ValidatorProperty - 验证器配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用验证器 | `true` |
| `fail-fast` | 快速失败模式 | `true` |

### GlobalErrorProperty - 全局异常处理配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用全局异常处理 | `true` |

### ResponseBodyProperty - 响应体处理配置

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `enabled` | 是否启用响应体处理 | `true` |

## 最佳实践

### 1. Controller 继承 BaseController

```java
// ✅ 推荐：继承 BaseController
@RestController
public class UserController extends BaseController {
    @GetMapping("/user-info")
    public ResultVO<UserBaseVO> getUserInfo() {
        UserBaseVO user = getUserInfo();
        return ResultVO.success(user);
    }
}

// ❌ 不推荐：不继承 BaseController
@RestController
public class UserController {
    @GetMapping("/user-info")
    public ResultVO<UserBaseVO> getUserInfo(HttpServletRequest request) {
        // 手动解析用户信息
        String userInfo = request.getHeader("X-User-Info");
        // ...
    }
}
```

### 2. 使用请求过滤器自动解析用户信息

```java
// ✅ 推荐：使用 UserContextHolder 获取用户信息
UserBaseVO user = UserContextHolder.getContext();

// ❌ 不推荐：手动解析请求头
String userInfo = request.getHeader("X-User-Info");
UserBaseVO user = JacksonUtil.parseObject(userInfo, UserBaseVO.class);
```

### 3. 使用 XSS 过滤器防止 XSS 攻击

```yaml
# ✅ 推荐：启用 XSS 过滤器
filter:
  xss:
    enabled: true
    tag-white-list:
      - "<p>"
      - "<br>"
```

### 4. 使用密码加密器

```java
// ✅ 推荐：使用 PasswordEncoder
String encodedPassword = passwordEncoder.encode(password);
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

// ❌ 不推荐：使用明文密码
String encodedPassword = password;  // 不安全
```

### 5. 使用参数验证

```java
// ✅ 推荐：使用 @Validated 注解
@PostMapping("/save")
public ResultVO<Void> save(@RequestBody @Validated(SaveGroup.class) UserDTO dto) {
    // 参数验证通过，执行业务逻辑
    return ResultVO.success();
}

// ❌ 不推荐：手动验证参数
@PostMapping("/save")
public ResultVO<Void> save(@RequestBody UserDTO dto) {
    if (StringUtils.isBlank(dto.getUsername())) {
        throw new BizException("用户名不能为空");
    }
    // ...
}
```

## 注意事项

1. **BaseController**：提供了常用的请求处理方法，所有 Controller 可以继承此类
2. **RequestFilter**：自动解析用户信息、设置真实 IP、国际化语言等，需要在网关或服务中配置
3. **XssFilter**：防止 XSS 攻击，支持标签白名单和忽略特定 API
4. **PasswordEncoder**：使用 BCrypt 算法，每次加密结果不同，但验证时可以正确匹配
5. **线程池配置**：支持自定义线程池参数，建议根据实际业务调整
6. **跨域配置**：生产环境建议配置具体的域名，不要使用 `*`
7. **全局异常处理**：统一处理异常，返回标准格式的错误响应
8. **响应体处理**：统一处理响应体，返回标准格式的响应

## 相关模块

- `iwindplus-base-domain`：领域模型模块，提供 DTO、VO 等基础定义
- `iwindplus-base-util`：工具类模块，提供加解密、JSON 序列化等功能
- `iwindplus-base-webmvc`：WebMVC 模块，提供 Spring MVC 相关功能
- `iwindplus-base-webflux`：WebFlux 模块，提供响应式编程支持
