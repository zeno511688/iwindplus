# 邮件模块（iwindplus-base-mail）

邮件模块提供邮件发送能力，支持多配置管理、自动故障转移、重试机制等功能。

## 功能特性

- ✅ 多配置管理（支持多个 SMTP 服务器配置）
- ✅ 自动故障转移（按优先级自动切换）
- ✅ 重试机制（指数退避策略）
- ✅ 附件支持
- ✅ HTML 邮件支持
- ✅ 抄送/密送支持
- ✅ 响应式编程（Reactor）

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-mail</artifactId>
</dependency>
```

## 架构设计

```text
业务代码
   │
   ▼
MailHandlerFactory（策略工厂）
   │  getHandler(code)           → 获取指定配置的执行器
   │  send(MailDTO)              → 自动故障转移发送
   │  getAvailableHandlers()     → 获取所有可用执行器
   ▼
MailExecuteHandler（策略接口）
   │  getCode()                  → 获取配置编码
   │  send(MailDTO)              → 发送邮件
   │  isHealthy()                → 健康检查
   │  getPriority()              → 获取优先级
   ▼
SpringMailExecuteHandler（Spring Mail 实现）
   │
   └── JavaMailSenderImpl（Spring Mail 核心）
```

## 核心接口

### MailExecuteHandler

邮件策略接口，定义统一的邮件操作方法：

```java
public interface MailExecuteHandler extends BaseService {
    
    /**
     * 获取配置编码
     */
    String getCode();
    
    /**
     * 发送邮件
     */
    Mono<MailVO> send(MailDTO entity);
}
```

### BaseService

基础服务接口，提供健康检查和优先级管理：

```java
public interface BaseService {
    
    /**
     * 健康检查
     */
    default boolean isHealthy() {
        return true;
    }
    
    /**
     * 获取优先级（数字越小优先级越高）
     */
    default int getPriority() {
        return Integer.MAX_VALUE;
    }
}
```

### MailHandlerFactory

策略工厂，负责管理和路由邮件策略：

```java
public class MailHandlerFactory {
    
    /**
     * 根据配置编码获取邮件执行器
     */
    public MailExecuteHandler getHandler(String code);
    
    /**
     * 根据配置编码获取邮件配置
     */
    public MailProperty.MailConfig getConfig(String code);
    
    /**
     * 发送邮件（自动故障转移）
     */
    public Mono<MailVO> send(MailDTO entity);
    
    /**
     * 获取所有可用（健康）的邮件策略，按优先级排序
     */
    public List<MailExecuteHandler> getAvailableHandlers();
}
```

## 配置

### 基础配置

```yaml
mail:
  enabled: true
  enabled-failover: true
  configs:
    - code: "default"
      name: "默认邮件配置"
      enabled: true
      priority: 1
      host: smtp.example.com
      port: 587
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
      protocol: smtp
      default-encoding: UTF-8
      nick-name: 系统通知
      enable-retry: true
      period: 5s
      max-period: 3600s
      max-attempts: 5
      properties:
        mail.smtp.auth: true
        mail.smtp.starttls.enable: true
        mail.smtp.ssl.trust: smtp.example.com
```

### 多配置示例

```yaml
mail:
  enabled-failover: true
  configs:
    # 默认邮件配置（优先级最高）
    - code: "default"
      name: "默认邮件配置"
      enabled: true
      priority: 1
      host: smtp.example.com
      port: 587
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
      nick-name: 系统通知
      properties:
        mail.smtp.auth: true
        mail.smtp.starttls.enable: true
    
    # 营销邮件配置（优先级次之）
    - code: "marketing"
      name: "营销邮件配置"
      enabled: true
      priority: 2
      host: smtp.marketing.example.com
      port: 465
      username: ${MAIL_MARKETING_USERNAME}
      password: ${MAIL_MARKETING_PASSWORD}
      nick-name: 营销推送
      properties:
        mail.smtp.auth: true
        mail.smtp.ssl.enable: true
    
    # 备用邮件配置（优先级最低）
    - code: "backup"
      name: "备用邮件配置"
      enabled: true
      priority: 3
      host: smtp.backup.example.com
      port: 465
      username: ${MAIL_BACKUP_USERNAME}
      password: ${MAIL_BACKUP_PASSWORD}
      nick-name: 系统通知
      properties:
        mail.smtp.auth: true
        mail.smtp.ssl.enable: true
