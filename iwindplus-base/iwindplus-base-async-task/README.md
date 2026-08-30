# 异步任务模块（iwindplus-base-async-task）

本模块把需要异步执行、失败重试或等待外部回调的业务持久化为异步任务。核心入口是 `AsyncTaskExecutor`，支持无子任务的单任务和带子任务的组任务。

## 适用场景

- 第三方接口调用失败后需要自动重试；
- 消息发送、文件处理等不适合阻塞当前请求的业务；
- 一个业务由多个按阶段执行的子任务组成；
- 外部系统先受理、后回调，业务需要等待回调结果。

## 工作流程

```text
submit / submitGroup
        │
        ▼
写入 async_task、async_task_sub
        │ 事务提交后
        ▼
AsyncTaskExecutor 投递任务
        │
        ├── SUCCESS：结束
        ├── FAILED：按 retry.frequency 重试
        └── WAITING：等待 callback 或执行器查询
                              │
                              ▼
                      继续执行 / 进入失败重试
```

## 1. 接入准备

### 1.1 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-async-task</artifactId>
</dependency>
```

实际版本由父工程的 Maven 依赖管理统一控制。

### 1.2 初始化数据库

执行模块内置脚本：

```text
src/main/resources/db/V202311211512__async_task_table.sql
```

脚本创建以下两张表：

- `async_task`：异步任务主任务；
- `async_task_sub`：组任务的子任务。

模块通过 MyBatis Mapper 访问这两张表，并依赖事务保证“任务落库”和后续投递的一致性。

### 1.3 配置 XXL-Job

模块提供 XXL-Job Handler：

```text
asyncTaskJob
```

在调度中心为该 Handler 配置定时触发。它负责处理待执行、重试和超时任务；不是分别配置 `RETRY_JOB`、`RESET_JOB` 两个 Handler。

应用需要正确配置 XXL-Job 执行器地址、访问令牌和任务线程池，具体配置由业务应用的 XXL-Job 模块负责。

## 2. 模块配置

配置前缀为 `async-task`，默认值来自 `AsyncTaskProperty`。

| 配置项 | 默认值 | 用途 |
|---|---:|---|
| `async-task.enabled` | `true` | 是否启用模块 |
| `async-task.enabled-success-delete` | `true` | 任务成功后是否删除数据 |
| `async-task.enabled-success-real-delete` | `true` | 成功删除时是否物理删除 |
| `async-task.enabled-exception-capture` | `true` | 是否截取并保存异常信息 |
| `async-task.exception-capture-length` | `4000` | 异常信息最大保存长度 |
| `async-task.max-page-size` | `10` | Job 每次分页处理的任务数量 |
| `async-task.timeout-seconds` | `120` | 执行租约超时时间；超时任务会被重新处理 |
| `async-task.async-wait-poll-seconds` | `60` | 异步等待任务的轮询间隔 |
| `async-task.async-wait-timeout-seconds` | `1800` | 异步等待最长时间，超时后进入失败流程 |
| `async-task.retry.frequency` | `30s,2m,10m,15m,20m,30m,1h` | 重试间隔序列 |
| `async-task.retry.enabled-unlimited-retry` | `false` | 是否无限重试 |
| `async-task.retry.max-attempts` | `30` | 最大重试次数 |
| `async-task.job.enabled` | `true` | 是否启用内置 Job |
| `async-task.job.max-loop-count` | `100` | 单次 Job 的最大循环次数 |
| `async-task.web.enabled` | `true` | 是否启用模块 Web 接口 |
| `async-task.web.path` | 空 | Web 接口路径前缀 |

示例：

```yaml
async-task:
  enabled: true
  timeout-seconds: 120
  async-wait-poll-seconds: 60
  async-wait-timeout-seconds: 1800
  retry:
    frequency: 30s,2m,10m,15m,20m,30m,1h
    enabled-unlimited-retry: false
    max-attempts: 30
  job:
    enabled: true
    max-loop-count: 100
  web:
    enabled: true
    path: /async-task
```

## 3. 实现主任务 Handler

主任务 Handler 必须是 Spring Bean。`execute` 返回 `AsyncTaskExecuteResultVO`，由返回状态决定任务如何流转。

```java
@Component
public class OrderExportTaskHandler implements AsyncTaskHandler {

