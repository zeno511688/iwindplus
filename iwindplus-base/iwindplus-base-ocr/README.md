# OCR 模块（iwindplus-base-ocr）

本模块封装两个 OCR 服务商：

- 印刷文字 OCR：`PrintWordOcrExecuteHandler`
- 翔云 OCR：`XiangyunOcrExecuteHandler`

两者都支持身份证和营业执照图片识别，调用参数是 Spring `MultipartFile`，返回统一的业务 VO 对象（`OcrIdCardVO` 和 `OcrBusinessLicenseVO`）。

模块采用策略模式设计，支持多配置，每个配置对应一个运行时策略实例，通过「OCR 类型 + 配置编码」二维结构区分不同策略。

```text
MultipartFile
    │
    └── OcrExecuteHandlerFactory（统一策略工厂）
            │
            ├── OcrTypeEnum.PRINT_WORD → PrintWordOcrExecuteHandler → OcrIdCardVO / OcrBusinessLicenseVO
            │
            └── OcrTypeEnum.XIANGYUN → XiangyunOcrExecuteHandler → OcrIdCardVO / OcrBusinessLicenseVO
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-ocr</artifactId>
</dependency>
```

## 2. 配置

配置前缀是 `ocr`，支持多配置，每个配置通过 `code` 唯一标识：

```yaml
ocr:
  enabled: true
  default-code: print-word-1
  print-word:
    - code: print-word-1
      name: 印刷文字OCR服务1
      enabled: true
      priority: 1
      app-code: ${OCR_PRINT_WORD_APP_CODE}
    - code: print-word-2
      name: 印刷文字OCR服务2
      enabled: false
      priority: 2
      app-code: ${OCR_PRINT_WORD_APP_CODE_2}
  xiangyun:
    - code: xiangyun-1
      name: 翔云OCR服务1
      enabled: false
      priority: 1
      access-key: ${OCR_XIANGYUN_ACCESS_KEY}
      secret-key: ${OCR_XIANGYUN_SECRET_KEY}
```

### 顶层配置

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `ocr.enabled` | `true` | 模块是否启用 |
| `ocr.default-code` | 无 | 默认 OCR 配置编码（可选，指定后可通过 `getDefaultHandler()` 获取默认策略） |

### 公共配置（每个配置项通用）

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `false` | 是否启用该配置 |
| `priority` | 无 | 优先级（数字越小优先级越高，用于选择默认策略） |

### 印刷文字 OCR 配置

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `ocr.print-word[].app-code` | 无 | 服务商认证 AppCode |

### 翔云 OCR 配置

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `ocr.xiangyun[].access-key` | 无 | 翔云访问 Key |
| `ocr.xiangyun[].secret-key` | 无 | 翔云密钥 |

两个服务可以同时启用多个配置，业务代码通过策略工厂和配置编码选择具体的服务实例。不要在配置文件中明文提交密钥，建议使用环境变量、配置中心或密钥管理服务。

## 3. 策略工厂使用

模块提供统一的策略工厂 `OcrExecuteHandlerFactory`，通过「OCR 类型 + 配置编码」二维结构管理策略实例。

### 获取 OCR 服务实例

```java
@Resource
private OcrExecuteHandlerFactory ocrExecuteHandlerFactory;

public void demo() {
    // 方式一：按 OCR 类型获取（该类型下优先级最高的可用策略）
    OcrExecuteHandler printWordHandler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.PRINT_WORD);

    // 方式二：按 OCR 类型 + 配置编码获取（精确指定配置）
    OcrExecuteHandler handler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.PRINT_WORD, "print-word-1");

    // 方式三：获取默认策略（根据 ocr.default-code 配置的编码查找，未配置时返回 null）
    OcrExecuteHandler defaultHandler = ocrExecuteHandlerFactory.getDefaultHandler();
}
```

> **注意**：`getHandler(type)` 和 `getHandler(type, code)` 在找不到对应策略时会抛出 `BizException`（`INVALID_STRATEGY`）；`getDefaultHandler()` 在未配置 `default-code` 或编码不存在时返回 `null`。

## 4. 身份证识别

### 印刷文字 OCR

```java
@Resource
private OcrExecuteHandlerFactory ocrExecuteHandlerFactory;

public OcrIdCardVO parseIdCard(MultipartFile file) {
    OcrExecuteHandler handler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.PRINT_WORD, "print-word-1");
    return handler.parseIdCardImage(file, OcrPrintIdTypeEnum.SECOND_ID_CARD_FRONT);
}
```

`OcrPrintIdTypeEnum` 的枚举值：

