# iwindplus-base-domain

`iwindplus-base-domain` 是基础领域模型模块，集中提供跨业务模块复用的 DTO、VO、枚举、异常、上下文、校验分组和字段注解。

它不提供 Controller、数据库访问或具体业务服务，主要用于统一接口契约和基础数据约定。

## 引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-domain</artifactId>
</dependency>
```

## 统一响应

### ResultVO

`ResultVO<T>` 的 JSON 字段为：

| 字段 | 说明 |
|---|---|
| `bizCode` | 业务编码，HTTP 200 成功时默认是 `ok` |
| `bizMessage` | 业务消息，支持国际化 |
| `bizData` | 业务数据 |
| `bizTimestamp` | 创建响应时的毫秒时间戳 |
| `bizTraceId` | 从 MDC 中读取的 TraceId |

```java
@GetMapping("/{id}")
public ResultVO<UserVO> detail(@PathVariable Long id) {
    UserVO user = userService.getById(id);
    return ResultVO.success(user);
}

public ResultVO<Void> save() {
    return ResultVO.success();
}
```

常用构造方法：

```java
ResultVO.success();
ResultVO.success(data);
ResultVO.error();
ResultVO.error(HttpStatus.BAD_REQUEST);
ResultVO.error(commonException);
ResultVO.buildResult("user.not-found", "用户不存在");
ResultVO.buildResult("user.invalid", "用户 {0} 无效", new Object[]{userId}, null);
ResultVO.buildSourceResult("custom-code", "直接返回的消息");
```

`buildResult` 会尝试通过 Spring `MessageSource` 国际化消息；需要消息原样返回时使用 `buildSourceResult`。判断结果使用：

```java
if (result.bizSuccess()) {
    // 成功
}
if (result.bizError()) {
    // 失败
}
```

响应式代码可以使用 `ResultVO` 提供的 `Mono` 相关辅助方法，统一由 Web/WebFlux 模块负责最终响应输出。

## 异常

### CommonException

`CommonException` 是统一异常契约，包含：

- `getBizCode()`：业务编码；
- `getBizMessage()`：业务消息，可使用 `{0}`、`{1}` 占位符；
- `getBizMessageParams()`：消息参数。

可以使用静态方法构造简单异常描述：

```java
CommonException common = CommonException.build(
    "user.not-found",
    "用户 {0} 不存在",
    new Object[]{userId}
);
throw new BizException(common);
```

也可以直接构造：

```java
throw new BizException("user.not-found", "用户不存在");
throw new BizException("user.invalid", "用户 {0} 无效", new Object[]{userId});
throw new BizException(HttpStatus.BAD_REQUEST);
```

### BizException

`BizException` 是运行时业务异常，支持从 `CommonException`、HTTP 状态、业务编码和业务消息构造。Web、WebMVC 和 WebFlux 模块会将它转换为统一的 `ResultVO`。

建议业务层抛出带业务编码的 `BizException`，不要在 Controller 中拼接统一错误响应。

## 上下文

三个上下文持有器都基于 TransmittableThreadLocal：

### UserContextHolder

保存当前用户 `UserBaseVO`：

```java
UserBaseVO user = UserBaseVO.builder()
    .userId(1001L)
    .orgId(10L)
    .username("zhangsan")
    .realName("张三")
    .build();

UserContextHolder.setContext(user);
UserBaseVO current = UserContextHolder.getContext();
UserContextHolder.remove();
```

没有设置用户时，`getContext()` 返回系统默认用户，而不是 `null`。默认用户的 ID 和组织 ID 为 `0`，用户名等字段为 `system`。

### HeaderContextHolder

保存请求头 Map：

```java
HeaderContextHolder.setContext(headers);
Map<String, String> currentHeaders = HeaderContextHolder.getContext();
HeaderContextHolder.remove();
```

请求结束、异步任务结束或手动设置后必须调用 `remove()`，避免线程池复用造成上下文串数据。

### TccContextHolder

用于 TCC 事务上下文，使用方式与其他上下文持有器一致：

```java
TccContextHolder.setContext(context);
Object current = TccContextHolder.getContext();
TccContextHolder.remove();
```

具体上下文对象由 TCC 集成模块约定，业务不要跨请求长期保存。

## 数据库通用 DTO/VO

### 基础对象继承关系

```text
DbCommonDTO
├── DbBaseDTO                 // 额外包含 id
├── DbBaseTwoDTO              // 双主键/双业务标识场景
├── DbPageDTO                 // 分页查询参数
├── DbSignBaseDTO             // 数据签名场景
└── DbVersionBaseDTO          // 乐观锁/版本场景
```

对应的 VO 位于 `vo` 包中：`DbCommonVO`、`DbBaseVO`、`DbBaseTwoVO`、`DbPageVO`、`DbVersionBaseVO` 和 `DbVersionBaseTwoVO`。

`DbBaseDTO.id` 在 `EditGroup` 和 `OtherEditGroup` 校验分组下要求非空：

```java
public class UserEditDTO extends DbBaseDTO {
    @NotBlank(groups = {SaveGroup.class, EditGroup.class})
    private String username;
}
```

新增时使用 `SaveGroup`，编辑时使用 `EditGroup`；查询及其他业务场景可使用 `QueryGroup`、`OtherQueryGroup`、`OtherSaveGroup` 和 `OtherEditGroup`。

### 分页和通用对象

- `DbPageDTO`：分页页码、页大小等查询参数；
- `DbPageVO<T>`：分页记录、总数和分页信息；
- `ConditionExpressionDTO`：条件表达式；
- `MessageBaseDTO`：消息基础数据；
- `AkSkDTO`：AK/SK 认证参数；
- `ValidListDTO`：列表参数校验；
- `UploadByteDTO`：文件名、内容字节和内容类型等上传数据。

## 上传、文件和 Excel 数据对象

### UploadByteDTO

供 OSS、邮件和 Excel 等模块传递文件内容：

```java
UploadByteDTO file = UploadByteDTO.builder()
    .fileName("report.xlsx")
    .content(bytes)
    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    .build();
