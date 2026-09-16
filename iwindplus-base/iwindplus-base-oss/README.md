# 对象存储与视频点播模块（iwindplus-base-oss）

本模块提供本地文件、云对象存储和阿里云视频点播能力，支持多配置管理，每个配置通过唯一编码（`code`）标识。

模块采用策略模式：每个配置对应一个运行时策略实例，通过策略工厂（`OssExecuteHandlerFactory` / `VodExecuteHandlerFactory`）管理策略实例，以「提供商类型 + 配置编码」二维结构区分不同策略。

```text
文件上传
   │
   ├── FileExecuteHandler（本地文件操作接口）
   │       └── FileExecuteHandlerLocal  本地 resources 文件下载/远程文件下载
   └── OssExecuteHandlerFactory（OSS 策略工厂，二维 Map：提供商类型 → 配置编码 → 策略）
          │
          ├── OssExecuteHandlerAliyun  阿里云 OSS 策略
          ├── OssExecuteHandlerQiniu   七牛云 OSS 策略
          └── OssExecuteHandlerMinio   MinIO 策略

视频点播
   └── VodExecuteHandlerFactory（VOD 策略工厂，二维 Map：提供商类型 → 配置编码 → 策略）
          │
          └── VodExecuteHandlerAliyun  阿里云 VOD 策略
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-oss</artifactId>
</dependency>
```

## 2. 对象存储配置

配置前缀为 `oss`，支持阿里云、七牛云和 MinIO 的多配置：

```yaml
oss:
  enabled: true
  default-code: "default"
  aliyun:
    - code: "default"
      name: "默认阿里云OSS配置"
      enabled: true
      priority: 1
      access-key: ${ALIYUN_ACCESS_KEY}
      secret-key: ${ALIYUN_SECRET_KEY}
      endpoint: oss-cn-shenzhen.aliyuncs.com
      part-size: 10
      broke: false
    - code: "backup"
      name: "备份阿里云OSS配置"
      enabled: true
      priority: 2
      access-key: ${ALIYUN_BACKUP_ACCESS_KEY}
      secret-key: ${ALIYUN_BACKUP_SECRET_KEY}
      endpoint: oss-cn-beijing.aliyuncs.com
  qiniu:
    - code: "qiniu-default"
      name: "七牛云配置"
      enabled: false
      access-key: ${QINIU_ACCESS_KEY}
      secret-key: ${QINIU_SECRET_KEY}
      broke: false
  minio:
    - code: "minio-default"
      name: "MinIO配置"
      enabled: false
      endpoint: http://minio.example.com:9000
      region: us-east-1
      access-key: ${MINIO_ACCESS_KEY}
      secret-key: ${MINIO_SECRET_KEY}
      part-size: 10
```

### 2.1 公共配置字段

三个供应商配置都继承 `OssProperty.BaseConfig`（`BaseConfig` 继承 `AkSkDTO`），公共字段如下：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `code` | 无 | 配置编码（唯一标识，必填） |
| `name` | 无 | 配置名称 |
| `enabled` | `true` | 是否启用 |
| `priority` | 无 | 优先级（数字越小优先级越高） |
| `access-key` | 无 | 访问密钥 |
| `secret-key` | 无 | 密钥 |

`oss` 顶层配置字段如下：

| 配置项            | 默认值 | 说明 |
|----------------|---:|---|
| `enabled`      | `true` | 是否启用 |
| `default-code` | 无 | 默认 OSS 配置编码（可选，指定后可通过 `getDefaultHandler()` 获取默认策略） |

> **说明**：`bucket-name`、`access-domain`、`return-absolute-path` 属于业务范畴（"存到哪里"、"用什么域名访问"），
> 已从配置中移除，改为通过请求 DTO 由调用方传入，避免切换配置时存储位置和访问域名跟着变化。

### 2.2 阿里云 OSS

