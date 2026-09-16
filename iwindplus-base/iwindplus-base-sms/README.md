# 短信模块（iwindplus-base-sms）

本模块统一封装短信验证码和模板短信发送，支持多配置管理、多供应商和自动故障转移。当前提供四个供应商服务：

- 阿里云：`SmsAliyunExecuteHandler`；
- 七牛云：`SmsQiniuExecuteHandler`；
- 凌凯：`SmsLingkaiExecuteHandler`；
- 麦讯通：`SmsMxtongExecuteHandler`。

## 架构设计

```text
业务代码
   │
   └── SmsExecuteHandlerFactory（策略工厂，统一对外门面，自动故障转移）
          │
          └── SmsExecuteHandler（策略标识接口，继承 BaseService）
                ├── SmsAliyunExecuteHandler（阿里云策略）
                ├── SmsQiniuExecuteHandler（七牛云策略）
                ├── SmsLingkaiExecuteHandler（凌凯策略）
                └── SmsMxtongExecuteHandler（麦讯通策略）
```

### 核心组件说明

- **SmsExecuteHandlerFactory**：策略工厂，负责管理和路由短信策略，支持多配置、动态路由和自动故障转移；
- **SmsExecuteHandler**：策略标识接口，继承 `BaseService`，定义短信发送能力；
- **BaseService**：通用业务层接口，提供健康检查和优先级获取能力；
- **BaseConfigService**：通用配置业务层接口，管理配置的获取和设置；
- **AbstractBaseServiceImpl**：短信业务基础抽象类，实现健康检查和优先级逻辑。

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-sms</artifactId>
</dependency>
```

## 2. 配置

配置前缀为 `sms`，支持四个供应商的多配置，每个配置通过唯一 `code` 标识：

```yaml
sms:
  enabled: true
  enabled-failover: true   # 自动故障转移开关，默认开启
  aliyun:
    - code: "aliyun-default"
      name: "默认阿里云短信配置"
      enabled: true
      priority: 1
      access-key: ${ALIYUN_SMS_ACCESS_KEY}
      secret-key: ${ALIYUN_SMS_SECRET_KEY}
      sts:
        endpoint: sts.cn-shenzhen.aliyuncs.com
        role-arn: acs:ram::123456789:role/sms-role
    - code: "aliyun-backup"
      name: "备用阿里云短信配置"
      enabled: true
      priority: 2
      access-key: ${ALIYUN_SMS_BACKUP_ACCESS_KEY}
      secret-key: ${ALIYUN_SMS_BACKUP_SECRET_KEY}
      sign-name: 备用签名
  qiniu:
    - code: "qiniu-default"
      name: "七牛云短信配置"
      enabled: false
      priority: 1
      access-key: ${QINIU_ACCESS_KEY}
      secret-key: ${QINIU_SECRET_KEY}
  lingkai:
    - code: "lingkai-default"
      name: "凌凯短信配置"
      enabled: false
      priority: 1
      access-key: ${LINGKAI_ACCESS_KEY}
      secret-key: ${LINGKAI_SECRET_KEY}
  mxtong:
    - code: "mxtong-default"
      name: "麦讯通短信配置"
      enabled: false
      priority: 1
      access-key: ${MXTONG_ACCESS_KEY}
      secret-key: ${MXTONG_SECRET_KEY}
```

### 2.1 公共配置字段

四个供应商配置都继承 `SmsProperty.BaseConfig`（`BaseConfig` 继承 `AkSkDTO`），公共字段如下：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `false` | 是否启用 |
| `priority` | 无 | 优先级（数字越小优先级越高，用于自动故障转移） |
| `access-key` | 无 | 供应商访问密钥 |
| `secret-key` | 无 | 供应商密钥 |

阿里云额外包含：

- `sign-name`：短信签名名称；
- `sts.endpoint`：STS 服务地址；
- `sts.role-arn`：RAM 角色 ARN；
- `sts.policy`：可选权限策略；
- `sts.access-key`、`sts.secret-key`、`sts.security-token`、`sts.expiration`：临时凭证相关字段。

> 说明：短信模板内容（`templateContent`）不属于配置，而是由调用方在发送时通过 DTO 传入，实现"发送内容"与"发送渠道"的职责分离。

## 3. 使用方式

### 3.1 注入策略工厂

统一注入 `SmsExecuteHandlerFactory` 即可使用短信发送能力，无需关心具体供应商：

```java
@Resource
private SmsExecuteHandlerFactory smsExecuteHandlerFactory;
```

### 3.2 发送验证码

```java
SmsSendCaptchaDTO dto = SmsSendCaptchaDTO.builder()
    .phoneNumber("13800138000")
    .templateContent("您的验证码是${code}，${timeout}分钟内有效。")
    .captchaLength(6)
    .captchaTimeout(10)
    .build();

