# 短信模块（iwindplus-base-sms）

本模块统一封装短信验证码和模板短信发送，当前提供四个独立的供应商服务：

- 阿里云：`SmsAliyunService`；
- 七牛云：`SmsQiniuService`；
- 凌凯：`SmsLingkaiService`；
- 麦讯通：`SmsMxtongService`。

模块没有自动的供应商策略工厂。每个供应商根据自己的 `enabled` 配置独立注册 Bean，业务代码应注入明确的供应商接口。

```text
业务代码
   │
   ├── SmsAliyunService
   ├── SmsQiniuService
   ├── SmsLingkaiService
   └── SmsMxtongService
          │
          ├── smsSendCaptcha()
          └── smsSend()
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-sms</artifactId>
</dependency>
```

## 2. 配置

配置前缀为 `sms`：

```yaml
sms:
  aliyun:
    enabled: true
    access-key: ${ALIYUN_SMS_ACCESS_KEY}
    secret-key: ${ALIYUN_SMS_SECRET_KEY}
    sign-name: 示例签名
    template-content: 您的验证码是${code}，${timeout}分钟内有效。
    sts:
      endpoint: sts.cn-shenzhen.aliyuncs.com
      role-arn: acs:ram::123456789:role/sms-role
  qiniu:
    enabled: false
    access-key: ${QINIU_ACCESS_KEY}
    secret-key: ${QINIU_SECRET_KEY}
    template-content: 您的验证码是${code}。
  lingkai:
    enabled: false
    access-key: ${LINGKAI_ACCESS_KEY}
    secret-key: ${LINGKAI_SECRET_KEY}
    template-content: 您的验证码是${code}。
  mxtong:
    enabled: false
    access-key: ${MXTONG_ACCESS_KEY}
    secret-key: ${MXTONG_SECRET_KEY}
    template-content: 您的验证码是${code}。
```

### 2.1 公共配置字段

四个供应商配置都继承 `AkSkDTO`，并包含：

| 配置项 | 说明 |
|---|---|
| `enabled` | 是否启用该供应商，默认 `false` |
| `access-key` | 供应商访问密钥 |
| `secret-key` | 供应商密钥 |
| `template-content` | 短信模板内容 |

阿里云额外包含：

- `sign-name`：短信签名名称；
- `sts.endpoint`：STS 服务地址；
- `sts.role-arn`：RAM 角色 ARN；
- `sts.policy`：可选权限策略；
- `sts.access-key`、`sts.secret-key`、`sts.security-token`、`sts.expiration`：临时凭证相关字段。

短信模板中的参数占位格式由具体供应商实现决定，配置中的 `template-content` 会参与供应商请求参数构造。

## 3. 注入短信服务

### 3.1 阿里云

` sms.aliyun.enabled=true ` 时注册 `SmsAliyunService`：

```java
@Resource
private SmsAliyunService smsAliyunService;
```

### 3.2 七牛云、凌凯、麦讯通

分别配置对应开关后注入：

```java
@Resource
private SmsQiniuService smsQiniuService;

@Resource
private SmsLingkaiService smsLingkaiService;

@Resource
private SmsMxtongService smsMxtongService;
```

如果多个供应商同时启用，不要直接按 `SmsBaseService` 注入，否则会产生多个候选 Bean；应按供应商接口注入，或由业务层自行定义供应商选择器。

## 4. 发送短信验证码

所有供应商服务都继承 `SmsBaseService`，提供验证码发送方法：

```java
SmsLogVO result = smsAliyunService.smsSendCaptcha(
    phoneNumber,
    6,
    10
);
```

参数含义：

- `phoneNumber`：目标手机号；
- `captchaLength`：验证码长度，默认 6 位；
- `captchaTimeout`：验证码有效时间，单位分钟，默认 10 分钟。

`SmsLogVO` 返回：

- `bizNumber`：业务流水号；
- `phoneNumber`：手机号；
- `captcha`：生成的验证码；
- `expireTime`：验证码过期时间。

验证码发送成功后，业务侧应使用 `bizNumber`、手机号和验证码完成后续校验，并对发送频率、同手机号并发请求和错误次数进行限制。不要把验证码直接写入普通业务日志或返回给前端。

## 5. 批量发送模板短信

使用 `smsSend` 向手机号集合发送模板短信：

```java
List<SmsBatchVO> results = smsAliyunService.smsSend(
    List.of("13800000001", "13800000002"),
    List.of("张三", "订单号20260825001"),
    100
);
```

参数含义：

- `phoneNumbers`：手机号集合；
- `templateParams`：模板参数集合，可选；
- `phoneNumberGroupSize`：每组手机号数量，默认 100。

返回值 `List<SmsBatchVO>` 每项包含：

- `bizNumber`：该批次业务流水号；
- `phoneNumbers`：该批次手机号集合；
- `templateParams`：该批次模板参数；
- `phoneNumberGroupSize`：批次分组大小。

分组大小应结合供应商单次请求限制设置，不要一次传入无限大的手机号集合。

## 6. 阿里云短信的额外参数

`SmsAliyunService` 在通用 `smsSend` 之外，提供带上行短信扩展码的重载：

```java
List<SmsBatchVO> results = smsAliyunService.smsSend(
    phoneNumbers,
    templateParams,
    smsUpExtendCode,
    100
);
```

`smsUpExtendCode` 用于阿里云上行短信场景；普通验证码或普通模板短信可传 `null`，具体是否允许为空以阿里云账号和模板配置为准。

## 7. 运行时读取和设置配置

所有短信服务都继承 `SmsBaseConfigService`：

```java
SmsProperty property = smsAliyunService.getConfig();
smsAliyunService.setConfig(property);
```

配置对象变更只影响已创建服务实例的运行配置，不会改变 Spring 启动阶段的条件 Bean 注册结果。应用启动时未开启某供应商，不能依赖运行时 `setConfig` 让该供应商 Bean 自动出现。

## 8. 选择供应商的建议

如果系统只使用一个供应商，可以直接注入对应服务：

```java
@Service
public class CaptchaService {

    private final SmsAliyunService smsService;

    public CaptchaService(SmsAliyunService smsService) {
        this.smsService = smsService;
    }
}
```

如果系统需要故障切换或按业务选择供应商，建议在业务层封装选择逻辑，不要假设本模块已经提供自动路由：

```text
业务短信门面
   │
   ├── 登录验证码 → 阿里云
   ├── 营销短信   → 七牛云
   └── 备用通道   → 凌凯/麦讯通
```

## 9. 注意事项

- 供应商服务只有在对应 `sms.*.enabled=true` 时才注册；
- 每个供应商的 `template-content` 是配置属性，不能把供应商模板 ID 当作本模块统一字段使用；
- AK/SK、STS Role ARN 和临时令牌应通过环境变量、配置中心或密钥管理系统提供；
- 生产环境应对验证码发送增加接口鉴权、图形验证码、手机号频率限制和 IP 限制；
- 不要把验证码、完整手机号和供应商密钥写入日志；
- 批量发送应合理设置 `phoneNumberGroupSize`，并根据 `SmsBatchVO.bizNumber` 做发送记录和结果追踪；
- `SmsBaseService` 有多个实现时不要直接按父接口注入；
- 本模块只负责调用供应商和返回发送结果，验证码校验、业务状态、重试策略和发送记录由业务层负责。
