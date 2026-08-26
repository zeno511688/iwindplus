# Elasticsearch 基础服务模块（iwindplus-base-es）

本模块基于 Spring Data Elasticsearch 提供一套接近 MyBatis-Plus 使用习惯的基础服务和查询包装器：

```text
EsBaseService<T>
      │
      ├── save / saveBatch / saveOrUpdateBatch
      ├── updateById / updateBatchById
      ├── removeById / removeByIds / remove
      ├── getById / getOne / list / listById
      ├── page / pageByAfter
      └── count / exists

EsWrappers.lambdaQuery()
      │
      └── eq / like / in / between / sort / nested / searchAfter / aggregation
```

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.iwindplus</groupId>
    <artifactId>iwindplus-base-es</artifactId>
</dependency>
```

项目还需要配置并引入 Spring Data Elasticsearch 对应的 Elasticsearch Client。模块自身只提供 `JacksonJsonpMapper`、基础实体、查询包装器和基础服务实现，不创建 Elasticsearch 集群。

## 2. 基础实体

业务实体继承 `EsDbBaseDO`：

```java
@Document(indexName = "user")
public class UserEsDO extends EsDbBaseDO {

    @Field(type = FieldType.Keyword)
    private String username;

    @Field(type = FieldType.Text)
    private String nickname;
}
```

`EsDbBaseDO` 提供以下公共字段：

| 字段 | Elasticsearch 类型 | 说明 |
|---|---|---|
| `id` | `keyword` | 文档主键 |
| `createdTimestamp` | `long` | 创建时间戳 |
| `createdBy` | `keyword` | 创建人，不参与索引 |
| `createdId` | `long` | 创建人 ID，不参与索引 |
| `modifiedTimestamp` | `long` | 修改时间戳 |
| `modifiedBy` | `keyword` | 修改人，不参与索引 |
| `modifiedId` | `long` | 修改人 ID，不参与索引 |
| `deleted` | `integer` | 删除标识，不参与索引 |
| `version` | `integer` | 版本号，不参与索引 |
| `remark` | `text` | 备注，不参与索引 |

实体需要使用 `@Document` 指定索引名称，业务字段需要根据查询方式选择 `Keyword`、`Text`、数值或日期类型。

## 3. 定义基础服务

继承 `EsBaseServiceImpl` 并实现业务接口：

```java
public interface UserEsService extends EsBaseService<UserEsDO> {
}

@Service
public class UserEsServiceImpl
        extends EsBaseServiceImpl<UserEsDO>
        implements UserEsService {
}
```

`EsBaseServiceImpl` 通过泛型反射识别实体类型，业务实现类需要直接或间接保留 `EsBaseServiceImpl<UserEsDO>` 的泛型信息。

## 4. 新增和修改

```java
@Resource
private UserEsService userEsService;

public UserEsDO create(UserEsDO user) {
    return userEsService.save(user);
}

public void update(UserEsDO user) {
    userEsService.updateById(user);
}

public Collection<UserEsDO> batchCreate(List<UserEsDO> users) {
    return userEsService.saveBatch(users, 500);
}

public boolean batchUpdate(List<UserEsDO> users) {
    return userEsService.updateBatchById(users, 500);
}
```

### 保存时的公共字段处理

- `save` 会清空实体原有 `id`，再生成文档；
- 插入时默认填充创建人、创建时间、修改人、修改时间；
- 插入时默认将 `deleted` 和 `version` 设置为 `0`；
- `updateById` 会根据当前用户更新修改人、修改时间；
- 当前用户信息来自 `UserContextHolder`，使用基础服务前应确保用户上下文可用；
- `saveOrUpdateBatch` 根据 `id` 是否为空区分新增和修改。

## 5. 删除

```java
// 默认物理删除
userEsService.removeById(id);
userEsService.removeByIds(ids);

// 逻辑删除：将 deleted 设置为 1
userEsService.removeById(id, false);
userEsService.removeByIds(ids, false);

// 根据条件删除，调用 Elasticsearch delete-by-query
userEsService.remove(
    EsWrappers.<UserEsDO>lambdaQuery()
        .eq(UserEsDO::getUsername, "demo")
);
```

注意：`removeById(id)` 和 `removeByIds(ids)` 默认执行物理删除；逻辑删除需要显式传入 `false`。

## 6. Lambda 查询

使用 `EsWrappers.lambdaQuery()` 创建类型安全的查询包装器：

```java
EsLambdaQueryWrapper<UserEsDO> wrapper = EsWrappers.<UserEsDO>lambdaQuery()
    .eq(UserEsDO::getDeleted, 0)
    .eq(UserEsDO::getUsername, "demo")
    .like(UserEsDO::getNickname, "张")
    .orderByDesc(UserEsDO::getModifiedTimestamp);

