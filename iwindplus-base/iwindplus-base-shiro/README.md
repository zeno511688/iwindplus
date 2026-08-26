# Shiro 权限模块（iwindplus-base-shiro）

本模块基于 Apache Shiro 提供两种互斥的认证形态：

- **JWT/无状态模式**：客户端携带 `Authorization: Bearer <accessToken>`，Token 和权限缓存保存到 Redis；
- **Session/有状态模式**：使用用户名密码登录，Shiro Session 保存到 Redis，并支持 Remember Me。

两种模式各自通过配置开关启用。应用应根据自身认证架构选择一种，避免两个配置同时启用导致 `SecurityManager`、过滤器和 Bean 冲突。

```text
ShiroService
    ├── getByUsername(username)  ──▶ 用户、密码、角色、资源权限
    └── listAccessPerms()        ──▶ URL 与 Shiro 过滤器链

JWT 模式：Authorization Header ──▶ CustomAuthenticationFilter ──▶ ShiroRealm
Session 模式：UsernamePasswordToken ──▶ ShiroSessionRealm ──▶ Redis Session
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-shiro</artifactId>
</dependency>
```

模块依赖 Redis 保存 JWT Token、Shiro 缓存或 Session，应用必须提供 `RedisTemplate<String, Object>`。JWT 模式还依赖项目中的 `PasswordEncoder` 验证密码。

## 2. 实现业务数据接口

业务应用必须实现 `ShiroService`：

```java
@Component
public class UserShiroService implements ShiroService {
    @Override
    public ShiroUserVO getByUsername(String username) {
        // 查询用户，并组装密码、角色编码、资源权限编码
        return userService.findShiroUser(username);
    }

    @Override
    public List<AccessPermsVO> listAccessPerms() {
        // 返回 URL 与过滤器链表达式
        return permissionService.listAccessPerms();
    }
}
```

### 2.1 `ShiroUserVO` 数据要求

`ShiroUserVO` 继承项目基础用户对象，关键字段包括：

- `username`：登录用户名；
- `password`：密码摘要，JWT 模式使用 `PasswordEncoder` 校验；
- `rolePermissions`：角色编码集合，用于 `roles` 过滤器和 `@RequiresRoles`；
- `resourcePermissions`：资源权限编码集合，用于 `perms` 过滤器和 `@RequiresPermissions`。

密码字段带有 `@JsonIgnore`，但业务查询对象仍应避免在日志和接口中暴露密码摘要。

### 2.2 `AccessPermsVO` 数据要求

每条权限至少包含：

- `apiUrl`：URL 匹配表达式，例如 `/api/user/**`；
- `permission`：Shiro 过滤器链，例如 `authc,perms[user:query]`、`authc,roles[admin]`。

模块启动时读取 `listAccessPerms()`，创建 Shiro Filter Chain；权限更新后可以使用 `ReloadPermissionManager.updatePermission()` 重新加载。

## 3. JWT 无状态模式

### 3.1 配置

```yaml
shiro:
  jwt:
    enabled: true
    authentication-filter-name: authc
    roles-authorization-filter-name: roles
    perms-authorization-filter-name: perms
    access-token-expire-time: 5m
    refresh-token-expire-time: 7d
  session:
    enabled: false
```

配置说明：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `shiro.jwt.enabled` | `false` | 启用 JWT 自动配置 |
| `shiro.jwt.authentication-filter-name` | `authc` | 认证过滤器名称 |
| `shiro.jwt.roles-authorization-filter-name` | `roles` | 角色过滤器名称 |
| `shiro.jwt.perms-authorization-filter-name` | `perms` | 权限过滤器名称 |
| `shiro.jwt.access-token-expire-time` | `5m` | Access Token 有效期 |
| `shiro.jwt.refresh-token-expire-time` | `7d` | Refresh Token 有效期 |

JWT 模式会关闭 Shiro 自带 Session 存储，Token、用户名映射、刷新 Token 和授权缓存由 Redis 管理。

### 3.2 登录生成 Token

注入 `ShiroRealm`，调用源码提供的用户名密码登录方法：

```java
@Resource
private ShiroRealm shiroRealm;

@PostMapping("/login")
public AccessTokenVO login(@RequestBody LoginDTO request) {
    return shiroRealm.getAccessTokenByUsername(
        request.getUsername(),
        request.getPassword()
    );
}
```

成功返回 `AccessTokenVO`：

- `accessToken`：访问 Token；
- `refreshToken`：刷新 Token；
- `expiresIn`：访问 Token 有效期，单位为秒。

密码错误或用户不存在时，Realm 会抛出项目业务异常。

### 3.3 请求携带 Token

```http
GET /api/user/profile HTTP/1.1
Host: api.example.com
Authorization: Bearer <accessToken>
```

`CustomAuthenticationFilter` 从 `Authorization` 请求头读取 Bearer Token，也兼容 WebSocket 使用的 `X-Sec-WebSocket-Protocol` 请求头。Token 有效时，Shiro 会将 `ShiroUserVO` 放入当前 Subject。

### 3.4 获取当前用户

```java
Subject subject = ShiroUtil.getSubject();
ShiroUserVO user = ShiroUtil.getUserInfo();
Long userId = ShiroUtil.getUserId();
```

无认证用户时，`getUserInfo()` 和 `getUserId()` 返回 `null`。受保护接口应通过过滤器或方法级注解控制访问，不要只依赖业务代码中的空值判断。

### 3.5 刷新和退出

```java
@PostMapping("/token/refresh")
public AccessTokenVO refresh(@RequestParam String refreshToken) {
    return shiroRealm.getAccessTokenByRefreshToken(refreshToken);
}

@PostMapping("/logout")
public void logout(@RequestHeader("Authorization") String authorization) {
    String accessToken = authorization.replace("Bearer ", "").trim();
    shiroRealm.logout(accessToken);
}
```

