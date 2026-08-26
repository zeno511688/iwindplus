# MongoDB 基础服务模块（iwindplus-base-mongo）

本模块基于 `MongoTemplate` 提供 MongoDB 基础服务，封装实体公共字段、逻辑删除、审计字段填充、批量写入、Lambda 条件查询和 MyBatis-Plus 分页对象。

```text
MongoBaseService<T extends MongoDbBaseDO>
          │
          ├── save / saveBatch / saveOrUpdateBatch
          ├── updateById / update / updateBatchById
          ├── removeById / removeByIds / remove
          ├── getById / getOne / list / listById
          ├── page / count / exists
          └── MongoLambdaQueryWrapper
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-mongo</artifactId>
</dependency>
```

项目还需要配置 Spring Data MongoDB 的连接信息。模块本身使用 Spring Boot 自动配置的 `MongoTemplate`，不创建 MongoDB 数据库或集合。

## 2. 定义实体

业务实体继承 `MongoDbBaseDO`：

```java
@Document(collection = "user")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserMongoDO extends MongoDbBaseDO {

    private String username;

    private String nickname;
}
```

`MongoDbBaseDO` 提供：

| 字段 | 说明 |
|---|---|
| `id` | Mongo 文档主键，使用 `@Id` |
| `createdTimestamp` | 创建时间戳 |
| `createdBy` / `createdId` | 创建人和创建人 ID |
| `modifiedTimestamp` | 更新时间戳 |
| `modifiedBy` / `modifiedId` | 修改人和修改人 ID |
| `deleted` | 删除标记，`0` 为未删除，`1` 为已删除 |
| `version` | 乐观锁版本号 |
| `remark` | 备注 |

集合名称由 `@Document(collection = "...")` 指定。模块不会自动创建索引，业务需要根据查询场景自行配置索引。

## 3. 定义基础服务

```java
public interface UserMongoService extends MongoBaseService<UserMongoDO> {
}

@Service
public class UserMongoServiceImpl
        extends MongoBaseServiceImpl<UserMongoDO>
        implements UserMongoService {
}
```

实现类必须保留 `MongoBaseServiceImpl<UserMongoDO>` 的泛型信息，基础实现通过泛型获取实体类型并调用 `MongoTemplate`。

## 4. 新增和批量新增

```java
@Resource
private UserMongoService userMongoService;

UserMongoDO user = new UserMongoDO();
user.setUsername("demo");
user.setNickname("示例用户");

UserMongoDO saved = userMongoService.save(user);

Collection<UserMongoDO> savedList = userMongoService.saveBatch(users, 500);
boolean result = userMongoService.saveOrUpdateBatch(users, 500);
```

不传批次大小时默认使用 `1000`：

```java
userMongoService.saveBatch(users);
userMongoService.saveOrUpdateBatch(users);
```

保存行为：

- `save` 会将实体 `id` 置空，再调用 `MongoTemplate.save`；
- 插入时默认填充创建人、创建时间、修改人、修改时间；
- 插入时默认将 `deleted`、`version` 设置为 `0`；
- `saveOrUpdateBatch` 根据 `id` 是否为空拆分新增和更新；
- 当前用户从 `UserContextHolder` 获取，后台任务使用前需要准备用户上下文。

## 5. 更新

```java
UserMongoDO user = new UserMongoDO();
user.setId(id);
user.setNickname("新昵称");

boolean updated = userMongoService.updateById(user);
```

按条件更新：

```java
MongoLambdaQueryWrapper<UserMongoDO> wrapper =
    new MongoLambdaQueryWrapper<UserMongoDO>()
        .eq(UserMongoDO::getUsername, "demo");

UserMongoDO update = new UserMongoDO();
update.setNickname("新昵称");

userMongoService.update(update, wrapper);
```

批量更新：

```java
userMongoService.updateBatchById(users, 500);
```

更新默认只处理 `deleted=0` 的文档，并自动更新修改人、修改人 ID 和修改时间。实体 `version` 不为空时，更新条件会匹配当前版本，成功后版本号加 1。

## 6. 删除

```java
// 默认物理删除
userMongoService.removeById(id);
userMongoService.removeByIds(ids);

// 逻辑删除
userMongoService.removeById(id, false);
userMongoService.removeByIds(ids, false);

// 按条件物理删除
userMongoService.remove(
    new MongoLambdaQueryWrapper<UserMongoDO>()
        .eq(UserMongoDO::getUsername, "demo")
);

// 按条件逻辑删除
userMongoService.remove(
    new MongoLambdaQueryWrapper<UserMongoDO>()
        .eq(UserMongoDO::getUsername, "demo"),
    false
);
```

参数 `deleted` 的含义是“是否真删”：

- `true`：调用 MongoDB 物理删除；
- `false`：将 `deleted` 更新为 `1`，同时更新修改人和修改时间。

## 7. Lambda 条件查询

