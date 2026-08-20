# iwindplus-base-http-client-integration

HTTP 客户端集成模块，用于基于 `iwindplus-base-http-client` 调用第三方接口。

## 模块职责

- 复用 `HttpClientExecutorStrategyFactory` 提供的 HTTP 客户端执行器。
- 封装第三方地址查询接口，避免业务模块直接处理请求参数和响应 JSON。
- 使用 DTO 接收第三方接口响应，并转换为统一的 `AddressVO`。
- 通过 Spring Boot 自动配置注册地址服务。

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-http-client-integration</artifactId>
</dependency>
```

该模块依赖 `iwindplus-base-http-client`，使用方需要确保 HTTP 客户端模块的配置已生效。

## 自动配置

模块提供 `HttpClientIntegrationConfiguration` 自动配置，自动注册 `AddressService` 实现类。

引入模块后，Spring Boot 会通过自动配置文件加载该配置，业务代码可以直接注入 `AddressService`。

## 地址服务

`AddressService` 提供以下第三方地址查询能力：

| 方法 | 第三方服务 | 必填参数 |
| --- | --- | --- |
| `getAddressByPconline` | 太平洋网络 | `ip` |
| `getAddressByGaode` | 高德云图 | `ip`、`appCode` |
| `getAddressByIp138` | IP138 | `ip`、`token` |
| `getAddressByBaidu` | 百度地图 | `ip`、`ak` |
| `getAddressByTencent` | 腾讯地图 | `ip`、`key` |

所有方法均返回 `Optional<AddressVO>`。当第三方接口没有返回有效地址信息时，返回空的 `Optional`。

## 使用示例

```java
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final AddressService addressService;

    public Optional<AddressVO> queryAddress(String ip, String appCode) {
        return addressService.getAddressByGaode(ip, appCode);
    }
}
```

## 返回对象

`AddressVO` 位于 `domain.vo` 包，统一提供以下字段：

- `ip`：IP 地址。
- `province`：省份或州。
- `city`：城市。

第三方响应对象位于 `domain.dto` 包，目前包括：

- `PconlineAddressDTO`
- `GaodeAddressDTO`
- `Ip138AddressDTO`
- `BaiduAddressDTO`
- `TencentAddressDTO`

## 地址接口常量

第三方接口地址和响应字段常量统一维护在 `HttpClientIntegrationConstant.AddressConstant` 中。业务代码不需要自行拼接固定 URL。