```

### 上传返回对象

- `FilePathVO`：文件路径/URL；
- `UploadBaseVO`：上传基础信息；
- `UploadVO`：普通文件上传结果；
- `UploadVideoVO`：视频上传结果。

### ExcelImportResultDTO

Excel 行数据需要继承该类，框架会填充行号和错误信息：

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class UserImportDTO extends ExcelImportResultDTO {
    private String username;
}
```

- `rowNum`：Excel 行号，从 1 开始；
- `errorMsg`：Bean Validation 或自定义校验错误信息。

## 枚举和统一枚举校验

### BaseEnum

基础枚举接口用于统一获取枚举值和描述。业务枚举可以实现 `BaseEnum`，在接口参数和数据库转换中保持统一编码。

常用枚举包括：

- `BizCodeEnum`：业务错误编码；
- `AlgorithmTypeEnum`：加密算法类型；
- `ConditionTypeEnum`：条件类型；
- `DbActionTypeEnum`：数据库操作类型；
- `EnableStatusEnum`、`YesNoEnum`：启用和是否状态；
- `OperateTypeEnum`：操作类型；
- `OssTypeEnum`、`VodTypeEnum`、`SmsTypeEnum`：基础设施供应商/业务类型；
- `PlatformTypeEnum`、`AppCertTypeEnum`、`UserSexEnum`：平台、认证和用户属性；
- `RequestMethodEnum`、`RequestProtocolEnum`：HTTP 请求信息；
- `ThreadRejectedStrategyEnum`、`TimeToLiveUnitEnum`：线程池和 TTL 配置。

### EnumValid

用于校验字符串或数值是否属于指定枚举：

```java
public class UserDTO {
    @EnumValid(enumClass = UserSexEnum.class, message = "性别不合法")
    private Integer sex;
}
```

具体枚举值字段以枚举实现的 `BaseEnum` 契约为准。

## 校验分组和校验注解

### 内置分组

```text
SaveGroup          新增
EditGroup          编辑
QueryGroup         查询
OtherSaveGroup     其他新增场景
OtherEditGroup     其他编辑场景
OtherQueryGroup    其他查询场景
```

Controller 中可以通过 `@Validated(SaveGroup.class)` 或 `@Validated(EditGroup.class)` 选择校验分组。

### English

校验字符串只能包含英文字符：

```java
@English(message = "只允许输入英文")
private String englishName;
```

### IdCard

校验中国居民身份证号码：

```java
@IdCard(message = "身份证号格式不正确")
private String idCard;
```

### 字段安全和脱敏注解

- `@Sensitive`：接口响应序列化时脱敏；
- `@TableFieldSensitive`：数据库字段敏感处理；
- `@TableFieldSafe`：数据库字段安全处理。

`@Sensitive` 的真实属性：

```java
@Sensitive(
    enabled = true,
    type = SensitiveTypeEnum.MOBILE_PHONE,
    startInclude = 2,
    endReserve = 2
)
private String mobile;
```

- `enabled`：是否启用，默认 `true`；
- `type`：脱敏类型，默认 `CUSTOM`；
- `startInclude`：脱敏起始位置，默认 2；
- `endReserve`：末尾保留位数，默认 2。

脱敏通常应用于响应数据，不建议将脱敏后的值写回数据库。

## 树、事件和扩展接口

- `BaseTreeVO`：树节点基础 VO；
- `BaseTreeCheckedVO`：带选中状态的树节点；
- `BaseEvent`：领域事件基础对象；
- `InputStreamProcessor<T>`：输入流处理函数式接口；
- `SupplierThrowable<T>`：允许抛出异常的 Supplier；
- `AppApiVO`：接口文档注册数据；
- `BaseSignVO`、`BaseSignExtendVO`：签名数据；
- `TccBaseVO`：TCC 基础返回对象；
- `UserBaseVO`：用户上下文和用户接口返回对象；
- `UserExtendFunctionValidDTO/VO`：用户扩展功能校验数据。

## 使用建议

1. DTO 用于请求、提交和更新参数，VO 用于接口返回，DO 不应直接暴露给外部接口。
2. 编辑请求继承 `DbBaseDTO` 后使用 `EditGroup`，新增请求使用 `SaveGroup`。
3. 业务错误统一抛出 `BizException`，由 Web 层转换为 `ResultVO`。
4. 线程池、异步任务和 Reactor 链路中使用上下文后要确保清理，避免用户信息和请求头串线。
5. 敏感字段使用注解和基础设施模块配合处理，不要在日志中打印原始手机号、身份证、密钥和 Token。
6. `iwindplus-base-domain` 只定义基础契约，具体的字段加密、脱敏、统一响应处理分别由 MyBatis、Web 和 Util 模块完成。
