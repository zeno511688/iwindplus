# 异步命令模块（iwindplus-base-async-cmd）

适用本地事务 + 消息最终一致性（Outbox）场景：调用提交方法后立即落库并在事务提交后开启异步线程执行业务，失败后由定时任务按退避频率重试。

优势：
- 支持本地消息事务，提交不丢失数据（任务先落库，事务提交后才投递执行）
- 支持动态线程池（DtpExecutor）
- 支持单任务、组任务（串行/并行/多阶段编排）、第三方回调等待、进度占位拆分

使用场景：调用其他业务失败需要重试的业务（发 kafka、调第三方接口等）。

# 一、对接流程

1. 在配置文件（yml/properties）中配置 `AsyncCmdProperty` 相关属性（前缀 `async-cmd`）
2. 实现 `AsyncCmdTaskHandler`（主任务）/ `AsyncCmdSubTaskHandler`（子任务）定制业务
3. 程序中注入 `AsyncCmdExecutor`，调用 `submit` / `submitGroup` 提交任务
4. 配置 `AsyncCmdJob` 定时任务（RETRY_JOB、RESET_JOB）

# 二、配置说明（AsyncCmdProperty）

| 配置项 | 默认值 | 说明 |
|---|---|---|
| async-cmd.enabled | true | 是否开启 |
| async-cmd.enabled-success-delete | true | 成功后删除数据（逻辑删除） |
| async-cmd.enabled-success-real-delete | true | 成功后真实删除数据 |
| async-cmd.enabled-exception-capture | true | 是否截取异常信息落库 |
| async-cmd.exception-capture-length | 4000 | 异常信息截取长度 |
| async-cmd.max-page-size | 10 | 定时任务每轮捞取条数 |
| async-cmd.timeout-seconds | 120 | 任务执行租约时间（秒），超时被 RESET_JOB 重置 |
| async-cmd.async-wait-poll-seconds | 60 | 回调结果轮询间隔（秒） |
| async-cmd.async-wait-timeout-seconds | 1800 | 回调等待超时（秒），从首次挂起算，超期转失败进重试链 |
| async-cmd.retry.frequency | 30s,2m,10m,15m,20m,30m,1h | 重试退避频率 |
| async-cmd.retry.enabled-unlimited-retry | false | 是否无限重试 |
| async-cmd.retry.max-attempts | 30 | 最大重试次数，超限丢弃 |
| async-cmd.job.enabled | true | 是否启用内置 job |
| async-cmd.web.enabled / async-cmd.web.path | true / - | web 管理接口开关与路径 |

# 三、实现执行器

## 主任务执行器（AsyncCmdTaskHandler）

```java
@Component
public class VideoMergeTaskHandler implements AsyncCmdTaskHandler {

    @Override
    public String getExecuteName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void execute(AsyncCmdVO entity) {
        // 组任务收尾时，entity.getSubTasks() 携带全部成功的子任务结果
        VideoParam param = entity.getParam(VideoParam.class);
        // 业务逻辑，抛异常即任务失败进入重试链
    }

    // 可选：声明 needCallback=true 时必须重写，返回 SUCCESS/FAILED/WAITING(null=WAITING)
    @Override
    public AsyncCmdCallbackResultEnum executeCallback(AsyncCmdVO entity) {
        return AsyncCmdCallbackResultEnum.WAITING;
    }

    // 可选：任务重试超限丢弃钩子（钩子异常被吞掉，不影响JOB循环）
    @Override
    public void onTaskDiscard(AsyncCmdVO entity) {
    }
}
```

## 子任务执行器（AsyncCmdSubTaskHandler）

```java
@Component
public class VideoSubmitSubHandler implements AsyncCmdSubTaskHandler {

    @Override
    public String getExecuteName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void executeSub(AsyncCmdSubVO entity) {
        // entity.getData(XxxParam.class) 取提交参数
        // entity.getPriorSubTasks() 取前置批次成功子任务结果
        // 调第三方后需要等回调时，提交须声明 needCallback=true
    }

    // 可选：声明 needCallback=true 时必须重写，返回 SUCCESS/FAILED/WAITING(null=WAITING)
    @Override
    public AsyncCmdCallbackResultEnum executeSubCallback(AsyncCmdSubVO entity) {
        return AsyncCmdCallbackResultEnum.WAITING;
    }
}
```

# 四、提交任务

## 1. 单任务 submit(AsyncCmdSubmitDTO)

```java
asyncCmdExecutor.submit(AsyncCmdSubmitDTO.builder()
    .bizName("订单导出")                    // 必填
    .bizKey("ORDER")                        // 必填，业务域标识
    .bizType("ORDER_EXPORT")                // 必填，业务类型
    .bizNumber("EXP20260812001")            // 可选，回调/重试定位键
    .param(Map.of("date", "2026-08-12"))    // 可选
    .executorClass(OrderExportTaskHandler.class)  // 必填，须已注册为Bean
    .needCallback(false)                    // 为true时handler必须重写executeCallback
    .needDisplay(true)                      // 可选，进度查询可见
    .build());
```

