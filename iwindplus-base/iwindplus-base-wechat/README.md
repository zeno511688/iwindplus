# 微信模块（iwindplus-base-wechat）

本模块基于 WxJava 封装三类微信能力，支持多配置管理，每个配置通过唯一编码标识，每个配置对应一个运行时策略实例：

- 微信小程序：登录换 Session、手机号、用户信息、小程序码；
- 微信公众号：OAuth2、用户信息、公众号二维码；
- 微信支付：支付配置、订单通知解析、退款通知解析。

```text
WechatMaExecuteHandlerFactory  ──▶ WechatMaExecuteHandler
WechatMpExecuteHandlerFactory  ──▶ WechatMpExecuteHandler
WechatPayExecuteHandlerFactory ──▶ WechatPayExecuteHandler
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-wechat</artifactId>
</dependency>
```

## 2. 微信小程序

### 2.1 配置

```yaml
wechat:
  enabled: true
  ma:
    - code: "default"
      name: "默认小程序配置"
      enabled: true
      priority: 1
      use-redis: false
      app-id: ${WECHAT_MA_APP_ID}
      secret: ${WECHAT_MA_SECRET}
      token: ${WECHAT_MA_TOKEN}
      aes-key: ${WECHAT_MA_AES_KEY}
      msg-data-format: JSON
    - code: "shop"
      name: "商城小程序配置"
      enabled: true
      priority: 2
      use-redis: true
      app-id: ${WECHAT_MA_SHOP_APP_ID}
      secret: ${WECHAT_MA_SHOP_SECRET}
      token: ${WECHAT_MA_SHOP_TOKEN}
      aes-key: ${WECHAT_MA_SHOP_AES_KEY}
      msg-data-format: JSON
```

顶层配置字段：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `enabled` | `true` | 是否启用微信模块自动配置 |

小程序配置字段：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `false` | 是否启用 |
| `priority` | 无 | 优先级（数字越小优先级越高） |
| `use-redis` | 无 | 是否使用 Redis 保存 WxJava 配置数据 |
| `app-id` | 无 | 小程序 AppID |
| `secret` | 无 | 小程序 AppSecret |
| `token` | 无 | 消息服务器 Token |
| `aes-key` | 无 | 消息服务器 EncodingAESKey |
| `msg-data-format` | 无 | 消息格式，例如 `JSON` 或 `XML` |

`use-redis=true` 时，模块使用项目中的 `StringRedisTemplate` 和固定前缀保存小程序配置数据，因此应用必须提供 Redis 能力。小程序配置缺少 AppID 或 Secret 时，策略仍可能被创建，但不会完成 WxJava 配置注入，生产环境必须完整配置。

### 2.2 使用方式

#### 注入策略工厂

```java
@Resource
private WechatMaExecuteHandlerFactory wechatMaExecuteHandlerFactory;
```

#### 获取小程序处理器

通过配置编码获取对应的小程序处理器：

```java
WechatMaExecuteHandler handler = wechatMaExecuteHandlerFactory.getHandler("default");
```

或获取默认处理器（优先级最高的可用策略）：

```java
WechatMaExecuteHandler handler = wechatMaExecuteHandlerFactory.getDefaultHandler();
```

`WechatMaExecuteHandler` 继承 WxJava 的 `WxMaService`，除 WxJava 原生能力外增加以下方法：

- `getSessionInfo(String code)`：使用登录临时 code 换取 Session 信息；
- `getPhoneNumberInfo(String code)`：使用手机号授权 code 获取手机号信息；
- `getUserInfo(WechatMaUserInfoDTO entity)`：获取并解密用户信息；
- `getQrCode(WechatMaQrCodeDTO entity)`：生成小程序码。

#### 登录示例

```java
WxMaJscode2SessionResult session =
    handler.getSessionInfo(loginCode);

WechatMaPhoneNumberVO phone =
    handler.getPhoneNumberInfo(phoneCode);
```

