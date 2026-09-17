# iwindplus-base-export-task

`iwindplus-base-export-task` 是基于 Alibaba EasyExcel 的**异步导出任务**模块，面向大数据量导出场景，提供任务落库、分页写入、进度回写、失败重试、XXL-JOB 定时调度、OSS/本地文件下载等能力，避免大数据量导出导致前端长时间等待。

## 引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-export-task</artifactId>
</dependency>
```

本模块依赖 `iwindplus-base-util`（传递依赖 `iwindplus-base-domain`）。若业务同时使用 Excel 同步导入导出，行对象需要继承 `ExcelImportResultBaseVO`（位于 `iwindplus-base-domain`），该工具类为 `ExcelsUtil`（位于 `iwindplus-base-util`）。

## 异步导出任务

异步导出是本模块的核心能力，整体流程如下：

```text
提交导出任务
   ↓
ExportTaskExecutor.submit(ExportTaskSubmitDTO)
   ↓
落库 export_task（状态 PENDING）
   ↓
线程池异步执行
   ↓
按 getQueryClass 反序列化查询参数 → pageByCondition 分页查询
   ↓
将查询结果转换为 getRowClass（导出行模型）后分页写入 Excel
   ↓
SUCCESS（可下载） / FAILED（按策略重试） / DISCARD（人工重试）
```

### 泛型约定

`ExportTaskHandler<Q, V, E>` 是导出任务的业务处理器接口，三个泛型含义如下：

| 泛型 | 含义 | 约束 |
|---|---|---|
| `Q` | 查询参数对象 | 必须继承 `DbPageDTO`（框架会回填 `current`、`size` 分页参数） |
| `V` | 查询返回值对象 | 业务视图对象，`pageByCondition` 的分页记录类型 |
| `E` | 导出行模型 | Excel 行对象，需用 `@ExcelProperty` 标注导出列 |

接口方法如下：

```java
public interface ExportTaskHandler<Q extends DbPageDTO, V, E> {

    default String getExecuteName() {        // 有默认值，可不用实现
        return this.getClass().getSimpleName();
    }

    Class<Q> getQueryClass();                // 查询参数类型，必实现

    Class<E> getRowClass();                  // 导出行模型类型，必实现

    String getFileName();                    // 导出文件名（含后缀），必实现

    default String getSheetName() {          // Sheet 名称，有默认值
        return ExcelConstant.DEFAULT_SHEET_NAME;
    }

    DbPageVO<V> pageByCondition(Q entity);   // 分页查询，必实现

    default void onTaskSuccess(ExportTaskVO entity) {}  // 成功回调，可选

    default void onTaskFail(ExportTaskVO entity) {}     // 失败回调，可选
}
```

> 说明：由于 Java 泛型在运行期会被擦除，`getQueryClass()`、`getRowClass()` 并非冗余。框架在反序列化 `queryParam` 以及创建 EasyExcel `ExcelWriter` 时，都需要这两个运行时 `Class` 对象，实现类必须返回真实类型。

### 初始化数据库

执行模块内置的建表脚本创建任务表：

- 表名：`export_task`
- 脚本路径：`src/main/resources/db/V202608272324__export_task_table.sql`

### 实现导出任务处理器

为每个业务导出场景实现一个 `ExportTaskHandler`：

```java
@Component
@RequiredArgsConstructor
public class OrderExportTaskHandler implements ExportTaskHandler<OrderSearchDTO, OrderPageVO, OrderExportVO> {

    private final OrderRepository orderRepository;

    @Override
    public Class<OrderSearchDTO> getQueryClass() {
        return OrderSearchDTO.class;
    }

    @Override
    public Class<OrderExportVO> getRowClass() {
        return OrderExportVO.class;
    }

    @Override
    public String getFileName() {
        return "订单导出" + FileTypeEnum.XLSX.getSuffix();
    }