刷新 Token 无效时会抛出无效刷新 Token 异常。退出会删除访问 Token、刷新 Token 及相关用户映射，并清理 Shiro 认证/授权缓存。

## 4. Session 有状态模式

### 4.1 配置

```yaml
shiro:
  jwt:
    enabled: false
  session:
    enabled: true
    login-url: /login
    success-url: /index
    unauthorized-url: /unauthorized
    cache-timeout: 2h
    session-id-cookie-name: sid
    remember-name: rememberMe
    remember-me-timeout: 7d
    remember-cipher-key: ${SHIRO_REMEMBER_CIPHER_KEY}
    authentication-filter-name: authc
    roles-authorization-filter-name: roles
    perms-authorization-filter-name: perms
```

Session 配置说明：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `shiro.session.enabled` | `false` | 启用 Session 自动配置 |
| `shiro.session.login-url` | `/login` | 未登录跳转地址 |
| `shiro.session.success-url` | `/index` | 登录成功地址 |
| `shiro.session.unauthorized-url` | `/unauthorized` | 无权限地址 |
| `shiro.session.cache-timeout` | `2h` | Session 缓存有效期 |
| `shiro.session.session-id-cookie-name` | `sid` | Session ID Cookie 名称 |
| `shiro.session.remember-name` | `rememberMe` | Remember Me Cookie 名称 |
| `shiro.session.remember-me-timeout` | `7d` | Remember Me 有效期 |
| `shiro.session.remember-cipher-key` | 无 | Remember Me 加密密钥，建议显式配置 |

Session 使用 Redis Session DAO 保存会话，模块会关闭 URL 重写中的 Session ID，并启用 Session 失效校验调度器。

### 4.2 登录

```java
@Resource
private ShiroService shiroService;

@PostMapping("/login")
public void login(@RequestBody LoginDTO request) {
    Subject subject = ShiroUtil.getSubject();
    ShiroSessionTokenDTO token = new ShiroSessionTokenDTO(
        request.getUsername(),
        request.getPassword(),
        request.getRememberMe()
    );
    subject.login(token);
}
```

`ShiroSessionTokenDTO` 继承 `UsernamePasswordToken`，第三个参数为是否记住我。认证成功后，客户端通过 Session Cookie 访问受保护接口。

### 4.3 获取用户和退出

```java
ShiroUserVO user = ShiroUtil.getUserInfo();
Long userId = ShiroUtil.getUserId();

ShiroUtil.getSubject().logout();
```

退出后 Session 失效，Remember Me Cookie 也应根据业务需要清理。

## 5. URL 权限过滤链

`ShiroFilterFactoryBean` 会从 `ShiroService.listAccessPerms()` 动态生成过滤链。例如：

```java
AccessPermsVO publicApi = AccessPermsVO.builder()
    .apiUrl("/api/public/**")
    .permission("anon")
    .build();

AccessPermsVO userApi = AccessPermsVO.builder()
    .apiUrl("/api/user/**")
    .permission("authc,perms[user:query]")
    .build();

AccessPermsVO adminApi = AccessPermsVO.builder()
    .apiUrl("/api/admin/**")
    .permission("authc,roles[admin]")
    .build();
```

当前模块注册的主要过滤器：

- `authc`：认证过滤器；
- `roles`：角色过滤器；
- `perms`：权限过滤器。

多个过滤器用逗号分隔，全部通过后请求才会放行。过滤器名称可以通过 `ShiroProperty` 配置修改，但数据库中的权限表达式必须同步修改。

## 6. 方法级权限

模块开启 Shiro AOP 注解支持，可以在 Controller 或 Service 方法上使用 Shiro 注解：

```java
@RequiresPermissions("user:query")
public UserVO detail(Long id) {
    return userService.detail(id);
}

@RequiresRoles("admin")
public void delete(Long id) {
    userService.delete(id);
}
```

方法级权限依赖 `ShiroUserVO.resourcePermissions` 和 `rolePermissions`，两者应与注解中的编码保持一致。

## 7. 热加载权限

当权限表发生变化时，注入 `ReloadPermissionManager` 并调用：

```java
@Resource
private ReloadPermissionManager reloadPermissionManager;

public void reloadPermission() {
    reloadPermissionManager.updatePermission();
}
```

该方法会：

1. 清空当前 Filter Chain；
2. 重新调用 `ShiroService.listAccessPerms()`；
3. 重建 URL 与过滤器链；
4. 使新的权限规则立即生效。

权限缓存与用户授权缓存仍可能存在，因此修改用户角色或资源权限后，建议同时清理对应用户的 Shiro 授权缓存，或调用 Realm 的缓存清理方法。

## 8. 安全注意事项

- JWT Token 实际保存在 Redis，不是自包含 JWT，不能按 JWT 解码方式读取内容；
- Access Token 和 Refresh Token 应通过 HTTPS 传输；
- `remember-cipher-key` 必须使用独立的高强度密钥，不要提交到代码仓库；
- 生产环境必须使用持久化 Redis，并规划 Token、Session 和 Shiro Cache 的过期时间；
- `ShiroService.getByUsername()` 返回的密码必须是当前 `PasswordEncoder` 能识别的摘要；
- URL 权限表达式应从更具体的路径到更通用的路径设计，避免通配规则提前匹配；
- 支付、文件上传、健康检查等公开接口应明确配置 `anon`，不要依赖默认行为；
- JWT 和 Session 建议只启用其中一种模式；
- 不要把密码、Access Token、Refresh Token 或 Remember Me 密钥写入日志。
