# iwindplus-base-mail

`iwindplus-base-mail` 基于 Spring Boot Mail，封装 SMTP 邮件发送、HTML 内容、抄送/密送、附件和发送失败重试。

模块自动注册 `MailService`，业务只需要注入服务并提交 `MailDTO`。

## 引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-mail</artifactId>
</dependency>
```

## SMTP 配置

模块配置前缀是 `spring.mail`，基础连接参数继承 Spring Boot 的 `MailProperties`：

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 465
    username: no-reply@example.com
    password: ${MAIL_PASSWORD}
    protocol: smtps
    default-encoding: UTF-8
    test-connection: false
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
```

不要把密码、授权码写入源码或提交到配置仓库，建议使用环境变量、配置中心或密钥管理服务注入。

### 模块扩展配置

| 配置 | 默认值 | 说明 |
|---|---:|---|
| `spring.mail.nick-name` | 空 | 发件人昵称 |
| `spring.mail.enable-retry` | `true` | 是否开启失败重试 |
| `spring.mail.period` | `5s` | 初始重试间隔 |
| `spring.mail.max-period` | `1h` | 最大重试间隔 |
| `spring.mail.max-attempts` | `5` | 最大尝试次数 |
| `spring.mail.properties` | 空 | 额外邮件参数 Map |

`spring.mail.properties` 同时会传给底层 JavaMail 配置，可用于补充供应商特定参数。

## 发送邮件

### MailDTO

```java
MailDTO mail = MailDTO.builder()
    .subject("账户安全提醒")
    .content("您的账户刚刚完成了一次登录。")
    .tos(List.of("user@example.com"))
    .html(false)
    .build();
```

字段说明：

| 字段 | 必填 | 说明 |
|---|---|---|
| `subject` | 是 | 邮件主题 |
| `content` | 是 | 邮件正文 |
| `tos` | 是 | 收件人列表，不能为空 |
| `ccs` | 否 | 抄送人列表 |
| `bccs` | 否 | 密送人列表 |
| `attachments` | 否 | `UploadByteDTO` 附件列表 |
| `html` | 否 | 是否按 HTML 内容发送 |

### 注入并发送

```java
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final MailService mailService;

    public Mono<MailVO> sendNotice(String email) {
        MailDTO mail = MailDTO.builder()
            .subject("验证码通知")
            .content("您的验证码是 <strong>123456</strong>")
            .tos(List.of(email))
            .html(true)
            .build();

        return mailService.send(mail);
    }
}
```

`MailService#send` 返回 `Mono<MailVO>`，调用方需要订阅或在 WebFlux Controller 中直接返回：

```java
@PostMapping("/send")
public Mono<MailVO> send(@Valid @RequestBody MailDTO mail) {
    return mailService.send(mail);
}
```

在 MVC 同步业务中可以使用：

```java
MailVO result = mailService.send(mail).block();
```

不要在 WebFlux 事件循环线程中随意调用 `block()`，响应式接口优先直接返回 `Mono`。

## 抄送和密送

```java
MailDTO mail = MailDTO.builder()
    .subject("周报")
    .content("本周工作内容见附件。")
    .tos(List.of("owner@example.com"))
    .ccs(List.of("manager@example.com"))
    .bccs(List.of("audit@example.com"))
    .build();

return mailService.send(mail);
```

收件人、抄送人和密送人由底层邮件客户端分别设置；业务层不要把密送地址拼到普通收件人列表中。

## 添加附件

附件使用 domain 模块的 `UploadByteDTO`：

```java
UploadByteDTO attachment = UploadByteDTO.builder()
    .fileName("invoice.pdf")
    .content(pdfBytes)
    .contentType("application/pdf")
    .build();

MailDTO mail = MailDTO.builder()
    .subject("发票")
    .content("请查收附件。")
    .tos(List.of("finance@example.com"))
    .attachments(List.of(attachment))
    .build();

return mailService.send(mail);
```

