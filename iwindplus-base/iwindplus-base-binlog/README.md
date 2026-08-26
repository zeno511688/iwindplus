# MySQL Binlog 模块（iwindplus-base-binlog）

本模块基于 Debezium Embedded 和 MySQL Binlog Connector 读取数据库变更，将 INSERT、UPDATE、DELETE 事件转换为 `BinlogDTO`，再发布为 Spring `BinLogEvent`，供业务监听处理。

```text
MySQL Binlog
     │
     ▼
Debezium Embedded Engine
     │
     ├── Kafka offset topic       保存消费位点
     ├── Kafka schema history     保存表结构历史
     └── JSON ChangeEvent
             │
             ▼
     BinlogProcessHandler
             │
             ├── 解析 payload
             ├── 仅保留 INSERT/UPDATE/DELETE
             └── 发布 BinLogEvent
                     │
                     ▼
              @EventListener
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus.base</groupId>
    <artifactId>iwindplus-base-binlog</artifactId>
</dependency>
```

模块依赖：

- Debezium Embedded；
- Debezium MySQL Connector；
- `iwindplus-base-kafka`，用于 offset、schema history 和主题管理；
- Dynamic TP，用于 Binlog Engine 执行和重启调度。

Kafka 模块必须先正确配置默认集群，因为 Binlog 引擎会读取 `kafka.multi.default-cluster` 对应集群的 `bootstrap-servers`。

## 2. MySQL 前置条件

Binlog 模块只能读取已开启 MySQL Binlog 的数据库。数据库至少需要：

```ini
server-id=1
log_bin=mysql-bin
binlog_format=ROW
binlog_row_image=FULL
```

同时使用 Binlog 账号连接数据库：

```sql
CREATE USER 'binlog'@'%' IDENTIFIED BY 'strong-password';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT
ON *.* TO 'binlog'@'%';
FLUSH PRIVILEGES;
```

不同 MySQL 版本的权限要求可能不同，应以当前 Debezium 版本和数据库安全规范为准。

## 3. 基础配置

配置前缀是 `binlog`：

```yaml
binlog:
  enabled: true
  enabled-dynamic-register: true
  snapshot-mode: INITIAL
  topic-prefix: iwindplus-binlog-topic
  data-sources:
    - type: mysql
      server-id: "1001"
      host: 127.0.0.1
      port: 3306
      username: binlog
      password: ${MYSQL_BINLOG_PASSWORD}
      database-include-list: app_db
      table-include-list: app_db.user,app_db.order
  offset:
    topic: iwindplus-binlog-topic-offset
    partitions: -1
    replication-factor: -1
  history:
    topic: iwindplus-binlog-topic-schema-history
    partitions: -1
    replication-factor: -1
```

核心字段：

| 字段 | 说明 |
|---|---|
| `enabled` | 是否启用 Binlog，默认 `true` |
| `enabled-dynamic-register` | 是否自动创建业务、offset 和 schema history Kafka Topic，默认 `true` |
| `snapshot-mode` | Debezium 快照模式，默认 `INITIAL` |
| `topic-prefix` | Debezium 业务 Topic 前缀，默认 `iwindplus-binlog-topic` |
| `data-sources` | 多个 MySQL 数据源配置 |
| `offset` | Binlog 位点 Topic 配置 |
| `history` | 表结构历史 Topic 配置 |

## 4. 多数据源配置

每个数据源通过 `server-id` 区分：

```yaml
binlog:
  data-sources:
    - type: mysql
      server-id: "1001"
      host: mysql-a
      port: 3306
      username: binlog
      password: ${MYSQL_A_PASSWORD}
      database-include-list: order_db
      table-include-list: order_db.order,order_db.order_item

    - type: mysql
      server-id: "1002"
      host: mysql-b
      port: 3306
      username: binlog
      password: ${MYSQL_B_PASSWORD}
      database-include-list: user_db
      table-include-list: user_db.user
```

每个数据源会创建一个独立 Debezium Engine：

- Connector 名称：`{type}-{server-id}`，例如 `mysql-1001`；
- 业务 Topic：`{topic-prefix}-{server-id}`，例如 `iwindplus-binlog-topic-1001`；
- `server-id` 必须稳定且不能与 MySQL 集群中其他复制客户端冲突；
- 生产环境不要让多个实例使用同一数据源和同一 `server-id` 同时读取，除非已经设计好重复事件处理。

## 5. 库表过滤

包含和排除字段支持逗号分隔以及通配符：

```yaml
binlog:
  data-sources:
    - server-id: "1001"
      database-include-list: app_*
      database-exclude-list: app_test
      table-include-list: app_*.user,app_*.order
      table-exclude-list: app_*.*_history
```

过滤优先由 Debezium Connector 执行。建议优先配置包含列表，减少无关表的快照和增量读取压力。

字段说明：

- `database-include-list`：包含数据库；
- `database-exclude-list`：排除数据库；
- `table-include-list`：包含表，通常需要带数据库名；
- `table-exclude-list`：排除表。

## 6. Kafka Topic

Binlog 模块使用 Kafka 默认集群：

```yaml
kafka:
  multi:
    enabled: true
    default-cluster: default
    clusters:
      default:
        bootstrap-servers: 127.0.0.1:9092
```

默认会创建以下 Topic：

```text
业务 Topic：iwindplus-binlog-topic-{server-id}
位点 Topic：iwindplus-binlog-topic-offset
历史 Topic：iwindplus-binlog-topic-schema-history
```

