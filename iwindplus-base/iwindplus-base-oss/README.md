# 对象存储与视频点播模块（iwindplus-base-oss）

本模块提供本地文件、云对象存储和阿里云视频点播能力。

```text
文件上传
   │
   ├── FileService             本地 resources 文件下载/远程文件下载
   ├── OssAliyunService        阿里云 OSS
   ├── OssQiniuService         七牛云 OSS
   └── OssMinioService         MinIO

视频点播
   └── VodAliyunService        阿里云 VOD
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-oss</artifactId>
</dependency>
```

云厂商服务的 Bean 是否注册由各自的 `enabled` 配置控制；本地 `FileService` 会自动注册。

## 2. 对象存储配置

配置前缀为 `oss`，支持阿里云、七牛云和 MinIO：

```yaml
oss:
  aliyun:
    enabled: true
    access-key: ${ALIYUN_ACCESS_KEY}
    secret-key: ${ALIYUN_SECRET_KEY}
    endpoint: oss-cn-shenzhen.aliyuncs.com
    bucket-name: demo-bucket
    access-domain: https://cdn.example.com
    part-size: 10
    broke: false
  qiniu:
    enabled: false
    access-key: ${QINIU_ACCESS_KEY}
    secret-key: ${QINIU_SECRET_KEY}
    bucket-name: demo-bucket
    access-domain: https://cdn.example.com
    part-size: 10
    broke: false
  minio:
    enabled: false
    access-key: ${MINIO_ACCESS_KEY}
    secret-key: ${MINIO_SECRET_KEY}
    endpoint: http://127.0.0.1:9000
    region: us-east-1
    bucket-name: demo-bucket
    access-domain: http://127.0.0.1:9000/demo-bucket
    part-size: 10
```

### 2.1 阿里云 OSS

`oss.aliyun.enabled=true` 时注册 `OssAliyunService`。

| 配置项 | 说明 |
|---|---|
| `oss.aliyun.access-key` | 继承 `AkSkDTO` 的访问密钥 |
| `oss.aliyun.secret-key` | 继承 `AkSkDTO` 的密钥 |
| `oss.aliyun.endpoint` | OSS 地域节点，例如 `oss-cn-shenzhen.aliyuncs.com` |
| `oss.aliyun.bucket-name` | Bucket 名称 |
| `oss.aliyun.access-domain` | 可选，自定义访问域名 |
| `oss.aliyun.part-size` | 可选，分片大小，单位 MB |
| `oss.aliyun.broke` | 可选，是否开启断点上传 |
| `oss.aliyun.sts` | 可选，STS 临时凭证配置 |

### 2.2 七牛云 OSS

`oss.qiniu.enabled=true` 时注册 `OssQiniuService`。

必填或常用字段：`access-key`、`secret-key`、`bucket-name`、`access-domain`。`part-size` 和 `broke` 用于分片及断点上传相关配置。

### 2.3 MinIO

`oss.minio.enabled=true` 时注册 `OssMinioService`。

必填或常用字段：`access-key`、`secret-key`、`endpoint`、`bucket-name`。`region` 和 `access-domain` 可按部署环境配置。

## 3. 注入服务

三个云存储服务是不同接口，没有统一的自动策略工厂。业务根据实际启用的供应商注入对应接口：

```java
@Resource
private OssAliyunService ossAliyunService;
```

或者：

```java
@Resource
private OssQiniuService ossQiniuService;

@Resource
private OssMinioService ossMinioService;
```

如果多个供应商同时启用，不要按 `OssBaseService` 直接注入，避免出现多个候选 Bean；应注入明确的供应商接口，或在业务层自行封装供应商选择逻辑。

## 4. 上传文件

所有云对象存储服务都继承 `OssBaseService`，支持 `byte[]`、`MultipartFile` 和 `File`。

### 4.1 上传 MultipartFile

```java
UploadVO result = ossAliyunService.uploadFile(
    multipartFile,
    "orders/2026/08",
    true,
    true
);
```

参数含义：

- 第一个参数：待上传文件；
- `prefix`：存储目录前缀；
- `renamed`：是否重新生成文件名；
- `returnAbsolutePath`：是否返回绝对访问路径。

如果已经有明确相对路径，可以使用：

```java
UploadVO result = ossAliyunService.uploadFile(
    multipartFile,
    "orders/2026/08/order.pdf",
    true
);
```

这里第二个参数是 `relativePath`，不是目录前缀。

### 4.2 上传字节数组或 File

```java
UploadVO byteResult = ossAliyunService.uploadFile(
    bytes,
    "avatar",
    "avatar.png",
    true,
    true
);

UploadVO fileResult = ossAliyunService.uploadFile(
    localFile,
    "backup",
    true,
    true
);
```

`UploadVO` 包含：

- `sourceFileName`：原始文件名；
- `fileName`：最终文件名；
- `fileSize`：文件大小；
- `relativePath`：相对路径；
- `accessDomain`：访问域名；
- `absolutePath`：绝对路径。

## 5. 获取签名访问地址

