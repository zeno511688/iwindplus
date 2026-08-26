# XXL-Job 执行器与监控模块（iwindplus-base-xxl-job）

本模块对 XXL-Job Spring Executor 做自动配置，并为所有 `@XxlJob` 标注的方法增加 Micrometer Observation：

```text
XXL-Job 调度中心
       │ HTTP
       ▼
XxlJobSpringExecutor
       │
       ▼
@XxlJob 方法
       │
       ▼
ObservationExecutor
       │
       └── observation: xxl.job.execute
           low-cardinality: job.name
```

模块不负责创建调度任务、不提供业务 Job Handler，也不替代 XXL-Job 调度中心。

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-xxl-job</artifactId>
</dependency>
```

自动配置默认开启，依赖项目中的 XXL-Job 客户端和 `iwindplus-base-monitor` 的 `ObservationExecutor`。

## 2. 配置

```yaml
xxl-job:
  enabled: true
  admin:
    addresses: http://127.0.0.1:8080/xxl-job-admin
    access-token: default_token
  executor:
    app-name: iwindplus-demo
    addresses: http://192.168.1.10:9999
    ip: 192.168.1.10
    port: 9999
    log-path: /data/logs/xxl-job
    log-retention-days: 30
```

| 配置项 | 是否必填 | 说明 |
|---|---|---|
| `xxl-job.enabled` | 否 | 是否启用模块，默认 `true` |
| `xxl-job.admin.addresses` | 是 | XXL-Job 调度中心地址 |
| `xxl-job.admin.access-token` | 否 | 调度中心访问 Token |
| `xxl-job.executor.app-name` | 否 | 执行器名称，未配置时使用 Spring 应用名 |
| `xxl-job.executor.addresses` | 否 | 执行器注册地址 |
| `xxl-job.executor.ip` | 否 | 执行器 IP |
| `xxl-job.executor.port` | 否 | 执行器端口 |
| `xxl-job.executor.log-path` | 否 | XXL-Job 执行日志目录 |
| `xxl-job.executor.log-retention-days` | 否 | 执行日志保留天数 |

源码会将以上配置直接设置到 `XxlJobSpringExecutor`：

- `admin.addresses` → `setAdminAddresses`；
- `admin.access-token` → `setAccessToken`；
- `executor.app-name` → `setAppname`；
- `executor.addresses` → `setAddress`；
- `executor.ip` → `setIp`；
- `executor.port` → `setPort`；
- `executor.log-path` → `setLogPath`；
- `executor.log-retention-days` → `setLogRetentionDays`。

未配置 `executor.app-name` 时自动使用 `SpringUtil.getApplicationName()`。

## 3. 定义 Job Handler

使用 XXL-Job 原生 `@XxlJob` 注解定义任务：

```java
@Component
public class DemoJob {

    @XxlJob("syncUserJob")
    public void syncUser(String param) {
        userService.sync(param);
    }
}
```

在 XXL-Job 管理后台创建任务时，JobHandler 填写注解值：

```text
syncUserJob
```

方法签名和参数处理遵循 XXL-Job 官方执行器规则，业务方法可以根据项目使用的 XXL-Job 版本接收任务参数或使用上下文工具读取参数。本模块不会改写任务参数，也不会处理任务返回值。

## 4. 注册流程

应用启动后，自动配置创建 `XxlJobSpringExecutor`。执行器启动后向调度中心注册：

```text
启动应用
   │
   ▼
读取 xxl-job 配置
   │
   ▼
创建 XxlJobSpringExecutor
   │
   ▼
执行器向 admin.addresses 注册
   │
   ▼
调度中心按 JobHandler 调度 @XxlJob 方法
```

`xxl-job.enabled=false` 时，整个 `XxlJobConfiguration` 不生效，不会创建执行器和 Observation 切面：

```yaml
xxl-job:
  enabled: false
```

## 5. Observation 监控

模块对带 `@XxlJob` 的方法织入环绕切面，Observation 名称固定为：

```text
xxl.job.execute
```

并写入低基数 Key：

```text
job.name = @XxlJob.value()
```

示例：

```java
@XxlJob("syncUserJob")
public void syncUser(String param) {
    // Observation 中的 job.name 为 syncUserJob
}
```

任务方法正常返回时 Observation 正常结束；任务方法抛出异常时，异常沿 XXL-Job 执行器处理流程继续传播，同时 Observation 记录异常。

## 6. 与 XXL-Job 管理后台配合

应用配置与管理后台需要保持一致：

| 应用配置 | 管理后台对应信息 |
|---|---|
| `executor.app-name` | 执行器名称/AppName |
| `executor.ip` | 执行器地址 |
| `executor.port` | 执行器端口 |
| `@XxlJob("syncUserJob")` | JobHandler |
| `admin.addresses` | 调度中心地址 |

建议先确认执行器已经在管理后台在线，再创建调度任务。执行器端口必须可被调度中心访问；容器部署时需要正确配置注册地址、IP 和端口。

## 7. 使用注意事项

- `admin.addresses` 是调度中心地址，不是执行器注册地址；
- `executor.addresses` 是完整注册地址，配置后 XXL-Job 会优先使用该地址；
- 不配置 `executor.app-name` 时使用 Spring 应用名，多个实例应保证执行器注册策略符合部署规划；
- `@XxlJob` 的 value 必须与管理后台 JobHandler 完全一致；
- 本模块不包含 XXL-Job Admin，必须单独部署调度中心；
- 本模块不负责失败重试、分片参数、路由策略和阻塞策略，这些由 XXL-Job 管理后台和执行器原生能力负责；
- Observation 依赖 `ObservationExecutor`，需要同时引入和正确配置监控基础模块；
- Job 名称会作为低基数 Observation 标签使用，不要动态拼接用户 ID、订单号等高基数字段；
- 生产环境应配置独立执行日志目录，并根据磁盘容量设置日志保留天数。