Optional<SmsSendResultVO> result = smsExecuteHandlerFactory.smsSendCaptcha(dto);
```

参数含义：

- `phoneNumber`：目标手机号（必填）；
- `templateContent`：短信模板内容（必填，业务模板内容）；
- `signName`：短信签名（可选，部分服务商需要，如阿里云）；
- `captchaLength`：验证码长度，默认 6 位；
- `captchaTimeout`：验证码有效时间，单位分钟，默认 10 分钟。

`SmsSendResultVO` 返回：

- `bizNumber`：业务流水号；
- `phoneNumber`：手机号；
- `captcha`：生成的验证码；
- `expireTime`：验证码过期时间。

验证码发送成功后，业务侧应使用 `bizNumber`、手机号和验证码完成后续校验，并对发送频率、同手机号并发请求和错误次数进行限制。不要把验证码直接写入普通业务日志或返回给前端。

### 3.3 批量发送模板短信

```java
SmsSendDTO dto = SmsSendDTO.builder()
    .phoneNumbers(List.of("13800000001", "13800000002"))
    .templateContent("尊敬的${name}，您的订单${orderNo}已发货。")
    .templateParams(List.of("张三", "订单号20260825001"))
    .phoneNumberGroupSize(100)
    .build();

Optional<List<SmsSendBatchResultVO>> result = smsExecuteHandlerFactory.smsSend(dto);
```

参数含义：

- `phoneNumbers`：手机号集合（必填）；
- `templateContent`：短信模板内容（必填，业务模板内容）；
- `signName`：短信签名（可选，部分服务商需要，如阿里云）；
- `templateParams`：模板参数集合，可选；
- `phoneNumberGroupSize`：每组手机号数量，默认 100。

返回值 `List<SmsSendBatchResultVO>` 每项包含：

- `bizNumber`：该批次业务流水号；
- `phoneNumbers`：该批次手机号集合；
- `templateParams`：该批次模板参数。

分组大小应结合供应商单次请求限制设置，不要一次传入无限大的手机号集合。

## 4. 自动故障转移

### 4.1 故障转移机制

`SmsExecuteHandlerFactory` 内置自动故障转移能力，发送失败时自动切换到下一个可用策略：

- **优先切换提供商**：同一提供商的多个配置共享基础设施（账号体系、网关、网络），一个配置出问题，其他配置大概率也有问题，因此优先切换到不同提供商；
- **再切换同提供商配置**：在同一提供商内按 `priority` 优先级依次尝试；
- **健康检查**：发送前检查策略的 `enabled` 状态，禁用的策略自动跳过。

### 4.2 故障转移开关

通过 `enabled-failover` 配置控制（默认开启）：

- `true`：同一提供商的所有健康配置都参与故障转移；
- `false`：每个提供商只使用优先级最高的一个配置。

### 4.3 返回值语义

所有发送方法返回 `Optional`：

- `Optional.of(result)`：发送成功；
- `Optional.empty()`：全部策略发送失败。

### 4.4 策略管理接口

`SmsExecuteHandlerFactory` 提供以下策略管理接口：

```java
// 根据短信类型获取优先级最高的可用策略
SmsExecuteHandler handler = smsExecuteHandlerFactory.getHandler(SmsTypeEnum.ALIYUN);

// 根据短信类型和配置编码获取指定策略
SmsExecuteHandler handler = smsExecuteHandlerFactory.getHandler(SmsTypeEnum.ALIYUN, "aliyun-default");

