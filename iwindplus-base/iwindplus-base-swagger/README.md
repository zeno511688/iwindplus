# Swagger API 注册模块（iwindplus-base-swagger）

本模块不是 Swagger UI，也不负责生成 OpenAPI JSON。它做两件事：

1. 通过 Spring MVC 的 `RequestMappingHandlerMapping` 扫描接口；
2. 应用启动完成后，把带有 `@Tag` 和 `@Operation` 注解的 API 信息注册到管理服务。

```text
Spring MVC HandlerMapping
          │
          ▼
筛选 @Tag + @Operation 接口
          │
          ▼
拆分多 URL / 多 HTTP Method
          │
          ▼
构造 AppApiVO
          │
          ▼
等待 DiscoveryClient 发现管理服务
          │
          ▼
HTTP POST swagger.server-api.url
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-swagger</artifactId>
</dependency>
```

应用还需要具备：

- Spring MVC；
- Knife4j/OpenAPI 注解依赖，至少能使用 `@Tag`、`@Operation`；
- 服务发现客户端 `DiscoveryClient`，用于等待目标管理服务可发现；
- `iwindplus-base-http-client`，用于注册 API。

## 2. 标注接口

只有同时满足以下条件的方法才会被收集：

- Controller 类上存在 `@Tag`，且 `name` 或 `description` 至少一个非空；
- 方法上存在 `@Operation`，且 `summary` 或 `description` 至少一个非空。

```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(summary = "查询用户", description = "按 ID 查询用户详情")
    @GetMapping({"/{id}", "/detail/{id}"})
    public UserVO detail(@PathVariable Long id) {
        return userService.detail(id);
    }
}
```

上例会拆分为两条 API：

```text
GET /api/users/{id}
GET /api/users/detail/{id}
```

`SwaggerServiceImpl` 会将多个 URL 与多个 HTTP Method 展开成多条 `ApiInfoVO`，并按 Controller、HTTP Method、URL、API 名称排序。

## 3. 生成的 API 信息

`SwaggerService#getServerInfo()` 返回 `AppApiVO`，主要内容包括：

- `appName`：当前 Spring 应用名；
- `appRemark`：`server.servlet.application-display-name`；
- `apis`：接口列表。

每条 `ApiInfoVO` 包含：

- `controllerName`：来自 `@Tag.name`，为空时使用 `@Tag.description`；
- `className`：Controller 完整类名；
- `methodName`：Java 方法名；
- `requestMethod`：HTTP 方法；
- `apiName`：来自 `@Operation.summary`，为空时使用 `@Operation.description`；
- `apiUrl`：规范化后的接口路径；
- `hideFlag`：`@Operation.hidden`。

业务代码可以注入 `SwaggerService` 获取当前应用 API 定义：

```java
@Resource
private SwaggerService swaggerService;

public AppApiVO apiInfo() {
    return swaggerService.getServerInfo();
}
```

如果没有符合条件的接口，`getServerInfo()` 返回 `null`。

## 4. 启动后自动注册

模块自动注册 `SwaggerListener`，监听 `ApplicationReadyEvent`。应用启动完成后按以下流程执行：

1. 检查 `swagger.server-api.enabled`；
2. 调用 `SwaggerService#getServerInfo()`；
3. 通过 `DiscoveryClient` 查询目标管理服务实例；
4. 目标服务可发现后，使用 WebClient POST API 信息；
5. 接口返回 `ResultVO<Boolean>` 且业务数据为 `true` 时认为注册成功；
6. 失败后按照重试配置继续执行。

```text
ApplicationReadyEvent
        │
        ├── enabled=false ──▶ 跳过
        │
        ├── 没有 API 信息 ──▶ 跳过
        │
        ├── 管理服务未发现 ──▶ 等待 retry-interval
        │
        ├── POST 注册失败 ──▶ 继续重试
        │
        └── 返回 ResultVO<Boolean=true> ──▶ 注册完成
```

## 5. 配置

```yaml
swagger:
  server-api:
    enabled: true
    service-name: iwindplus-mgt
    url: lb://iwindplus-mgt/inner/serverApi/saveOrEdit
    max-retry: 5
    retry-interval: 3
```

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `swagger.server-api.enabled` | `true` | 是否启动 API 注册 |
| `swagger.server-api.service-name` | `iwindplus-mgt` | DiscoveryClient 查询的服务名 |
| `swagger.server-api.url` | `lb://iwindplus-mgt/inner/serverApi/saveOrEdit` | API 注册接口地址 |
| `swagger.server-api.max-retry` | `5` | 最大注册尝试次数 |
| `swagger.server-api.retry-interval` | `3` | 重试间隔，单位秒 |

`max-retry` 包含首次尝试次数。若 `DiscoveryClient` 不存在、目标服务没有实例、HTTP 请求异常或返回业务失败，都会进入下一次重试；超过次数后记录注册失败日志。

## 6. 管理服务接口约定

模块使用 WebClient 调用配置中的 URL，并发送当前应用的 `AppApiVO`。远程接口需要返回项目统一响应结构：

```json
{
  "success": true,
  "bizData": true
}
```

源码会先调用 `ResultVO#errorThrow()` 检查响应错误，再判断 `bizData` 是否为 `true`。因此管理服务应保证：

- HTTP 请求成功时返回合法 JSON；
- 响应结构与 `ResultVO<Boolean>` 兼容；
- API 注册成功返回 `bizData=true`；
- API 注册接口具备幂等能力，同一应用重复启动不会产生重复数据。

## 7. 接口扫描边界

以下接口不会被收集：

- Controller 类没有 `@Tag`；
- 方法没有 `@Operation`；
- `@Tag.name`、`@Tag.description` 都为空；
- `@Operation.summary`、`@Operation.description` 都为空。

其他行为：

- 没有显式 HTTP Method 的映射默认记录为 `POST`；
- 没有 URL 的映射默认记录为 `/`；
- URL 会补充前导 `/`、合并重复斜杠并去除末尾斜杠；
- `@Operation.hidden=true` 不会阻止收集，只会在 `hideFlag` 中标记隐藏状态；
- 一个方法多个 URL 或多个 HTTP Method 会拆分成多条 API 记录。

## 8. 使用注意事项

- 本模块不提供 Swagger UI、Knife4j 页面或 OpenAPI 文档生成能力；
- 必须使用 `@Tag` 和 `@Operation`，仅使用 `@ApiOperation` 等旧版注解不会被当前实现识别；
- 目标服务必须能被 `DiscoveryClient` 发现，否则即使注册 URL 可访问也不会发起注册；
- 单体应用没有服务发现时，当前自动注册流程会因不存在 `DiscoveryClient` 而跳过，可关闭 `swagger.server-api.enabled`；
- 注册 URL 默认是负载均衡地址，使用固定地址时请改为实际 HTTP URL；
- API 注册服务应按应用名和接口唯一键实现幂等保存；
- 修改接口注解后，需要重启应用才能重新扫描并注册；
- `@Operation.hidden` 仅作为返回字段标记，管理服务是否隐藏由管理端决定。