```java
MongoLambdaQueryWrapper<UserMongoDO> wrapper =
    new MongoLambdaQueryWrapper<UserMongoDO>()
        .eq(UserMongoDO::getUsername, "demo")
        .like(UserMongoDO::getNickname, "张")
        .in(UserMongoDO::getId, List.of("1", "2"))
        .orderByDesc(UserMongoDO::getModifiedTimestamp);

List<UserMongoDO> users = userMongoService.list(wrapper);
UserMongoDO user = userMongoService.getOne(wrapper);
long count = userMongoService.count(wrapper);
boolean exists = userMongoService.exists(wrapper);
```

支持的常用条件：

- `eq`、`ne`；
- `like`、`likeLeft`、`likeRight`；
- `in`、`notIn`；
- `between`；
- `gt`、`ge`、`lt`、`le`；
- `exists`、`notExists`；
- `orEq`、`orIn`；
- `orderByAsc`、`orderByDesc`；
- `include`、`exclude`。

所有条件都有带 `boolean condition` 的重载，可以动态决定是否添加：

```java
MongoLambdaQueryWrapper<UserMongoDO> wrapper =
    new MongoLambdaQueryWrapper<UserMongoDO>()
        .eq(StringUtils.hasText(username), UserMongoDO::getUsername, username)
        .like(StringUtils.hasText(keyword), UserMongoDO::getNickname, keyword)
        .between(minTime != null && maxTime != null,
            UserMongoDO::getModifiedTimestamp, minTime, maxTime);
```

`like` 使用 MongoDB 正则表达式；`likeLeft` 和 `likeRight` 分别生成左匹配、右匹配表达式。用户输入直接用于正则前应做好转义和长度限制。

## 8. 查询和逻辑删除过滤

以下方法会自动追加 `deleted=0`：

- `list()`；
- `list(Query)`；
- `getOne(Query)`；
- `count(Query)`；
- `exists(MongoLambdaQueryWrapper)`；
- `page`；
- `update`；
- 逻辑删除；
- 按条件删除。

需要特别注意：

- `getById(id)` 直接调用 `MongoTemplate.findById`，实现中不会显式追加 `deleted=0`；
- `listById(ids)` 直接按 ID 查询，实现中不会显式追加 `deleted=0`。

如果业务必须隐藏已删除文档，按 ID 查询时应在业务层明确校验 `deleted`，不要只依赖方法名推断行为。

## 9. 分页查询

分页使用 MyBatis-Plus 的 `IPage`：

```java
IPage<UserMongoDO> page = new Page<>(1, 20);

IPage<UserMongoDO> result = userMongoService.page(
    page,
    new MongoLambdaQueryWrapper<UserMongoDO>()
        .like(UserMongoDO::getNickname, "张")
);
```

分页结果包含：

- `records`：当前页数据；
- `total`：未删除文档总数；
- `current`、`size`：来自传入分页对象。

如果没有指定排序，默认按 `modifiedTimestamp` 倒序；也可以在 `IPage` 上指定排序：

```java
Page<UserMongoDO> page = new Page<>(1, 20);
page.addOrder(OrderItem.asc("createdTimestamp"));
```

## 10. 配置

模块绑定 `mongo` 配置，当前源码使用字段填充配置：

```yaml
mongo:
  field:
    fill:
      enabled: true
      enabled-insert-strict: true
```

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `mongo.field.fill.enabled` | `true` | 是否启用公共字段填充 |
| `mongo.field.fill.enabled-insert-strict` | `true` | 插入时是否严格覆盖审计字段 |

`enabled-insert-strict=true` 时，创建人、创建时间、修改人、修改时间始终由框架覆盖；设置为 `false` 时，实体中已有非空值可以保留，空值才由框架填充。

关闭公共字段填充：

```yaml
mongo:
  field:
    fill:
      enabled: false
```

## 11. BigDecimal 支持

模块提供 `BigDecimalCodec`，用于 MongoDB 中 `BigDecimal` 的 BSON 编解码。具体是否生效取决于项目 MongoDB Codec 注册方式和 Spring Data MongoDB 配置；业务需要确认当前 Mongo Client 使用了该 Codec。

## 12. 使用注意事项

- 业务实体必须继承 `MongoDbBaseDO`；
- 服务实现类必须保留泛型实体类型；
- `removeById/removeByIds` 默认物理删除，逻辑删除必须传入 `false`；
- 更新和条件查询默认过滤 `deleted=1`，但 `getById/listById` 的当前实现不会追加该过滤；
- 版本号只有在实体 `version` 非空时参与更新条件和递增；
- `save` 会清空传入实体 ID；
- 批量操作应根据文档大小和 MongoDB 集群压力调整批次大小；
- `like` 类方法使用正则查询，可能导致索引失效，应限制输入并根据查询模式设计索引；
- 审计字段依赖 `UserContextHolder`，定时任务、消息消费和异步线程需要正确传递用户上下文；
- 模块不负责 MongoDB 连接、集合创建和索引维护，生产环境应单独规划索引和读写策略。