    @Override
    public AsyncTaskExecuteResultVO execute(AsyncTaskVO entity) {
        ExportParam param = entity.getParam(ExportParam.class);
        String fileId = export(param);

        return AsyncTaskExecuteResultVO.success(
            Map.of("fileId", fileId));
    }

    @Override
    public AsyncTaskCallbackResultVO executeCallback(AsyncTaskVO entity) {
        // 仅当任务 needCallback=true 时按需实现
        return AsyncTaskCallbackResultVO.waiting();
    }

    @Override
    public void onTaskSuccess(AsyncTaskVO entity) {
        // 可选：任务成功后的业务处理
    }

    @Override
    public void onTaskFail(AsyncTaskVO entity) {
        // 可选：任务失败后的业务处理
    }

    @Override
    public void onTaskAsyncWait(AsyncTaskVO entity) {
        // 可选：任务首次进入异步等待时处理
    }

    @Override
    public void onTaskDiscard(AsyncTaskVO entity) {
        // 可选：重试次数耗尽后的补偿或告警
    }
}
```

`AsyncTaskExecuteResultVO` 的状态方法：

| 方法 | 状态 | 说明 |
|---|---|---|
| `execute()` | `EXECUTE` | 继续保持执行状态 |
| `success()` | `SUCCESS` | 执行成功 |
| `failed()` | `FAILED` | 执行失败，进入重试流程 |
| `waiting()` | `WAITING` | 等待外部回调或后续查询 |

需要保存业务结果时，可以使用带 `Map` 参数的 `success/failed/waiting`，或调用返回对象的 `setResultData(data)`。业务异常也会按模块配置保存。

## 4. 提交单任务

`AsyncTaskSubmitDTO` 的必填业务标识为 `bizName`、`bizKey`、`bizType`，`executorClass` 指定主任务 Handler。`bizNumber` 用于回调和人工重试定位，建议业务侧保证唯一。

```java
@Resource
private AsyncTaskExecutor asyncTaskExecutor;

public AsyncTaskSubmitVO submitExport(ExportParam param) {
    AsyncTaskSubmitDTO command = AsyncTaskSubmitDTO.builder()
        .bizName("订单导出")
        .bizKey("ORDER")
        .bizType("ORDER_EXPORT")
        .bizNumber("EXP-20260825-0001")
        .executorClass(OrderExportTaskHandler.class)
        .needCallback(false)
        .needDisplay(true)
        .build();
    command.setParam(param);

    return asyncTaskExecutor.submit(command);
}
```

提交后框架完成以下工作：

1. 保存主任务；
2. 在事务提交后投递执行；
3. 抢占任务执行权并调用 Handler；
4. 根据 `AsyncTaskExecuteResultVO` 更新状态；
5. 失败时按 `retry.frequency` 计算下一次执行时间。

## 5. 实现子任务 Handler

组任务的业务逻辑由 `AsyncTaskSubHandler` 实现。子任务通过 `executeSub` 返回执行结果，支持 `executeSubCallback` 查询异步结果。

```java
@Component
public class OrderSubmitSubHandler implements AsyncTaskSubHandler {

    @Override
    public AsyncTaskExecuteResultVO executeSub(AsyncTaskSubVO entity) {
        SubmitParam param = entity.getData(SubmitParam.class);
        submitToExternalSystem(param);
        return AsyncTaskExecuteResultVO.waiting();
    }

    @Override
    public AsyncTaskCallbackResultVO executeSubCallback(AsyncTaskSubVO entity) {
        ExternalStatus status = queryExternalStatus(entity);
        if (status.success()) {
            return AsyncTaskCallbackResultVO.success();
        }
        if (status.failed()) {
            return AsyncTaskCallbackResultVO.failed();
        }
        return AsyncTaskCallbackResultVO.waiting();
    }

    @Override
    public void onSubTaskSuccess(AsyncTaskSubVO entity) {
        // 可选
    }

    @Override
    public void onSubTaskFail(AsyncTaskSubVO entity) {
        // 可选
    }