执行逻辑：落库 → 事务提交后投递线程池 → CAS 抢锁转执行中 → `handler.execute()`
- needCallback=false：执行成功即 SUCCESS，异常转 FAILED 进重试链
- needCallback=true：执行成功后转 ASYNC_WAIT 等待回调/轮询结果

## 2. 组任务 submitGroup(AsyncCmdGroupSubmitDTO)

以"本地准备 → 调第三方(等回调) → 3 个进度占位 → 汇总收尾"为例：

```java
asyncCmdExecutor.submitGroup(AsyncCmdGroupSubmitDTO.builder()
    .bizName("视频合成").bizKey("VIDEO").bizType("VIDEO_MERGE")
    .executorClass(VideoMergeTaskHandler.class)   // 主收尾执行器，必填，子任务全部成功后执行
    .subTasks(List.of(
        // seq=1 本地任务
        AsyncCmdSubSubmitDTO.builder()
            .bizName("素材准备").bizKey("VIDEO").bizType("VIDEO_PREPARE")
            .seq(1).stage(1)
            .executorClass(VideoPrepareSubHandler.class)
            .param(Map.of("videoId", videoId))
            .build(),
        // seq=2 调第三方，提交后等回调
        AsyncCmdSubSubmitDTO.builder()
            .bizName("提交第三方合成").bizKey("VIDEO").bizType("VIDEO_SUBMIT")
            .bizNumber("VID20260812001")          // 回调定位须自行指定且全局唯一
            .seq(2).stage(1)
            .executorClass(VideoSubmitSubHandler.class)
            .param(Map.of("videoId", videoId))
            .needCallback(true)                   // handler须重写executeSubCallback
            .build(),
        // seq=3~5 进度占位：executorClass=null，无业务执行，执行到位直接置成功
        // ⚠️ 占位子任务必须显式指定 stage，不指定则默认 0 会触发 stage 单调不减校验失败
        AsyncCmdSubSubmitDTO.builder()
            .bizName("合成中").bizKey("VIDEO").bizType("VIDEO_PROGRESS")
            .seq(3).stage(2).needDisplay(true)
            .build(),
        AsyncCmdSubSubmitDTO.builder()
            .bizName("转码中").bizKey("VIDEO").bizType("VIDEO_PROGRESS")
            .seq(4).stage(2).needDisplay(true)
            .build(),
        AsyncCmdSubSubmitDTO.builder()
            .bizName("上传中").bizKey("VIDEO").bizType("VIDEO_PROGRESS")
            .seq(5).stage(2).needDisplay(true)
            .build(),
        // seq=6 后续任务
        AsyncCmdSubSubmitDTO.builder()
            .bizName("结果转存").bizKey("VIDEO").bizType("VIDEO_SAVE")
            .seq(6).stage(3)
            .executorClass(VideoSaveSubHandler.class)
            .param(Map.of("videoId", videoId))
            .build()
    )).build());
```

### 提交校验规则

- seq 从 1 开始连续且唯一；stage 按 seq 顺序单调不减（seq 顺序即执行顺序）
- 有执行器的子任务：param 可选；needCallback=true 时 handler 必须重写 `executeSubCallback`
- 进度占位子任务（executorClass=null）：禁止声明 needCallback；param 可省略；**必须显式指定 stage**，不指定则默认 0 会触发 stage 单调不减校验失败

### 调度执行逻辑

1. **分批**（groupByStage）：stage 变化切分批次；stage=0 或未指定的子任务每个单独一批；同 stage 连续子任务合并为一批并发执行（占位子任务同样按 stage 分组，支持与同 stage 任务并行）
2. **批次顺序推进**：前一批未全部成功不进入下一批；批次内任一失败则停止后续批次
3. **结果传递**：前置批次成功子任务以 `priorSubTasks` 快照注入后续批次子任务
4. **回调等待**：needCallback 子任务执行后转 ASYNC_WAIT，主任务转回待执行（不算失败、不占重试次数），等待下轮拾起；此时**后续所有批次中的占位子任务（executeName 为空）全部预置为执行中**（不限批次类型，第三方处理期间进度可见）；下轮消费回调预存结果（或调 `executeSubCallback` 查询），回调等待超时转失败进重试链
5. **收尾**：全部子任务成功且数量与提交一致 → 续租 → 执行主收尾 handler → 主任务 SUCCESS（主任务声明 needCallback 时先进 ASYNC_WAIT）

### 编排场景示例

#### 场景一：占位指定独立 stage（回调期间进度可见）