- `SECOND_ID_CARD_FRONT`：二代身份证正面（值 `face`）
- `SECOND_ID_CARD_BACK`：二代身份证背面（值 `back`）

返回的 `OcrIdCardVO` 包含以下字段：

- `name`：姓名
- `sex`：性别
- `nation`：民族
- `birth`：出生日期
- `address`：住址
- `idNumber`：身份证号
- `authority`：签发机关
- `validPeriod`：有效期限

### 翔云 OCR

```java
@Resource
private OcrExecuteHandlerFactory ocrExecuteHandlerFactory;

public OcrIdCardVO parseIdCard(MultipartFile file) {
    OcrExecuteHandler handler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.XIANGYUN, "xiangyun-1");
    return handler.parseIdCardImage(file, OcrXiangyunIdTypeEnum.SECOND_ID_CARD_FRONT);
}
```

`OcrXiangyunIdTypeEnum` 的枚举值：

- `SECOND_ID_CARD_FRONT`：二代身份证正面（值 `2`）
- `SECOND_ID_CARD_BACK`：二代身份证背面（值 `3`）

返回的 `OcrIdCardVO` 字段与印刷文字 OCR 一致。

### 类型安全检查

实现类会对 `idType` 参数进行类型检查：

- `PrintWordOcrExecuteHandler` 要求 `idType` 必须是 `OcrPrintIdTypeEnum` 类型
- `XiangyunOcrExecuteHandler` 要求 `idType` 必须是 `OcrXiangyunIdTypeEnum` 类型

如果类型不匹配，将抛出 `IllegalArgumentException` 异常。

## 5. 营业执照识别

### 印刷文字 OCR

```java
@Resource
private OcrExecuteHandlerFactory ocrExecuteHandlerFactory;

public OcrBusinessLicenseVO parseLicense(MultipartFile file) {
    OcrExecuteHandler handler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.PRINT_WORD, "print-word-1");
    return handler.parseBusinessLicenseImage(file);
}
```

### 翔云 OCR

```java
@Resource
private OcrExecuteHandlerFactory ocrExecuteHandlerFactory;

public OcrBusinessLicenseVO parseLicense(MultipartFile file) {
    OcrExecuteHandler handler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.XIANGYUN, "xiangyun-1");
    return handler.parseBusinessLicenseImage(file);
}
```

返回的 `OcrBusinessLicenseVO` 包含以下字段：

- `companyName`：公司名称
- `creditCode`：统一社会信用代码
- `legalPerson`：法定代表人
- `address`：注册地址
- `businessScope`：经营范围
- `establishDate`：成立日期
- `businessTerm`：营业期限
- `registrationAuthority`：登记机关

## 6. Controller 接入示例

```java
@Resource
private OcrExecuteHandlerFactory ocrExecuteHandlerFactory;

@PostMapping("/id-card")
public ResultVO<OcrIdCardVO> idCard(@RequestPart("file") MultipartFile file) {
    OcrExecuteHandler handler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.PRINT_WORD, "print-word-1");
    OcrIdCardVO result = handler.parseIdCardImage(file, OcrPrintIdTypeEnum.SECOND_ID_CARD_FRONT);
    return ResultVO.success(result);
}
```

接口需要使用 `multipart/form-data`：

```http
POST /ocr/id-card HTTP/1.1
Content-Type: multipart/form-data

file=<身份证图片>
```

上传前建议在业务层校验：

- 文件不能为空；
- 文件大小；
- 图片 MIME 类型；
- 图片扩展名；
- 图片分辨率和清晰度；
- 是否包含敏感信息；
- 是否允许重复识别。

## 7. 返回结果

服务接口返回统一的业务 VO 对象，模块已将不同供应商的响应转换为统一的结构：

- 身份证识别：返回 `OcrIdCardVO`
- 营业执照识别：返回 `OcrBusinessLicenseVO`

业务代码可以直接使用这些 VO 对象，无需关心底层供应商的差异：

```java
OcrExecuteHandler handler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.PRINT_WORD, "print-word-1");
OcrIdCardVO idCardResult = handler.parseIdCardImage(file, OcrPrintIdTypeEnum.SECOND_ID_CARD_FRONT);
String name = idCardResult.getName();
String idNumber = idCardResult.getIdNumber();

OcrExecuteHandler xiangyunHandler = ocrExecuteHandlerFactory.getHandler(OcrTypeEnum.XIANGYUN, "xiangyun-1");
OcrBusinessLicenseVO licenseResult = xiangyunHandler.parseBusinessLicenseImage(file);
String companyName = licenseResult.getCompanyName();
String creditCode = licenseResult.getCreditCode();
```

