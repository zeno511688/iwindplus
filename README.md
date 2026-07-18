# iwindplus

#### 项目目录结构
```plaintext
iwindplus/
├── deploy/                      # 部署配置目录
│   ├── apiPost/                 # API接口文档配置
│   ├── codeStyle/               # 代码风格配置
│   ├── docker/                  # Docker部署配置
│   │   ├── app/                 # 应用服务Docker配置
│   │   ├── middle/              # 中间件Docker配置
│   │   │   ├── alertmanager/    # 告警中间件
│   │   │   ├── apollo/          # 配置中心
│   │   │   ├── elasticsearch/   # 搜索引擎
│   │   │   │   ├── config/      # ES配置
│   │   │   │   └── plugin/      # ES插件
│   │   │   ├── grafana/         # 监控可视化
│   │   │   ├── kibana/          # 日志可视化
│   │   │   │   ├── linux/config/
│   │   │   │   ├── mac/config/
│   │   │   │   └── windows/config/
│   │   │   ├── logstash/        # 日志收集
│   │   │   │   ├── linux/config/
│   │   │   │   ├── mac/config/
│   │   │   │   └── windows/config/
│   │   │   ├── mongo/           # 文档数据库
│   │   │   ├── mysql/           # 关系型数据库
│   │   │   ├── nacos/           # 注册配置中心
│   │   │   ├── otel-collector/   # 链路追踪采集器
│   │   │   ├── prometheus/      # 指标监控
│   │   │   │   ├── mac/
│   │   │   │   └── windows/
│   │   │   ├── rabbitmq/        # 消息队列
│   │   │   ├── redis/           # 缓存数据库
│   │   │   ├── rocketmq/        # 消息队列
│   │   │   │   ├── linux/
│   │   │   │   ├── mac/
│   │   │   │   └── windows/
│   │   │   ├── seata/           # 分布式事务
│   │   │   ├── sentinel/        # 流量控制
│   │   │   ├── snail-job/       # 分布式任务调度
│   │   │   ├── xxl-job/         # 分布式任务调度
│   │   │   └── zipkin/          # 链路追踪
│   │   └── nginx/               # 反向代理
│   │       ├── cert/            # SSL证书
│   │       └── etc/conf.d/      # Nginx配置
│   ├── k8s/                     # Kubernetes部署配置
│   │   ├── kube/                # K8s资源文件
│   │   └── middle/              # 中间件K8s配置
│   └── websocket/               # WebSocket相关配置
├── iwindplus-base/              # 基础公共模块（所有业务服务共享）
│   ├── iwindplus-base-ai/       # AI能力封装
│   ├── iwindplus-base-alert/    # 告警能力封装
│   ├── iwindplus-base-async-cmd/ # 异步命令封装
│   ├── iwindplus-base-binlog/   # Binlog监听封装
│   ├── iwindplus-base-disruptor/ # 高并发队列封装
│   ├── iwindplus-base-domain/   # 公共领域模型
│   ├── iwindplus-base-es/       # Elasticsearch封装
│   ├── iwindplus-base-excel/    # Excel处理封装
│   ├── iwindplus-base-feign/    # Feign调用封装
│   ├── iwindplus-base-http-client/ # HTTP客户端封装
│   ├── iwindplus-base-i18n/     # 国际化封装
│   ├── iwindplus-base-kafka/    # Kafka消息封装
│   ├── iwindplus-base-loadbalancer/ # 负载均衡封装
│   ├── iwindplus-base-log/      # 日志处理封装
│   ├── iwindplus-base-mail/     # 邮件发送封装
│   ├── iwindplus-base-mongo/    # MongoDB封装
│   ├── iwindplus-base-monitor/  # 监控指标封装
│   ├── iwindplus-base-mybatis/  # MyBatis-Plus封装
│   ├── iwindplus-base-ocr/      # OCR识别封装
│   ├── iwindplus-base-operate/  # 操作日志封装
│   ├── iwindplus-base-oss/      # 对象存储封装
│   ├── iwindplus-base-rabbit/   # RabbitMQ封装
│   ├── iwindplus-base-redis/    # Redis封装
│   ├── iwindplus-base-rocket/   # RocketMQ封装
│   ├── iwindplus-base-shiro/    # Shiro权限封装
│   ├── iwindplus-base-sms/      # 短信发送封装
│   ├── iwindplus-base-snail-job/ # SnailJob任务封装
│   ├── iwindplus-base-swagger/  # Swagger文档封装
│   ├── iwindplus-base-util/     # 公共工具类
│   ├── iwindplus-base-web/      # Web基础封装
│   ├── iwindplus-base-webflux/  # WebFlux响应式封装
│   ├── iwindplus-base-webmvc/   # WebMVC封装
│   ├── iwindplus-base-websocket/ # WebSocket封装
│   ├── iwindplus-base-wechat/   # 微信生态封装
│   └── iwindplus-base-xxl-job/  # XXL-Job任务封装
└── iwindplus-server/            # 业务服务模块
    ├── iwindplus-auth/          # 认证授权服务
    │   ├── deploy/              # 部署配置
    │   ├── iwindplus-auth-api/   # 对外接口定义
    │   ├── iwindplus-auth-client/ # 客户端调用封装
    │   ├── iwindplus-auth-domain/ # 领域模型
    │   └── iwindplus-auth-server/ # 服务实现
    ├── iwindplus-binlog/         # Binlog服务
    │   └── iwindplus-binlog-consumer-server/ # Binlog消费者者服务
    │   └── iwindplus-binlog-producer-server/ # Binlog生产者服务
    ├── iwindplus-flow/           # 流程服务
    │   └── iwindplus-flow-domain/ # 流程领域模型
    ├── iwindplus-im/             # 即时通讯服务
    │   └── iwindplus-im-domain/  # IM领域模型
    ├── iwindplus-log/            # 日志服务
    │   └── iwindplus-log-domain/ # 日志领域模型
    ├── iwindplus-mgt/            # 管理服务
    │   ├── iwindplus-mgt-api/    # 对外接口定义
    │   ├── iwindplus-mgt-client/ # 客户端调用封装
    │   ├── iwindplus-mgt-domain/ # 领域模型
    │   ├── iwindplus-mgt-server/ # 服务实现
    │   └── iwindplus-mgt-test/   # 测试模块
    └── iwindplus-monitor/        # 监控服务
        └── iwindplus-monitor-server/ # 监控服务实现
```
#  iwindplus-web-mgt
    对应前端代码地址：https://github.com/zeno511688/iwindplus-web-mgt.git
#### 软件架构

一款开源、免费、轻量级 Java 微服务框架，开发重点考虑了安全漏洞处理
框架内容：boot + cloud基础框架
微服务：认证服务，网关服务，即时通讯服务，日志服务，通用设置服务，监控服务
登陆账号密码：admin/123456

#### 安装教程

1.  jdk17
2.  deploy目录下middle中间件启动
3.  iwindplus-server服务启动

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 技术支持
1.  框架问题咨询：邮箱：zeno511688@gmail.com