    @Override
    public void onSubTaskAsyncWait(AsyncTaskSubVO entity) {
        // 可选
    }
}
```

前置阶段结果会随 `AsyncTaskSubVO` 提供给后续子任务；业务侧应将外部请求使用的业务流水号保存到 `bizNumber`，以便回调定位。

## 6. 提交组任务

`AsyncTaskGroupSubmitDTO` 由一个主任务 Handler 和多个 `AsyncTaskSubSubmitDTO` 组成。子任务通过 `seq` 和 `stage` 编排：

- `seq` 从 1 开始并保持连续、唯一；
- `stage` 按 `seq` 顺序不能递减；
- 连续相同 `stage` 的子任务可以并行执行；
- 前一阶段未全部成功，不会进入下一阶段；
- `executorClass` 为空的子任务可以作为进度占位，不执行具体业务；
- 需要外部回调的子任务设置 `needCallback(true)`。

```java
AsyncTaskSubSubmitDTO submitSub = AsyncTaskSubSubmitDTO.builder()
    .bizName("提交第三方")
    .bizKey("ORDER")
    .bizType("ORDER_SUBMIT")
    .bizNumber("ORDER-EXT-0001")
    .seq(1)
    .stage(1)
    .executorClass(OrderSubmitSubHandler.class)
    .needCallback(true)
    .needDisplay(true)
    .build();
submitSub.setParam(new SubmitParam(orderId));

AsyncTaskSubSubmitDTO saveSub = AsyncTaskSubSubmitDTO.builder()
    .bizName("保存结果")
    .bizKey("ORDER")
    .bizType("ORDER_SAVE")
    .seq(2)
    .stage(2)
    .executorClass(OrderSaveSubHandler.class)
    .build();

AsyncTaskGroupSubmitDTO group = AsyncTaskGroupSubmitDTO.builder()
    .bizName("订单处理")
    .bizKey("ORDER")
    .bizType("ORDER_PROCESS")
    .bizNumber("ORDER-0001")
    .executorClass(OrderProcessTaskHandler.class)
    .subTasks(List.of(submitSub, saveSub))
    .build();

AsyncTaskSubmitVO result = asyncTaskExecutor.submitGroup(group);
```

## 7. 外部回调

业务收到外部系统通知后，调用 `AsyncTaskExecutor.callback`。主任务可以通过 `id` 或 `bizNumber` 定位；子任务通过 `subTasks` 列表携带各自的 `bizNumber`。

回调结果只允许 `SUCCESS` 或 `FAILED`；`WAITING` 仅用于执行器的轮询返回值，不用于外部完成通知。

```java
AsyncTaskCallbackDTO callback = AsyncTaskCallbackDTO.builder()
    .bizNumber("ORDER-0001")
    .callbackResult(AsyncTaskCallbackResultEnum.SUCCESS)
    .progress(100)
    .build();
callback.setResultData(Map.of("externalId", "EXT-001"));

asyncTaskExecutor.callback(callback);
```

组任务回调示例：

```java
AsyncTaskCallbackDTO callback = AsyncTaskCallbackDTO.builder()
    .bizNumber("ORDER-0001")
    .subTasks(List.of(
        AsyncTaskSubCallbackDTO.builder()
            .bizNumber("ORDER-EXT-0001")
            .callbackResult(AsyncTaskCallbackResultEnum.SUCCESS)
            .progress(100)
            .build()))
    .build();

asyncTaskExecutor.callback(callback);
```

回调 DTO 还支持 `result`、`errorMsg` 和 `costTime` 字段。失败回调建议同时填写 `errorMsg`，便于排查问题。

## 8. 重试和删除

```java
// 仅按主任务定位
asyncTaskExecutor.retryById(id);
asyncTaskExecutor.retryByBizNumber("ORDER-0001");

// 删除主任务；组任务会级联处理子任务
asyncTaskExecutor.removeById(id);
asyncTaskExecutor.removeByBizNumber("ORDER-0001");
```

人工重试和删除前应确认业务补偿策略，避免重复调用外部系统。

## 9. 状态流转

```text
PENDING ──► EXECUTING ──► SUCCESS
    ▲             │
    │             ├──► WAITING ──► SUCCESS
    │             │                 └──► FAILED
    │             └──► FAILED
    │
    └──── retry ◄──── DISCARD（达到最大重试次数）
```

### 使用注意

- 业务 Handler 必须是 Spring Bean，`executorClass` 必须与实际 Bean 类型一致；
- 不要在业务代码中直接修改异步任务状态，应返回结果对象或调用 `callback`；
- 外部系统重试可能产生重复请求，提交前应使用业务流水号做幂等控制；
- `async-task.web.path` 为空时不应假设存在固定管理 URL，实际路径以应用路由和控制器映射为准；
- 关闭 `async-task.job.enabled` 后，任务不会由模块内置 Job 自动推进，需要业务自行提供调度驱动。
