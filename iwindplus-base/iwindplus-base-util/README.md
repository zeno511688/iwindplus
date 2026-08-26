# iwindplus-base-util

工具类模块，提供 JSON 序列化、对象复制、日期处理、加解密、脱敏、文件处理等常用工具。

## 功能特性

- ✅ JSON 序列化/反序列化（JacksonUtil）
- ✅ 对象复制（BeanCopierUtil）
- ✅ 日期处理（DatesUtil）
- ✅ 加解密（CryptoUtil）
- ✅ 数据脱敏（SensitiveUtil）
- ✅ 文件处理（FilesUtil）
- ✅ 异常处理（ExceptionUtil）
- ✅ 正则验证（PatternUtil）
- ✅ 树形结构处理（TreesUtil）
- ✅ Excel 处理（ExcelsUtil）
- ✅ PDF 处理（PdfUtil、HtmlToPdfUtil）
- ✅ 图片处理（ImageUtil）
- ✅ 序列化（KryoUtil、ProtostuffUtil）
- ✅ 其他工具类

## 依赖引入

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-util</artifactId>
</dependency>
```

## 核心工具类

### 1. JacksonUtil - JSON 序列化工具

基于 Jackson 的 JSON 序列化/反序列化工具，支持 Java 8 日期时间类型。

#### 使用示例

```java
// 对象转 JSON 字符串
User user = new User(1L, "张三", "zhangsan@example.com");
String json = JacksonUtil.toJsonStr(user);
// {"id":1,"name":"张三","email":"zhangsan@example.com"}

// JSON 字符串转对象
String json = "{\"id\":1,\"name\":\"张三\"}";
User user = JacksonUtil.parseObject(json, User.class);

// 对象转字节数组
byte[] bytes = JacksonUtil.toJsonBytes(user);

// 格式化输出（带缩进）
String prettyJson = JacksonUtil.toJsonPrettyStr(user);
/*
{
  "id" : 1,
  "name" : "张三",
  "email" : "zhangsan@example.com"
}
*/

// 对象类型转换
Map<String, Object> map = new HashMap<>();
map.put("id", 1L);
map.put("name", "张三");
User user = JacksonUtil.convertValue(map, User.class);

// 处理泛型类型
String json = "[{\"id\":1,\"name\":\"张三\"},{\"id\":2,\"name\":\"李四\"}]";
List<User> users = JacksonUtil.parseObject(json, new ParameterizedTypeReference<List<User>>() {});

// 处理复杂泛型类型
String json = "{\"data\":[{\"id\":1,\"name\":\"张三\"}]}";
ResultVO<List<User>> result = JacksonUtil.parseObject(json, 
    new ParameterizedTypeReference<ResultVO<List<User>>>() {});

// 从输入流读取
InputStream is = new FileInputStream("user.json");
User user = JacksonUtil.parseObject(is, User.class);

// 清理 JSON 中的 HTML 标签
JsonNode node = mapper.readTree(json);
JacksonUtil.cleanJsonNode(node, value -> 
    JacksonUtil.cleanByJsoup(value, Arrays.asList("b", "i", "u")));
```

#### 主要方法

| 方法 | 说明 |
|------|------|
| `toJsonStr(Object obj)` | 对象转 JSON 字符串 |
| `toJsonPrettyStr(Object obj)` | 对象转格式化 JSON 字符串 |
| `toJsonBytes(Object obj)` | 对象转字节数组 |
| `parseObject(String text, Class<T> clazz)` | JSON 字符串转对象 |
| `parseObject(String text, ParameterizedTypeReference<T> typeRef)` | JSON 字符串转泛型对象 |
| `parseBytes(byte[] bytes, Class<T> clazz)` | 字节数组转对象 |
| `convertValue(Object obj, Class<T> clazz)` | 对象类型转换 |
| `getMapper()` | 获取全局 ObjectMapper |
| `setObjectMapper(ObjectMapper mapper)` | 设置全局 ObjectMapper |

#### 特性

- 支持 Java 8 日期时间类型（LocalDateTime、LocalDate、LocalTime、Instant）
- 支持自定义日期格式
- 支持 MyBatis Page 分页对象序列化
- 支持敏感字段脱敏（通过 SensitiveAnnotationIntrospect）
- 线程安全的单例 ObjectMapper

### 2. BeanCopierUtil - 对象复制工具

基于 CGLIB BeanCopier 的高性能对象复制工具，支持缓存。

#### 使用示例

```java
// 单个对象复制（方式一：使用 Class）
UserDO userDO = new UserDO(1L, "张三", "zhangsan@example.com");
UserVO userVO = BeanCopierUtil.copyProperties(userDO, UserVO.class);

// 单个对象复制（方式二：使用 Supplier，性能更好）
UserVO userVO = BeanCopierUtil.copyProperties(userDO, UserVO::new);

// 集合复制（方式一：使用 Class）
List<UserDO> userDOList = new ArrayList<>();
List<UserVO> userVOList = BeanCopierUtil.copyToList(userDOList, UserVO.class);

// 集合复制（方式二：使用 Supplier，性能更好）
List<UserVO> userVOList = BeanCopierUtil.copyToList(userDOList, UserVO::new);

// 使用自定义转换器
UserVO userVO = BeanCopierUtil.copyProperties(userDO, UserVO::new, (value, targetClass, context) -> {
    if (value instanceof String && targetClass == String.class) {
        return ((String) value).trim();  // 去除空格
    }
    return value;
});

// 集合复制 + 自定义转换器
List<UserVO> userVOList = BeanCopierUtil.copyToList(userDOList, UserVO::new, converter);
```

#### 主要方法

| 方法 | 说明 |
|------|------|
| `copyProperties(K source, Class<T> target)` | 单个对象复制（使用 Class） |
| `copyProperties(K source, Supplier<T> target)` | 单个对象复制（使用 Supplier） |
| `copyProperties(K source, Class<T> target, Converter converter)` | 单个对象复制 + 转换器 |
| `copyToList(List<K> sources, Class<T> target)` | 集合复制（使用 Class） |
| `copyToList(List<K> sources, Supplier<T> target)` | 集合复制（使用 Supplier） |
| `copyToList(List<K> sources, Class<T> target, Converter converter)` | 集合复制 + 转换器 |

#### 特性

- 使用 CGLIB BeanCopier，性能优于 Spring BeanUtils
- 支持缓存，避免重复创建 BeanCopier
- 支持自定义转换器
- 支持 Supplier 方式创建目标对象，性能更好

### 3. DatesUtil - 日期处理工具

扩展 Hutool DateUtil，提供更多日期处理功能。

#### 使用示例

```java
// 毫秒转 LocalDateTime
long millis = System.currentTimeMillis();
LocalDateTime dateTime = DatesUtil.parseDate(millis);

