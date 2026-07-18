# iwindplus-bae

#### 介绍

base框架: iwindplus-base

编码枚举规范

#### 软件架构
软件架构说明
project-name/
├── src/main/java/com/example/
│   ├── application/           // 应用层
│   │   ├── command/           // 命令对象
│   │   ├── dto/               // 数据传输对象
│   │   ├── service/           // 应用服务
│   │   └── mapper/            // DTO与领域对象映射
│   ├── domain/                // 领域层
│   │   ├── model/             // 领域模型
│   │   │   ├── entity/        // 实体
│   │   │   ├── vo/            // 值对象
│   │   │   └── aggregate/     // 聚合根
│   │   ├── repository/        // 仓储接口
│   │   └── service/           // 领域服务
│   ├── infrastructure/        // 基础设施层
│   │   ├── persistence/       // 持久化实现
│   │   ├── messaging/         // 消息组件
│   │   └── config/            // 配置类
│   └── interfaces/            // 用户接口层
│       ├── rest/              // REST接口
│       └── facade/            // 外部服务接口
└── src/main/resources/
├── application.yml        // 应用配置
└── db/migration/          // 数据库迁移脚本

#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
