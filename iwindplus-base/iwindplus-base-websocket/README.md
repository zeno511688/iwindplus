# WebSocket 模块（iwindplus-base-websocket）

本模块基于 Tio 提供独立 WebSocket 服务端和可选 HTTP 服务端，并通过 Redisson 的 Tio Cluster Topic 支持多节点广播。

```text
Spring Boot
    │
    ├── WebSocketServerBootstrap ── Tio WebSocket Server :9326
    │       └── IWsMsgHandler（业务消息处理）
    │
    ├── WebSocketHttpServerBootstrap（可选）
    │       └── HttpRequestHandler（业务 HTTP 处理）
    │
    └── RedissonTioClusterTopic ── Redis ── 多节点广播
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-websocket</artifactId>
</dependency>
```

模块自动配置依赖 `RedissonClient`，因此项目必须先引入并正确配置 `iwindplus-base-redis` 或其他 Redisson 配置。启动时会自动创建 `RedissonTioClusterTopic`。

## 2. 基础配置

配置前缀是 `websocket`：

```yaml
websocket:
  server:
    enabled: true
    http-enabled: false
    name: iwindplus-websocket
    ip: 0.0.0.0
    port: 9326
    debug: false
    share: true
    use-queue-send: true
    use-queue-decode: false
    log-when-decode-error: false
    heartbeat-timeout: 5s
    ip-stat-durations: 1,5,10,30,60
  cluster:
    enabled: true
    group: true
    bs-id: true
    user: true
    ip: true
    channel: true
    all: true
```

服务端配置：

| 配置项 | 说明 |
|---|---|
| `server.enabled` | 是否启动 Tio WebSocket 服务，默认 `true` |
| `server.http-enabled` | 是否额外启动 Tio HTTP 服务，默认 `false` |
| `server.name` | Tio 服务名称，默认 `iwindplus-websocket` |
| `server.ip` | 绑定 IP，空值表示不固定绑定地址 |
| `server.port` | WebSocket 和 HTTP 服务使用的端口，默认 `9326` |
| `server.debug` | 是否开启 Tio 调试 |
| `server.share` | 是否共享 Tio 服务配置 |
| `server.use-queue-send` | 是否使用发送队列 |
| `server.use-queue-decode` | 是否使用解码队列 |
| `server.log-when-decode-error` | 解码异常时是否打印日志 |
| `server.heartbeat-timeout` | 心跳超时时间，超时自动关闭连接 |
| `server.ip-stat-durations` | IP 统计时间窗口 |

## 3. 实现 WebSocket 消息处理器

WebSocket 服务开启时，容器中必须存在 `IWsMsgHandler`：

```java
@Component
public class BusinessWsMsgHandler implements IWsMsgHandler {

    @Override
    public WsResponse onText(
        RequestLine requestLine,
        HttpRequest httpRequest,
        ChannelContext channelContext,
        String text,
        String str) {
        return WsResponse.fromText(
            "received: " + text,
            StandardCharsets.UTF_8.name()
        );
    }

    // 其他 IWsMsgHandler 方法按 Tio 版本要求实现
}
```

`WebSocketServerBootstrap` 启动时通过 Spring 容器查找 `IWsMsgHandler`。如果未提供，启动初始化会失败，WebSocket 服务不会正常工作。

业务处理器负责：

- 处理连接建立、关闭和握手；
- 处理文本、二进制和心跳消息；
- 返回 `WsResponse`；
- 按业务保存用户、业务 ID、分组和 Channel 的关联关系。

## 4. 启动 WebSocket 服务

```yaml
websocket:
  server:
    enabled: true
    port: 9326
```

自动配置会注册 `WebSocketServerBootstrap`。它实现 Spring `SmartLifecycle`，应用启动时启动 Tio 服务，应用停止时关闭服务。

客户端连接地址示例：

```text
ws://127.0.0.1:9326/ws
```

实际路径和握手参数由业务实现的 Tio Handler 决定，模块本身不提供固定 Controller 路径。

## 5. 启动 HTTP 服务

如果需要使用 Tio HTTP Server：

```yaml
websocket:
  server:
    enabled: true
    http-enabled: true
    port: 9326
```

业务可以提供 `HttpRequestHandler`：

```java
@Component
public class BusinessHttpRequestHandler implements HttpRequestHandler {
    // 按当前 Tio 版本实现 HTTP 请求处理方法
}
```