```

### 配置字段说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `enabled-failover` | `true` | 是否启用自动故障转移（全局开关） |
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `true` | 是否启用 |
| `priority` | 无 | 优先级（数字越小优先级越高，用于自动故障转移） |
| `host` | 无 | SMTP 服务器主机 |
| `port` | 无 | SMTP 服务器端口 |
| `username` | 无 | 用户名 |
| `password` | 无 | 密码 |
| `protocol` | `smtp` | 协议 |
| `default-encoding` | `UTF-8` | 默认编码 |
| `nick-name` | 无 | 发件人昵称（可选） |
| `enable-retry` | `true` | 是否开启重试 |
| `period` | `5s` | 初始间隔时间 |
| `max-period` | `3600s` | 最大重试间隔时间 |
| `max-attempts` | `5` | 最大重试次数 |
| `properties` | 无 | 其他参数（如 SSL、TLS 配置） |

### 常见 SMTP 服务器配置

#### QQ 邮箱

```yaml
mail:
  configs:
    - code: "qq"
      host: smtp.qq.com
      port: 587
      username: your@qq.com
      password: 授权码
      properties:
        mail.smtp.auth: true
        mail.smtp.starttls.enable: true
```

#### 163 邮箱

```yaml
mail:
  configs:
    - code: "163"
      host: smtp.163.com
      port: 465
      username: your@163.com
      password: 授权码
      properties:
        mail.smtp.auth: true
        mail.smtp.ssl.enable: true
```

#### Gmail

```yaml
mail:
  configs:
    - code: "gmail"
      host: smtp.gmail.com
      port: 587
      username: your@gmail.com
      password: 应用专用密码
      properties:
        mail.smtp.auth: true
        mail.smtp.starttls.enable: true
```

#### 阿里云企业邮箱

```yaml
mail:
  configs:
    - code: "aliyun"
      host: smtp.qiye.aliyun.com
      port: 465
      username: your@company.com
      password: 密码
      properties:
        mail.smtp.auth: true
        mail.smtp.ssl.enable: true
```

## 使用方式

### 注入策略工厂

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailHandlerFactory mailHandlerFactory;
}
```

### 发送邮件（自动故障转移）

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailHandlerFactory mailHandlerFactory;

    /**
     * 发送邮件（自动故障转移）
     * 
     * 按优先级最高的配置发送，失败则自动切换到下一个优先级配置
     */
    public Mono<MailVO> sendMail(MailDTO mailDTO) {
        return mailHandlerFactory.send(mailDTO);
    }
}
```

### 发送邮件（指定配置）

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailHandlerFactory mailHandlerFactory;

    /**
     * 使用指定配置发送邮件
     */
    public Mono<MailVO> sendMailByCode(String code, MailDTO mailDTO) {
        MailExecuteHandler handler = mailHandlerFactory.getHandler(code);
        return handler.send(mailDTO);
    }
}
```

### 发送简单邮件

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailHandlerFactory mailHandlerFactory;

    /**
     * 发送简单文本邮件
     */
    public Mono<MailVO> sendSimpleMail(String to, String subject, String content) {
        MailDTO mailDTO = MailDTO.builder()
            .subject(subject)
            .content(content)
            .tos(List.of(to))
            .html(false)
            .build();
        
        return mailHandlerFactory.send(mailDTO);
    }
}
```

### 发送 HTML 邮件

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailHandlerFactory mailHandlerFactory;

    /**
     * 发送 HTML 邮件
     */
    public Mono<MailVO> sendHtmlMail(String to, String subject, String htmlContent) {
        MailDTO mailDTO = MailDTO.builder()
            .subject(subject)
            .content(htmlContent)
            .tos(List.of(to))
            .html(true)
            .build();
        
        return mailHandlerFactory.send(mailDTO);
    }
}
```

### 发送带附件的邮件

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailHandlerFactory mailHandlerFactory;

    /**
     * 发送带附件的邮件
     */
    public Mono<MailVO> sendMailWithAttachment(String to, String subject, String content, 
                                                byte[] data, String filename) {
        UploadByteDTO attachment = UploadByteDTO.builder()
            .sourceFileName(filename)
            .data(ArrayUtil.wrap(data))
            .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .build();
        
        MailDTO mailDTO = MailDTO.builder()
            .subject(subject)
            .content(content)
            .tos(List.of(to))
            .attachments(List.of(attachment))
            .build();
        
        return mailHandlerFactory.send(mailDTO);
    }
}
```

### 发送抄送/密送邮件

```java
@Service
@RequiredArgsConstructor
public class MailService {

    private final MailHandlerFactory mailHandlerFactory;