```java
FilePathVO filePath = ossAliyunService.getSignUrl(
    "orders/2026/08/order.pdf",
    60
);
```

第二个参数为过期时间，单位分钟。批量获取时可使用：

```java
List<FilePathVO> paths = ossAliyunService.listSignUrl(
    relativePaths,
    60,
    taskExecutor
);
```

`FilePathVO` 包含 `accessDomain`、`relativePath` 和 `absolutePath`。签名 URL 适合私有文件的临时访问，不应长期缓存为永久地址。

## 6. 删除和下载

批量删除：

```java
boolean removed = ossAliyunService.removeFiles(
    List.of("orders/2026/08/order.pdf")
);
```

云存储下载需要传入 Servlet 响应：

```java
ossAliyunService.downloadFile(
    response,
    "orders/2026/08/order.pdf",
    "order.pdf"
);
```

## 7. 本地文件服务

`FileConfiguration` 会无条件注册 `FileService`。它用于：

- 获取 `src/main/resources` 下的 Resource；
- 下载 `src/main/resources` 下的文件；
- 下载远程绝对路径文件。

```java
@Resource
private FileService fileService;

Resource resource = fileService.getResource("templates/demo.xlsx");

fileService.downloadResourceFile(
    response,
    "templates/demo.xlsx",
    "demo.xlsx"
);

fileService.downloadRemoteFile(
    response,
    "https://example.com/files/demo.xlsx",
    "demo.xlsx"
);
```

`relativePath` 相对于 `src/main/resources`，不要把本地文件服务当作云对象存储服务使用。

## 8. STS 临时凭证

阿里云 OSS 和阿里云 VOD 配置都支持 `sts`：

```yaml
oss:
  aliyun:
    sts:
      access-key: ${STS_ACCESS_KEY}
      secret-key: ${STS_SECRET_KEY}
      endpoint: sts.cn-shenzhen.aliyuncs.com
      role-arn: acs:ram::123456789:role/demo-role
      policy: '{"Version":"1","Statement":[]}'
```

`StsTokenDTO` 还包含 `security-token` 和 `expiration` 字段，用于保存生成或传入的临时授权信息。STS 的权限策略应只授予业务所需 Bucket、目录和操作。

## 9. 阿里云视频点播

配置前缀为 `vod.aliyun`，只有 `vod.aliyun.enabled=true` 时才注册 `VodAliyunService`：

```yaml
vod:
  aliyun:
    enabled: true
    access-key: ${ALIYUN_VOD_ACCESS_KEY}
    secret-key: ${ALIYUN_VOD_SECRET_KEY}
    region: cn-shanghai
    sts:
      endpoint: sts.cn-shanghai.aliyuncs.com
      role-arn: acs:ram::123456789:role/vod-role
```

注入服务：

```java
@Resource
private VodAliyunService vodAliyunService;
```

### 9.1 上传和播放

```java
UploadVideoVO uploadResult = vodAliyunService.uploadVideo(multipartFile);

String playAuth = vodAliyunService.getPlayAuth(
    uploadResult.getVideoId(),
    60L
);
```

同时支持 `File` 上传：

```java
UploadVideoVO uploadResult = vodAliyunService.uploadVideo(localFile);
```

### 9.2 查询、删除和审核

```java
GetVideoInfoResponse.Video video =
    vodAliyunService.getVideoInfo(videoId);

GetMezzanineInfoResponse.Mezzanine source =
    vodAliyunService.getSourceVideoInfo(videoId);

Boolean removed = vodAliyunService.removeVideo(List.of(videoId));

vodAliyunService.auditVideoByAi(videoId);
vodAliyunService.auditVideoByManual(videoId);
```

播放凭证过期时间单位为分钟；视频删除、审核等操作应由业务侧做好权限和状态校验。

## 10. 动态更新配置

供应商服务同时实现 `BaseConfigService<OssProperty>` 或 `BaseConfigService<VodProperty>`，可以通过对应服务读取或设置配置：

```java
OssProperty property = ossAliyunService.getConfig();
ossAliyunService.setConfig(property);
```

动态设置配置不会改变 Spring Bean 的注册条件；例如启动时未开启阿里云 OSS，不应依赖运行时 `setConfig` 让 `OssAliyunService` 自动出现。

## 11. 注意事项

- AK/SK、STS 密钥和 Role ARN 使用密钥管理系统或环境变量注入；
- 云服务 Bean 只在对应 `enabled=true` 时注册；
- 阿里云 OSS 的 `endpoint`、Bucket，MinIO 的 `endpoint`、Bucket，七牛云的 Bucket、访问域名需要按实际厂商配置；
- `returnAbsolutePath` 为 `false` 时不要假设返回结果一定包含可直接访问的完整 URL；
- 上传文件名和目录前缀应经过业务校验，避免路径穿越和非法对象名；
- 私有资源优先使用 `getSignUrl`，不要将永久访问地址直接暴露给前端；
- 视频点播和对象存储是两套独立配置，不能用 `oss.aliyun` 配置替代 `vod.aliyun`。