    @Override
    public DbPageVO<OrderPageVO> pageByCondition(OrderSearchDTO entity) {
        IPage<OrderPageVO> page = orderRepository.page(entity);
        return new DbPageVO<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
```

要点：

- `OrderSearchDTO` 必须继承 `DbPageDTO`，框架会自动回填当前页和每页条数；
- `OrderPageVO` 是查询返回的业务视图对象，与 `OrderExportVO`（Excel 行模型）可以不同；
- 框架会通过 `BeanUtil.copyToList` 自动把查询结果转换为 `getRowClass()` 对应的行模型后写入 Excel，业务无需手动转换。

### 提交导出任务

在 Controller 中注入 `ExportTaskExecutor`，通过 `ExportTaskSubmitDTO` 提交：

```java
@PostMapping("exportTask")
public ResultVO<ExportTaskSubmitVO> exportTask(@RequestBody @Validated OrderSearchDTO entity) {
    ExportTaskSubmitDTO param = ExportTaskSubmitDTO.builder()
        .executorClass(OrderExportTaskHandler.class)
        .build();
    param.setQueryParam(entity);

    ExportTaskSubmitVO data = exportTaskExecutor.submit(param);
    return ResultVO.success(data);
}
```

`ExportTaskSubmitDTO` 字段：

| 字段 | 说明 | 是否必填 |
|---|---|---|
| `executorClass` | 处理器类，用于定位对应的 `ExportTaskHandler` | 是 |
| `queryParam` | 查询参数，通过 `setQueryParam(T)` 自动序列化为 Map | 是 |
| `bizNumber` | 业务流水号，便于业务侧关联 | 否 |
| `remark` | 备注 | 否 |
| `ext` | 扩展对象 | 否 |

提交返回 `ExportTaskSubmitVO`，包含 `id`（任务主键）和 `bizNumber`（业务流水号），用于后续查询进度和下载。

### 查询进度与下载

模块内置了查询进度和下载接口（`ExportTaskController`），无需业务自行实现：

- 查询进度：`GET admin/report/exportTask/getDetail?id={任务ID}`
- 下载文件：`GET admin/report/exportTask/download?id={任务ID}`

接口基础路径可通过 `export-task.web.path` 配置（默认 `admin/report/exportTask`）。

`ExportTaskVO` 关键字段：

| 字段 | 说明 |
|---|---|
| `status` | 任务状态 |
| `progress` | 进度（0-100） |
| `exportedCount` | 已导出数量 |
| `totalCount` | 总数 |
| `errorMsg` | 失败原因 |
| `fileName` / `filePath` | 文件名 / 文件路径 |
| `bizNumber` | 业务流水号 |

### 任务状态

`ExportTaskStatusEnum` 定义的状态流转如下：

| 状态 | 值 | 说明 |
|---|---|---|
| `PENDING` | 0 | 待执行 |
| `EXECUTING` | 10 | 执行中 |
| `SUCCESS` | 20 | 成功，可下载 |
| `FAILED` | 30 | 失败，等待重试 |
| `DISCARD` | 40 | 废弃（超过最大重试次数），需人工重试 |

### 失败重试

框架对 `FAILED` 任务按重试策略自动调度重试，超过最大重试次数后转为 `DISCARD`。

`DISCARD` 状态的任务可通过 `ExportTaskExecutor` 人工重试：

```java
exportTaskExecutor.retryById(taskId);            // 通过主键重试
exportTaskExecutor.retryByBizNumber(bizNumber);  // 通过业务流水号重试
```

### 定时任务配置

在 XXL-JOB 管理后台配置定时任务：

- 任务名称：`exportTask`
- 运行模式：分片广播
- Cron 表达式：按业务需要配置（如 `0/10 * * * * ?` 每 10 秒执行一次）

定时任务会遍历内置的任务处理器（当前为重试任务 `RETRY_JOB`），按分片参数驱动未完成任务执行。

### 配置项

```yaml
export-task:
  enabled: true          # 模块总开关（默认 true）
  job:
    enabled: true                  # 是否启用内置任务（默认 true）
    max-loop-count: 100            # 单次调度最大循环次数（默认 100）
  web:
    enabled: true        # 内置查询/下载接口开关（默认 true）
    path: admin/report/exportTask   # 内置接口基础路径（默认 admin/report/exportTask）
  max-page-size: 10                # 定时任务分页每页条数（默认 10）
  timeout-seconds: 120             # 任务执行最大时长，超时将被重置（默认 120）
  oss:
    enabled: false                 # 是否上传 OSS（默认 false，使用本地文件存储）
    type: MINIO                    # OSS 类型（默认 MINIO）
    code: minio                    # OSS 配置编码（可选，不配置使用默认策略）
    bucket-name: your-bucket       # 空间名（启用 OSS 时必填）
    access-domain: https://oss.example.com   # 访问域名（可选）
    relative-path-prefix: export-task/       # 相对路径前缀（可选）
  retry:
    frequency: 1m,2m,5m,10m        # 重试频率（默认 1m,2m,5m,10m）
    enabled-unlimited-retry: false # 是否无限重试（默认 false）
    max-attempts: 10               # 最大重试次数（默认 10）
```

## Excel 同步导入导出（ExcelsUtil）

同步导入导出由 `iwindplus-base-util` 模块的 `ExcelsUtil` 提供，本模块通过依赖传递可直接使用。

### 行对象基类

行对象必须继承 `ExcelImportResultBaseVO`，它提供两个框架字段：

| 字段 | 说明 |
|---|---|
| `rowNum` | Excel 行号，读取时由监听器自动填充，从 1 开始 |
| `errorMsg` | 当前行的校验错误信息 |

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class UserImportVO extends ExcelImportResultBaseVO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

业务字段可继续使用 Jakarta Validation 注解，也可通过 `EasyExcelImportVerifyHandler` 编写跨字段或数据库校验。

### 导入

```java
try (InputStream inputStream = file.getInputStream()) {
    ExcelImportResultVO<UserImportVO> result = ExcelsUtil.importExcel(
        inputStream,
        UserImportVO.class,
        null,
        1
    );

    List<UserImportVO> allRows = result.getList();        // 全部行
    List<UserImportVO> validRows = result.getRightList(); // 校验通过行
    List<UserImportVO> invalidRows = result.getFailList(); // 校验失败行
}
```

导入有两个重载：

```java
// 基础导入
importExcel(InputStream inputStream, Class<?> pojoClass,
    EasyExcelImportVerifyHandler<T> verifyHandler, Integer headRowNumber)

// 带 Bean Validation 的导入
importExcel(InputStream inputStream, Validator validator, Class<?>[] groups,
    Class<?> pojoClass, EasyExcelImportVerifyHandler<T> verifyHandler, Integer headRowNumber)
```

参数说明：

- `inputStream`：Excel 输入流，必填；
- `validator` / `groups`：Bean Validation 校验器和校验分组（可选，分组为空时使用 `Default` 分组）；
- `pojoClass`：行对象类型，同时用于表头校验；
- `verifyHandler`：自定义校验器，可为空；
- `headRowNumber`：表头行数，传 `null` 时默认为 `1`。

### 自定义行校验

`EasyExcelImportVerifyHandler` 是函数式接口，返回 `ExcelVerifyResultVO`：

```java
EasyExcelImportVerifyHandler<UserImportVO> verifyHandler = row -> {
    if (userService.existsByUsername(row.getUsername())) {
        return ExcelVerifyResultVO.builder().success(false).msg("用户名已存在").build();
    }
    return ExcelVerifyResultVO.builder().success(true).build();
};

ExcelImportResultVO<UserImportVO> result = ExcelsUtil.importExcel(
    inputStream,
    validator,
    new Class<?>[]{SaveGroup.class},
    UserImportVO.class,
    verifyHandler,
    1
);
```

自定义校验失败时，`msg` 会写入 `errorMsg`，该行进入 `failList`；成功行进入 `rightList`。Bean Validation 错误和自定义校验错误会合并。

### 导出

```java
@GetMapping("/export")
public void export(HttpServletResponse response) {
    List<UserImportVO> rows = userService.listForExport();
    ExcelsUtil.exportExcel(response, rows, UserImportVO.class, "user.xlsx", null);
}
```

导出有两个重载：

```java
// 默认 Sheet 名称（取文件名主名）
exportExcel(HttpServletResponse response, List<T> data, Class<?> pojoClass,
    String fileName, HorizontalCellStyleStrategy horizontalCellStyleStrategy)

// 指定 Sheet 名称
exportExcel(HttpServletResponse response, List<T> data, Class<?> pojoClass,
    String fileName, String sheetName, HorizontalCellStyleStrategy horizontalCellStyleStrategy)
```

导出文件名必须带 `FileTypeEnum` 支持的 Excel 后缀（如 `.xls`、`.xlsx`、`.xlsm`、`.csv`），无法识别的后缀会抛出 Excel 格式错误业务异常。未传样式策略时使用默认样式（表头宋体 14 号、内容宋体 12 号、水平和垂直居中）。

### 表头校验

传入 `pojoClass` 后，监听器会通过 `ExcelsUtil.listHeadByAnnotation(pojoClass)` 获取模型表头，并检查 Excel 表头是否包含模型定义的表头。表头不匹配时抛出 `BizCodeEnum.EXCEL_TEMPLATE_ERROR` 对应的 `BizException`。

### 导出失败行

导出数据中存在非空 `errorMsg` 时，工具会自动注册 `EasyExcelErrorRowWriteHandler`，在导出结果中增加错误信息列并标记失败行，适合将导入失败行原样导出给用户查看：

```java
ExcelsUtil.exportExcel(response, result.getList(), UserImportVO.class,
    "user-import-result.xlsx", "导入结果", null);
```

## 注意事项

1. 异步导出的查询参数会被序列化为 JSON 存储，需确保参数对象可序列化。
2. 异步导出默认每批 `EXPORT_BATCH_SIZE` 条分页写入，避免一次性加载全部数据导致内存溢出。
3. 导出文件默认写入系统临时目录（`java.io.tmpdir`），未启用 OSS 时需自行关注临时文件清理；分布式部署建议启用 `document.oss` 上传到 OSS。
4. 提交任务时 `executorClass` 与 `ExportTaskHandler` 实现类必须一一对应，框架按执行器名称（默认类名）定位处理器。
5. 同步导入的 `rightList` 只代表框架校验通过，不代表数据库写入成功。
6. 自定义校验器中不要执行不可控的高频远程请求，建议提前批量加载校验数据。
7. 同步导入流使用完毕后由调用方关闭，推荐使用 try-with-resources。

## 相关模块

- `iwindplus-base-domain`：提供 `ExcelImportResultBaseVO`、`ExcelVerifyResultVO`、`DbPageDTO`、`DbPageVO` 等基础对象。
- `iwindplus-base-util`：提供 `ExcelsUtil`、`FilesUtil` 等基础工具。
- `iwindplus-base-mybatis`：提供分页查询等持久化能力。
- `iwindplus-base-oss`：提供导出文件上传 OSS 的能力。
- `iwindplus-base-xxl-job`：提供 XXL-JOB 定时调度能力。
- `iwindplus-base-web`：提供统一 Web 请求和文件响应能力。
