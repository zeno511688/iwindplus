# Nacos 动态国际化模块（iwindplus-base-i18n）

本模块将 Spring Boot 的主 `MessageSource` 替换为 `NacosBundleMessageSource`，从 Nacos 配置中心读取 `.properties` 格式的国际化资源，并通过 Nacos Listener 监听配置变更、刷新本地 Caffeine 缓存。

```text
MessageSource.getMessage(code, args, locale)
                │
                ▼
      计算 basename + Locale 文件名
                │
                ▼
        Nacos ConfigService.getConfig
                │
                ▼
      Caffeine 缓存 Properties
                │
     Nacos 配置变更 Listener
                ▼
        精确刷新对应文件缓存
```

## 1. 生效条件

模块自动配置同时满足以下条件才会生效：

1. `spring.messages.enabled-remote=true`，默认值为 `true`；
2. Spring 容器中存在 `NacosConfigManager`；
3. 应用已正确引入并初始化 Nacos Config。

配置生效后，模块会注册一个 `@Primary MessageSource`，因此业务代码可以继续使用 Spring 标准的 `MessageSource`、`MessageSourceAccessor` 或 `LocaleContextHolder`，不需要直接依赖 Nacos 客户端。

## 2. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-i18n</artifactId>
</dependency>
```

同时需要保证应用已经引入 Nacos 配置中心依赖，并能够注入 `NacosConfigManager`。模块依赖一个名为 `i18nTaskExecutor` 的动态线程池执行器，用于异步处理 Nacos 配置变更通知。

## 3. 配置

```yaml
spring:
  messages:
    enabled-remote: true
    basename: messages
    encoding: UTF-8
    fallback-to-system-locale: false
    cache-duration: 10m
    use-code-as-default-message: false
    always-use-message-format: false
    group: I18N_GROUP
    max-cache-size: 300
```

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `spring.messages.enabled-remote` | `true` | 是否启用 Nacos 远程国际化；设为 `false` 时不注册本模块 MessageSource |
| `spring.messages.basename` | Spring Boot 默认值 | 基础文件名，可配置多个 basename |
| `spring.messages.encoding` | 系统默认字符集 | Nacos Properties 文件编码 |
| `spring.messages.fallback-to-system-locale` | Spring Boot 默认值 | 找不到指定 Locale 时是否回退系统 Locale |
| `spring.messages.cache-duration` | Spring Boot 默认值 | 本地缓存时长；`0s` 表示不设置过期时间 |
| `spring.messages.use-code-as-default-message` | Spring Boot 默认值 | 找不到消息时是否直接返回 code |
| `spring.messages.always-use-message-format` | Spring Boot 默认值 | 是否始终使用 MessageFormat |
| `spring.messages.group` | `I18N_GROUP` | Nacos 配置分组 |
| `spring.messages.max-cache-size` | `300` | Caffeine 最大缓存文件数 |

注意：源码固定使用 `.properties` 作为 Nacos dataId 后缀，不存在 `fileSuffix` 或 `languages` 配置项。

## 4. 在 Nacos 中创建资源

假设配置如下：

```yaml
spring:
  messages:
    basename: messages
    group: I18N_GROUP
```

在当前 Nacos Namespace 下，以 `I18N_GROUP` 分组创建以下配置：

### 4.1 默认语言

Data ID：`messages.properties`

```properties
app.title=iwindplus
user.login.success=Login succeeded
user.greeting=Hello, {0}
```

### 4.2 中文

Data ID：`messages_zh_CN.properties`

```properties
app.title=iwindplus
user.login.success=登录成功
user.greeting=你好，{0}
```

### 4.3 英文

Data ID：`messages_en_US.properties`

```properties
app.title=iwindplus
user.login.success=Login succeeded
user.greeting=Hello, {0}
```

源码会将 `basename` 去掉路径和扩展名后生成 dataId，因此建议直接使用 `messages`、`messages_zh_CN` 这样的命名。Locale 的匹配顺序按具体程度优先：

```text
messages_zh_CN.properties
        │ 找不到
        ▼
messages_zh.properties
        │ 找不到
        ▼