    /**
     * 发送抄送/密送邮件
     */
    public Mono<MailVO> sendMailWithCcBcc(String to, String cc, String bcc, 
                                          String subject, String content) {
        MailDTO mailDTO = MailDTO.builder()
            .subject(subject)
            .content(content)
            .tos(List.of(to))
            .ccs(StringUtils.isNotBlank(cc) ? List.of(cc) : null)
            .bccs(StringUtils.isNotBlank(bcc) ? List.of(bcc) : null)
            .build();
        
        return mailHandlerFactory.send(mailDTO);
    }
}
```

### Controller 示例

```java
@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailHandlerFactory mailHandlerFactory;

    /**
     * 发送邮件（自动故障转移）
     */
    @PostMapping("/send")
    public Mono<MailVO> send(@RequestBody @Valid MailDTO mailDTO) {
        return mailHandlerFactory.send(mailDTO);
    }

    /**
     * 使用指定配置发送邮件
     */
    @PostMapping("/send/{code}")
    public Mono<MailVO> sendByCode(@PathVariable String code, @RequestBody @Valid MailDTO mailDTO) {
        MailExecuteHandler handler = mailHandlerFactory.getHandler(code);
        return handler.send(mailDTO);
    }
}
```

## 请求 DTO

### MailDTO

邮件发送数据传输对象：

```java
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MailDTO implements Serializable {

    /**
     * 邮件主题（必填）
     */
    @NotBlank(message = "{subject.notEmpty}")
    private String subject;

    /**
     * 邮件内容（必填）
     */
    @NotBlank(message = "{content.notEmpty}")
    private String content;

    /**
     * 收件人（必填）
     */
    @NotEmpty(message = "{tos.notEmpty}")
    private List<String> tos;

    /**
     * 抄送人（可选）
     */
    private List<String> ccs;

    /**
     * 密送人（可选）
     */
    private List<String> bccs;

    /**
     * 附件（可选）
     */
    private List<UploadByteDTO> attachments;

    /**
     * 是否是 HTML 方式（默认 false）
     */
    private Boolean html;
}
```

### UploadByteDTO

附件数据传输对象：

```java
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UploadByteDTO implements Serializable {

    /**
     * 源文件名
     */
    private String sourceFileName;

    /**
     * 文件数据（字节数组）
     */
    private Byte[] data;

    /**
     * 内容类型
     */
    private String contentType;
}
```

## 响应 VO

### MailVO

邮件发送结果视图对象：

```java
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MailVO implements Serializable {

    /**
     * 业务流水号
     */
    private String bizNumber;

    /**
     * 发送结果（true 成功，false 失败）
     */
    private Boolean result;

    /**
     * 发送次数
     */
    private Integer sendCount;

    /**
     * 错误信息（失败时）
     */
    private String errorMsg;

    /**
     * 创建成功结果
     */
    public static MailVO ok(String bizNumber, Integer sendCount);

    /**
     * 创建失败结果
     */
    public static MailVO fail(String bizNumber, Integer sendCount, String errorMsg);
}
```

## 重试机制

当 `enable-retry=true` 时，邮件发送失败会自动重试：

- **初始间隔**：`period`（默认 5 秒）
- **最大间隔**：`max-period`（默认 3600 秒）
- **最大次数**：`max-attempts`（默认 5 次）
- **策略**：指数退避（Exponential Backoff），间隔时间会逐步增加

### 重试示例

```yaml
mail:
  configs:
    - code: "default"
      host: smtp.example.com
      port: 587
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}
      enable-retry: true
      period: 5s        # 第 1 次重试间隔 5 秒
      max-period: 60s   # 最大间隔 60 秒
      max-attempts: 3   # 最多重试 3 次
```

重试间隔计算（指数退避 + 抖动）：

```
第 1 次重试：~5 秒
第 2 次重试：~7.5 秒（5 * 1.5）
第 3 次重试：~11.25 秒（7.5 * 1.5）
```

## 自动故障转移

当 `enabled-failover=true` 时，发送邮件会按 `priority` 从小到大依次尝试所有健康配置：

### 故障转移流程

```text
1. 获取所有健康（isHealthy()=true）的邮件执行器
2. 按 priority 从小到大排序
3. 依次尝试发送邮件：
   - 若发送成功，返回结果
   - 若发送失败，切换到下一个执行器
4. 全部失败时返回 Mono.empty()
```

### 配置示例

```yaml
mail:
  enabled-failover: true  # 启用自动故障转移
  configs:
    # 主配置（优先级最高）
    - code: "primary"
      priority: 1
      host: smtp.primary.com
      # ...
    
    # 备用配置（优先级次之）
    - code: "backup"
      priority: 2
      host: smtp.backup.com
      # ...