List<UserEsDO> users = userEsService.list(wrapper);
UserEsDO user = userEsService.getOne(wrapper);
long count = userEsService.count(wrapper);
boolean exists = userEsService.exists(wrapper);
```

常用条件：

| 方法 | 生成的查询语义 |
|---|---|
| `eq` | `term` 精确匹配 |
| `ne` | `mustNot term` |
| `like` | `match` 模糊匹配 |
| `multiMatch` | 多字段匹配 |
| `prefix` | 前缀匹配 |
| `wildcard` | 通配符匹配 |
| `regexp` | 正则匹配 |
| `in` / `notIn` | `terms` / `mustNot terms` |
| `between` | 闭区间 `gte` + `lte` |
| `gt` / `ge` / `lt` / `le` | 范围查询 |
| `exists` / `notExists` | 字段存在性 |
| `filterEq` | 不参与评分的精确匹配 |
| `nested` | 嵌套对象查询 |
| `orderByAsc` / `orderByDesc` | 排序 |
| `source` | 返回字段包含/排除 |
| `limit` | 限制返回数量 |
| `max` | 添加最大值聚合 |

### 按条件动态追加

```java
EsLambdaQueryWrapper<UserEsDO> wrapper = EsWrappers.<UserEsDO>lambdaQuery()
    .eq(StringUtils.hasText(username), UserEsDO::getUsername, username)
    .between(minTimestamp != null && maxTimestamp != null,
        UserEsDO::getModifiedTimestamp, minTimestamp, maxTimestamp);
```

### OR 查询

```java
EsLambdaQueryWrapper<UserEsDO> wrapper = EsWrappers.<UserEsDO>lambdaQuery()
    .or(or -> or
        .eq(UserEsDO::getUsername, "alice")
        .eq(UserEsDO::getUsername, "bob")
    );
```

`or` 内部使用 `should`，并自动设置最小匹配数量为 `1`。

### Keyword 排序

文本字段排序通常需要使用 `.keyword` 子字段：

```java
EsWrappers.<UserEsDO>lambdaQuery()
    .orderByDesc(UserEsDO::getNickname, true);
```

第二个参数为 `true` 时，模块会在字段名后追加 `.keyword`。

## 7. 普通分页

基础服务支持 MyBatis-Plus `IPage`：

```java
IPage<UserEsDO> page = new Page<>(current, size);
IPage<UserEsDO> result = userEsService.page(
    page,
    EsWrappers.<UserEsDO>lambdaQuery()
        .like(StringUtils.hasText(keyword), UserEsDO::getNickname, keyword)
        .orderByDesc(UserEsDO::getModifiedTimestamp)
);
```

结果会回写：

- `records`：当前页数据；
- `total`：Elasticsearch 命中总数；
- 其他分页字段由传入的 `IPage` 维护。

普通查询默认最多返回 1000 条，设置 `limit` 或使用分页控制返回数量。

## 8. search_after 深分页

大数据量场景使用 `EsPageDTO` 和 `pageByAfter`：

```java
EsPageDTO<UserEsDO> page = new EsPageDTO<>();
page.setSize(100);
page.setSearchAfter(previousSearchAfter);

EsPageDTO<UserEsDO> result = userEsService.pageByAfter(
    page,
    EsWrappers.<UserEsDO>lambdaQuery()
        .eq(UserEsDO::getUsername, "demo")
);
```

模块会：

1. 自动按 `modifiedTimestamp` 倒序排列；
2. 使用传入的 `searchAfter` 作为游标；
3. 返回当前页 `records`；
4. 返回总数 `total` 和计算后的 `pages`；
5. 当前页数量等于 `size` 时返回下一页游标，否则清空 `searchAfter`。

调用方应将返回的 `searchAfter` 原样传给下一次请求：

```text
请求 N：searchAfter = null
响应 N：searchAfter = [最后一条记录的排序值]
请求 N+1：searchAfter = 响应 N 的游标
```

不要使用普通页码循环替代 `search_after`，也不要修改游标内容。

## 9. 自动过滤逻辑删除

`page`、`pageByAfter`、`list`、`getOne`、`count`、`exists` 和条件删除等操作会自动追加 `deleted = 0` 条件。

因此：

```java
userEsService.list(EsWrappers.<UserEsDO>lambdaQuery());
```

默认只查询未删除文档。若业务需要查询已删除数据，需要在使用基础服务前确认自动过滤行为，不能简单依赖包装器手动追加条件覆盖该逻辑。

## 10. 配置

模块绑定 `es` 配置，目前实际使用的是公共字段填充配置：

```yaml
es:
  field:
    fill:
      enabled: true
      enabled-insert-strict: true
```

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `es.field.fill.enabled` | `true` | 是否启用公共审计字段填充 |
| `es.field.fill.enabled-insert-strict` | `true` | 插入时是否严格覆盖公共字段 |

`enabled-insert-strict=true` 时，插入时由框架统一设置创建/修改时间和用户；设置为 `false` 时，已有非空公共字段可以保留，空字段仍会补充默认值。

## 11. 使用注意事项

- 业务实体必须继承 `EsDbBaseDO`，否则无法使用基础服务实现中的公共字段和逻辑删除处理；
- 业务实体必须配置 `@Document(indexName = "...")`；
- `save` 会清空传入实体的 `id`，新增时不要依赖调用前的 ID；
- 删除默认是物理删除，逻辑删除必须显式传入 `false`；
- `pageByAfter` 固定按 `modifiedTimestamp` 倒序，排序字段变化可能影响游标稳定性；
- `search_after` 适合深分页，不适合通过页码随机跳转；
- `Text` 字段不能直接用于普通排序，通常应使用 `.keyword`；
- 当前用户信息由 `UserContextHolder` 提供，后台任务或匿名任务需要提前处理用户上下文；
- 批量操作会按批次调用 Elasticsearch，批次大小应结合文档大小和集群压力设置；
- 模块配置的是 JSON 映射器和公共字段规则，Elasticsearch 连接地址、账号和索引生命周期由 Spring Data Elasticsearch 及基础设施配置负责。
