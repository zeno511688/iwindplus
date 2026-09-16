# iwindplus-base-address

## 1. 模块定位

本模块基于 `iwindplus-base-http-client` 封装 IP 地址查询服务，通过策略模式集成多家地址供应商：

```text
业务代码
   └── AddressExecuteHandlerFactory ── 地址供应商策略 ── 百度 / 高德 / 腾讯 / IP138 / 太平洋网络
```

模块通过 Spring Boot 自动配置注册策略，并根据供应商配置的 `priority` 和 `enabled` 实现自动路由、健康检查与故障转移。

## 2. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-address</artifactId>
</dependency>
```

模块依赖 `iwindplus-base-http-client` 提供的 `HttpClientExecuteHandlerFactory`。使用前应确保 HTTP Client 的自动配置和客户端依赖已生效。

## 3. 支持的供应商

| 编码 | 枚举 | 说明 |
|---|---|---|
| `baidu` | `AddressProviderEnum.BAIDU` | 百度地图 |
| `gaode` | `AddressProviderEnum.GAODE` | 高德地图 |
| `tencent` | `AddressProviderEnum.TENCENT` | 腾讯地图 |
| `ip138` | `AddressProviderEnum.IP138` | IP138 |
| `pconline` | `AddressProviderEnum.PCONLINE` | 太平洋网络，不建议生产使用 |

## 4. 配置

`AddressProperty` 的配置前缀是 `address`，供应商配置为 Map，key 必须使用上表中的编码。

```yaml
address:
  enabled: true
  providers:
    baidu:
      enabled: true
      priority: 1
      api-key: your-baidu-ak
      secret-key: your-baidu-sk
    gaode:
      enabled: true
      priority: 2
      api-key: your-gaode-key
    tencent:
      enabled: true
      priority: 3
      api-key: your-tencent-key
    ip138:
      enabled: false
      priority: 4
      api-key: your-ip138-token
    pconline:
      enabled: false
      priority: 99
```

配置字段：

- `address.enabled`：是否启用地址服务，默认 `true`。
- `address.providers.<code>.enabled`：是否启用当前供应商，默认 `true`。
- `priority`：数字越小优先级越高；未配置时按最低优先级处理。
- `api-key`：供应商 API Key。
- `secret-key`：百度签名场景使用的 Secret Key。

## 5. 自动路由调用

```java
@RequiredArgsConstructor
@Service
public class IpAddressService {

    private final AddressExecuteHandlerFactory addressExecuteHandlerFactory;

    public Optional<AddressVO> query(String ip) {
        return addressExecuteHandlerFactory.getAddress(ip);
    }
}
```

`getAddress(String ip)` 的处理流程：

1. 检查地址服务是否启用；
2. 跳过本机回环地址和内网 IP；
3. 按已启用供应商的 `priority` 升序遍历；
4. 跳过健康检查失败的供应商；
5. 当前供应商失败后尝试下一个供应商；
6. 全部失败时返回 `Optional.empty()`。

## 6. 指定供应商调用

```java
Optional<AddressVO> result = addressExecuteHandlerFactory.getAddress(
    "8.8.8.8",
    AddressProviderEnum.BAIDU
);

List<AddressProviderEnum> providers = addressExecuteHandlerFactory.getAvailableProviders();
```

`AddressVO` 用于承载统一结果，字段包括 `ip`、`nation`、`province`、`city`、`longitude`、`latitude`。供应商原始响应 DTO 位于 `domain.dto` 包中。

注意：源码会过滤内网 IP，因此不能使用 `127.0.0.1`、`192.168.x.x` 等地址验证第三方查询效果。

## 7. 自动配置

模块包含一个自动配置：

- `AddressConfiguration`：由 `address.enabled` 控制，注册各供应商策略实例和 `AddressExecuteHandlerFactory`。

供应商没有配置或必要凭证缺失时，对应策略不会注册，或会被健康检查过滤。

## 8. 相关对象

- `AddressProviderEnum`：供应商枚举
- `AddressVO`：统一结果对象
- `AddressProperty`：配置属性
- `AddressExecuteHandler`：策略接口
- `AddressExecuteHandlerFactory`：策略工厂
- `BaiduAddressExecuteHandler` / `GaodeAddressExecuteHandler` / `TencentAddressExecuteHandler` / `Ip138AddressExecuteHandler` / `PconlineAddressExecuteHandler`：各供应商策略实现

固定请求路径、请求参数和字段常量维护在 `AddressConstant` 中。

## 9. 注意事项

- 地址服务对内网 IP 直接跳过，不代表供应商接口不可用；
- 自动路由只遍历配置中启用且有对应执行策略的供应商；
- `Optional.empty()` 表示调用失败、无结果或服务未启用，业务必须处理；
- API 密钥和 Secret 应放入配置中心或密钥管理系统；
- 太平洋网络（`pconline`）接口不稳定，不建议生产环境使用。