发送前应限制附件数量、单个大小和总大小；附件内容来自用户上传时，需先完成类型、大小和安全检查。

## HTML 邮件

```java
MailDTO mail = MailDTO.builder()
    .subject("欢迎注册")
    .content("<h2>欢迎使用</h2><p>您的账号已经创建成功。</p>")
    .tos(List.of("user@example.com"))
    .html(true)
    .build();
```

HTML 内容来自用户输入时必须进行模板渲染和 XSS 处理，不要直接把未经处理的用户输入拼进邮件 HTML。

## 发送结果

`MailVO` 字段如下：

| 字段 | 说明 |
|---|---|
| `bizNumber` | 本次发送业务流水号 |
| `result` | 是否发送成功 |
| `sendCount` | 已尝试发送次数 |
| `errorMsg` | 失败时的错误信息 |

```java
return mailService.send(mail)
    .doOnNext(result -> {
        if (Boolean.TRUE.equals(result.getResult())) {
            log.info("邮件发送成功，流水号={}", result.getBizNumber());
        } else {
            log.warn("邮件发送失败，流水号={}，原因={}",
                result.getBizNumber(), result.getErrorMsg());
        }
    });
```

发送失败通常通过 `MailVO.fail(...)` 返回，不应只依赖异常判断业务结果。业务可以根据 `result`、`sendCount` 和 `errorMsg` 记录告警或安排补偿。

## 重试行为

开启重试时，模块使用以下配置控制重试：

```yaml
spring:
  mail:
    enable-retry: true
    period: 5s
    max-period: 1h
    max-attempts: 5
```

重试间隔从 `period` 开始，并受 `max-period` 限制；最大尝试次数由 `max-attempts` 控制。需要注意：邮件发送是外部副作用，重试可能导致收件人收到重复邮件，因此业务应：

1. 为业务邮件生成幂等业务流水号；
2. 对验证码、通知和账单邮件区分重试策略；
3. 对已经提交给 SMTP 服务但响应超时的情况保留发送记录；
4. 不要在调用方再套一层无边界重试。

关闭重试：

```yaml
spring:
  mail:
    enable-retry: false
```

## 动态修改配置

`MailService` 继承 `MailBaseConfigService`，可以读取或设置当前配置：

```java
MailProperty config = mailService.getConfig();
config.setNickName("系统通知");
mailService.setConfig(config);
```

动态修改配置只影响当前 `MailService` 使用的配置对象，不等同于持久化配置；生产环境建议通过配置中心统一管理 SMTP 参数。

## 推荐流程

```text
构造 MailDTO
   ↓
校验主题、正文和收件人
   ↓
mailService.send(mail)
   ↓
Mono<MailVO>
   ↓
根据 result 记录发送结果
   ↓
失败时结合业务流水号补偿
```

## 注意事项

1. 必须配置可用 SMTP 服务，否则邮件只能构造成功，实际发送会失败。
2. `tos` 不能为空，主题和正文不能为空。
3. `html` 为 `true` 时正文按 HTML 发送，为 `false` 或未设置时按普通文本处理。
4. 密码、授权码和 SMTP 私钥不得写入日志。
5. 不要把 `Mono<MailVO>` 丢弃不订阅，否则响应式发送链可能不会执行。
6. 附件和正文均应限制大小，避免超过 SMTP 服务限制。
7. 邮件重试可能产生重复发送，必须结合业务幂等记录设计。
8. 该模块只负责邮件发送，不负责邮件模板存储、发送记录持久化和业务补偿任务。

## 相关模块

- `iwindplus-base-domain`：提供 `UploadByteDTO`、基础异常和统一领域对象。
- `iwindplus-base-util`：提供文件、字节和模板处理工具。
- `iwindplus-base-web`：提供 Web 请求和响应集成。
- `iwindplus-base-i18n`：提供动态国际化消息源，可用于生成多语言邮件内容。
