# iwindplus-base

`iwindplus-base` 是项目基础能力聚合模块，为业务服务提供可复用的基础设施适配、Web 能力、数据访问、消息、任务、监控和通用工具。

## 模块组织

| 模块 | 职责 |
| --- | --- |
| `iwindplus-base-domain` | 公共领域模型、基础枚举和通用对象 |
| `iwindplus-base-util` | 日期、JSON、加解密、校验、事务等通用工具 |
| `iwindplus-base-web` | Web 基础过滤器、上下文、脱敏和校验能力 |
| `iwindplus-base-webmvc` / `iwindplus-base-webflux` | MVC、WebFlux 全局异常、跨域和响应增强 |
| `iwindplus-base-http-client` | HTTP 客户端执行器及策略工厂 |
| `iwindplus-base-http-client-integration` | 基于 HTTP 客户端的第三方接口集成 |
| `iwindplus-base-feign` | Feign 请求传递、重试和统一异常处理 |
| `iwindplus-base-mybatis` / `iwindplus-base-es` | MyBatis、Elasticsearch 数据访问能力 |
| `iwindplus-base-redis` / `iwindplus-base-mongo` | Redis、MongoDB 集成能力 |
| `iwindplus-base-kafka` / `iwindplus-base-rabbit` / `iwindplus-base-rocket` | 消息中间件集成能力 |
| `iwindplus-base-async-cmd` | 本地事务消息、异步命令和失败重试 |
| `iwindplus-base-binlog` | Binlog 事件监听和投递 |
| `iwindplus-base-disruptor` | Disruptor 高并发队列处理 |
| `iwindplus-base-loadbalancer` | 服务发现、权重负载均衡和灰度发布 |
| `iwindplus-base-monitor` / `iwindplus-base-log` | 链路上下文、监控埋点和日志告警 |
| `iwindplus-base-alert` | 告警渠道和告警执行器 |
| `iwindplus-base-oss` / `iwindplus-base-mail` / `iwindplus-base-sms` | 对象存储、邮件和短信服务 |
| `iwindplus-base-wechat` / `iwindplus-base-ocr` | 微信生态和 OCR 能力 |
| `iwindplus-base-shiro` / `iwindplus-base-operate` | 权限、安全操作校验和操作日志 |
| `iwindplus-base-i18n` | 国际化资源和动态更新 |
| `iwindplus-base-document` | 文档处理，支持 Excel、PDF、Docx 等多种格式的导入导出 |
| `iwindplus-base-swagger` | API 文档和接口定义查询 |
| `iwindplus-base-snail-job` / `iwindplus-base-xxl-job` | 任务调度集成 |

## 使用原则

1. 业务模块只引入实际使用的基础模块，避免引入无关传递依赖。
2. 优先使用模块提供的接口、策略工厂和自动配置，不直接依赖具体实现类。
3. 配置项、扩展点和示例以对应子模块 README 为准。
4. 第三方客户端和基础设施连接由基础模块统一管理，业务代码不重复创建客户端。

## 构建

```bash
mvn -pl iwindplus-base -am clean install
```

单独使用子模块时，请先确认其父工程版本和必要的基础依赖已安装。