```

### 故障转移策略

| 场景 | `enabled-failover` | 行为 |
|------|-------------------|------|
| 启用故障转移 | `true` | 按优先级依次尝试所有健康配置 |
| 禁用故障转移 | `false` | 仅使用优先级最高的健康配置 |

## 健康检查

每个邮件执行器都实现了 `isHealthy()` 方法，用于判断是否可用：

```java
@Override
public boolean isHealthy() {
    return Boolean.TRUE.equals(this.getConfig().getEnabled());
}
```

- `enabled=true`：执行器健康，可参与发送
- `enabled=false`：执行器不健康，不参与发送

## 优先级管理

每个邮件执行器都实现了 `getPriority()` 方法，用于故障转移时的排序：

```java
@Override
public int getPriority() {
    return Optional.ofNullable(this.getConfig().getPriority())
        .orElse(Integer.MAX_VALUE);
}
```

- 数字越小，优先级越高
- 未配置时默认为 `Integer.MAX_VALUE`（最低优先级）

## 最佳实践

### 1. 配置管理

```yaml
# ✅ 推荐：使用环境变量注入敏感信息
mail:
  configs:
    - code: "default"
      username: ${MAIL_USERNAME}
      password: ${MAIL_PASSWORD}

# ❌ 不推荐：明文配置密码
mail:
  configs:
    - code: "default"
      username: user@example.com
      password: password123
```

### 2. 多配置管理

```yaml
# ✅ 推荐：配置多个 SMTP 服务器，启用故障转移
mail:
  enabled-failover: true
  configs:
    - code: "primary"
      priority: 1
      host: smtp.primary.com
    - code: "backup"
      priority: 2
      host: smtp.backup.com

# ❌ 不推荐：只配置一个 SMTP 服务器
mail:
  configs:
    - code: "default"
      host: smtp.example.com
```

### 3. 重试配置

```yaml
# ✅ 推荐：配置合理的重试次数和间隔
mail:
  configs:
    - code: "default"
      enable-retry: true
      period: 5s
      max-period: 60s
      max-attempts: 3

# ❌ 不推荐：重试次数过多或间隔过长
mail:
  configs:
    - code: "default"
      enable-retry: true
      period: 30s
      max-period: 3600s
      max-attempts: 10
```

### 4. 异步发送

```java
// ✅ 推荐：使用响应式编程异步发送
public Mono<MailVO> sendMail(MailDTO mailDTO) {
    return mailHandlerFactory.send(mailDTO);
}

// ❌ 不推荐：阻塞等待发送结果
public MailVO sendMail(MailDTO mailDTO) {
    return mailHandlerFactory.send(mailDTO).block();
}
```

### 5. 批量发送

```java
// ✅ 推荐：分批发送，控制频率
public Flux<MailVO> sendBatch(List<MailDTO> mailDTOs) {
    return Flux.fromIterable(mailDTOs)
        .delayElements(Duration.ofSeconds(1))  // 每秒发送一封
        .flatMap(mailHandlerFactory::send);
}

// ❌ 不推荐：一次性发送大量邮件
public Flux<MailVO> sendBatch(List<MailDTO> mailDTOs) {
    return Flux.fromIterable(mailDTOs)
        .flatMap(mailHandlerFactory::send);
}
```

## 注意事项

1. **配置编码唯一性**：每个配置必须有唯一的 `code`，用于标识和获取对应的邮件处理器
2. **配置启用状态**：`enabled=false` 的配置不会被初始化为策略实例
3. **优先级设置**：`priority` 数字越小优先级越高，用于自动故障转移时的发送顺序
4. **敏感信息保护**：邮箱密码、用户名应通过环境变量或密钥管理系统注入
5. **响应式编程**：发送邮件返回 `Mono<MailVO>`，需要在响应式环境中使用
6. **重试机制**：只针对网络异常、SMTP 服务不可用等情况，不针对收件人地址错误等业务异常
7. **附件安全**：附件路径应经过业务校验，避免路径穿越
8. **日志安全**：不要将邮件密码、完整邮件内容写入日志
9. **批量发送**：应合理控制收件人数量，避免被 SMTP 服务器限流
10. **HTML 邮件**：应确保内容安全，避免 XSS 攻击

## 相关模块

- `iwindplus-base-domain`：领域模型模块，提供 DTO、VO 等基础定义
- `iwindplus-base-util`：工具类模块，提供加解密、JSON 序列化等工具