messages.properties
```

对于 `Locale("zh", "CN")`，模块会优先读取 `messages_zh_CN.properties`；对于 `Locale("zh")`，读取 `messages_zh.properties`，再根据 Spring MessageSource 的回退设置处理默认资源。

## 5. 业务代码中读取消息

### 5.1 注入 `MessageSource`

```java
@Resource
private MessageSource messageSource;

public String getMessage(String username, Locale locale) {
    return messageSource.getMessage(
        "user.greeting",
        new Object[] {username},
        locale
    );
}
```

输出示例：

```text
Locale.CHINA  -> 你好，张三
Locale.US     -> Hello, 张三
```

### 5.2 使用当前请求语言

在 Web 请求中可以使用 `LocaleContextHolder`：

```java
Locale locale = LocaleContextHolder.getLocale();
String message = messageSource.getMessage(
    "user.login.success",
    null,
    locale
);
```

当前 Locale 的来源由应用自身的 Spring MVC/WebFlux Locale 配置决定，本模块只负责根据传入的 Locale 加载 Nacos 资源。

### 5.3 参数格式化

消息使用 Java `MessageFormat` 参数：

```properties
order.summary=订单 {0} 共 {1} 件商品，金额 {2,number,#.##}
```

```java
String message = messageSource.getMessage(
    "order.summary",
    new Object[] {"O20250101", 3, 199.5},
    Locale.CHINA
);
```

`MessageSource` 会根据 `always-use-message-format`、`use-code-as-default-message` 等标准 Spring 配置处理参数和缺失消息。

## 6. 动态刷新机制

第一次读取某个 Locale 的消息时，模块会：

1. 计算候选文件名；
2. 调用 Nacos `ConfigService.getConfig(dataId, group, 3000)`；
3. 解析为 `Properties`；
4. 放入 Caffeine 文件缓存；
5. 为该 dataId 注册 Nacos Listener。

Nacos 中配置发生变化后：

```text
Nacos 配置变更
      │
      ▼
Listener.receiveConfigInfo
      │
      ▼
解析新的 Properties
      │
      ▼
替换对应 dataId 的缓存
      │
      ▼
后续 getMessage 读取新内容
```

已缓存的文件会按 `spring.messages.cache-duration` 过期；`cache-duration: 0s` 时不设置写入过期时间，但 Nacos Listener 仍会对配置变更进行精确刷新。

## 7. 多 basename

Spring 的 `basename` 支持多个资源名，使用逗号分隔：

```yaml
spring:
  messages:
    basename: messages,validation,error
```

对应 Nacos 配置：

```text
messages.properties
messages_zh_CN.properties
validation.properties
validation_zh_CN.properties
error.properties
error_zh_CN.properties
```

模块按照 basename 配置顺序查找资源，找到包含目标 code 的 Properties 后停止查找。

## 8. 关闭远程国际化

如果某个应用不使用 Nacos 动态国际化，可以关闭：

```yaml
spring:
  messages:
    enabled-remote: false
```

关闭后本模块不会创建 `NacosBundleMessageSource`，应用回退使用其他 Spring Boot MessageSource 配置。由于 `enabled-remote` 是 `I18nProperty` 的自定义字段，必须写在 `spring.messages` 下，不能写成 `i18n.enabled`。

## 9. 使用注意事项

- Nacos 配置的 Data ID 必须带 `.properties` 后缀，源码不会读取 YAML 或 JSON 国际化文件；
- Nacos 配置分组必须与 `spring.messages.group` 一致，默认是 `I18N_GROUP`；
- Nacos Namespace 必须与应用的 Nacos Config 配置一致；
- 模块仅在存在 `NacosConfigManager` 时生效，单独引入模块但没有 Nacos 配置客户端不会替换 MessageSource；
- 需要提供名为 `i18nTaskExecutor` 的 `DtpExecutor`，否则自动配置无法完成依赖注入；
- Nacos 配置内容使用 Java Properties 语法，特殊字符、冒号、等号和换行应按 Properties 规则转义；
- 消息缓存按文件缓存，不是按消息 code 缓存；一个文件更新会刷新该文件的全部消息；
- 资源不存在或解析失败时，模块返回空 Properties，最终是否显示 code 取决于 `use-code-as-default-message`；
- 应用退出时模块会移除已经注册的 Nacos Listener 并清理本地缓存。