阿里云在公共字段基础上额外包含：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `endpoint` | 无 | OSS 地域节点，例如 `oss-cn-shenzhen.aliyuncs.com` |
| `part-size` | 无 | 可选，分片大小，单位 MB |
| `broke` | 无 | 可选，是否开启断点上传 |
| `sts` | 无 | 可选，STS 临时凭证配置 |

### 2.3 七牛云 OSS

七牛云在公共字段基础上额外包含：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `broke` | 无 | 可选，是否开启断点上传 |

### 2.4 MinIO

MinIO 在公共字段基础上额外包含：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `endpoint` | 无 | MinIO 服务地址 |
| `region` | 无 | 可选，区域 |
| `part-size` | 无 | 可选，分片大小，单位 MB |

## 3. 使用方式

### 3.1 注入策略工厂

OSS 模块通过 `OssExecuteHandlerFactory` 对外提供策略路由能力，注入后先获取策略再调用操作方法：

```java
@Resource
private OssExecuteHandlerFactory ossExecuteHandlerFactory;
```

获取策略有两种方式：

```java
// 方式一：按提供商类型获取（该提供商下优先级最高的可用策略）
OssExecuteHandler handler = ossExecuteHandlerFactory.getHandler(OssTypeEnum.ALIYUN);

// 方式二：按提供商类型 + 配置编码获取（精确指定配置）
OssExecuteHandler handler = ossExecuteHandlerFactory.getHandler(OssTypeEnum.ALIYUN, "default");

// 方式三：获取默认策略（根据 oss.default-code 配置的编码查找，未配置时返回 null）
OssExecuteHandler handler = ossExecuteHandlerFactory.getDefaultHandler();
```

> **重要**：OSS 不支持自动故障转移，存储和查询必须使用同一配置编码（`type` + `code`），
> 否则会导致存储和查询不匹配。

### 3.2 上传文件

所有 OSS 策略都支持 `byte[]`、`MultipartFile` 和 `File`，通过 `OssCloudUploadDTO` 传入业务参数：

```java
OssExecuteHandler handler = ossExecuteHandlerFactory.getHandler(OssTypeEnum.ALIYUN, "default");

UploadVO result = handler.uploadFile(
    OssCloudUploadDTO.builder()
        .bucketName("demo-bucket")
        .accessDomain("https://cdn.example.com")
        .returnAbsolutePath(true)
        .file(multipartFile)
        .relativePath("orders/2026/08/order.pdf")
        .build()
);
```

`OssCloudUploadDTO` 字段说明：

| 字段 | 必填 | 说明 |
|---|---:|---|
| `bucketName` | 是 | 存储空间名 |
| `accessDomain` | 否 | 自定义访问域名 |
| `returnAbsolutePath` | 否 | 是否返回绝对路径，默认 `true` |
| `file` / `data` | 是 | 待上传文件（二选一） |
| `relativePath` | 是 | 相对路径（含文件名） |
| `sourceFileName` | 否 | 原始文件名（`byte[]` 上传时使用） |

`UploadVO` 包含：

- `sourceFileName`：原始文件名；
- `fileName`：最终文件名；
- `fileSize`：文件大小；
- `relativePath`：相对路径；
- `accessDomain`：访问域名；
- `absolutePath`：绝对路径。

### 3.3 获取签名访问地址

```java
OssExecuteHandler handler = ossExecuteHandlerFactory.getHandler(OssTypeEnum.ALIYUN, "default");

FilePathVO filePath = handler.getSignUrl(
    OssCloudGetSignUrlDTO.builder()
        .bucketName("demo-bucket")
        .accessDomain("https://cdn.example.com")
        .relativePath("orders/2026/08/order.pdf")
        .timeout(60)
        .build()
);
```

`timeout` 为过期时间，单位分钟。批量获取时可使用：

```java
List<FilePathVO> paths = handler.listSignUrl(
    OssCloudListSignUrlDTO.builder()
        .bucketName("demo-bucket")
        .accessDomain("https://cdn.example.com")
        .relativePaths(relativePaths)
        .timeout(60)
        .threadPoolExecutor(taskExecutor)
        .build()
);
```

