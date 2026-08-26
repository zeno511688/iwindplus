# 微信模块（iwindplus-base-wechat）

本模块基于 WxJava 封装三类微信能力：

- 微信小程序：登录换 Session、手机号、用户信息、小程序码；
- 微信公众号：OAuth2、用户信息、公众号二维码；
- 微信支付：支付配置、订单通知解析、退款通知解析。

三类服务相互独立，通过各自 `enabled` 配置注册 Bean。

```text
wechat.ma.enabled   ──▶ WechatMaService
wechat.mp.enabled   ──▶ WechatMpService
wechat.pay.enabled  ──▶ WechatPayService
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
  ma:
    enabled: true
    use-redis: false
    app-id: ${WECHAT_MA_APP_ID}
    secret: ${WECHAT_MA_SECRET}
    token: ${WECHAT_MA_TOKEN}
    aes-key: ${WECHAT_MA_AES_KEY}
    msg-data-format: JSON
```

配置字段：

| 配置项 | 说明 |
|---|---|
| `wechat.ma.enabled` | 是否注册 `WechatMaService`，默认 `false` |
| `wechat.ma.use-redis` | 是否使用 Redis 保存 WxJava 配置数据 |
| `wechat.ma.app-id` | 小程序 AppID |
| `wechat.ma.secret` | 小程序 AppSecret |
| `wechat.ma.token` | 消息服务器 Token |
| `wechat.ma.aes-key` | 消息服务器 EncodingAESKey |
| `wechat.ma.msg-data-format` | 消息格式，例如 `JSON` 或 `XML` |

`use-redis=true` 时，模块使用项目中的 `StringRedisTemplate` 和固定前缀保存小程序配置数据，因此应用必须提供 Redis 能力。小程序配置缺少 AppID 或 Secret 时，Bean 仍可能被创建，但不会完成 WxJava 配置注入，生产环境必须完整配置。

### 2.2 注入服务

```java
@Resource
private WechatMaService wechatMaService;
```

`WechatMaService` 继承 WxJava 的 `WxMaService`，除 WxJava 原生能力外增加以下方法：

- `getSessionInfo(String code)`：使用登录临时 code 换取 Session 信息；
- `getPhoneNumberInfo(String code)`：使用手机号授权 code 获取手机号信息；
- `getUserInfo(String code, String rawData, String signature, String encryptedData, String iv)`：获取并解密用户信息；
- `getQrCode(String scene, String page, Boolean checkPath, String envVersion, Integer width, Boolean isHyaline)`：生成小程序码。

登录示例：

```java
WxMaJscode2SessionResult session =
    wechatMaService.getSessionInfo(loginCode);

WechatMaPhoneNumberVO phone =
    wechatMaService.getPhoneNumberInfo(phoneCode);
```

`WechatMaPhoneNumberVO` 在 WxJava 手机号信息基础上增加：

- `openid`；
- `unionId`；
- `sessionKey`。

手机号、SessionKey 和用户原始数据属于敏感信息，不应直接返回给非可信客户端或写入日志。

生成小程序码：

```java
String qrCode = wechatMaService.getQrCode(
    "order=10001",
    "pages/order/detail",
    true,
    "release",
    430,
    false
);
```

## 3. 微信公众号

### 3.1 配置

```yaml
wechat:
  mp:
    enabled: true
    use-redis: false
    app-id: ${WECHAT_MP_APP_ID}
    secret: ${WECHAT_MP_SECRET}
    token: ${WECHAT_MP_TOKEN}
    aes-key: ${WECHAT_MP_AES_KEY}
```

配置字段：

| 配置项 | 说明 |
|---|---|
| `wechat.mp.enabled` | 是否注册 `WechatMpService`，默认 `false` |
| `wechat.mp.use-redis` | 是否使用 Redis 保存 WxJava 配置数据 |
| `wechat.mp.app-id` | 公众号 AppID |
| `wechat.mp.secret` | 公众号 AppSecret |
| `wechat.mp.token` | 公众号消息服务器 Token |
| `wechat.mp.aes-key` | 公众号消息服务器 EncodingAESKey |

### 3.2 OAuth2 和用户信息

```java
@Resource
private WechatMpService wechatMpService;

WxOAuth2AccessToken token =
    wechatMpService.getAccessToken(code);

WxOAuth2UserInfo userInfo =
    wechatMpService.getUserInfo(code, "zh_CN");
```

公众号 OAuth2 回调中的 `code` 应由微信授权流程产生，服务端需要校验 state、防止重放，并根据业务需要保存用户与 OpenID 的绑定关系。

生成公众号二维码：

```java
String qrCode = wechatMpService.getQrCode("campaign=summer");
```

`WechatMpService` 同时继承 WxJava `WxMpService`，可继续使用 WxJava 提供的菜单、消息、OAuth2 等原生能力。

## 4. 微信支付

### 4.1 配置

`WechatProperty.PayConfig` 继承 WxJava 的 `WxPayConfig`，模块不重新定义支付字段，直接使用 WxJava 支付配置：

```yaml
wechat:
  pay:
    enabled: true
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
```

具体字段以当前 WxJava `WxPayConfig` 版本为准。配置支付证书、商户号、密钥和回调地址时，必须与微信商户平台保持一致。

### 4.2 注入支付服务

```java
@Resource
private WechatPayService wechatPayService;
```

`WechatPayService` 继承 WxJava `WxPayService`，并增加两个回调解析方法：

- `orderNotify(HttpServletRequest request)`：解析支付成功通知；
- `refundNotify(HttpServletRequest request)`：解析退款成功通知。

控制器示例：

```java
@PostMapping("/pay/wechat/notify")
public WxPayOrderNotifyResult orderNotify(
    HttpServletRequest request) {
    WxPayOrderNotifyResult result =
        wechatPayService.orderNotify(request);

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

## 5. Bean 注册条件

| 服务 | 条件 |
|---|---|
| `WechatMaService` | `wechat.ma.enabled=true` |
| `WechatMpService` | `wechat.mp.enabled=true` |
| `WechatPayService` | `wechat.pay.enabled=true` |

同一应用可以同时启用小程序、公众号和支付，但应分别配置完整凭证。

## 6. Redis 使用要求

当 `wechat.ma.use-redis` 或 `wechat.mp.use-redis` 为 `true` 时：

1. 应用必须引入并配置 Redis；
2. 模块使用 `StringRedisTemplate` 创建 WxJava Redis 配置存储；
3. 小程序和公众号使用不同的固定 Key 前缀，避免相互覆盖。

不使用 Redis 时，采用内存配置存储，适合单实例或开发环境；生产集群建议使用 Redis，避免多实例之间的微信 AccessToken、JsapiTicket 等状态不一致。

## 7. 注意事项

- AppID、Secret、商户密钥、API V3 Key、证书私钥都应通过密钥管理系统注入；
- `enabled` 未设置或不是 `true` 时，对应服务 Bean 不会注册；
- 支付回调必须先验签并校验金额、商户号和业务订单号，再修改订单状态；
- 支付和退款通知都可能重复到达，业务处理必须幂等；
- 不要记录 `sessionKey`、AppSecret、商户密钥和完整支付报文；
- 小程序码的 `scene`、`page`、环境版本和宽度必须符合微信接口限制；
- 本模块封装的是 WxJava 服务，未在本 README 中列出的原生能力应以当前 WxJava 版本 API 为准。