// 毫秒转格式化字符串
String dateStr = DatesUtil.parseDate(millis, "yyyy-MM-dd HH:mm:ss");

// 字符串转 LocalDateTime
LocalDateTime dateTime = DatesUtil.parseDate("2024-01-01 12:00:00");
LocalDateTime dateTime = DatesUtil.parseDate("2024-01-01");  // 自动补全时间

// UTC 字符串转 LocalDateTime
LocalDateTime dateTime = DatesUtil.parseUtcDate("2024-01-01T12:00:00Z");

// 获取当天开始时间（00:00:00）
LocalDateTime morning = DatesUtil.getTimesMorning();

// 获取当天结束时间（23:59:59）
LocalDateTime night = DatesUtil.getTimesNight();

// 获取两个日期之间的所有日期
List<String> days = DatesUtil.getDaysBetween("2024-01-01", "2024-01-10");
// ["2024-01-01", "2024-01-02", ..., "2024-01-10"]

// 获取两个日期之间的所有周
List<String> weeks = DatesUtil.getWeeksBetween("2024-01-01", "2024-12-31");

// 获取两个日期之间的所有月
List<String> months = DatesUtil.getMonthsBetween("2024-01-01", "2024-12-31");

// 解析频率字符串（如 "30s", "5m", "2h"）
Duration duration = DatesUtil.parseFrequency("30s");  // 30 秒
Duration duration = DatesUtil.parseFrequency("5m");   // 5 分钟
Duration duration = DatesUtil.parseFrequency("2h");   // 2 小时

// 计算两个日期之间的毫秒数
long millis = DatesUtil.betweenMs(LocalDateTime.now(), LocalDateTime.now().plusDays(1));