如果没有提供 `HttpRequestHandler`，模块会使用 Tio 的 `DefaultHttpRequestHandler`。HTTP 服务和 WebSocket 服务使用同一个端口配置，开启 HTTP 后需要确认 Tio 当前版本支持对应协议复用方式。

## 6. SSL 配置

当 `ssl.key-store` 和 `ssl.trust-store` 都非空时，模块才会创建服务端 SSL 配置：

```yaml
websocket:
  ssl:
    key-store: classpath:tls/server.keystore
    trust-store: classpath:tls/server.truststore
    password: ${WEBSOCKET_SSL_PASSWORD}
```

只有同时配置 KeyStore 和 TrustStore 才会启用 SSL。配置后客户端使用 `wss://` 连接：

```text
wss://example.com:9326/ws
```

证书格式、密码和路径必须符合 Tio `SslConfig.forServer` 的要求。

## 7. 集群广播

模块会自动创建 Redisson Tio Cluster Topic，Topic 名称由 `spring.application.name` 生成。集群开关和广播目标如下：

```yaml
websocket:
  cluster:
    enabled: true
    group: true
    bs-id: true
    user: true
    ip: true
    channel: true
    all: true
```

| 配置项 | 广播目标 |
|---|---|
| `cluster.group` | 指定分组 |
| `cluster.bs-id` | 指定业务 ID |
| `cluster.user` | 指定用户 |
| `cluster.ip` | 指定 IP |
| `cluster.channel` | 指定通道 |
| `cluster.all` | 所有连接 |

启用集群广播的前提：

1. 所有节点使用同一个 Redis 集群或 Redis 哨兵配置；
2. 各节点的 `spring.application.name` 与集群命名规则保持一致；
3. 节点间使用相同的广播目标配置；
4. 业务保存的用户、分组和 Channel 标识在各节点间具有一致语义。

## 8. 发送消息

模块不提供独立的业务发送 Service，发送使用 Tio 原生 API，并通过 `ServerTioConfig` 或 `ChannelContext` 定位连接：

```java
@Resource
private WebSocketServerBootstrap webSocketServerBootstrap;

public void sendToUser(String userId, String content) {
    WsResponse response = WsResponse.fromText(
        content,
        StandardCharsets.UTF_8.name()
    );

    Tio.sendToUser(
        webSocketServerBootstrap.getServerTioConfig(),
        userId,
        response
    );
}
```

常用发送范围由 Tio API 决定：

- `Tio.sendToUser`：发送给指定用户；
- `Tio.sendToGroup`：发送给指定分组；
- `Tio.sendToBsId`：发送给指定业务 ID；
- `Tio.sendToIp`：发送给指定 IP；
- `Tio.sendToChannel`：发送给指定 Channel；
- `Tio.sendToAll`：发送给所有连接。

只有对应 `websocket.cluster.*` 开关开启时，目标才会参与集群广播配置。

## 9. 获取底层 Tio 配置

两个 Bootstrap 都提供 `getServerTioConfig()`：

```java
ServerTioConfig tioConfig =
    webSocketServerBootstrap.getServerTioConfig();
```

在应用完全启动前不要调用该方法执行发送，因为 Tio Server 可能尚未完成初始化。推荐在业务服务启动完成后使用。

## 10. 使用注意事项

- 必须先提供 `RedissonClient`，否则自动配置无法创建集群 Topic；
- 开启 WebSocket 服务时必须提供 `IWsMsgHandler`；
- `server.enabled` 只控制 WebSocket 服务；`server.http-enabled` 独立控制 HTTP 服务；
- WebSocket 和 HTTP 服务使用同一个 `server.port`；
- `ssl.key-store` 和 `ssl.trust-store` 必须同时配置才会启用 SSL；
- 生产环境不要开启 `server.debug`；
- 心跳超时时间过短可能导致移动端、弱网客户端频繁断开；
- 多节点广播依赖 Redisson 和 Redis，单机模式可以关闭 `cluster.enabled`；
- 发送 API 使用的是 Tio 原生对象，不要把 `WsResponse` 与 Spring WebSocket 的消息对象混用；
- 业务应自行维护用户、分组、业务 ID 和 Channel 的绑定关系；
- 应用停止时由 Spring 生命周期关闭 Tio Server，不要在业务代码中重复关闭同一个 Server。