```
提交编排：
  seq=1  stage=1  PrepareHandler
  seq=2  stage=1  ThirdPartyHandler (needCallback=true)
  seq=3  stage=2  占位（合成）    ← 显式指定 stage=2
  seq=4  stage=2  占位（转码）
  seq=5  stage=2  占位（上传）
  seq=6  stage=3  FinalHandler

分批：[seq=1,2]  [seq=3,4,5]  [seq=6]

执行时序：
  batch1: seq=1 → SUCCESS
         seq=2 → handler.executeSub → ASYNC_WAIT
  batch1 未全部SUCCESS → markFollowingPlaceholdersExecuting
    → 扫描后续批次 → seq=3/4/5 占位 + TO_BE_EXECUTE
    → 批量 TO_BE_EXECUTE → EXECUTE（回调期间进度可见）
  → 主任务 TO_BE_EXECUTE，等待回调

  回调到达 → 更新 seq=3/4/5 progress → 存 seq=2 result → CAS 加速
  下一轮拾起：
    batch1: seq=2 消费预存结果 → SUCCESS
    batch2: seq=3/4/5 并发 → executePlaceholderSubTask → SUCCESS
    batch3: seq=6 → SUCCESS
  → 主任务 SUCCESS
```

#### 场景二：占位有 handler（空实现），同 stage 并行

```
提交编排：
  seq=1  stage=1  PrepareHandler
  seq=2  stage=1  ThirdPartyHandler (needCallback=true)
  seq=3  stage=2  SynthHandler     （executeSub 空实现）
  seq=4  stage=2  TranscodeHandler （executeSub 空实现）
  seq=5  stage=2  UploadHandler    （executeSub 空实现）
  seq=6  stage=3  FinalHandler

分批：[seq=1,2]  [seq=3,4,5]  [seq=6]

执行时序：
  batch1: seq=1 SUCCESS, seq=2 ASYNC_WAIT
  → markFollowingPlaceholdersExecuting
    seq=3/4/5 有 executeName，不是占位 → 不预置

  回调到达 → seq=2 SUCCESS
  batch2: seq=3/4/5 并发 → handler.executeSub（空实现） → SUCCESS
  batch3: seq=6 → SUCCESS

特点：有 handler 的任务不算占位，走正常 handler 路径，回调后才执行
```

# 五、回调通知 callback(AsyncCmdCallbackDTO)

业务收到外部系统回调后调用，状态流转由框架完成（业务不直接改状态）：

```java
// 主任务回调
asyncCmdExecutor.callback(AsyncCmdCallbackDTO.builder()
    .bizNumber("VID20260812001")                    // 主任务定位键
    .callbackResult(AsyncCmdCallbackResultEnum.SUCCESS)  // 主任务回调结果（可选，非空时处理主任务）
    .result(Map.of("url", fileUrl))                 // 可选，业务数据
    .progress(100)                                  // 可选，主任务整体进度
    .subTasks(List.of(                              // 可选，子任务回调列表
        AsyncCmdSubCallbackDTO.builder()
            .bizNumber("VID20260812001_SUB3")       // 子任务定位键
            .callbackResult(AsyncCmdCallbackResultEnum.SUCCESS)
            .progress(80)                           // 占位子任务独立进度
            .build(),
        AsyncCmdSubCallbackDTO.builder()
            .bizNumber("VID20260812001_SUB4")
            .callbackResult(AsyncCmdCallbackResultEnum.SUCCESS)
            .progress(50)
            .build()
    ))
    .build());
```

执行逻辑（回调预存机制）：
- 通过 id（优先）或 bizNumber 定位主任务
- callbackResult 非空时预存主任务结果与进度
- 遍历 subTasks 列表，按各子回调的 bizNumber 定位子任务并预存结果与进度
- 处理完所有子任务后，从 subTasks 提取 bizNumber→progress 批量更新占位任务进度（1 SELECT + 1 UPDATE）
- 每个子任务处理后聚合进度到主任务
- 任务处于 ASYNC_WAIT 时 CAS 刷新重试时间并立即投递，加速消费
- 已终态主任务 / 已成功子任务幂等忽略重复通知

# 六、重试与删除

```java
// 重试：仅 DISCARD 状态可重试，重置重试次数并立即投递
asyncCmdExecutor.retryById(id);
asyncCmdExecutor.retryByBizNumber("EXP20260812001");

// 删除：级联删除子任务
asyncCmdExecutor.removeById(id);
asyncCmdExecutor.removeByBizNumber("EXP20260812001");
```

# 七、定时任务（调度中心配置）

| Job | 实现类 | 职责 |
|---|---|---|
| RETRY_JOB | AsyncCmdJobHandlerRetry | 捞取"待执行/异步等待 + nextRetryTime 已到期"的任务投回线程池执行，是失败重试、回调轮询消费的统一驱动源 |
| RESET_JOB | AsyncCmdJobHandlerReset | 捞取"执行中/异步等待 + 租约 expireTime 过期"的卡死任务；重试次数未超限重置回待执行，超限丢弃并触发 `onTaskDiscard` 钩子 |

兜底链：业务线程崩溃导致状态卡住 → BizProcessor 兜底转失败 → 租约到期被 RESET_JOB 重置或丢弃，任务不会永久悬挂。

# 八、状态机

```
TO_BE_EXECUTE(0) → EXECUTE(10) → SUCCESS(30)
                                 → ASYNC_WAIT(20) → SUCCESS(30) / FAILED(40)
                                 → FAILED(40) →（重试）TO_BE_EXECUTE
                                                →（超限）DISCARD(50) →（手动retry）TO_BE_EXECUTE
```
