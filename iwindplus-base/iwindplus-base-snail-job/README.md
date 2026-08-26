# Snail-Job 客户端增强模块（iwindplus-base-snail-job）

本模块在 Snail-Job 客户端基础上提供三项自动配置能力：

1. 自动启用 `@EnableSnailJob`；
2. 为 `@JobExecutor` 方法增加 Observation；
3. 监听 Snail 客户端启动事件，将指定级别以上的 Logback 根日志发送到 Snail-Job。

```text
应用启动
   │
   ▼
@EnableSnailJob
   │
   ├── Snail 客户端启动
   │       └── 注册 SNAIL_LOG_APPENDER 到 Logback Root Logger
   │
   └── @JobExecutor 方法执行
           └── Observation: snail.job.execute
               job.name = @JobExecutor.name()
```

本模块不定义 Snail-Job 的任务业务接口，也不替代 Snail-Job 官方客户端配置。调度中心、执行器和任务参数配置请按照 Snail-Job 客户端版本的官方方式完成。

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-snail-job</artifactId>
</dependency>
```

模块自动配置默认开启，并依赖：

- Snail-Job 客户端的 `@EnableSnailJob`；
- Snail-Job 的 `@JobExecutor`；
- `iwindplus-base-monitor` 提供的 `ObservationExecutor`；
- Logback（只有 Logback 相关类存在时才注册日志 Appender）。

## 2. 模块配置

```yaml
snail-job:
  enabled: true
  logback:
    enabled: true
    level: ERROR
```

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `snail-job.enabled` | `true` | 是否启用整个模块 |
| `snail-job.logback.enabled` | `true` | 是否启用 Snail Logback Appender |
| `snail-job.logback.level` | `ERROR` | 转发到 Snail 的最低日志级别 |

支持的级别遵循 Logback `Level.toLevel` 规则，例如：

```yaml
snail-job:
  logback:
    level: WARN
```

配置为 `WARN` 时，WARN、ERROR 等级别日志会被接受，INFO 和 DEBUG 会被过滤。

关闭整个模块：

```yaml
snail-job:
  enabled: false
```

关闭日志转发但保留 Snail-Job 客户端和任务监控：

```yaml
snail-job:
  enabled: true
  logback:
    enabled: false
```

## 3. 定义 Snail-Job 任务

使用 Snail-Job 客户端提供的 `@JobExecutor` 定义任务：

```java
@Component
public class DemoJob {

    @JobExecutor(name = "syncUserJob")
    public void syncUser() {
        userService.sync();
    }
}
```

任务方法的参数、返回值和调度配置遵循当前 Snail-Job 客户端版本的规则。本模块不会修改方法参数和返回值，只在方法外围增加监控。

## 4. Observation 监控

每个被 `@JobExecutor` 标注的方法都会被 `SnailJobObservationAspect` 拦截：

```text
Observation 名称：snail.job.execute
低基数标签：job.name
标签值：@JobExecutor.name()
```

示例：

```java
@JobExecutor(name = "syncUserJob")
public void syncUser() {
    // job.name = syncUserJob
}
```

任务执行成功时 Observation 正常结束；任务方法抛出异常时，异常继续向 Snail-Job 客户端传播，同时 Observation 记录该异常。

不要将用户 ID、订单号、批次号等动态值放入 Job 名称，否则会造成高基数监控标签。`@JobExecutor.name` 应保持稳定。

## 5. Logback 日志转发

当以下条件同时满足时，模块会注册日志 Appender：

1. `snail-job.enabled=true`；
2. `snail-job.logback.enabled=true`；
3. Logback `LoggerContext` 和 `SnailLogbackAppender` 存在；
4. Snail 客户端发布 `SnailClientStartingEvent`。

模块将 Appender 加到 Logback 根 Logger：

```text
Root Logger
     │
     ├── 控制台/文件 Appender
     │
     └── SNAIL_LOG_APPENDER
             │
             └── 按 level 过滤并发送到 Snail-Job
```

重复收到启动事件时，如果已存在名称为 `SNAIL_LOG_APPENDER` 的 Appender，模块不会重复注册。

应用优雅关闭时会：

1. 从 Root Logger 移除 `SNAIL_LOG_APPENDER`；
2. 停止 Appender；
3. 清理关闭动作。

## 6. Snail-Job 官方配置

本模块的 `SnailJobProperty` 只管理 `enabled` 和 Logback 转发配置，不包含 Snail-Job 服务地址、应用名称、执行器地址等官方客户端属性。因此这些配置应按照项目引入的 Snail-Job Starter 版本进行配置，不要误写到本模块属性下。

示例结构仅表示本模块负责的部分：

```yaml
snail-job:
  enabled: true
  logback:
    enabled: true
    level: ERROR

# Snail-Job 官方客户端配置
# 按实际客户端版本填写，不由本模块解析
```

## 7. 日志级别建议

| 场景 | 建议级别 | 说明 |
|---|---|---|
| 只关注任务异常 | `ERROR` | 默认配置，噪声较少 |
| 关注任务警告和异常 | `WARN` | 会增加发送量 |
| 排查任务执行过程 | 临时使用 `INFO` | 生产环境不建议长期启用 |
| 调试客户端问题 | 临时使用 `DEBUG` | 需关注日志量和敏感信息 |

日志转发到第三方平台前，应检查日志中是否包含密码、Token、身份证号、手机号等敏感信息。

## 8. 使用注意事项

- `snail-job.enabled=false` 时，不会启用 `@EnableSnailJob`、Observation 切面和 Logback 配置；
- `snail-job.logback.enabled=false` 只关闭日志 Appender，不影响任务客户端；
- 日志 Appender 添加到 Root Logger，所有满足级别的应用日志都可能被转发；
- 模块只在 `SnailClientStartingEvent` 后注册日志 Appender，过早阶段的启动日志不会被转发；
- Appender 名称固定为 `SNAIL_LOG_APPENDER`，模块会自动避免重复注册；
- 日志发送失败行为由 Snail-Job 官方 `SnailLogbackAppender` 实现决定；
- `@JobExecutor` 方法的任务参数和返回值不要根据本模块文档自行推断，应以当前 Snail-Job 客户端版本为准；
- Observation 依赖 `ObservationExecutor`，需要同时引入监控基础模块；
- 生产环境建议使用 `ERROR` 或 `WARN`，并控制日志内容中的敏感信息和堆栈长度。