// 获取所有可用的短信策略（按优先级排序）
List<SmsExecuteHandler> handlers = smsExecuteHandlerFactory.getAvailableHandlers();

// 获取所有可用的短信类型
List<SmsTypeEnum> providers = smsExecuteHandlerFactory.getAvailableProviders();

// 根据短信类型和配置编码获取短信配置
SmsProperty.BaseConfig config = smsExecuteHandlerFactory.getConfig(SmsTypeEnum.ALIYUN, "aliyun-default");
```

## 5. 参数校验

策略实现类在发送前对必填参数进行校验，参数不合法时抛出 `IllegalArgumentException`：

- `smsSendCaptcha`：`entity` 非空、`phoneNumber` 非空、`templateContent` 非空；
- `smsSend`：`entity` 非空、`phoneNumbers` 非空、`templateContent` 非空。

## 6. 使用注意事项

- 每个配置必须有唯一的 `code`，用于标识和获取对应的短信处理器；
- 配置的 `enabled` 为 `false` 时，对应的策略不会被创建；
- 供应商服务只有在对应配置的 `enabled=true` 时才可用；
- 短信模板内容（`templateContent`）由调用方传入，不属于配置，切换供应商时模板内容保持一致；
- AK/SK、STS Role ARN 和临时令牌应通过环境变量、配置中心或密钥管理系统提供；
- 生产环境应对验证码发送增加接口鉴权、图形验证码、手机号频率限制和 IP 限制；
- 不要把验证码、完整手机号和供应商密钥写入日志；
- 批量发送应合理设置 `phoneNumberGroupSize`，并根据 `SmsSendBatchResultVO.bizNumber` 做发送记录和结果追踪；
- 本模块只负责调用供应商和返回发送结果，验证码校验、业务状态、重试策略和发送记录由业务层负责。

## 7. 扩展开发

### 7.1 添加新的短信供应商

如需添加新的短信供应商，按以下步骤操作：

1. **创建配置类**：在 `SmsProperty` 中添加新的配置类，继承 `BaseConfig`；

2. **创建策略实现类**：实现 `SmsExecuteHandler` 接口，继承 `AbstractBaseServiceImpl`；

```java
public class SmsXxxExecuteHandler extends AbstractBaseServiceImpl<SmsProperty.XxxConfig> 
    implements SmsExecuteHandler {
    
    public SmsXxxExecuteHandler(XxxConfig config) {
        this.setConfig(config);
    }
    
    @Override
    public SmsTypeEnum getProvider() {
        return SmsTypeEnum.XXX;
    }
    
    @Override
    public String getCode() {
        return this.getConfig().getCode();
    }
    
    @Override
    public Optional<SmsSendResultVO> smsSendCaptcha(SmsSendCaptchaDTO entity) {
        // 实现验证码发送逻辑
    }
    
    @Override
    public Optional<List<SmsSendBatchResultVO>> smsSend(SmsSendDTO entity) {
        // 实现批量短信发送逻辑
    }
}
```

3. **注册策略 Bean**：在 `SmsConfiguration` 中添加策略注册方法；

```java
@Bean
public List<SmsExecuteHandler> xxxSmsExecuteHandlers(SmsProperty property) {
    List<SmsExecuteHandler> handlers = new ArrayList<>(10);
    Set<String> codes = new HashSet<>(16);
    property.getXxx().stream()
        .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
        .filter(config -> codes.add(config.getCode()))
        .forEach(config -> {
            log.info("Initializing Xxx SMS strategy [code={}]", config.getCode());
            handlers.add(new SmsXxxExecuteHandler(config));
        });
    return handlers;
}
```

4. **添加枚举值**：在 `SmsTypeEnum` 中添加新的供应商类型。

### 7.2 策略生命周期

- 策略实例在 Spring 容器启动时创建，每个启用的配置对应一个策略实例；
- 策略实例通过 `SmsExecuteHandlerFactory` 统一管理，支持按提供商类型和配置编码查找；
- 策略的健康状态通过 `BaseService.isHealthy()` 检查，默认检查配置的 `enabled` 状态；
- 策略的优先级通过 `BaseService.getPriority()` 获取，默认返回配置的 `priority` 值。