`FilePathVO` 包含 `accessDomain`、`relativePath` 和 `absolutePath`。签名 URL 适合私有文件的临时访问，不应长期缓存为永久地址。

### 3.4 删除和下载

批量删除：

```java
boolean removed = handler.removeFiles(
    OssCloudRemoveDTO.builder()
        .bucketName("demo-bucket")
        .relativePaths(List.of("orders/2026/08/order.pdf"))
        .build()
);
```

云存储下载需要传入 Servlet 响应：

```java
handler.downloadFile(
    OssCloudDownloadDTO.builder()
        .bucketName("demo-bucket")
        .response(response)
        .relativePath("orders/2026/08/order.pdf")
        .fileName("order.pdf")
        .build()
);
```

### 3.5 请求 DTO 分层

OSS 请求 DTO 分为本地文件和云 OSS 两套，通过继承关系区分：

```text
OssUploadDTO（上传基类，implements Serializable）
├── data：字节数组（与 file 二选一）
├── file：文件（与 data 二选一）
├── relativePath：相对路径（必填）
└── sourceFileName：源文件名（必填）
    └── OssCloudUploadDTO（云 OSS 上传）
        ├── bucketName：空间名（必填）
        ├── accessDomain：访问域名（可选）
        └── returnAbsolutePath：是否返回绝对路径（可选，默认 true）

OssDownloadDTO（下载基类，implements Serializable）
├── response：响应（必填）
├── relativePath：相对路径（必填）
└── fileName：新文件名（必填）
    └── OssCloudDownloadDTO（云 OSS 下载）
        ├── bucketName：空间名（必填）
        └── accessDomain：访问域名（可选）

OssRemoveDTO（删除基类，implements Serializable）
└── relativePaths：相对路径集合（必填）
    └── OssCloudRemoveDTO（云 OSS 删除）
        └── bucketName：空间名（必填）

OssDownloadRemoteDTO（远程下载，implements Serializable，独立）
├── response：响应（必填）
├── absolutePath：绝对路径（必填）
└── fileName：新文件名（可选）

OssCloudGetSignUrlDTO（获取签名 URL，implements Serializable，独立）
├── relativePath：相对路径（必填）
├── timeout：过期时间（可选，单位：分钟，默认 60）
├── bucketName：空间名（必填）
└── accessDomain：访问域名（可选）

OssCloudListSignUrlDTO（批量获取签名 URL，implements Serializable，独立）
├── relativePaths：相对路径集合（必填）
├── timeout：过期时间（可选，单位：分钟，默认 60）
├── threadPoolExecutor：线程池（可选）
├── bucketName：空间名（必填）
└── accessDomain：访问域名（可选）
```

## 4. 本地文件服务

`FileConfiguration` 会无条件注册 `FileExecuteHandler`（实现类为 `FileExecuteHandlerLocal`）。它用于：

- 获取 `src/main/resources` 下的 Resource；
- 下载 `src/main/resources` 下的文件；
- 下载远程绝对路径文件。

```java
@Resource
private FileExecuteHandler fileExecuteHandler;

Resource resource = fileExecuteHandler.getResource("templates/demo.xlsx");

fileExecuteHandler.downloadResourceFile(
    OssDownloadDTO.builder()
        .response(response)
        .relativePath("templates/demo.xlsx")
        .fileName("demo.xlsx")
        .build()
);

fileExecuteHandler.downloadRemoteFile(
    OssDownloadRemoteDTO.builder()
        .response(response)
        .absolutePath("https://example.com/files/demo.xlsx")
        .fileName("demo.xlsx")
        .build()
);
```

`relativePath` 相对于 `src/main/resources`，不要把本地文件服务当作云对象存储服务使用。

## 5. STS 临时凭证

阿里云 OSS 和阿里云 VOD 配置都支持 `sts`：

