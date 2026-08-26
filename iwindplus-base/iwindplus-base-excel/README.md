# iwindplus-base-excel

`iwindplus-base-excel` 基于 Alibaba EasyExcel，提供项目统一的 Excel 导入、校验和导出封装。

核心入口是 `EasyExcelUtil`，导入入口返回 `EasyExcelListener`，导出入口直接写入 `HttpServletResponse`。

## 引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-excel</artifactId>
</dependency>
```

该模块依赖 `iwindplus-base-domain`，导入行对象必须继承 `ExcelImportResultDTO`。

## Excel 行对象

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class UserImportDTO extends ExcelImportResultDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

`ExcelImportResultDTO` 提供两个框架字段：

| 字段 | 说明 |
|---|---|
| `rowNum` | Excel 行号，读取时由监听器自动填充，从 1 开始 |
| `errorMsg` | 当前行的校验错误信息 |

业务字段可以继续使用 Jakarta Validation 注解，也可以通过 `EasyExcelImportVerifyHandler` 编写跨字段或数据库校验。

## 导入 Excel

### 基础导入

```java
try (InputStream inputStream = file.getInputStream()) {
    EasyExcelListener<UserImportDTO> listener = EasyExcelUtil.importExcel(
        inputStream,
        UserImportDTO.class,
        null,
        1
    );

    List<UserImportDTO> allRows = listener.getList();
    List<UserImportDTO> validRows = listener.getRightList();
    List<UserImportDTO> invalidRows = listener.getFailList();
}
```

参数说明：

- `inputStream`：Excel 输入流，必填；
- `pojoClass`：行对象类型，同时用于表头校验；
- `verifyHandler`：自定义校验器，可为空；
- `headRowNumber`：表头行数，传 `null` 时默认为 `1`。

### 使用 Bean Validation

需要将 `Validator` 和校验分组传入：

```java
EasyExcelListener<UserImportDTO> listener = EasyExcelUtil.importExcel(
    inputStream,
    validator,
    new Class<?>[]{SaveGroup.class},
    UserImportDTO.class,
    null,
    1
);
```

校验分组为空时，监听器使用 Jakarta Validation 的 `Default` 分组。

### 使用自定义行校验

```java
EasyExcelImportVerifyHandler<UserImportDTO> verifyHandler = row -> {
    if (userService.existsByUsername(row.getUsername())) {
        return ExcelVerifyResultVO.fail("用户名已存在");
    }
    return ExcelVerifyResultVO.success();
};

EasyExcelListener<UserImportDTO> listener = EasyExcelUtil.importExcel(
    inputStream,
    validator,
    new Class<?>[]{SaveGroup.class},
    UserImportDTO.class,
    verifyHandler,
    1
);
```

自定义校验返回失败时，错误信息会写入 `errorMsg`，该行进入 `failList`；成功行进入 `rightList`。Bean Validation 错误和自定义校验错误会合并。

### 表头校验

传入 `pojoClass` 后，监听器会通过 `ExcelsUtil.listHeadByAnnotation(pojoClass)` 获取模型表头，并检查 Excel 表头是否包含模型定义的表头。

表头不匹配时会抛出 `BizCodeEnum.EXCEL_TEMPLATE_ERROR` 对应的 `BizException`。

注意：当前实现检查的是“Excel 表头是否包含模型表头”，不是对任意列顺序都进行强制等值校验。

### 忽略空行

空对象和 EasyExcel 判定的空行会被直接忽略，不会进入 `list`、`rightList` 或 `failList`。

## 导入结果处理

```java
List<UserImportDTO> allRows = listener.getList();
List<UserImportDTO> successRows = listener.getRightList();
List<UserImportDTO> failedRows = listener.getFailList();

for (UserImportDTO row : failedRows) {
    log.warn("第 {} 行导入失败：{}", row.getRowNum(), row.getErrorMsg());
}

