# OCR 模块（iwindplus-base-ocr）

本模块封装两个 OCR 服务商：

- 印刷文字 OCR：`OcrPrintWordService`；
- 翔云 OCR：`OcrXiangyunService`。

两者都支持身份证和营业执照图片识别，调用参数是 Spring `MultipartFile`，返回服务商原始结果对应的 Jackson `JsonNode`。

```text
MultipartFile
    │
    ├── OcrPrintWordService
    │       └── 印刷文字 OCR
    │
    └── OcrXiangyunService
            └── 翔云 OCR
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-ocr</artifactId>
</dependency>
```

## 2. 配置

配置前缀是 `ocr`：

```yaml
ocr:
  print-word:
    enabled: true
    app-code: ${OCR_PRINT_WORD_APP_CODE}
  xiangyun:
    enabled: false
    access-key: ${OCR_XIANGYUN_ACCESS_KEY}
    secret-key: ${OCR_XIANGYUN_SECRET_KEY}
```

### 印刷文字 OCR

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `ocr.print-word.enabled` | `false` | 是否注册 `OcrPrintWordService` |
| `ocr.print-word.app-code` | 无 | 服务商认证 AppCode |

### 翔云 OCR

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `ocr.xiangyun.enabled` | `false` | 是否注册 `OcrXiangyunService` |
| `ocr.xiangyun.access-key` | 无 | 翔云访问 Key |
| `ocr.xiangyun.secret-key` | 无 | 翔云密钥 |

两个服务可以同时启用，业务代码通过不同接口选择供应商。不要在配置文件中明文提交密钥，建议使用环境变量、配置中心或密钥管理服务。

## 3. 身份证识别

### 印刷文字 OCR

```java
@Resource
private OcrPrintWordService ocrPrintWordService;

public JsonNode parseIdCard(MultipartFile file) {
    return ocrPrintWordService.parseIdCardImage(
        file,
        OcrPrintIdTypeEnum.FRONT
    );
}
```

`OcrPrintIdTypeEnum` 的具体枚举值以当前版本源码为准，应根据图片是身份证人像面还是国徽面选择对应类型。

### 翔云 OCR

```java
@Resource
private OcrXiangyunService ocrXiangyunService;

public JsonNode parseIdCard(MultipartFile file) {
    return ocrXiangyunService.parseIdCardImage(
        file,
        OcrXiangyunIdTypeEnum.FRONT
    );
}
```

`OcrXiangyunIdTypeEnum` 同样用于区分证件类型或身份证面，具体枚举值以当前版本源码为准。

## 4. 营业执照识别

### 印刷文字 OCR

```java
@Resource
private OcrPrintWordService ocrPrintWordService;

public JsonNode parseLicense(MultipartFile file) {
    return ocrPrintWordService.parseBusinessLicenseImage(file);
}
```

### 翔云 OCR

```java
@Resource
private OcrXiangyunService ocrXiangyunService;

public JsonNode parseLicense(MultipartFile file) {
    return ocrXiangyunService.parseBusinessLicenseImage(file);
}
```

## 5. Controller 接入示例

```java
@PostMapping("/id-card")
public ResultVO<JsonNode> idCard(@RequestPart("file") MultipartFile file) {
    JsonNode result = ocrPrintWordService.parseIdCardImage(
        file,
        OcrPrintIdTypeEnum.FRONT
    );
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

## 6. 返回结果

服务接口返回 `JsonNode`，模块不会将不同供应商的响应统一转换成同一个业务 VO。业务代码应根据实际供应商响应结构解析字段：

```java
JsonNode result = ocrPrintWordService.parseBusinessLicenseImage(file);
String licenseNumber = result.path("licenseNumber").asText(null);
```

建议在业务层再定义自己的结果对象，避免把第三方返回结构直接暴露给前端：

```java
public record LicenseOcrResult(
    String licenseNumber,
    String companyName,
    String legalPerson
) {
}
```

字段名称需要以服务商真实响应为准，不能直接假设两个供应商的字段完全一致。

## 7. Bean 注册规则

自动配置通过条件属性注册服务：

```text
ocr.print-word.enabled=true
    └── 注册 OcrPrintWordService

ocr.xiangyun.enabled=true
    └── 注册 OcrXiangyunService
```

如果对应开关为 `false` 或未配置，注入该服务会失败。业务项目如果需要支持可选供应商，应使用条件装配或先确认对应配置已启用。

## 8. 供应商选择建议

可以根据业务场景选择单一供应商：

```yaml
# 只启用印刷文字 OCR
ocr:
  print-word:
    enabled: true
  xiangyun:
    enabled: false
```

也可以同时启用，在业务层做主动路由：

```text
业务类型或租户
       │
       ├── 供应商 A → OcrPrintWordService
       └── 供应商 B → OcrXiangyunService
```

模块本身没有统一的 `OcrRouter` 或故障转移策略。若要主备切换、重试、计费控制或按租户选择，应在业务层封装路由服务。

## 9. 异常和重试

OCR 调用依赖外部 HTTP 服务，业务层应处理：

- 网络超时；
- 服务商认证失败；
- 图片格式不支持；
- OCR 识别失败；
- 服务商限流；
- 返回 JSON 结构变更。

建议：

- 只对明确可重试的网络异常进行有限次数重试；
- 不要对认证失败、参数错误无限重试；
- 对同一图片设置业务幂等键，避免重复计费；
- 记录请求流水号，不要记录完整身份证图片或完整敏感响应；
- 对供应商原始响应做脱敏后再保存日志。

## 10. 使用注意事项

- 图片通过 `MultipartFile` 传入，不要把本地文件路径直接传给服务接口；
- 两个供应商服务返回的 `JsonNode` 结构可能不同；
- 身份证识别必须传入对应的证件类型枚举；
- `app-code`、`access-key`、`secret-key` 属于敏感凭证；
- 不要把 OCR 原始结果直接返回给前端，建议转换为业务 VO；
- 不要在日志中打印图片内容、完整身份证号或完整营业执照信息；
- 生产环境应配置文件大小、上传类型和接口超时限制；
- OCR 服务没有内置统一路由、重试和熔断策略，需要业务自行实现；
- 如果只使用一个供应商，只开启对应的 `enabled` 配置即可。