```yaml
oss:
  aliyun:
    - code: "default"
      sts:
        enabled: true
        access-key: ${STS_ACCESS_KEY}
        secret-key: ${STS_SECRET_KEY}
        endpoint: sts.cn-shenzhen.aliyuncs.com
        role-arn: acs:ram::123456789:role/demo-role
        policy: '{"Version":"1","Statement":[]}'
```

`StsTokenDTO` 字段说明：

| 字段 | 必填 | 说明 |
|---|---:|---|
| `enabled` | 否 | 是否启用 STS |
| `access-key` | 是 | 访问密钥 |
| `secret-key` | 是 | 密钥 |
| `endpoint` | 是 | STS 地域节点，例如 `sts.cn-shenzhen.aliyuncs.com` |
| `role-arn` | 是 | RAM 角色，例如 `acs:ram::xxx:role/xxx` |
| `policy` | 否 | RAM 权限策略 |
| `security-token` | 否 | 上传授权安全令牌（会自动生成） |
| `expiration` | 否 | 安全令牌过期时间（会自动生成） |

STS 的权限策略应只授予业务所需 Bucket、目录和操作。

## 6. 阿里云视频点播

配置前缀为 `vod.aliyun`：

```yaml
vod:
  enabled: true
  aliyun:
    - code: "default"
      enabled: true
      priority: 1
      access-key: ${ALIYUN_VOD_ACCESS_KEY}
      secret-key: ${ALIYUN_VOD_SECRET_KEY}
      region: cn-shanghai
      sts:
        endpoint: sts.cn-shanghai.aliyuncs.com
        role-arn: acs:ram::123456789:role/vod-role
```

注入策略工厂：

```java
@Resource
private VodExecuteHandlerFactory vodExecuteHandlerFactory;
```

获取策略：

```java
VodExecuteHandler handler = vodExecuteHandlerFactory.getHandler(VodTypeEnum.ALIYUN, "default");
```

### 6.1 上传和播放

```java
UploadVideoVO uploadResult = handler.uploadVideo(multipartFile);

String playAuth = handler.getPlayAuth(uploadResult.getVideoId(), 60L);
```

同时支持 `File` 上传：

```java
UploadVideoVO uploadResult = handler.uploadVideo(localFile);
```

### 6.2 查询、删除和审核

```java
GetVideoInfoResponse.Video video = handler.getVideoInfo(videoId);

GetMezzanineInfoResponse.Mezzanine source = handler.getSourceVideoInfo(videoId);

Boolean removed = handler.removeVideo(List.of(videoId));

handler.auditVideoByAi(videoId);
handler.auditVideoByManual(videoId);
```

播放凭证过期时间单位为分钟；视频删除、审核等操作应由业务侧做好权限和状态校验。

## 7. 使用注意事项

- 每个配置必须有唯一的 `code`，用于标识和获取对应的 OSS 策略；
- `default-code` 用于指定默认 OSS 配置编码，配置后可通过 `getDefaultHandler()` 获取默认策略，未配置时该方法返回 `null`；
- 配置的 `enabled` 为 `false` 时，对应的策略不会被创建；
- `priority` 用于同提供商多配置下的自动故障转移，数字越小优先级越高，未配置时默认最低优先级；
- **OSS 不支持自动故障转移**，存储和查询必须使用同一配置编码（`type` + `code`），否则会导致存储和查询不匹配；
- `bucket-name`、`access-domain`、`return-absolute-path` 属于业务范畴，通过请求 DTO 由调用方传入，不放在配置中；
- AK/SK、STS 密钥和 Role ARN 使用密钥管理系统或环境变量注入；
- 阿里云 OSS 的 `endpoint`，MinIO 的 `endpoint` 需要按实际厂商配置；
- `returnAbsolutePath` 为 `false` 时不要假设返回结果一定包含可直接访问的完整 URL；
- 上传文件名和目录前缀应经过业务校验，避免路径穿越和非法对象名；
- 私有资源优先使用 `getSignUrl`，不要将永久访问地址直接暴露给前端；
- 视频点播和对象存储是两套独立配置，不能用 `oss.aliyun` 配置替代 `vod.aliyun`。