// 计算两个日期之间的秒数
long seconds = DatesUtil.betweenSeconds(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
```

#### 主要方法

| 方法 | 说明 |
|------|------|
| `parseDate(long millis)` | 毫秒转 LocalDateTime |
| `parseDate(long millis, String format)` | 毫秒转格式化字符串 |
| `parseDate(String stringDate)` | 字符串转 LocalDateTime |
| `parseUtcDate(String stringDate)` | UTC 字符串转 LocalDateTime |
| `getTimesMorning()` | 获取当天开始时间 |
| `getTimesNight()` | 获取当天结束时间 |
| `getDaysBetween(String start, String end)` | 获取两个日期之间的所有日期 |
| `getWeeksBetween(String start, String end)` | 获取两个日期之间的所有周 |
| `getMonthsBetween(String start, String end)` | 获取两个日期之间的所有月 |
| `parseFrequency(String frequency)` | 解析频率字符串（如 "30s", "5m", "2h"） |
| `betweenMs(LocalDateTime start, LocalDateTime end)` | 计算两个日期之间的毫秒数 |
| `betweenSeconds(LocalDateTime start, LocalDateTime end)` | 计算两个日期之间的秒数 |

### 4. CryptoUtil - 加解密工具

提供多种加解密算法支持，包括 AES、RSA、SM2、Base64 等。

#### 使用示例

```java
// AES 加密（AES/GCM/NoPadding，自动生成随机 IV）
String key = "12345678901234567890123456789012";  // 32 位密钥
String encrypted = CryptoUtil.encryptByAes("敏感数据", key);

// AES 解密
String decrypted = CryptoUtil.decryptByAes(encrypted, key);

// RSA 加密
String publicKey = "公钥字符串";
String privateKey = "私钥字符串";
String encrypted = CryptoUtil.encryptByRsa("敏感数据", publicKey);

// RSA 解密
String decrypted = CryptoUtil.decryptByRsa(encrypted, privateKey);

// SM2 加密（国密算法）
String publicKey = "公钥字符串";
String encrypted = CryptoUtil.encryptBySm2("敏感数据", publicKey);

// SM2 解密
String decrypted = CryptoUtil.decryptBySm2(encrypted, privateKey);

// Base64 加密
String encoded = CryptoUtil.encryptByBase64("原始数据");

// Base64 解密
String decoded = CryptoUtil.decryptByBase64(encoded);

// 聚合加密（根据配置自动选择算法）
CryptoDTO config = CryptoDTO.builder()
    .enabled(true)
    .algorithm(AlgorithmTypeEnum.AES)
    .key("12345678901234567890123456789012")
    .build();
String encrypted = CryptoUtil.encrypt("敏感数据", config);

// 聚合解密
String decrypted = CryptoUtil.decrypt(encrypted, config);
```

#### 主要方法

| 方法 | 说明 |
|------|------|
| `encryptByAes(String data, String key)` | AES 加密（AES/GCM/NoPadding） |
| `decryptByAes(String data, String key)` | AES 解密 |
| `encryptByRsa(String data, String publicKey)` | RSA 加密 |
| `decryptByRsa(String data, String privateKey)` | RSA 解密 |
| `encryptBySm2(String data, String publicKey)` | SM2 加密（国密） |
| `decryptBySm2(String data, String privateKey)` | SM2 解密（国密） |
| `encryptByBase64(String data)` | Base64 加密 |
| `decryptByBase64(String data)` | Base64 解密 |
| `encrypt(String data, CryptoDTO config)` | 聚合加密 |
| `decrypt(String data, CryptoDTO config)` | 聚合解密 |

#### 特性

- AES 使用 GCM 模式，每次加密自动生成随机 IV，安全性更高
- 支持国密算法 SM2
- 支持聚合加解密，根据配置自动选择算法
- 密钥长度验证

### 5. SensitiveUtil - 数据脱敏工具

提供多种数据脱敏规则，支持自定义脱敏位置。

#### 使用示例

```java
// 手机号脱敏
String mobile = "13812345678";
SensitiveDTO config = SensitiveDTO.builder()
    .type(SensitiveTypeEnum.MOBILE_PHONE)
    .build();
String desensitized = SensitiveUtil.desensitized(mobile, config);
// 结果：138****5678

// 身份证号脱敏
String idCard = "110101199001011234";
SensitiveDTO config = SensitiveDTO.builder()
    .type(SensitiveTypeEnum.ID_CARD)
    .build();
String desensitized = SensitiveUtil.desensitized(idCard, config);
// 结果：110***********1234

// 邮箱脱敏
String email = "zhangsan@example.com";
SensitiveDTO config = SensitiveDTO.builder()
    .type(SensitiveTypeEnum.EMAIL)
    .build();
String desensitized = SensitiveUtil.desensitized(email, config);
// 结果：z***@example.com

// 银行卡脱敏
String bankCard = "6222021234567890123";
SensitiveDTO config = SensitiveDTO.builder()
    .type(SensitiveTypeEnum.BANK_CARD)
    .build();
String desensitized = SensitiveUtil.desensitized(bankCard, config);
// 结果：6222********1234

// 自定义脱敏
String data = "ABCDEFGHIJ";
SensitiveDTO config = SensitiveDTO.builder()
    .type(SensitiveTypeEnum.CUSTOM)
    .startInclude(2)  // 从第 3 位开始（索引从 0 开始）
    .endReserve(2)    // 最后保留 2 位
    .build();
String desensitized = SensitiveUtil.desensitized(data, config);
// 结果：AB******IJ

// 清空为空串
String data = "敏感数据";
SensitiveDTO config = SensitiveDTO.builder()
    .type(SensitiveTypeEnum.CLEAR_TO_EMPTY)
    .build();
String desensitized = SensitiveUtil.desensitized(data, config);
// 结果：""

// 清空为 null
String data = "敏感数据";
SensitiveDTO config = SensitiveDTO.builder()
    .type(SensitiveTypeEnum.CLEAR_TO_NULL)
    .build();
String desensitized = SensitiveUtil.desensitized(data, config);
// 结果：null
```

#### 支持的脱敏类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `CUSTOM` | 自定义脱敏 | `AB******IJ` |
| `USER_ID` | 用户主键脱敏 | `12***45` |
| `CHINESE_NAME` | 中文名脱敏 | `张*` |
| `ID_CARD` | 身份证号脱敏 | `110***********1234` |
| `FIXED_PHONE` | 座机号脱敏 | `010****1234` |
| `MOBILE_PHONE` | 手机号脱敏 | `138****5678` |
| `ADDRESS` | 地址脱敏 | `北京市****` |
| `EMAIL` | 邮箱脱敏 | `z***@example.com` |
| `PASSWORD` | 密码脱敏 | `******` |
| `CAR_LICENSE` | 车牌脱敏 | `京A****` |
| `BANK_CARD` | 银行卡脱敏 | `6222********1234` |
| `IPV4` | IPv4 脱敏 | `192.168.*.*` |
| `IPV6` | IPv6 脱敏 | `2001:****:****` |
| `FIRST_MASK` | 只显示第一个 | `张` |
| `CLEAR_TO_EMPTY` | 清空为空串 | `` |
| `CLEAR_TO_NULL` | 清空为 null | `null` |

### 6. FilesUtil - 文件处理工具

扩展 Hutool FileUtil，提供更多文件处理功能。

#### 使用示例

```java
MultipartFile file = ...;
byte[] bytes = FilesUtil.getBytes(file);
UploadByteDTO uploadByteDTO = FilesUtil.getUploadBytes(file);
List<UploadByteDTO> uploads = FilesUtil.listUploadBytes(List.of(file));

File localFile = new File("test.txt");
byte[] localBytes = FilesUtil.getBytes(localFile);

InputStream inputStream = ...;
HttpServletResponse response = ...;
FilesUtil.downloadFile(inputStream, "test.txt", response);

String contentType = FilesUtil.getContentType("test.pdf");
Set<Class<?>> classes = FilesUtil.scanClasses(
    "com.iwindplus.base",
    BaseType.class
);
```

#### 主要方法

| 方法 | 说明 |
|------|------|
| `getBytes(MultipartFile file)` | MultipartFile 转 byte[] |
| `getBytes(File file)` | File 转 byte[] |
| `getUploadBytes(MultipartFile file)` | MultipartFile 转 UploadByteDTO |
| `listUploadBytes(List<MultipartFile> multipartFiles)` | MultipartFile 列表转 UploadByteDTO 列表 |
| `downloadFile(InputStream inputStream, String fileName, HttpServletResponse response)` | 下载文件 |
| `setHttpServletResponse(String fileName, HttpServletResponse response)` | 设置文件下载响应头 |
| `getContentType(String fileName)` | 获取文件 MIME 类型 |
| `scanClasses(String packagePatterns, Class<?> assignableType)` | 扫描指定包下的指定类型实现类 |
| `closeInputStream(InputStream inputStream)` | 关闭输入流 |
| `closeOutputStream(OutputStream outputStream)` | 关闭输出流 |

## 工具类完整索引

本模块的工具类均位于 `com.iwindplus.base.util`，除特别说明外都是无状态工具类，可以直接通过静态方法使用。下面的索引按实际源码中的职责划分，未列出的方法不会被本模块提供。

| 类 | 主要用途 | 关键入口 |
|---|---|---|
| `ApiSignUtil` / `DbSignUtil` | API 签名、数据库字段签名 | `generateSign`、`verifySign` |
| `CryptoUtil` / `KeysUtil` / `YubikeyUtil` | 加密、唯一 Key、YubiKey 签名 | `encrypt/decrypt`、`generate`、`verifySign` |
| `JacksonUtil` / `KryoUtil` / `ProtostuffUtil` | JSON、Kryo、Protostuff 序列化 | `toJsonBytes`、`parseBytes` |
| `BeanCopierUtil` / `MapstructUtil` | Bean 和集合转换 | `copyProperties`、`copyToList` |
| `DatesUtil` / `LengthUtil` / `CheckDataUtil` | 日期、长度、批量数量校验 | `getNextRetryTime`、`checkLength` |
| `ValidUtil` / `PatternUtil` / `ObjectEmptyCheckUtil` | Bean Validation、正则、深度判空 | `validateEntity`、`getPatternLike` |
| `SensitiveUtil` | 字段脱敏 | `desensitized` |
| `ExpressionUtil` / `TemplateUtil` / `PropertyUtil` | SpEL、模板参数、Properties | `parse`、`getTemplateParam` |
| `DiffUtil` / `FlattenUtil` | 对象差异、嵌套结构扁平化 | `compare`、`flatten` |
| `HttpsUtil` / `ReactorUtil` / `MdcUtil` | Servlet、WebFlux、Trace 上下文 | `getRealIp`、`readRequestBody` |
| `NetsUtil` / `PathMatchUtil` | 重试间隔、路径匹配 | `nextMaxInterval`、`match` |
| `FilesUtil` / `IosUtil` | 文件、流和资源关闭 | `getBytes`、`closeQuietly` |
| `ExcelsUtil` / `ImageUtil` / `PdfUtil` / `HtmlToPdfUtil` | Excel、图片、PDF | `checkHeader`、`generate`、`replacePdfFields` |
| `GoogleAuthUtil` | TOTP 双因素认证 | `generateCredentials`、`validate` |
| `TransactionUtil` | Spring 事务辅助 | `registerAfterCommit`、`executeInTransaction` |
| `CpuDetectorUtil` | CPU 线程数建议 | `getEffectiveCpu`、`recommendCore` |
| `LambdaUtil` | Lambda 解析字段名 | `getFieldName` |
| `TreesUtil` | 树节点追加 | `appendToLastLeaf` |

## 签名、加密与 Key

### ApiSignUtil 和 DbSignUtil

两个工具都采用 DTO 作为输入，避免调用方遗漏签名所需字段：

```java
String sign = ApiSignUtil.generateSign(apiSignGenerateDTO);
boolean valid = ApiSignUtil.verifySign(apiSignVerifyDTO);

String dbSign = DbSignUtil.generateSign(dbSignGenerateDTO);
boolean dbValid = DbSignUtil.verifySign(dbSignVerifyDTO);
```

`ApiSignUtil` 用于请求级签名，`DbSignUtil` 用于数据库数据签名。验签时应使用与生成签名时相同的参数、密钥和算法配置；签名字符串不要直接记录到普通业务日志中。

### CryptoUtil

源码提供聚合配置入口以及常用摘要/对称/非对称算法入口：

```java
String encrypted = CryptoUtil.encrypt(plainText, cryptoConfig);
String plainText = CryptoUtil.decrypt(encrypted, cryptoConfig);

String aesText = CryptoUtil.encryptByAes(plainText, key);
String sm4Text = CryptoUtil.encryptBySm4(plainText, key);
String sha256 = CryptoUtil.encryptBySha256(plainText);
String sm3 = CryptoUtil.encryptBySm3(plainText);
```

还提供 Base64、AES、SM4、SM2、RSA、MD5、SHA-256 和 SM3 方法，以及 `generateSm2Key()`、`generateRsaKey()` 密钥对生成方法。密钥管理应放在配置中心或密钥管理系统中，不要硬编码到 Java 代码或 README 示例中。

### KeysUtil

根据目标类、方法和参数生成稳定 Key，可选用 SM3 对生成结果进行摘要：

```java
String key = KeysUtil.generate(
    true,
    target,
    method,
    requestId,
    userId
);
```

生成结果由类名、方法名和参数 JSON 组成；适合幂等、锁和缓存 Key。参数对象应保证序列化结果稳定，敏感参数开启加密摘要时不要再把原始参数写入日志。

### YubikeyUtil

用于校验 YubiKey 产生的签名数据：

```java
boolean valid = YubikeyUtil.verifySign(publicKey, source, sign);
```

公钥、原文和签名必须来自同一认证流程，校验失败时不要继续执行受保护操作。

## 序列化和对象转换

### JacksonUtil

除 README 前文的 JSON 基础用法外，还支持字节数组、输入流、Map、集合、泛型、`JsonNode` 和 `JavaType`：

```java
byte[] bytes = JacksonUtil.toJsonBytes(value);
User user = JacksonUtil.parseBytes(bytes, User.class);
Map<String, Object> map = JacksonUtil.parseMap(json);
List<User> users = JacksonUtil.parseList(json, User.class);
JsonNode tree = JacksonUtil.parseTree(json);
```

`getMapper()` 返回模块使用的全局 `ObjectMapper`；需要临时替换时使用 `setObjectMapper`，不要在并发请求处理中反复修改全局对象。`cleanJsonNode` 和 `cleanByJsoup` 用于按规则清理 JSON/HTML 内容。

### KryoUtil 和 ProtostuffUtil

两者都提供 `toJsonBytes`、`parseBytes` 和 `executeAction`：

```java
byte[] kryoBytes = KryoUtil.toJsonBytes(value);
User kryoUser = KryoUtil.parseBytes(kryoBytes, User.class);

byte[] protostuffBytes = ProtostuffUtil.toJsonBytes(value);
User protostuffUser = ProtostuffUtil.parseBytes(protostuffBytes, User.class);
```

序列化和反序列化必须使用兼容的类结构及协议，跨服务传输时应固定版本和注册方式。不要把不可信来源的二进制数据直接反序列化为任意类型。

### BeanCopierUtil、MapstructUtil 和 LambdaUtil

`BeanCopierUtil` 使用 CGLIB BeanCopier，支持构造器 Supplier、目标 Class、集合和自定义 Converter：

```java
UserVO vo = BeanCopierUtil.copyProperties(userDO, UserVO::new);
List<UserVO> list = BeanCopierUtil.copyToList(userDOList, UserVO::new);
```

`MapstructUtil` 适合项目中已经配置 MapStruct 映射器的场景，支持 Bean、Map 到 Bean 和集合转换：

```java
UserVO vo = MapstructUtil.copyProperties(userDO, UserVO.class);
UserVO fromMap = MapstructUtil.copyProperties(data, UserVO.class);
```

`LambdaUtil` 将 MyBatis-Plus 风格的 getter Lambda 转成字段名：

```java
String fieldName = LambdaUtil.getFieldName(UserDO::getUserName);
```

## 日期、校验和结构处理

### DatesUtil

`DatesUtil` 继承 Hutool `DateUtil`，并额外提供项目常用日期计算：

```java
LocalDateTime time = DatesUtil.parseDate(timestamp);
LocalDateTime utcTime = DatesUtil.parseUtcDate(utcText);
List<String> days = DatesUtil.getDaysBetween("2026-01-01", "2026-01-05");
List<String> months = DatesUtil.getMonthsBetween("2026-01", "2026-03");
long retryAt = DatesUtil.getNextRetryTime(baseTime, "10s", retryCount);
```

`getNextRetryTime` 的频率格式为数字加单位，目前支持秒 `s`、分钟 `m`、小时 `h`。`getTimesMorning` 和 `getTimesNight` 返回当天起止时间。

### ValidUtil、LengthUtil 和 CheckDataUtil

```java
String errors = ValidUtil.validateEntity(dto, validator, SaveGroup.class);
boolean lengthOk = LengthUtil.checkLength(text.length(), 1, 100);
CheckDataUtil.checkBatchOperationSize(ids.size(), 500);
```

`validateEntity` 校验通过返回 `null`，失败时返回以分号连接的错误消息；批量数量超限会抛出业务异常。

### PatternUtil、ObjectEmptyCheckUtil 和 PathMatchUtil

`PatternUtil.getPatternLike(param)` 将查询参数转换为“模糊匹配”正则 Pattern，不能把它当作手机号或身份证校验 API。深度判空使用：

```java
boolean empty = ObjectEmptyCheckUtil.isDeepEmpty(value);
boolean matched = PathMatchUtil.match(patterns, requestPath);
```

路径匹配应使用项目统一的 Ant 风格路径规则，公共放行路径可集中放在配置中维护。

### ExpressionUtil、TemplateUtil 和 PropertyUtil

`ExpressionUtil` 只把以 `#` 开头的字符串当作 SpEL；方法参数按参数名绑定，返回值通过 `#result` 访问：

```java
List<String> values = ExpressionUtil.parse(
    method,
    args,
    result,
    new String[]{"#user.id", "#result.code", "fixed-value"},
    String.class
);
```

模板工具用于从模板内容中提取并替换参数：

```java
Map<String, String> params = TemplateUtil.getTemplateParam(template, templateParams);
String content = TemplateUtil.getTemplateContent(template, templateParams);
```

JSON 文本转 `Properties`：

```java
Properties properties = PropertyUtil.parseToProperties(jsonText);
```

### DiffUtil 和 FlattenUtil

`DiffUtil.compare` 返回对象或两个 Map 的差异字段路径；`FlattenUtil` 将嵌套 Map/List 转成点号和下标路径：

```java
List<String> differences = DiffUtil.compare(oldValue, newValue);
Map<String, Object> flat = FlattenUtil.flatten(data);
// 例如：user.name、items.[0].id
```

差异比较对象应避免循环引用；扁平化结果中的 List 下标使用 `[index]` 形式。

## Web、WebFlux 和链路上下文

### HttpsUtil

`HttpsUtil` 继承 Hutool `HttpUtil`，同时提供 Servlet 请求解析、客户端 IP、请求头、参数、响应和用户 Token 辅助：

```java
String ip = HttpsUtil.getRealIp(request);
Map<String, String> headers = HttpsUtil.getFilteredHeaders(request);
Map<String, Object> params = HttpsUtil.getRequestAndJsonParams(request);
boolean json = HttpsUtil.isJsonRequest(request);
String query = HttpsUtil.paramToQueryString(params);
```

还支持 WebFlux 的 `ServerWebExchange` 获取真实 IP，支持 URL 编解码、User-Agent、MDC、响应写出和 JWT 用户信息解析。`getFilteredHeaders` 会排除内容长度、内容类型及项目 Trace Header，转发请求时应优先使用它。

### ReactorUtil

用于 WebFlux 中读取和缓存只能消费一次的请求体/响应体：

```java
Mono<ReactorRequestDTO> request = ReactorUtil.readRequestBody(exchange, nextExchange -> filterChain.filter(nextExchange));
Mono<ReactorResponseDTO> response = ReactorUtil.readResponseBody(exchange, nextExchange -> filterChain.filter(nextExchange));
```

读取结果分别缓存到 `ReactorUtil.REQUEST_BODY` 和 `ReactorUtil.RESPONSE_BODY` 属性，可通过 `getRequestBodyByAttr`、`getResponseBodyByAttr` 读取。`buildAuthorizationByCookie` 会将 `access_token` Cookie 写入 `Authorization` 请求头；`getMonoResponse` 用于写出统一 `ResultVO`。

### MdcUtil

用于同步线程、线程池和 Reactor Context 之间传递 `X-Trace-Id`：

```java
MdcUtil.setTraceId(traceId);
Runnable wrapped = MdcUtil.wrap(task);
Mono<Result> traced = MdcUtil.withTraceId(mono);
```

任务完成后会清理 MDC；业务代码不要长期手动保留旧 TraceId。

### NetsUtil 和 CpuDetectorUtil

```java
long interval = NetsUtil.nextMaxInterval(1000, 60000, attempt);
int core = CpuDetectorUtil.recommendCore();
int max = CpuDetectorUtil.recommendMax();
```

`nextMaxInterval` 用于计算带上限的指数退避间隔；线程池大小仍应结合任务类型、阻塞比例和部署资源调整。

## 文件、Excel、图片和 PDF

### FilesUtil 和 IosUtil

```java
byte[] bytes = FilesUtil.getBytes(multipartFile);
UploadByteDTO upload = FilesUtil.getUploadBytes(multipartFile);
FilesUtil.downloadFile(inputStream, fileName, response);
Set<Class<?>> classes = FilesUtil.scanClasses(packagePatterns, BaseType.class);
```

`FilesUtil` 还支持 MultipartFile 批量转换、MIME 类型获取和资源扫描。`IosUtil` 提供 `Closeable`、`AutoCloseable`、输入流、输出流、Reader、Writer 和 Netty Channel 的安全关闭；优先使用 `closeQuietly` 或 `close...`，避免关闭异常覆盖业务异常。

### ExcelsUtil

```java
int headSize = ExcelsUtil.checkHeader(inputStream, UserRow.class, 1);
ExcelsUtil.checkFormat(multipartFile, ".xlsx");
List<String> headers = ExcelsUtil.listHeadByAnnotation(UserRow.class);
ExcelsUtil.dropDownOption(workbook, options, 'A', 1, 100);
String base64 = ExcelsUtil.getBase64(workbook);
```

支持 Excel 格式/表头校验、读取表头、单级或级联下拉选项、Workbook 下载/字节/Base64 转换和错误文件名生成。Workbook 使用完后调用 `closeWorkbook`。

### ImageUtil

```java
byte[] avatar = ImageUtil.generate(imagePaths, 200, 200, 6, "png");
BufferedImage resized = ImageUtil.resize(filePath, 200, 200, true);
```

`generate` 用于将多张图片合成为群组头像；图片 URL/路径、尺寸和格式必须来自可信输入，并注意远程图片读取的超时和 SSRF 风险。

### HtmlToPdfUtil 和 PdfUtil

```java
byte[] pdf = HtmlToPdfUtil.toPdfBytesByHtml(htmlContent);
HtmlToPdfUtil.toPdfByUrl(url, fileName, response);
byte[] filled = PdfUtil.replacePdfFields(templateFileName, variables);
```

HTML 转 PDF 支持 URL 和 HTML 字符串两种输入；PDF 表单填充使用模板文件名和字段变量 Map。外部 URL 和 HTML 内容应先做白名单、大小和协议校验。

## Google Auth、事务和树结构

### GoogleAuthUtil

```java
GoogleAuthenticatorKey credentials = GoogleAuthUtil.generateCredentials();
GoogleAuthVO qr = GoogleAuthUtil.generateQrContent("iwindplus", username);
boolean valid = GoogleAuthUtil.validate(secret, code);
```

支持生成 TOTP 凭据、二维码 URL/内容/图片和验证码校验。Secret 只能安全存储，不能返回到普通日志或前端长期缓存。

### TransactionUtil

```java
TransactionUtil.registerAfterCommit(() -> publishEvent(event));
boolean active = TransactionUtil.isTransactionActive();
Order order = TransactionUtil.executeInTransaction(transactionTemplate, () -> saveOrder());
```

存在活跃事务时，`registerAfterCommit` 会在提交成功后执行；没有活跃事务时会立即执行。需要保证提交后的任务具备幂等性。

### TreesUtil

该工具当前公开入口是向树结构最后一个叶子节点追加节点：

```java
TreesUtil.appendToLastLeaf(sourceNodes, resultNode);
```

它不是通用的树构建/扁平化工具；树的 ID、父子关系和 children 组装应由业务代码完成。

## 其他工具

### I18nUtil

通过基础名和 Locale 获取 ResourceBundle 消息，并支持参数格式化：

```java
I18nUtil i18n = I18nUtil.getInstance("messages", Locale.SIMPLIFIED_CHINESE);
String text = i18n.getString("user.not-found");
String formatted = i18n.getFormattedString("welcome", username);
```

同一 `baseName#locale` 会被缓存；动态 Nacos 国际化由 `iwindplus-base-i18n` 模块负责，不要将两者混淆。

### SecureRandomUtil

```java
String token = SecureRandomUtil.randomString(32);
String code = SecureRandomUtil.randomNumbers(6);
byte[] bytes = SecureRandomUtil.randomBytes(16);
int value = SecureRandomUtil.randomInt(100);
```

用于验证码、随机 Token 和随机字节；安全场景不要使用 `Math.random()` 替代。

## 42 个工具类逐类 API 速查

下面按源码目录 `com.iwindplus.base.util` 中的实际类逐个说明。除特别标注外，方法均为静态方法。

### 1. ApiSignUtil

API 请求签名与验签，输入为 `ApiSignGenerateDTO` 或 `ApiSignVerifyDTO`：

```java
String sign = ApiSignUtil.generateSign(generateDTO);
boolean valid = ApiSignUtil.verifySign(verifyDTO);
```

签名 DTO 的字段必须按调用方和服务端约定完整填写；验签失败时不要继续执行业务。

### 2. BeanCopierUtil

基于 BeanCopier 的对象和集合复制：

```java
UserVO vo = BeanCopierUtil.copyProperties(userDO, UserVO.class);
UserVO vo2 = BeanCopierUtil.copyProperties(userDO, UserVO::new);
List<UserVO> list = BeanCopierUtil.copyToList(userDOList, UserVO.class);
```

支持传入 `Converter` 处理类型转换，也支持 `Supplier<T>` 创建目标对象。只复制名称匹配的 Bean 属性，不负责深度复制和业务字段映射。

### 3. CheckDataUtil

限制批量操作数量：

```java
CheckDataUtil.checkBatchOperationSize(ids.size(), 500);
```

数量超过上限时抛出业务异常，适合在批量删除、批量导入和批量发送前使用。

### 4. CpuDetectorUtil

获取 CPU 数量和线程池建议值：

```java
int effectiveCpu = CpuDetectorUtil.getEffectiveCpu();
int core = CpuDetectorUtil.recommendCore();
int max = CpuDetectorUtil.recommendMax();
```

返回值只是根据机器资源计算的建议，不应替代对任务阻塞比例和队列容量的评估。

### 5. CryptoUtil

统一加解密和摘要入口：

```java
String aes = CryptoUtil.encryptByAes(text, aesKey);
String plain = CryptoUtil.decryptByAes(aes, aesKey);
String sm4 = CryptoUtil.encryptBySm4(text, sm4Key);
String md5 = CryptoUtil.encryptByMd5(text);
String sha256 = CryptoUtil.encryptBySha256(text);
String sm3 = CryptoUtil.encryptBySm3(text);
```

另外支持 `encrypt/decrypt(String, CryptoDTO)`、Base64、SM2 和 RSA：

```java
String encoded = CryptoUtil.encryptByBase64(text);
Map<String, String> rsaKeys = CryptoUtil.generateRsaKey();
String rsa = CryptoUtil.encryptByRsa(text, rsaKeys.get("publicKey"));
String sm2 = CryptoUtil.encryptBySm2(text, sm2PublicKey);
```

摘要方法不可逆；密钥、私钥和配置对象必须通过安全配置注入，不得写入日志。

### 6. DatesUtil

日期解析、日期范围、时间窗口和重试时间计算：

```java
String date = DatesUtil.parseDate(System.currentTimeMillis(), "yyyy-MM-dd");
LocalDateTime value = DatesUtil.parseDate("2024-01-01 12:00:00");
List<String> days = DatesUtil.getDaysBetween("2024-01-01", "2024-01-10");
long millis = DatesUtil.getMillis(begin, end);
long nextRetry = DatesUtil.getNextRetryTime(baseMillis, frequency, retryCount);
```

还提供 UTC 解析、当天起止时间、周/月范围、时间间隔检查和按日期字段检查。日期格式必须与源码约定的格式一致。

### 7. DbSignUtil

数据库记录签名和验签，使用 `DbSignGenerateDTO`、`DbSignVerifyDTO`：

```java
String sign = DbSignUtil.generateSign(generateDTO);
boolean valid = DbSignUtil.verifySign(verifyDTO);
```

适合对数据库字段做完整性校验；签名字段的参与顺序和密钥必须保持服务端一致。

### 8. DbUtil

从 JDBC 连接读取真实数据库名称：

```java
String databaseName = DbUtil.getRealDbName(connection);
```

连接必须是有效且已建立的 JDBC `Connection`；调用方负责连接生命周期。

### 9. DiffUtil

比较两个对象或两个 Map 的差异字段：

```java
List<String> fields = DiffUtil.compare(before, after);
List<String> mapFields = DiffUtil.compare(beforeMap, afterMap);
```

返回差异字段路径。比较对象应避免循环引用，超大对象比较前应评估运行时成本。

### 10. ExcelsUtil

Excel 表头、格式、下拉选项和 Workbook 转换：

```java
int headerSize = ExcelsUtil.checkHeader(inputStream, UserImportDTO.class, 1);
List<String> headers = ExcelsUtil.listHeadByAnnotation(UserImportDTO.class);
ExcelsUtil.dropDownOption(workbook, options, 'A', 1, 100);
byte[] bytes = ExcelsUtil.getByte(workbook);
String base64 = ExcelsUtil.getBase64(workbook);
```

还提供 `checkFormat(MultipartFile, String)`、从输入流读取表头、级联下拉选项、Workbook 下载、错误文件名生成和 `closeWorkbook`。Workbook 使用完后必须关闭。

### 11. ExceptionUtil

将异常转换为统一响应实体：

```java
ResponseEntity<ResultVO<Object>> response =
    ExceptionUtil.getException(exception, controllerClassName);
```

主要用于统一异常处理器，不建议在普通业务代码中主动调用并吞掉异常。

### 12. ExpressionUtil

解析方法参数和返回值中的 SpEL 表达式：

```java
List<String> values = ExpressionUtil.parse(
    method,
    args,
    result,
    new String[]{"#user.id", "#result.code", "fixed-value"},
    String.class
);
```

不带返回值时使用 `parse(method, args, definitionKeys, clazz)`；方法参数按方法参数名绑定，返回值使用 `#result`。表达式来自外部输入时必须限制来源。

### 13. FilesUtil

文件、MultipartFile、下载响应和类扫描：

```java
byte[] bytes = FilesUtil.getBytes(multipartFile);
UploadByteDTO upload = FilesUtil.getUploadBytes(multipartFile);
List<UploadByteDTO> uploads = FilesUtil.listUploadBytes(files);
FilesUtil.downloadFile(inputStream, "report.pdf", response);
String contentType = FilesUtil.getContentType("report.pdf");
```

还提供 File 转字节、设置下载响应头、关闭输入/输出流和 `scanClasses(packagePatterns, assignableType)`。用户上传文件必须先限制大小、扩展名和 MIME 类型。

### 14. FlattenUtil

将嵌套对象、Map 和 List 扁平化为路径 Map：

```java
Map<String, Object> result = FlattenUtil.flatten(value);
Map<String, Object> result2 = FlattenUtil.flatten(value, "root", new HashMap<>());
```

适用于差异比较、日志字段和条件转换；注意循环引用和大对象递归深度。

### 15. GoogleAuthUtil

Google Authenticator TOTP 凭据、二维码和验证码校验：

```java
GoogleAuthenticatorKey key = GoogleAuthUtil.generateCredentials();
GoogleAuthVO qr = GoogleAuthUtil.generateQrContent("iwindplus", username);
boolean valid = GoogleAuthUtil.validate(secret, code);
```

还支持 `generateQrUrl` 和指定宽高的 `generateImage`。Secret 必须加密存储，不能写入日志。

### 16. HtmlToPdfUtil

从 URL 或 HTML 生成 PDF：

```java
byte[] pdf1 = HtmlToPdfUtil.toPdfBytesByUrl(url);
byte[] pdf2 = HtmlToPdfUtil.toPdfBytesByHtml(html);
HtmlToPdfUtil.toPdfByHtml(html, "result.pdf", response);
```

还提供 `toPdfByUrl` 下载方法。URL、HTML 和远程资源必须做协议、域名、大小和超时限制，防止 SSRF 和资源消耗攻击。

### 17. HttpsUtil

Servlet/WebFlux 请求解析和响应辅助：

```java
String ip = HttpsUtil.getRealIp(request);
Map<String, String> headers = HttpsUtil.getFilteredHeaders(request);
Map<String, Object> params = HttpsUtil.getRequestAndJsonParams(request);
boolean json = HttpsUtil.isJsonRequest(request);
String query = HttpsUtil.paramToQueryString(params);
```

还支持获取请求头、Authorization、请求参数、MDC、User-Agent、当前请求/响应、URL 编解码、JSON 判断、用户 Token 解析和响应写出。转发请求时优先使用过滤后的请求头。

### 18. I18nUtil

基于 ResourceBundle 的本地国际化：

```java
I18nUtil i18n = I18nUtil.getInstance("messages", Locale.SIMPLIFIED_CHINESE);
String text = i18n.getString("user.not-found");
String formatted = i18n.getFormattedString("welcome", username);
```

也可使用无参 `getInstance()`。它与 `iwindplus-base-i18n` 的 Nacos 动态消息源不同。

### 19. ImageUtil

生成群组头像和调整图片尺寸：

```java
byte[] avatar = ImageUtil.generate(paths, 200, 200, 6, "jpg");
BufferedImage image = ImageUtil.resize(filePath, 200, 200, true);
```

图片路径必须来自可信来源；读取远程图片时要限制协议、域名、超时和文件大小。

### 20. IosUtil

安全关闭资源：

```java
IosUtil.closeQuietly(inputStream, outputStream);
IosUtil.closeReader(reader);
IosUtil.closeWriter(writer);
IosUtil.closeChannel(channel);
```

支持 `Closeable`、`AutoCloseable`、自定义关闭器、输入流、输出流、Reader、Writer 和 Netty Channel。需要感知关闭异常时使用 `close`，忽略关闭异常时使用 `closeQuietly`。

### 21. JacksonUtil

统一 JSON 序列化、反序列化和树处理：

```java
String json = JacksonUtil.toJsonStr(value);
byte[] bytes = JacksonUtil.toJsonBytes(value);
UserDTO user = JacksonUtil.parseObject(json, UserDTO.class);
List<UserDTO> users = JacksonUtil.parseList(json, UserDTO.class);
JsonNode tree = JacksonUtil.parseTree(json);
```

还支持 Pretty JSON、Map/Set、`TypeReference`、`JavaType`、`ParameterizedTypeReference`、InputStream、`convertValue`、JsonNode 清理、Jsoup 内容清理和获取/设置全局 `ObjectMapper`。设置全局 ObjectMapper 前要确认不会影响其他模块。

### 22. KeysUtil

根据目标类、方法和参数生成幂等/缓存 Key：

```java
String key = KeysUtil.generate(
    true,
    target,
    method,
    methodArgs
);
```

Key 原文由类名、方法名和 JSON 参数组成；`enabledCrypto=true` 时使用 SM3 摘要。参数必须稳定、可序列化，生成的 Key 不应包含明文敏感信息。

### 23. KryoUtil

Kryo 二进制序列化：

```java
byte[] bytes = KryoUtil.toJsonBytes(value);
UserDTO value2 = KryoUtil.parseBytes(bytes, UserDTO.class);
```

还支持通过 `executeAction` 操作 Kryo 和创建默认 Kryo 对象。序列化双方必须使用兼容的类结构和注册配置，不要反序列化不可信字节。

### 24. LambdaUtil

从 Lambda 字段引用解析 Java 属性名：

```java
String field = LambdaUtil.getFieldName(UserDO::getUsername);
```

适合构造类型安全的查询条件，Lambda 必须是标准 getter 引用。

### 25. LengthUtil

检查长度是否处于最小值和最大值之间：

```java
boolean valid = LengthUtil.checkLength(value.length(), 1, 64);
```

`min` 或 `max` 可以按源码约定传空，边界检查应在业务层明确编码和空值策略。

### 26. MapstructUtil

使用 MapStruct 生成的 Mapper 进行对象和集合转换：

```java
UserVO vo = MapstructUtil.copyProperties(userDO, UserVO.class);
UserVO vo2 = MapstructUtil.copyProperties(attributes, UserVO.class);
List<UserVO> list = MapstructUtil.copyToList(userDOList, UserVO.class);
```

Map 转 Bean 依赖字段名称匹配；复杂字段、嵌套对象和自定义转换应在 MapStruct Mapper 中定义。

### 27. MdcUtil

TraceId 和 MDC/线程、Reactor 链路传递：

```java
MdcUtil.setTraceId(traceId);
Runnable wrapped = MdcUtil.wrap(task);
Mono<Result> traced = MdcUtil.withTraceId(mono);
MdcUtil.clearTraceId();
```

任务执行完会清理上下文；线程池和异步任务应使用 `wrap`，避免 TraceId 串线。

### 28. NetsUtil

网络基础能力继承 Hutool `NetUtil`，本类额外提供带上限的退避间隔：

```java
long interval = NetsUtil.nextMaxInterval(1000L, 60000L, attempt);
```

间隔按 `period * 1.5^(attempt - 1)` 增长，并限制为 `maxPeriod`。

### 29. ObjectEmptyCheckUtil

递归判断对象是否为空：

```java
boolean empty = ObjectEmptyCheckUtil.isDeepEmpty(value);
```

适合请求条件、嵌套 DTO 和 Map 的空值判断；业务字段有默认值时不要用它替代业务语义判断。

### 30. PathMatchUtil

使用项目路径匹配规则判断路径是否匹配模式集合：

```java
boolean matched = PathMatchUtil.match(
    List.of("/public/**", "/health"),
    requestPath
);
```

适合白名单、忽略 API 和权限路径判断。公共路径应集中配置，不要散落在业务代码中。

### 31. PatternUtil

将查询参数转换为模糊匹配 Pattern：

```java
Pattern pattern = PatternUtil.getPatternLike(keyword);
boolean matched = pattern.matcher(value).matches();
```

它不是手机号、邮箱或身份证校验工具；参数来自用户输入时要防止构造高复杂度正则。

### 32. PdfUtil

填充 PDF 模板表单字段：

```java
byte[] pdf = PdfUtil.replacePdfFields(
    "templates/invoice.pdf",
    Map.of("customerName", "张三", "amount", "100.00")
);
```

模板字段名必须与变量 Map 的 Key 一致；模板路径和字段内容必须来自可信输入。

### 33. PropertyUtil

把文本转换为 `Properties`：

```java
Properties properties = PropertyUtil.parseToProperties(text);
String value = properties.getProperty("app.name");
```

适合解析配置文本，不建议直接把不可信文本当作系统配置执行。

### 34. ProtostuffUtil

Protostuff 二进制序列化：

```java
byte[] bytes = ProtostuffUtil.toJsonBytes(value);
UserDTO value2 = ProtostuffUtil.parseBytes(bytes, UserDTO.class);
```

还支持 `executeAction` 操作 Protostuff 序列化器。生产者和消费者需要保持类型结构兼容，不要处理不可信输入。

### 35. ReactorUtil

读取和缓存 WebFlux 请求体/响应体，解决请求体只能消费一次的问题：

```java
Mono<ReactorRequestDTO> request = ReactorUtil.readRequestBody(
    exchange,
    nextExchange -> filterChain.filter(nextExchange)
);
Mono<ReactorResponseDTO> response = ReactorUtil.readResponseBody(
    exchange,
    nextExchange -> filterChain.filter(nextExchange)
);
```

还提供请求头删除、Cookie 转 Authorization、构造新请求、构造响应装饰器、统一 `ResultVO` 响应、请求查询参数、字节截断和 Exchange 属性读写。请求体/响应体缓存必须限制大小，避免大请求造成内存占用。

### 36. SecureRandomUtil

使用 `SecureRandom` 生成安全随机值：

```java
String token = SecureRandomUtil.randomString(32);
String code = SecureRandomUtil.randomNumbers(6);
byte[] bytes = SecureRandomUtil.randomBytes(16);
int value = SecureRandomUtil.randomInt(100);
long number = SecureRandomUtil.randomLong();
```

还可通过 `getSecureRandom()` 获取随机源。适用于验证码、临时 Token 和随机密钥材料。

### 37. SensitiveUtil

根据 `SensitiveDTO` 配置执行脱敏：

```java
String masked = SensitiveUtil.desensitized(phone, sensitiveConfig);
```

脱敏范围和类型由 DTO 配置决定；脱敏结果只能用于展示，不能写回原始业务字段。

### 38. TemplateUtil

从模板文本提取参数并生成替换结果：

```java
Map<String, String> params = TemplateUtil.getTemplateParam(
    template,
    templateParams
);
String content = TemplateUtil.getTemplateContent(
    template,
    templateParams
);
```

模板参数名称和替换值必须由业务明确控制，外部输入不能直接作为可执行表达式。

### 39. TransactionUtil

Spring 事务状态、事务执行和提交后回调：

```java
boolean active = TransactionUtil.isTransactionActive();
TransactionUtil.registerAfterCommit(() -> publishEvent(event));
Order order = TransactionUtil.executeInTransaction(
    transactionTemplate,
    () -> saveOrder()
);
```

有活跃事务时，`registerAfterCommit` 在提交成功后执行；没有事务时按源码行为执行回调。提交后任务必须具备幂等性。

### 40. TreesUtil

向树结构最后一个叶子节点追加节点：

```java
TreesUtil.appendToLastLeaf(sourceNodes, resultNode);
```

该类不负责通用树构建、树扁平化或父子关系推导，节点 ID、父 ID 和 children 结构由业务先完成。

### 41. ValidUtil

使用 Jakarta Bean Validation 校验对象并返回错误信息：

```java
String errorMessage = ValidUtil.validateEntity(
    dto,
    validator,
    SaveGroup.class
);
```

校验分组为空时使用默认分组；返回空字符串表示没有校验错误，业务可据此抛出 `BizException`。

### 42. YubikeyUtil

验证 YubiKey 签名：

```java
boolean valid = YubikeyUtil.verifySign(
    publicKey,
    source,
    sign
);
```

公钥、原文和签名必须使用同一协议和编码规则；验证前应确认来源和防重放字段。

## 逐类使用边界

1. `ApiSignUtil`、`DbSignUtil`、`YubikeyUtil` 和 `CryptoUtil` 的密钥不能硬编码、不能记录日志。
2. `JacksonUtil`、`KryoUtil`、`ProtostuffUtil` 的反序列化输入必须可信或经过长度、类型校验。
3. `HttpsUtil`、`ReactorUtil`、`FilesUtil`、`HtmlToPdfUtil` 和 `ImageUtil` 处理外部输入时必须限制大小、协议、域名和超时。
4. `MdcUtil`、`ReactorUtil` 和上下文类涉及线程传递，线程池任务结束后必须清理上下文。
5. `TransactionUtil.registerAfterCommit` 只保证回调时机，不保证消息最终投递成功；消息仍需重试和幂等。
6. `BeanCopierUtil` 和 `MapstructUtil` 只解决属性映射，不会自动执行业务校验、权限判断或对象深复制。
7. `DatesUtil`、`NetsUtil` 的重试时间计算需要结合业务最大重试次数和外部服务幂等性使用。
8. `PatternUtil`、`ExpressionUtil`、`TemplateUtil` 和反射扫描工具不要直接处理未经限制的用户输入。

## 相关模块

- `iwindplus-base-domain`：提供本模块使用的 DTO、VO、枚举、上下文和异常。
- `iwindplus-base-web`：使用 Jackson、文件、Web 请求和 Trace 工具。
- `iwindplus-base-webmvc`：使用 Servlet 请求、统一响应和异常处理。
- `iwindplus-base-webflux`：使用 Reactor 请求/响应体包装和异步上下文。
- `iwindplus-base-mybatis`：使用签名、字段加密、脱敏和事务辅助。
- `iwindplus-base-oss`：使用文件、MultipartFile 和上传 DTO。
- `iwindplus-base-i18n`：提供基于 Nacos 的动态国际化消息源。