userService.batchSave(successRows);
```

推荐只将 `rightList` 写入数据库。`failList` 可以返回给前端，或作为带错误信息的 Excel 再次导出。

## 导出 Excel

### 默认样式导出

```java
@GetMapping("/export")
public void export(HttpServletResponse response) {
    List<UserImportDTO> rows = userService.listForExport();
    EasyExcelUtil.exportExcel(
        response,
        rows,
        UserImportDTO.class,
        "user.xlsx",
        null
    );
}
```

当 `sheetName` 不单独传入时，默认使用文件名作为 Sheet 名称。

### 指定 Sheet 名称

```java
EasyExcelUtil.exportExcel(
    response,
    rows,
    UserImportDTO.class,
    "user.xlsx",
    "用户数据",
    null
);
```

### 自定义样式

```java
HorizontalCellStyleStrategy styleStrategy = new HorizontalCellStyleStrategy(
    headWriteCellStyle,
    contentWriteCellStyle
);

EasyExcelUtil.exportExcel(
    response,
    rows,
    UserImportDTO.class,
    "user.xlsx",
    "用户数据",
    styleStrategy
);
```

没有传入样式策略时，模块使用默认样式：表头宋体 14 号、内容宋体 12 号，水平和垂直居中。

### 导出失败行

如果导出数据中存在非空 `errorMsg`，工具会自动注册 `EasyExcelErrorRowWriteHandler`，在导出结果中增加错误信息列并标记失败行。

这适合将 `listener.getList()` 原样导出给用户，让用户看到哪些行失败以及失败原因：

```java
EasyExcelUtil.exportExcel(
    response,
    listener.getList(),
    UserImportDTO.class,
    "user-import-result.xlsx",
    "导入结果",
    null
);
```

## 文件格式和响应处理

导出文件名必须带有支持的 Excel 后缀，例如 `.xls` 或 `.xlsx`。无法识别的后缀会抛出 Excel 格式错误业务异常。

工具会自动：

1. 根据文件名设置下载响应头；
2. 根据文件后缀设置 Content-Type；
3. 创建 EasyExcel Writer；
4. 写入 Sheet 和数据；
5. Flush 输出流并刷新响应缓冲区。

Controller 不要再重复设置相同的响应头，避免文件名编码或 Content-Type 被覆盖。

## 推荐导入流程

```text
上传文件
   ↓
EasyExcelUtil.importExcel
   ↓
表头校验
   ↓
Bean Validation
   ↓
自定义业务校验
   ↓
┌──────────────┬──────────────┐
│ rightList    │ failList     │
│ 批量入库     │ 返回错误信息 │
└──────────────┴──────────────┘
```

## 注意事项

1. 导入对象必须继承 `ExcelImportResultDTO`，否则不能使用当前泛型入口。
2. `headRowNumber` 是表头行数，不是数据起始行的任意偏移量。
3. `rightList` 只代表框架校验通过，不代表数据库写入已经成功。
4. 自定义校验器中不要执行不可控的高频远程请求，建议提前批量加载校验数据。
5. 大文件导入时不要长期持有 `getList()` 的全部数据，应结合业务控制文件大小和批量处理策略。
6. 导出数据中含有错误信息时会自动增加错误处理列，前端导入模板不要直接把结果文件当作原始模板使用。
7. `exportExcel` 捕获输出过程中的 `IOException` 并记录日志，业务层需要结合响应状态和日志做好失败监控。
8. 导入流使用完毕后由调用方关闭，推荐使用 try-with-resources。

## 相关模块

- `iwindplus-base-domain`：提供 `ExcelImportResultDTO`、`ExcelVerifyResultVO`、校验分组和业务异常。
- `iwindplus-base-util`：提供 `ExcelsUtil`、`FilesUtil`、`ValidUtil` 等基础工具。
- `iwindplus-base-oss`：处理导入文件上传和导出文件存储。
- `iwindplus-base-web`：提供统一 Web 请求和文件响应能力。