`WechatMaPhoneNumberVO` 在 WxJava 手机号信息基础上增加：

- `openid`；
- `unionId`；
- `sessionKey`。

手机号、SessionKey 和用户原始数据属于敏感信息，不应直接返回给非可信客户端或写入日志。

#### 生成小程序码

```java
WechatMaQrCodeDTO qrCodeDTO = WechatMaQrCodeDTO.builder()
    .scene("order=10001")
    .page("pages/order/detail")
    .checkPath(true)
    .envVersion("release")
    .width(430)
    .hyaline(false)
    .build();

String qrCode = handler.getQrCode(qrCodeDTO);
```

## 3. 微信公众号

### 3.1 配置

```yaml
wechat:
  enabled: true
  mp:
    - code: "default"
      name: "默认公众号配置"
      enabled: true
      priority: 1
      use-redis: false
      app-id: ${WECHAT_MP_APP_ID}
      secret: ${WECHAT_MP_SECRET}
      token: ${WECHAT_MP_TOKEN}
      aes-key: ${WECHAT_MP_AES_KEY}
    - code: "service"
      name: "服务号配置"
      enabled: true
      priority: 2
      use-redis: true
      app-id: ${WECHAT_MP_SERVICE_APP_ID}
      secret: ${WECHAT_MP_SERVICE_SECRET}
      token: ${WECHAT_MP_SERVICE_TOKEN}
      aes-key: ${WECHAT_MP_SERVICE_AES_KEY}
```

公众号配置字段：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `false` | 是否启用 |
| `priority` | 无 | 优先级（数字越小优先级越高） |
| `use-redis` | 无 | 是否使用 Redis 保存 WxJava 配置数据 |
| `app-id` | 无 | 公众号 AppID |
| `secret` | 无 | 公众号 AppSecret |
| `token` | 无 | 公众号消息服务器 Token |
| `aes-key` | 无 | 公众号消息服务器 EncodingAESKey |

### 3.2 使用方式

#### 注入策略工厂

```java
@Resource
private WechatMpExecuteHandlerFactory wechatMpExecuteHandlerFactory;
```

#### 获取公众号处理器

通过配置编码获取对应的公众号处理器：

```java
WechatMpExecuteHandler handler = wechatMpExecuteHandlerFactory.getHandler("default");
```

或获取默认处理器：

```java
WechatMpExecuteHandler handler = wechatMpExecuteHandlerFactory.getDefaultHandler();
```

#### OAuth2 和用户信息

```java
WxOAuth2AccessToken token =
    handler.getAccessToken(code);

WxOAuth2UserInfo userInfo =
    handler.getUserInfo(code, "zh_CN");
```

公众号 OAuth2 回调中的 `code` 应由微信授权流程产生，服务端需要校验 state、防止重放，并根据业务需要保存用户与 OpenID 的绑定关系。

#### 生成公众号二维码

```java
String qrCode = handler.getQrCode("campaign=summer");
```

`WechatMpExecuteHandler` 同时继承 WxJava `WxMpService`，可继续使用 WxJava 提供的菜单、消息、OAuth2 等原生能力。

## 4. 微信支付

### 4.1 配置

`WechatProperty.PayConfig` 继承 WxJava 的 `WxPayConfig`，模块不重新定义支付字段，直接使用 WxJava 支付配置：

```yaml
wechat:
  enabled: true
  pay:
    - code: "default"
      name: "默认支付配置"
      enabled: true
      priority: 1
      app-id: ${WECHAT_PAY_APP_ID}
      mch-id: ${WECHAT_PAY_MCH_ID}
      mch-key: ${WECHAT_PAY_MCH_KEY}
      sub-app-id: ${WECHAT_PAY_SUB_APP_ID}
      sub-mch-id: ${WECHAT_PAY_SUB_MCH_ID}
      key-path: classpath:cert/apiclient_cert.p12
      private-key-path: classpath:cert/apiclient_key.pem
      private-cert-path: classpath:cert/apiclient_cert.pem
      cert-serial-no: ${WECHAT_PAY_CERT_SERIAL_NO}
      api-v3-key: ${WECHAT_PAY_API_V3_KEY}
      notify-url: https://api.example.com/pay/wechat/notify
    - code: "h5"
      name: "H5支付配置"
      enabled: true
      priority: 2
      app-id: ${WECHAT_PAY_H5_APP_ID}
      mch-id: ${WECHAT_PAY_H5_MCH_ID}
      mch-key: ${WECHAT_PAY_H5_MCH_KEY}
      notify-url: https://api.example.com/pay/wechat/h5/notify
```