如果业务需要扩展字段或添加额外信息，可以在业务层定义自己的结果对象：

```java
public record LicenseOcrResult(
    String licenseNumber,
    String companyName,
    String legalPerson,
    LocalDateTime recognizeTime
) {
    public static LicenseOcrResult from(OcrBusinessLicenseVO vo) {
        return new LicenseOcrResult(
            vo.getCreditCode(),
            vo.getCompanyName(),
            vo.getLegalPerson(),
            LocalDateTime.now()
        );
    }
}
```

## 8. 架构设计

### 策略模式

模块采用策略模式设计，核心组件：

- `BaseService`：通用业务接口，定义健康检查（`isHealthy`）和优先级（`getPriority`）
- `BaseConfigService<T>`：通用配置接口，定义配置的读写（`getConfig`/`setConfig`）
- `OcrExecuteHandler`：策略标识接口，继承 `BaseService`，定义 OCR 类型、配置编码和统一 OCR 方法
- `OcrExecuteHandlerFactory`：统一策略工厂，管理策略实例
- `AbstractBaseConfigServiceImpl<T>`：配置抽象基类，提供文件校验等公共功能
- `AbstractBaseServiceImpl<T>`：业务抽象基类，实现健康检查、优先级和配置编码

### 多配置支持

每个 OCR 类型支持多配置：

- 每个配置通过 `code` 唯一标识
- 每个配置对应一个运行时策略实例
- 策略实例由统一工厂管理，通过「OCR 类型 + 配置编码」二维结构路由
- 工厂通过 `ObjectProvider` 自动收集所有 `OcrExecuteHandler` 策略实例

### 类层次结构

```text
BaseService
    └── OcrExecuteHandler (策略标识接口)
            └── AbstractBaseServiceImpl<T>
                    ├── PrintWordOcrExecuteHandler
                    └── XiangyunOcrExecuteHandler

BaseConfigService<T>
    └── AbstractBaseConfigServiceImpl<T>
            └── AbstractBaseServiceImpl<T>
```

## 9. 供应商选择建议

可以根据业务场景选择单一供应商：

```yaml
# 只启用印刷文字 OCR
ocr:
  enabled: true
  default-code: print-word-1
  print-word:
    - code: print-word-1
      enabled: true
      app-code: ${OCR_PRINT_WORD_APP_CODE}
  xiangyun:
    - code: xiangyun-1
      enabled: false
```

也可以同时启用多个配置，在业务层做主动路由：

```text
业务类型或租户
       │
       ├── 配置 A → PrintWordOcrExecuteHandler (print-word-1)
       └── 配置 B → XiangyunOcrExecuteHandler (xiangyun-1)
```

模块本身通过策略工厂支持多配置路由。若要主备切换、重试、计费控制或按租户选择，可以在业务层封装路由服务。

## 10. 异常和重试

OCR 调用依赖外部 HTTP 服务，业务层应处理：

- 网络超时；
- 服务商认证失败；
- 图片格式不支持；
- OCR 识别失败；
- 服务商限流；
- 返回 JSON 结构变更；
- 类型参数不匹配（`IllegalArgumentException`）；
- 策略不存在（`BizException`，`INVALID_STRATEGY`）。

建议：

- 只对明确可重试的网络异常进行有限次数重试；
- 不要对认证失败、参数错误无限重试；
- 对同一图片设置业务幂等键，避免重复计费；
- 记录请求流水号，不要记录完整身份证图片或完整敏感响应；
- 对供应商原始响应做脱敏后再保存日志。

## 11. 使用注意事项

- 图片通过 `MultipartFile` 传入，不要把本地文件路径直接传给服务接口；
- 两个供应商服务返回统一的 VO 对象，屏蔽了底层供应商的差异；
- 身份证识别必须传入对应的证件类型枚举，类型不匹配会抛出异常；
- `app-code`、`access-key`、`secret-key` 属于敏感凭证；
- 不要在日志中打印图片内容、完整身份证号或完整营业执照信息；
- 生产环境应配置文件大小、上传类型和接口超时限制；
- 通过统一策略工厂 `OcrExecuteHandlerFactory` 获取服务实例，支持多配置路由；
- `default-code` 用于指定默认 OCR 配置编码，配置后可通过 `getDefaultHandler()` 获取默认策略，未配置时该方法返回 `null`；
- `priority` 用于同类型多配置下选择默认策略，数字越小优先级越高，未配置时默认最低优先级；
- `getHandler(type)` 和 `getHandler(type, code)` 找不到策略时会抛出 `BizException`，调用前应确保配置已启用；
- 如果只使用一个供应商，只开启对应的 `enabled` 配置即可。