开启 `enabled-dynamic-register` 后，模块会通过 Kafka 动态注册能力创建 Topic：

- 数据源业务 Topic：自动创建；
- offset Topic：根据 `partitions` 和 `replication-factor` 创建；
- schema history Topic：根据 `partitions` 和 `replication-factor` 创建；
- offset Topic 使用 `cleanup.policy=compact`；
- history Topic 使用 `cleanup.policy=delete`。

当 `partitions` 或 `replication-factor` 为 `-1` 时，使用 Kafka Broker 默认配置。

## 7. 快照模式

```yaml
binlog:
  snapshot-mode: INITIAL
```

`SnapshotMode` 是 Debezium 的枚举，具体可用值以当前 Debezium 版本为准。常用模式包括：

- `INITIAL`：首次启动先做全量快照，再读取增量 Binlog；
- 其他模式：按 Debezium 当前版本支持的枚举选择。

使用 `INITIAL` 时，业务监听器可能首先收到历史数据事件，不能只按实时变更处理。需要区分快照事件和增量事件时，应读取 `BinlogDTO.source` 中的 Debezium 元数据。

## 8. 监听 BinLogEvent

模块只发布 DML 事件。业务通过 Spring 事件监听器消费：

```java
@Component
public class UserBinlogListener {

    @Async
    @EventListener(BinLogEvent.class)
    public void onBinlog(BinLogEvent event) {
        BinlogDTO data = event.getLogData();
        if (data == null) {
            return;
        }

        switch (data.getOp()) {
            case "c", "r" -> handleInsertOrSnapshot(data);
            case "u" -> handleUpdate(data);
            case "d" -> handleDelete(data);
            default -> {
                // 当前模块只会发布 DML，未知操作可记录日志
            }
        }
    }
}
```

`BinLogEvent` 是 Spring `ApplicationEvent`，事件数据通过 `getLogData()` 获取。监听器是否异步由业务决定；使用 `@Async` 时需要项目自行启用异步执行能力。

## 9. BinlogDTO 字段

```java
public class BinlogDTO {
    private String op;
    private Object transaction;
    private Long tsMs;
    private Long tsUs;
    private Long tsNs;
    private Object source;
    private Object before;
    private Object after;
}
```

字段含义：

| 字段 | 说明 |
|---|---|
| `op` | Debezium 操作码，通常 `c` 创建、`u` 更新、`d` 删除、`r` 快照读取 |
| `transaction` | 事务信息 |
| `tsMs` | 毫秒时间戳 |
| `tsUs` | 微秒时间戳 |
| `tsNs` | 纳秒时间戳 |
| `source` | 数据库、表、位点、快照状态等元数据 |
| `before` | 更新或删除前数据 |
| `after` | 插入或更新后数据 |

建议：

- 插入、快照读取主要使用 `after`；
- 更新同时比较 `before` 和 `after`；
- 删除主要使用 `before`；
- 不要假设 `before`、`after` 永远非空；
- `source` 是 `JsonNode`，业务需要按实际字段转换或读取。

## 10. 事件处理流程

```text
Debezium ChangeEvent<String, String>
              │
              ▼
BinlogProcessHandler.processHandler(raw)
              │
              ├── JSON 解析失败：记录日志并丢弃
              ├── payload 缺失：丢弃
              ├── op 缺失：丢弃
              ├── 非 INSERT/UPDATE/DELETE：丢弃
              ├── source 缺失：丢弃
              └── 发布 BinLogEvent
```

当前实现只发布以下 DML 操作：

- `INSERT`；
- `UPDATE`；
- `DELETE`。

DDL、心跳以及不包含有效 `payload`、`op` 或 `source` 的事件不会发布为业务事件。

## 11. 引擎重启

每个 Debezium Engine 由 `BinlogEngineManager` 管理：

- 应用启动时异步提交各数据源 Engine；
- Engine 异常结束时记录日志；
- Engine 结束后延迟 5 秒自动重启；
- 应用停止时取消已提交的 Engine；
- 重启调度依赖 `binlogTaskExecutor` 和 `binlogTaskScheduler`。

因此需要确保 Dynamic TP 中存在对应的执行器：

```text
binlogTaskExecutor
binlogTaskScheduler
```

## 12. 关闭模块

```yaml
binlog:
  enabled: false
```

`binlog.enabled=false` 时不会注册 Binlog 自动配置。默认值是 `true`，生产环境如暂时不使用该模块，建议显式关闭。

## 13. 使用注意事项

- Binlog 模块不是业务消息生产者，不会把 `BinlogDTO` 自动发送到业务 Kafka Topic；
- Kafka 主要保存 Debezium offset、schema history，并负责必要的 Topic 管理；
- 业务数据通过 Spring `BinLogEvent` 交给监听器处理；
- 业务监听器必须自行处理幂等、失败重试、顺序和事件落库；
- `enabled-dynamic-register=true` 要求 Kafka 账号具备创建 Topic 的权限；
- offset Topic 和 schema history Topic 必须持久化，不能随意删除；
- 首次使用 `INITIAL` 可能产生大量历史事件，应提前评估快照时间和下游处理能力；
- `server-id` 必须唯一且稳定；
- 库表过滤建议优先使用 include，避免读取不需要的数据；
- 不要在事件监听器中假设所有操作都有 `before` 或 `after`；
- 业务事件监听失败不会自动回滚已发生的数据库变更，必须设计补偿机制；
- 数据库密码、Kafka 地址等敏感配置应放在外部配置中心或密钥管理系统中。