支付配置字段（在 WxJava `WxPayConfig` 基础上增加）：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `false` | 是否启用 |
| `priority` | 无 | 优先级（数字越小优先级越高） |

具体支付字段以当前 WxJava `WxPayConfig` 版本为准。配置支付证书、商户号、密钥和回调地址时，必须与微信商户平台保持一致。

### 4.2 使用方式

#### 注入策略工厂

```java
@Resource
private WechatPayExecuteHandlerFactory wechatPayExecuteHandlerFactory;
```

#### 获取支付处理器

通过配置编码获取对应的支付处理器：

```java
WechatPayExecuteHandler handler = wechatPayExecuteHandlerFactory.getHandler("default");
```

或获取默认处理器：

```java
WechatPayExecuteHandler handler = wechatPayExecuteHandlerFactory.getDefaultHandler();
```

`WechatPayExecuteHandler` 继承 WxJava `WxPayService`，并增加两个回调解析方法：

- `orderNotify(HttpServletRequest request)`：解析支付成功通知；
- `refundNotify(HttpServletRequest request)`：解析退款成功通知。

#### 控制器示例

```java
@PostMapping("/pay/wechat/notify")
public WxPayOrderNotifyResult orderNotify(
    HttpServletRequest request) {
    WxPayOrderNotifyResult result =
        handler.orderNotify(request);

    if (result == null) {
        // 返回失败响应，具体协议按微信支付版本处理
        return null;
    }

    // 校验支付状态、金额、商户号、订单号，并执行幂等更新
    return result;
}
```

源码中的回调实现负责读取请求体并调用 WxJava 解析方法；异常时记录日志并返回 `null`。它不负责：

- 业务订单状态更新；
- 支付金额校验；
- 重复通知幂等；
- 微信支付回调响应格式封装。

这些逻辑必须由业务控制器或领域服务完成。

## 5. Redis 使用要求

当 `wechat.ma.use-redis` 或 `wechat.mp.use-redis` 为 `true` 时：

1. 应用必须引入并配置 Redis；
2. 模块使用 `StringRedisTemplate` 创建 WxJava Redis 配置存储；
3. 小程序和公众号使用不同的固定 Key 前缀，避免相互覆盖。

不使用 Redis 时，采用内存配置存储，适合单实例或开发环境；生产集群建议使用 Redis，避免多实例之间的微信 AccessToken、JsapiTicket 等状态不一致。

## 6. 使用注意事项

- 每个配置必须有唯一的 `code`，用于标识和获取对应的微信处理器；
- 配置的 `enabled` 为 `false` 时，对应的策略不会被创建；
- 顶层 `wechat.enabled` 为 `false` 时，整个微信模块自动配置关闭；
- `priority` 数字越小优先级越高，`getDefaultHandler()` 返回优先级最高的可用策略；
- AppID、Secret、商户密钥、API V3 Key、证书私钥都应通过密钥管理系统注入；
- 支付回调必须先验签并校验金额、商户号和业务订单号，再修改订单状态；
- 支付和退款通知都可能重复到达，业务处理必须幂等；
- 不要记录 `sessionKey`、AppSecret、商户密钥和完整支付报文；
- 小程序码的 `scene`、`page`、环境版本和宽度必须符合微信接口限制；
- 本模块封装的是 WxJava 服务，未在本 README 中列出的原生能力应以当前 WxJava 版本 API 为准。
